
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
// https://github.com/dwyl/english-words/blob/master/words_alpha.txt - word list

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QuerySpellChecker {
    PlainTextDictionary wordDict;
    SpellChecker spellchecker;

    public QuerySpellChecker() throws IOException {
        wordDict = new PlainTextDictionary(new File(".//words_alpha.txt"));
        //File dir = new File(".//spell_checker.txt");
        Path path = Paths.get(".//spell_checker.txt");
        Directory directory = FSDirectory.open(path);
        spellchecker = new SpellChecker(directory);

        //spellchecker = new SpellChecker(new RAMDirectory());
    }

    public String[] querySpellCheckerCreation(String field) throws IOException {
       //RAMDirectory dir = new RAMDirectory();

        //Directory spellIndexDirectory = new SimpleFSDirectory(f.getPath());
        //SpellChecker spellchecker = new SpellChecker(spellIndexDirectory);
        spellchecker.indexDictionary(wordDict);
        String[] suggestions = spellchecker.suggestSimilar(field, 5);
        return suggestions;
    }
}
