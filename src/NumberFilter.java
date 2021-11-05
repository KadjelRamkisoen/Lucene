import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
//https://stackoverflow.com/questions/46050874/lucene-tokenfilter-with-englishanalyzer-for-removing-numbers-in-scientific-artic

import java.io.IOException;

public class NumberFilter extends FilteringTokenFilter {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    /**
     * Create a new {@link FilteringTokenFilter}.
     *
     * @param in the {@link TokenStream} to consume
     */
    public NumberFilter(TokenStream in) {
        super(in);
    }

    @Override
    protected boolean accept() throws IOException {
        String token = new String(termAtt.buffer(), 0 ,termAtt.length());
        if (token.matches("[0-9,.]+")) {
            return false;
        }
        return true;
    }
}
