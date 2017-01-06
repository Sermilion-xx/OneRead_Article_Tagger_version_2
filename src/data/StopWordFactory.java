package data;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class StopWordFactory {

    public static String[] obtainStopWords(String lang) {
        String[] stopWords = null;
        try {
            if (lang.equals("ru")) {
                stopWords = readFileToArray("russian_stopwords.txt");
            } else if(lang.equals("en")) {
                stopWords = readFileToArray("english_stopwords.txt");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopWords;
    }

    //to read file to array
    @NotNull
    private static String[] readFileToArray(String filename) throws IOException {
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        bufferedReader.close();
        fileReader.close();
        return lines.toArray(new String[lines.size()]);
    }
}
