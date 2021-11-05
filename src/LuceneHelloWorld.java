import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;


// https://www.baeldung.com/lucene-analyzers

public class LuceneHelloWorld {
    public static String removeStopWords(String textFile) throws Exception {
        //https://lucene.apache.org/core/4_7_0/core/org/apache/lucene/analysis/TokenStream.html
        // https://stackoverflow.com/questions/23931699/apache-lucene-tokenstream-contract-violation
        CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
        StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
        System.out.println(stopWords);
        TokenStream tokenStream = standardAnalyzer.tokenStream("context", new StringReader(textFile));
        tokenStream = new StopFilter(tokenStream, stopWords);

        StringBuilder sb = new StringBuilder();

        CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
        tokenStream.reset();
        System.out.println(token.toString());
        while (tokenStream.incrementToken()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(token.toString());
        }
        tokenStream.end();
        tokenStream.close();
        return sb.toString();
    }


    private static void getTokensForField(IndexReader reader, String fieldName) throws IOException {
        List<LeafReaderContext> list = reader.leaves();
        for (LeafReaderContext lrc : list) {
            Terms terms = lrc.reader().terms(fieldName);
            if (terms != null) {
                TermsEnum termsEnum = terms.iterator();

                BytesRef term;
                while ((term = termsEnum.next()) != null) {
                    System.out.println(term.utf8ToString());
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        //New index
       /* StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(standardAnalyzer);
        //Create a writer
        IndexWriter writer = new IndexWriter(directory, config);

        String docsDirectoryPath = ".//docs_small//full_docs_small";
        File dir = new File(docsDirectoryPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                // Do something with child
                Document document = new Document ();
                FileReader fileReader =  new FileReader(child);
                document.add(new TextField("content", fileReader));
                document.add(new TextField("path", child.getPath(), Field.Store.YES));
                document.add(new TextField("filename", child.getName(), Field.Store.YES));
                writer.addDocument(document);
            }
        }
        writer.close();
*/
        //New index
        Analyzer sptilAnalyzer = new WordSplitter();

        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(sptilAnalyzer);
        //Create a writer
        IndexWriter writer = new IndexWriter(directory, config);
        String docsDirectoryPath = ".//docs_small//full_docs_small";
        File dir = new File(docsDirectoryPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                // Do something with child
                Document document = new Document ();
                FileReader fileReader =  new FileReader(child);
                document.add(new TextField("content", fileReader));
                document.add(new TextField("path", child.getPath(), Field.Store.YES));
                document.add(new TextField("filename", child.getName(), Field.Store.YES));
                writer.addDocument(document);
            }
        }
        writer.close();
        //Now let's try to search for Science
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher (reader);
        QueryParser parser = new QueryParser ("content", sptilAnalyzer);
        Query query = parser.parse("what was the fundamental cause of the great depression");
        TopDocs results = searcher.search(query, 10);
        System.out.println("Hits for Science -->" + results.totalHits);

        List<ScoreDoc> hitsDocs = Arrays.stream((results.scoreDocs)).toList();

        for(ScoreDoc scoreDoc: hitsDocs){
            System.out.println(reader.document(scoreDoc.doc));
        }

        //case insensitive search
        query = parser.parse("what was the fundamental cause of the great depression");
        results = searcher.search(query, 10);
        System.out.println("Hits for science insensitive -->" + results.totalHits);

        hitsDocs = Arrays.stream((results.scoreDocs)).toList();

        for(ScoreDoc scoreDoc: hitsDocs){
            System.out.println(reader.document(scoreDoc.doc));
        }
        System.out.println();
        //System.out.println(removeStopWords("Science & Mathematics PhysicsThe hot glowing surfaces of stars emit energy in the form of electromagnetic radiation.?It is a good approximation to assume that the emissivity e is equal to 1 for these surfaces.  Find the radius of the star Rigel, the bright blue star in the constellation Orion that radiates energy at a rate of 2.7 x 10^32 W and has a surface temperature of 11,000 K. Assume that the star is spherical. Use σ =... show moreFollow 3 answersAnswersRelevanceRatingNewestOldestBest Answer: Stefan-Boltzmann law states that the energy flux by radiation is proportional to the forth power of the temperature: q = ε · σ · T^4 The total energy flux at a spherical surface of Radius R is Q = q·π·R² = ε·σ·T^4·π·R² Hence the radius is R = √ ( Q / (ε·σ·T^4·π) ) = √ ( 2.7x10+32 W / (1 · 5.67x10-8W/m²K^4 · (1100K)^4 · π) ) = 3.22x10+13 mSource (s):http://en.wikipedia.org/wiki/Stefan_bolt...schmiso · 1 decade ago0 18 CommentSchmiso, you forgot a 4 in your answer. Your link even says it: L = 4pi (R^2)sigma (T^4). Using L, luminosity, as the energy in this problem, you can find the radius R by doing sqrt (L/ (4pisigma (T^4)). Hope this helps everyone.Caroline · 4 years ago4 1 Comment (Stefan-Boltzmann law) L = 4pi*R^2*sigma*T^4 Solving for R we get: => R = (1/ (2T^2)) * sqrt (L/ (pi*sigma)) Plugging in your values you should get: => R = (1/ (2 (11,000K)^2)) *sqrt ( (2.7*10^32W)/ (pi * (5.67*10^-8 W/m^2K^4))) R = 1.609 * 10^11 m? · 3 years ago0 1 CommentMaybe you would like to learn more about one of these?Want to build a free website? Interested in dating sites?Need a Home Security Safe? How to order contacts online?\n"));

        reader = DirectoryReader.open(directory);
        getTokensForField(reader, "content");


    }

}

// stop words
// lemmatization
