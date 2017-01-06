package rocchio;

import base.AbstractTrainer;
import utility.Delegate;
import utility.exceptions.IllegalArticleIdException;
import utility.exceptions.NoArticleForTagException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;

/**
 * Created by Sermilion on 01/01/2017.
 */
public class RocchioTrainer extends AbstractTrainer {

    private static final int SAVE_STOP_WORDS = 0;

    private Delegate delegate;

    public RocchioTrainer(String lang){
        this.lang = lang;
    }

    public RocchioTrainer(int tagId, String lang, Delegate delegate) {
        this(lang);
        this.tagId = tagId;
        this.delegate = delegate;
    }

    public double[] calculateCentroid(List<String> articleList,
                                      List<String>  allTerms,
                                      double[] oldCentroid, int oldDivisor) {
        if (tagId > -1) {
            if (articleList.size() > -1) {

                List<String[]> tokenized =  tokenize(articleList);
                List<List<String>> noStopWords = removeStopwords(tokenized);
                //asynchroniously saving new words
                delegate.delegateData(noStopWords, SAVE_STOP_WORDS);
                List<double[]> tfws = tfwForTag(noStopWords, allTerms);
                double[][] tfwMatrix = listTo2dMatrix(tfws);
                double[] sumOfTfwColumns = sum2dMatrixColumns(tfwMatrix);
                //increment divisor count.
                divisor = tfwMatrix.length + oldDivisor;
                double[] processedOldCentroid = calculateOldCentroid(oldDivisor, oldCentroid);
                //getting old centroid
                double[] centroid = mergeOldAndNewCentroids(processedOldCentroid, sumOfTfwColumns);
                return centroid;
            } else throw new NoArticleForTagException("No articles for tag:" + tagId) {
            };
        } else throw new IllegalArticleIdException("ID should be greater then -1") {
        };
    }



    /**
     * Method for converting list of arrays into 2D matrix
     * @param list list of arrays
     * @return 2d matrix
     */
    public double[][] listTo2dMatrix(List<double[]> list){
        double[][] tfw_matrix = new double[list.size()][list.get(0).length];
        for (int i = 0; i < list.size(); i++) {
            tfw_matrix[i] = list.get(i);
        }
        return tfw_matrix;
    }

    /**
     * Method to calculate sums of each column in 2D matrix
     * @param matrix: 2D matrix for processing
     * @return array of sums
     */
    public double[] sum2dMatrixColumns(double[][] matrix){
        double[] sumArray = new double[matrix[0].length];
        for (int i = 0; i < matrix[0].length; i++) {
            double sum = 0;
            for (double[] aMatrix : matrix) {
                sum += aMatrix[i];
            }
            sumArray[i] = sum;
        }
        return sumArray;
    }

    /**
     * (For memory saving purposes calculation of centroids is performed with chunks of
     * selected sizes. Because of that, the system saves calculated part of centroid,
     * and then calculates another part and merges it with previously saved one until the end.)
     * Method to get previously saved part of centroid and merge it with the next calculated part
     * @param oldCentroidParam previously saved part of centroid
     * @param divisor: old divisor
     * @return calculated previous centrois ready to be merged with currently calculated
     */
    public double[] calculateOldCentroid(int divisor, double[] oldCentroidParam){
        double[] oldCentroid = null;
        if (tagId > -1) {
            //creating array for storing old Centroid
            oldCentroid = new double[oldCentroidParam.length];
            //calculating old centroid
            if (oldCentroidParam.length > 2) {
                DoubleStream ds = Arrays.stream(oldCentroidParam);
                oldCentroid = ds.map(n -> n * divisor).toArray();
            }
        }
        return oldCentroid;
    }

    public double[] mergeOldAndNewCentroids(final double[] oldCentroid, double[] sumOftfwColumns){
        double[] newCentroid= new double[sumOftfwColumns.length];

        if (oldCentroid != null && oldCentroid.length > 1) {
            double[] newSemiCentroid = new double[sumOftfwColumns.length];
            if (newSemiCentroid.length > oldCentroid.length) {
                double[] newArray = new double[newSemiCentroid.length];
                System.arraycopy(oldCentroid, 0, newArray, 0, oldCentroid.length);
                Arrays.setAll(newSemiCentroid, i -> ((newArray[i] + sumOftfwColumns[i])/this.divisor));
            } else {
                Arrays.setAll(newSemiCentroid, i -> ((oldCentroid[i] + sumOftfwColumns[i]) / this.divisor));
            }
            newCentroid = newSemiCentroid;
        } else {
            for (int i = 0; i < sumOftfwColumns.length; i++) {
                if (sumOftfwColumns[i] / this.divisor > 0) {
                    newCentroid[i] = sumOftfwColumns[i] / this.divisor;
                }
            }
        }
        return newCentroid;
    }

}
