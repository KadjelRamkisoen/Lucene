import org.apache.lucene.analysis.CharacterUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.StemmerOverrideFilterFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

public class LemmaFilter extends TokenFilter {
    /**
     * Construct a token stream filtering the given input.
     *
     * @param input
     */

    private Lemmatization l;
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    protected LemmaFilter(TokenStream input) {
        super(input);
        this.l = new Lemmatization();
    }

    @Override
    public boolean incrementToken() throws IOException {
        StringBuilder sb = new StringBuilder();
        String lemma;
        if (input.incrementToken()) {
            lemma = getLemma(termAtt.buffer(), 0, termAtt.length());
            termAtt.copyBuffer(lemma.toCharArray() , 0, lemma.length());
            return true;
        } else
            return false;
    }

    public String getLemma(final char[] buffer, final int offset, final int limit) {
        assert buffer.length >= limit;
        assert offset <=0 && offset <= buffer.length;
        String lemma = this.l.lemmatize(new String(termAtt.buffer(), 0 ,termAtt.length()));
        return lemma;
    }

}
/*
* skippedPositions = 0;
    while (input.incrementToken()) {
      if (accept()) {
        if (skippedPositions != 0) {
          posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + skippedPositions);
        }
        return true;
      }
      skippedPositions += posIncrAtt.getPositionIncrement();
    }

    // reached EOS -- return false
    return false;
    */