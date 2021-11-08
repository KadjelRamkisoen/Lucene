import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.standard.ClassicTokenizer;

import java.io.IOException;
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

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {

        Tokenizer tokenizer = new ClassicTokenizer();
        TokenStream stream = getWordDelimiter().create(tokenizer);
        stream = new LowerCaseFilter(stream);
        stream = new StopFilter(stream, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
        //stream = new StandardFilter(tokenizer);
        stream = new NumberFilter(stream);
        try {
            stream = new SpellFilter(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        stream = new LemmaFilter(stream);

        stream = new RemoveDuplicatesTokenFilter(stream);


        /*
        //Pattern p = Pattern.compile("[\\?\\*]");
        //boolean replaceAll = Boolean.TRUE;
        //stream = new PatternReplaceFilter(stream, p, "", replaceAll);
        TokenFilterFactory.availableTokenFilters();
        System.out.println(TokenizerFactory.availableTokenizers());
        //Order matters!  If LowerCaseFilter and StopFilter were swapped here, StopFilter's
        //matching would be case sensitive, so "the" would be eliminated, but not "The"


        stream = new LemmaFilter(stream);
      //
        Set<String> str = TokenFilterFactory.availableTokenFilters();

        stream = new DelimitedTermFrequencyTokenFilter(stream);
        System.out.println(str);
        stream = new CachingTokenFilter(stream);
*/
        return new TokenStreamComponents(tokenizer, stream);
    }
    public int calculateStreamLength(TokenStream stream) throws IOException {
        int i = 0;
        System.out.println(stream.toString());
        return i;
    }
}