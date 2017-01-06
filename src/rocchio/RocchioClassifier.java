package rocchio;

import utility.Delegate;

/**
 * Created by Sermilion on 07/08/2016.
 */
public class RocchioClassifier {

    private Delegate delegate;

    public RocchioClassifier(){

    }

    public RocchioClassifier(Delegate delegate) {
        this.delegate = delegate;
    }

    public double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        int times;
        if (vectorA.length > vectorB.length) {
            times = vectorB.length;
        } else {
            times = vectorA.length;
        }
        double a;
        for (int i = 0; i < times; i++) {
            a = vectorA[i] * vectorB[i];
            dotProduct += a;
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

}
