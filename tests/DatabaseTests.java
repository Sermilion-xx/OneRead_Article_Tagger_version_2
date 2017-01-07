import data.Database;
import org.javatuples.Pair;
import org.junit.Rule;
import org.junit.Test;

import org.junit.contrib.java.lang.system.SystemOutRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Created by Sermilion on 02/01/2017.
 */
public class DatabaseTests {

    private static final String TAG = "ios";
    private static final String LANG = "en";
    @Rule
    public final SystemOutRule log =
            new SystemOutRule().enableLog();

    @Test
    public void getWordsDictionaryTest() {
        List<String> allTerms = Database.getInstance().getWordsDictionary(LANG);
        assertThat("An exception occurred", allTerms != null);
    }

    @Test
    public void addTagIdTest() {
        int id = Database.getInstance().addTag(TAG, LANG);
        assertThat("is must be greater then 0", id > 0);
    }

    @Test
    public void getIdByTagTest() {
        int id = Database.getInstance().getIdByTag(TAG);
        assertThat("is must be greater then 0", id > 0);
    }

    @Test
    public void getIdsNamesForTagTest() {
        List<String> namesForTag = Database.getInstance().getIdsNamesForTag("23,1,4,5,7");
        assertThat("An exception occurred", namesForTag != null);
    }

    @Test
    public void getIdsByTagsNamesTest() {
        List<Integer> isdForTagNames = Database.getInstance().getIdsByTagsNames("'ios', 'android', 'microsoft'");
        assertThat("An exception occurred", isdForTagNames != null);
    }

    @Test
    public void getTagCountsAndIdsWhereNumOfTagsMoreThanTest() {
        List<Pair<Integer, Integer>> idsAndNames = Database.getInstance().getTagCountsAndIdsWhereNumOfTagsMoreThan(101);
        assertThat("An exception occurred", idsAndNames != null);
    }

    @Test
    public void getArticlesForTagTest() {
        Pair<List<String>, Integer> articlesForTag = Database.getInstance().getArticlesForTag(1, 100, LANG, 0);
        assertThat("An exception occurred", articlesForTag != null);
    }

    @Test
    public void getArticlesAndIdsForTagTest() {
        List<Pair<Integer, String>> list = Database.getInstance().getArticlesAndIdsForTag(23, 10, LANG, 0);
        assertThat("An exception occurred", list != null);
    }

    @Test
    public void addWordsTest() {
        List<List<String>> allTermsForTag = new ArrayList<>();
        List<String> stringList = new ArrayList<>();
        stringList.add("a");
        allTermsForTag.add(stringList);
        List list = Database.getInstance().addWords(allTermsForTag, "temp_tags", LANG);
        assertThat("An exception occurred", list != null);
    }

    @Test
    public void saveCentroidTest() {
        Database.getInstance().saveCentroid("test",1, false, 1, LANG);
        assertEquals("Centroid saved!\n", log.getLog());
    }

    @Test
    public void getCentroidAndDivisorTest(){
        Pair<Integer, double[]> centroidAndDivisor =  Database.getInstance().getCentroidAndDivisor(11);
        assertThat("An exception occurred", centroidAndDivisor != null);
    }

    @Test
    public void getAllCentroidsTest(){
        HashMap<Integer, Pair<String, double[]>> allCentroids = Database.getInstance().getAllCentroids();
        assertThat("An exception occurred", allCentroids != null);
    }

    @Test
    public void deleteCentroidsTest(){
        Database.getInstance().deleteCentroids();
        assertEquals("Centroid deleted\n", log.getLog());
    }

    @Test
    public void getAllTempTermsTest(){
        List<String> list = Database.getInstance().getAllTempTerms(LANG);
        assertThat("An exception occurred", list != null);
    }

    @Test
    public void copyWordsFromTempTest(){
        Database.getInstance().copyWordsFromTemp(LANG);
        assertEquals("Words are copied\n", log.getLog());
    }

    @Test
    public void getIdsCountForTagTest(){
        int count = Database.getInstance().getIdsCountForTag(TAG);
        assertThat("An exception occurred, count must be > -1", count>-1);
    }

    @Test
    public void getIdsByTagsTest(){
        List<Integer> list = Database.getInstance().getIdsByTags("'ios','android'");
        assertThat("An exception occurred", list != null);
    }

}
