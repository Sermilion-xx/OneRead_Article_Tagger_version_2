import base.AbstractTrainer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Sermilion on 01/01/2017.
 */
public class AbstractTrainerTests {

    private ArrayList<String> allTerms;
    private List<List<String>> listOfTerms;
    private List<String> terms1;
    private AbstractTrainer trainer;

    @Before
    public void initTrainer() {
        trainer = new AbstractTrainer(){
            @Override
            public double[] calculateCentroid(List<String> articleList, List<String> allTerms) {
                return new double[0];
            }
        };
    }

    void initTfwForTagTest(){
        listOfTerms = new ArrayList<>();
        terms1 = new ArrayList<>();
        terms1.add("d");
        terms1.add("d");
        terms1.add("a");

        listOfTerms.add(terms1);

        allTerms = new ArrayList<>();
        allTerms.add("a");
        allTerms.add("b");
        allTerms.add("c");
        allTerms.add("d");
    }

    @Test
    public void tfwForTagTest(){
        initTfwForTagTest();
        List<double[]> b = trainer.tfwForTag(listOfTerms, allTerms);
        assertThat("Wrong calculations",b.get(0)[0]==1.0 && b.get(0)[3]==1.3010299956639813);
    }

    @Test
    public void tokenizeTest(){
        String[] expected = new String[]{"hello","bitch", "how", "are", "you","doing"};
        List<String> articles = new ArrayList<>();
        articles.add("Hello, ^&!@£$%^&*() bitch \" how ?<> ! are you doing :';");
        List<String[]> tokenized =  trainer.tokenize(articles);
        assertThat("Not tokenized properly", Arrays.equals(tokenized.get(0), expected));
    }

    @Test
    public void removeStopWordsTest(){
        trainer.setLang("en");
        List<String> articles = new ArrayList<>();
        articles.add("Hello, bitch how are you doing?");
        List<String[]> tokenized =  trainer.tokenize(articles);
        List<List<String>> noStopWords = trainer.removeStopwords(tokenized);
        assertThat("", noStopWords.get(0).get(0).equals("hello") && noStopWords.get(0).get(1).equals("bitch"));
    }




}
