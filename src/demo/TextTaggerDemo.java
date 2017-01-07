package demo;

import base.CentroidTrainer;
import base.TrainerAbstractFactory;
import data.Database;
import org.javatuples.Pair;
import rocchio.RocchioTrainerFactory;
import utility.Delegate;
import utility.exceptions.IllegalArticleIdException;
import utility.exceptions.NoArticleForTagException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Sermilion on 06/01/2017.
 */
public class TextTaggerDemo implements Delegate {

    private static final Logger LOGGER = Logger.getLogger(TextTaggerDemo.class.getName());
    private static final int ARTICLE_NUMBER_LIMIT_FOR_ITERATION = 500;
    private static final int ARTICLE_NUMBER_TO_PROCESS_FOR_TAG = 1000;
    private static final boolean TRAIN = true;
    private static final String sessionLang = "en";
    private static final int ARTICLE_NUMBER_THRESHOLD = 500;
    private static CentroidTrainer trainer;
    private Pair<List<String>, Integer> articleList;
    private Pair<List<String>, Integer> bufferedArticleList;

    private void bufferArticlesForNextTag() {
        bufferedArticleList = Database.getInstance().getArticlesForTag(trainer.getTagId(), ARTICLE_NUMBER_LIMIT_FOR_ITERATION, sessionLang, trainer.getLastTagId());
        trainer.setLastTagId(this.articleList.getValue1());
    }

    public static void main(String[] args) {

        TextTaggerDemo textTaggerDemo = new TextTaggerDemo();
        List<String> allTerms = textTaggerDemo.initAllTerms();
        if (TRAIN) {
            TrainerAbstractFactory trainerFactory = new RocchioTrainerFactory(sessionLang, textTaggerDemo);
            trainer = trainerFactory.getTrainer();
            //Step 1 & 2: Delete old centroids to train new
            Database.getInstance().deleteCentroids();
            //Step 3: Initialise tags to train
            List<Pair<Integer, Integer>> countsAndTags = Database.getInstance().getTagCountsAndIdsWhereNumOfTagsMoreThan(ARTICLE_NUMBER_THRESHOLD);
            //Step 4: Train each tag
            for (int i = 0; i < countsAndTags.size(); i++) {
                Pair<Integer, Integer> tagAndCount = countsAndTags.get(i);
                trainer.setTagId(tagAndCount.getValue1());
                int articleListSize = countsAndTags.get(i).getValue0();
                int iterationsForTag = articleListSize / ARTICLE_NUMBER_LIMIT_FOR_ITERATION + ((articleListSize % ARTICLE_NUMBER_LIMIT_FOR_ITERATION > 0) ? 1 : 0);
                if (ARTICLE_NUMBER_TO_PROCESS_FOR_TAG / ARTICLE_NUMBER_LIMIT_FOR_ITERATION < articleListSize) {
                    iterationsForTag = ARTICLE_NUMBER_TO_PROCESS_FOR_TAG / ARTICLE_NUMBER_LIMIT_FOR_ITERATION;
                }
                Thread saveCentroidThread = null;

                for (int j = 0; j < iterationsForTag; j++) {
                    textTaggerDemo.articleList = Database.getInstance().getArticlesForTag(trainer.getTagId(), ARTICLE_NUMBER_LIMIT_FOR_ITERATION, sessionLang, trainer.getLastTagId());
                    trainer.setLastTagId(textTaggerDemo.articleList.getValue1());
                    List<String> articlesList = textTaggerDemo.articleList.getValue0();
                    try {
                        trainer.setLastTagId(textTaggerDemo.articleList.getValue1());
                        final Pair<Integer, double[]> oldDivisorAndCentroid = Database.getInstance().getCentroidAndDivisor(trainer.getTagId());
                        double[] centroid = trainer.calculateCentroid(articlesList, allTerms, oldDivisorAndCentroid.getValue1(), oldDivisorAndCentroid.getValue0());
                        Database.getInstance().saveCentroidToDB(centroid, trainer.getTagId(), trainer.getDivisor());
//                        saveCentroidThread = new Thread(() -> Database.getInstance().saveCentroidToDB(centroid, trainer.getTagId(), trainer.getDivisor()));
                        Thread.currentThread().wait();
                    } catch (IllegalArticleIdException | NoArticleForTagException e) {
                        e.printStackTrace();
                        LOGGER.log(Level.INFO, e.getMessage());
                        i = iterationsForTag;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                LOGGER.log(Level.INFO, "A tag was trained-----------------------");
            }
            LOGGER.log(Level.INFO, "Training finished!");
        } else {

        }


    }

    private List<String> initAllTerms() {
        //Step 1: Copy all temp words that might have been added during clustering or training to all words table
        Database.getInstance().copyWordsFromTemp(sessionLang);
        //Step 2: Getting all words from database
        return Database.getInstance().getWordsDictionary(sessionLang);
    }

    @Override
    public void delegateData(Object data, int requestId) {

    }
}
