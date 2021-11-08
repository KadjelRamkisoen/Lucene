import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.standard.ClassicTokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WordSplitter extends Analyzer {
    // https://riptutorial.com/lucene/example/17013/creating-a-custom-analyzer
    // https://www.javatips.net/api/org.apache.lucene.analysis.miscellaneous.worddelimiterfilterfactory
    // https://stackoverflow.com/questions/68115969/seaching-for-product-codes-phone-numbers-in-lucene
    private WordDelimiterGraphFilterFactory getWordDelimiter() {
        Map<String, String> settings = new HashMap<>();
        settings.put("generateWordParts", "1");   // e.g. "PowerShot" => "Power" "Shot"
        settings.put("preserveOriginal", "0");
        settings.put("splitOnCaseChange", "1");   // e.g. "fooBar" => "foo" "Bar"
        settings.put("splitOnNumerics", "1");
        settings.put("stemEnglishPossessive", "1");

        return new WordDelimiterGraphFilterFactory(settings);
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        TokenStream tokenStream = new LowerCaseFilter(in);
        return tokenStream;
    }

    public WordSplitter() throws IOException {
        Analyzer analyzer = CustomAnalyzer.builder()
                .withTokenizer("standard")
                .build();
    }

    /**
     * TokenStreamComponents
     * Process the documents and create tokens
     * @param fieldName
     * @return
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {

        Tokenizer tokenizer = new ClassicTokenizer();
        TokenStream stream = getWordDelimiter().create(tokenizer);
        stream = new LowerCaseFilter(stream);
        stream = new StopFilter(stream, getStopWords());

        stream = new NumberFilter(stream);
        try {
            stream = new SpellFilter(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stream = new LemmaFilter(stream);
        stream = new RemoveDuplicatesTokenFilter(stream);

        return new TokenStreamComponents(tokenizer, stream);
    }
    public int calculateStreamLength(TokenStream stream) throws IOException {
        int i = 0;
        System.out.println(stream.toString());
        return i;
    }

    protected static CharArraySet getStopWords(){
        ArrayList<String> myStopwordList = new ArrayList<>(); //initializing a new ArrayList out of String[]'s
        File wordListFile = new File("stop_words.txt");
        try (BufferedReader TSVReader = new BufferedReader(new FileReader(wordListFile))) {
            String line;
            while ((line = TSVReader.readLine()) != null) {
                myStopwordList.add(line); //adding the splitted line array to the ArrayList
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        CharArraySet stopset = org.apache.lucene.analysis.core.StopFilter.makeStopSet(myStopwordList);
        return stopset;
    }
}