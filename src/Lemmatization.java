// https://github.com/collab-uniba/nlp_utilities/blob/master/src/Lemmatization.java
// https://stanfordnlp.github.io/CoreNLP/

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

class Lemmatization {

    private StanfordCoreNLP pipeline;

    public static void main(String[] args) throws IOException {
        Lemmatization l = new Lemmatization();
        l.stem("staying");

    }

    public Lemmatization(){
        // Create StanfordCoreNLP object properties, with POS tagging
        // (required for lemmatization), and lemmatization
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, pos, lemma");

        // StanfordCoreNLP loads a lot of models, so you probably
        // only want to do this once per execution
        this.pipeline = new StanfordCoreNLP(props);
    }

    public void stem(String documentText) throws IOException {
        Analyzer analyzer = new EnglishAnalyzer();
        TokenStream stream = analyzer.tokenStream("content", documentText);
        stream.reset();
        while (stream.incrementToken()) {
            String lemma = stream.getAttribute(CharTermAttribute.class).toString();
            System.out.println("lemma: " + lemma + " ");
        }
        stream.end();
        stream.close();

    }

    public String lemmatize(String documentText){

        String lemma = "";

        // create an empty Annotation just with the given text
        Annotation text = new Annotation(documentText);

        // run all Annotators on this text
        this.pipeline.annotate(text);
        // Iterate over all of the sentences found
        List<CoreMap> sentences = text.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the list of lemmas
                lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
            }
        }
        return lemma;
    }
}