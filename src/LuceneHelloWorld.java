import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class LuceneHelloWorld {

    public static void main(String[] args) throws IOException, ParseException {
        //New index
        StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(standardAnalyzer);
        //Create a writer
        IndexWriter writer = new IndexWriter(directory, config);

        String docsDirectoryPath = ".//full_docs_small";
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
        QueryParser parser = new QueryParser ("content", standardAnalyzer);
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
//
    }
}