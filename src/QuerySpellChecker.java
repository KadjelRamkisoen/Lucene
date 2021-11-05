import org.apache.lucene.search.SpellChecker;

import java.io.File;

public class QuerySpellChecker {
    SpellChecker spellchecker = new SpellChecker(spellIndexDirectory);
    // To index a field of a user index:
  spellchecker.indexDictionary(new LuceneDictionary(my_lucene_reader, a_field));
    // To index a file containing words:
  spellchecker.indexDictionary(new PlainTextDictionary(new File("myfile.txt")));
    String[] suggestions = spellchecker.suggestSimilar("misspelt", 5);
}
