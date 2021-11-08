import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
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
        String lemma;
        if (input.incrementToken()) {
            lemma = getLemma(termAtt.buffer(), 0, termAtt.length());
            termAtt.copyBuffer(lemma.toCharArray() , 0, lemma.length());
            return true;
        } else
            return false;
    }

    public String getLemma(final char[] buffer, final int offset, final int limit) {
        String lemma = this.l.lemmatize(new String(termAtt.buffer(), 0 ,termAtt.length()));
        return lemma;
    }
}