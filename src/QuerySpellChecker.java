
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

// https://github.com/dwyl/english-words/blob/master/words_alpha.txt - word list

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QuerySpellChecker {
    Dictionary wordDict;
    SpellChecker spellchecker;

    public QuerySpellChecker() throws IOException {
        Path pathWordDict = Paths.get(".//words_alpha.txt");
        wordDict = new PlainTextDictionary(pathWordDict);

        Path path = Paths.get(".//spell_checker.txt");
        Directory directory = FSDirectory.open(path);
        spellchecker = new SpellChecker(directory);

        //spellchecker = new SpellChecker(new RAMDirectory());
    }

    public String[] querySpellCheckerCreation(String field) throws IOException {
        //RAMDirectory dir = new RAMDirectory();
        //Directory spellIndexDirectory = new SimpleFSDirectory(f.getPath());
        //SpellChecker spellchecker = new SpellChecker(spellIndexDirectory);

        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        spellchecker.indexDictionary(wordDict, config, true);
        String[] suggestions = spellchecker.suggestSimilar(field, 5);
        return suggestions;
    }

    public static void main (String args[]) throws IOException {
        QuerySpellChecker query = new QuerySpellChecker();

    }
}
