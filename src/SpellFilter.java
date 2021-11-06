import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.spell.LuceneLevenshteinDistance;
import org.apache.lucene.search.spell.StringDistance;

import java.io.IOException;

public class SpellFilter extends TokenFilter {
    private QuerySpellChecker spellChecker;
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    protected SpellFilter(TokenStream input) throws IOException {
        super(input);
        StringDistance sd = new LuceneLevenshteinDistance();
        this.spellChecker = new QuerySpellChecker(sd);

        //JaroWinklerDistance, LevensteinDistance, LuceneLevenshteinDistance, NGramDistance
    }

    @Override
    public boolean incrementToken() throws IOException {
        String correction;
        if (input.incrementToken()) {
            correction = getCorrection(termAtt.buffer(), 0, termAtt.length());
            termAtt.copyBuffer(correction.toCharArray() , 0, correction.length());
            return true;
        } else
            return false;
    }

    public String getCorrection(final char[] buffer, final int offset, final int limit) throws IOException {
        String str = new String(termAtt.buffer(), 0 ,termAtt.length());
        String correction = this.spellChecker.querySpellCheckerCreation(str);
        return correction;
    }
}
