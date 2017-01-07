package base;

import java.util.List;

/**
 * Created by Sermilion on 01/01/2017.
 */
public interface CentroidTrainer {

    double[] calculateCentroid(List<String> articleList,
                               List<String>  allTerms,
                               double[] oldCentroid, int oldDivisor);

    int getTagId();
    void setTagId(int tagId);
    int getLastTagId();
    void setLastTagId(int lastTagId);
    int getDivisor();
}
