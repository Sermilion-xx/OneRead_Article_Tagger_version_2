import rocchio.RocchioTrainerFactory;
import base.TrainerAbstractFactory;
import rocchio.RocchioTrainer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Created by Sermilion on 02/01/2017.
 */
public class RocchioTrainerTest {

    private RocchioTrainer trainer;
    private static final String LANG = "en";

    @Before
    public void initTrainer() {
        TrainerAbstractFactory trainerFactory = new RocchioTrainerFactory(LANG);
        trainer = (RocchioTrainer) trainerFactory.getTrainer();
    }

    @Test
    public void listTo2dMatrixTest() {
        List<double[]> list = TestUtilities.initListWithArrays();
        double[][] matrix = trainer.listTo2dMatrix(list);
        assertThat("", matrix.length == 2 && matrix[0].length == 5);
    }

    @Test
    public void sum2dMatrixColumnsTest() {
        double[][] matrix = TestUtilities.init2Darray(2, 5);
        double[] result = trainer.sum2dMatrixColumns(matrix);
        assertThat("Wrong values of elements", result[1] == 2 && result[4] == 8);
    }

    @Test
    public void calculateOldCentroidTest() {
        double[] array = TestUtilities.initArray(5);
        int divisor = 3;
        double[] result = trainer.calculateOldCentroid(divisor, array);
        assertThat("Wrong values of elements", result[3] == array[3] * divisor);
    }

    @Test
    public void mergeOldAndNewCentroidsTest() {
        double[] centroidNew = TestUtilities.initArray(10);
        double[] centroidOld = TestUtilities.initArray(7);
        trainer.setDivisor(3);
        double[] centroid = trainer.mergeOldAndNewCentroids(centroidOld, centroidNew);
        assertThat("Wrong values of elements", centroid[5] == (centroidNew[5] + centroidOld[5]) / trainer.getDivisor());
    }



}
