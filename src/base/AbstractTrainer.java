package base;


import data.StopWordFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sermilion on 07/08/2016.
 */
public abstract class AbstractTrainer implements CentroidTrainer {

    protected String lang;
    protected int tagId;
    protected int divisor;

    /**
     * Method to get term frequency weighing
     * @param articlesTerms: list of article terms
     * @param allTerms  - all terms from database
     * @return list of arrays of tfws
     */
    public List<double[]> tfwForTag(List<List<String>> articlesTerms, List<String> allTerms) {
        List<double[]> tfws = new ArrayList<>();
        for (List<String> termsForArticle : articlesTerms) {
            double[] tfw_for_article = new double[allTerms.size()];
            for (int i = 0; i < allTerms.size(); i++) {
                String term = allTerms.get(i);
                int occurrences = Collections.frequency(termsForArticle, term);
                if (occurrences == 0) {
                    tfw_for_article[i] = 0;
                } else {
                    tfw_for_article[i] = 1 + Math.log10(occurrences);
                }
            }
            tfws.add(tfw_for_article);
        }
        return tfws;
    }

    /**
     * Method to tokenize articles and remove punctuations
     */
    public List<String[]> tokenize(List<String> articleForTag) {
        ArrayList<String[]> termsForTag = new ArrayList<>();
        for (String article : articleForTag) {
            String[] tokens = article.replaceAll("[\\d\\r#€¡¢∞§•ªº\"≠!?@£$%^&*()_+=,.`~;:<>'\\|{}/›]", "").replaceAll("…", " ").replaceAll("\\n", " ").split(" ");
            List<String> list = new ArrayList<>(Arrays.asList(tokens));
            list.removeAll(Arrays.asList("", null, "-"));
            list = list.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
            termsForTag.add(list.toArray(new String[list.size()]));
        }
        return termsForTag;
    }

    /**
     * Method to remove stop-words from a list of word arrays
     * @param termsForTag a list of words arrays
     * @return list with removed stop words
     */
    public List<List<String>> removeStopwords(List<String[]> termsForTag) {
        List<List<String>> noStopwords = new ArrayList<>();
        String[] stopWords = StopWordFactory.obtainStopWords(lang);
        List<String> list1;
        for (String[] article : termsForTag) {
            list1 = new ArrayList<>(Arrays.asList(article));
            list1.removeAll(new ArrayList<>(Arrays.asList(stopWords)));
            noStopwords.add(list1);
        }
        return noStopwords;
    }

    public int getTagId() {
        return tagId;
    }

    public int getDivisor() {
        return divisor;
    }

    public void setDivisor(int divisor) {
        this.divisor = divisor;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
