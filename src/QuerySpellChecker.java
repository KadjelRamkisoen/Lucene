import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// https://github.com/dwyl/english-words/blob/master/words_alpha.txt - word list

public class QuerySpellChecker {
    Dictionary wordDict;
    SpellChecker spellchecker;

    public QuerySpellChecker(StringDistance sd) throws IOException {
        Path pathWordDict = Paths.get(".//words_alpha.txt");
        wordDict = new PlainTextDictionary(pathWordDict);

        Path path = Paths.get(".//spell_checker.txt");
        Directory directory = FSDirectory.open(path);
        spellchecker = new SpellChecker(directory, sd);
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        spellchecker.indexDictionary(wordDict, config, true);
    }

    /**
     * querySpellCheckerCreation
     * Check spelling in queries
     * @param field
     * @return Similar sugestions
     * @throws IOException
     */
    public String querySpellCheckerCreation(String field) throws IOException {
        String[] suggestions = spellchecker.suggestSimilar(field, 1, 0.7f);
        if (spellchecker.exist(field)) {
            return field;
        }
        else {
            if(suggestions!=null && suggestions.length > 0) {
                return suggestions[0];
            } else {
                return field;
            }

        }
    }

    public static void main (String args[]) throws IOException {
        /*
        QuerySpellChecker query = new QuerySpellChecker(new LuceneLevenshteinDistance());
        String suggestion = query.querySpellCheckerCreation("somthing");
        System.out.println(suggestion);*/
        ArrayList<String> myStopwordList = new ArrayList<>(); //initializing a new ArrayList out of String[]'s
        File queryFile = new File("stop_words.txt");
        try (BufferedReader TSVReader = new BufferedReader(new FileReader(queryFile))) {
            String line;
            while ((line = TSVReader.readLine()) != null) {
                myStopwordList.add(line); //adding the splitted line array to the ArrayList
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        CharArraySet stopset = StopFilter.makeStopSet(myStopwordList);
        System.out.println(stopset);
    }
}