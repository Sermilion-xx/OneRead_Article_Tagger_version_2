package demo;

import base.CentroidTrainer;
import base.TrainerAbstractFactory;
import data.Database;
import org.javatuples.Pair;
import rocchio.RocchioTrainerFactory;
import utility.Delegate;
import utility.exceptions.IllegalArticleIdException;
import utility.exceptions.NoArticleForTagException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Sermilion on 06/01/2017.
 */
public class TextTaggerDemo implements Delegate{

    private static final Logger LOGGER = Logger.getLogger(TextTaggerDemo.class.getName());
    private static final int LIMIT = 100;
    private static final boolean TRAIN = true;
    private static final String sessionLang = "en";
    private static final int ARTICLE_NUMBER_THRESHOLD = 500;
    private static CentroidTrainer trainer;

    public static void main(String[] args){

        TextTaggerDemo textTaggerDemo = new TextTaggerDemo();
        List<String> allTerms = textTaggerDemo.initAllTerms();
        if (TRAIN) {
            //Step 1 & 2: Delete old centroids to train new
            Database.getInstance().deleteCentroids();
            //Step 3: Initialise tags to train
            List<Pair<Integer, Integer>> tagsAndCounts = Database.getInstance().getTagCountsAndIdsWhereNumOfTagsMoreThan(ARTICLE_NUMBER_THRESHOLD);
            //Step 4: Train each tag
            for (Pair<Integer, Integer> tagAndCount : tagsAndCounts) {
                //Step 4.1: Get number of words for tag
                int tagIdCount = tagAndCount.getValue0();
                //Step 4.2: Divide batch training operations into number of steps
                //to reduce required RAM
                int times = tagIdCount / LIMIT + ((tagIdCount % LIMIT > 0) ? 1 : 1);
                //Step 4.3: Initialise trainer class
                TrainerAbstractFactory trainerFactory = new RocchioTrainerFactory(tagAndCount.getValue1(), sessionLang, textTaggerDemo);
                trainer = trainerFactory.getTrainer();

                for (int i = 0; i < times; i++) {
                    //Step 4.4: Calculate centroid for tag
                    double[] centroid;
                    try {
                        //Getting <LIMIT> number of articles and id of last retrieved article
                        Pair<List<String>, Integer> articleList = Database.getInstance().getArticlesForTag(trainer.getTagId(), LIMIT, sessionLang, 0);
                        //setting last retrieved id to trainer
                        trainer.setLastTagId(articleList.getValue1());
                        //Getting old centroid
                        final Pair<Integer, double[]> oldDivisorAndCentroid = Database.getInstance().getCentroidAndDivisor(trainer.getTagId());
                        centroid = trainer.calculateCentroid(articleList.getValue0(), allTerms, oldDivisorAndCentroid.getValue1(), oldDivisorAndCentroid.getValue0());
                        //Step 4.5: Save centroid to database
                        Database.getInstance().saveCentroidToDB(centroid, trainer.getTagId(), trainer.getDivisor());
                    } catch (IllegalArticleIdException | NoArticleForTagException e) {
                        e.printStackTrace();
                        LOGGER.log(Level.INFO, e.getMessage());
                        i = times;
                    }
                }

            }
            LOGGER.log(Level.INFO, "Training finished!");
        }else {

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
