import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.IOException;

public class QuerySpellChecker {

    public static void LuceneSpellChecking(IndexReader reader, String field) throws IOException {
        Directory spellIndexDirectory = new RAMDirectory();
        SpellChecker spellchecker = new SpellChecker(spellIndexDirectory);
        // To index a field of a user index:
        spellchecker.indexDictionary(new LuceneDictionary(reader, field));
        // To index a file containing words:
        spellchecker.indexDictionary(new PlainTextDictionary(new File("myfile.txt")));
        String[] suggestions = spellchecker.suggestSimilar("misspelt", 5);
        
    }


}
