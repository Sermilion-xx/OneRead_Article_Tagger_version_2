import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sermilion on 03/01/2017.
 */
class TestUtilities {

    @Contract(pure = true)
    static List<double[]> initListWithArrays() {
        List<double[]> list = new ArrayList<>();
        double[] arrayTwo = initArray(5);
        double[] arrayOne = initArray(5);
        list.add(arrayOne);
        list.add(arrayTwo);
        return list;
    }

    @Contract(pure = true)
    static double[][] init2Darray(int rows, int columns) {
        double[][] array = new double[2][5];
        for (int i = 0; i < rows; i++) {
            array[i] = initArray(5);
        }
        return array;
    }

    @Contract(pure = true)
    static double[] initArray(int size) {
        double[] array = new double[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }
        return array;
    }

}
