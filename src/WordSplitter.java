import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.io.IOException;
import java.util.*;

public class WordSplitter extends Analyzer {
// https://riptutorial.com/lucene/example/17013/creating-a-custom-analyzer
    //https://www.javatips.net/api/org.apache.lucene.analysis.miscellaneous.worddelimiterfilterfactory
    // https://stackoverflow.com/questions/68115969/seaching-for-product-codes-phone-numbers-in-lucene
    private WordDelimiterGraphFilterFactory getWordDelimiter() {
        Map<String, String> settings = new HashMap<>();
        settings.put("generateWordParts", "1");   // e.g. "PowerShot" => "Power" "Shot"
        settings.put("generateNumberParts", "0"); // e.g. "500-42" => "500" "42"
        settings.put("catenateWords", "1");         // e.g. "wi-fi" => "wifi" and "500-42" => "50042"
        settings.put("preserveOriginal", "0");    // e.g. "500-42" => "500" "42" "500-42"
        settings.put("splitOnCaseChange", "1");   // e.g. "fooBar" => "foo" "Bar"
        settings.put("preserveOriginal", "0");   // e.g. "fooBar" => "foo" "Bar"
        settings.put("splitOnNumerics", "1");   // e.g. "fooBar" => "foo" "Bar"
        settings.put("stemEnglishPossessive", "1");   // e.g. "fooBar" => "foo" "Bar"

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
                .addTokenFilter("lowercase")
                .addTokenFilter("stop")
                .addTokenFilter("porterstem")
                .addTokenFilter("capitalization")
                .addTokenFilter("standard")
                .build();
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {

        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream stream = new StandardFilter(tokenizer);

        //Order matters!  If LowerCaseFilter and StopFilter were swapped here, StopFilter's
        //matching would be case sensitive, so "the" would be eliminated, but not "The"


        stream = getWordDelimiter().create(stream);
        stream = new LowerCaseFilter(stream);
        stream = new StopFilter(stream, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
        stream = new NumberFilter(stream);
        stream = new LemmaFilter(stream);
        stream = new StandardFilter(stream);


        stream = new CachingTokenFilter(stream);

        return new TokenStreamComponents(tokenizer, stream);
    }


}