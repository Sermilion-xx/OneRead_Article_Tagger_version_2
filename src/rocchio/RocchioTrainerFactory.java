package rocchio;

import base.CentroidTrainer;
import base.TrainerAbstractFactory;
import utility.Delegate;

/**
 * Created by Sermilion on 04/01/2017.
 */
public class RocchioTrainerFactory implements TrainerAbstractFactory {

    private int    tagId;
    private String lang;
    private Delegate delegate;

    public RocchioTrainerFactory(int tagId, String lang, Delegate delegate){
        this.tagId = tagId;
        this.lang = lang;
        this.delegate = delegate;
    }

    public RocchioTrainerFactory(String lang){
        this.lang = lang;
    }

    @Override
    public CentroidTrainer getTrainer() {
        if(tagId == 0 && delegate == null){
            return new RocchioTrainer(lang);
        } else {
            return new RocchioTrainer(tagId, lang, delegate);
        }
    }

}
