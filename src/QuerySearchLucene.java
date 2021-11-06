import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.regex.Pattern;

public class QuerySearchLucene {
    public static void main(String[] args) throws Exception {
        Analyzer splitAnalyzer = new WordSplitter();
        Path path = Paths.get(".//indexes");
        Directory directory = FSDirectory.open(path);
        IndexWriterConfig config = new IndexWriterConfig(splitAnalyzer);
        IndexReader reader = DirectoryReader.open(directory);

//        TopDocs documents = queryDocuments(directory,splitAnalyzer, "science");

        File queryFile = new File("dev_queries.tsv");
        ArrayList<String[]> queries = readQueryTsv(queryFile);
        TopDocs documents;
        String filename;
        Integer[] record = new Integer[2];
        String[] words;
        ArrayList<Integer[]> query_results = new ArrayList<>();
        //System.out.println(Arrays.toString(queries.toArray()));
        for (String[] g: queries){
            System.out.println(g[1]);
            String regex_str = "[\\?\\*\\(\\)]";
            documents = queryDocuments(directory,splitAnalyzer, g[1].replaceAll(regex_str, "").replaceAll("[\\/\\:\\;]", " "));
            for(ScoreDoc scoreDoc : documents.scoreDocs) {

                int doc_id = scoreDoc.doc;
                //System.out.println(doc_id);
                Document doc = reader.document(doc_id);
                System.out.println("File: "+ doc.get("filename"));

                filename = doc.get("filename");
                words = Pattern.compile("[_.]").split(filename);

                record[0] = Integer.parseInt(g[0]);
                record[1] = Integer.parseInt(words[1]);
                query_results.add(record);
                System.out.println("Record: " + record[0] + ", " + record[1]);
            }

        }
    }

    private static TopDocs queryDocuments (Directory directory, Analyzer splitAnalyzer,  String queryString) throws IOException, TikaException, SAXException, ParseException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher (reader);
        QueryParser parser = new ComplexPhraseQueryParser("content", splitAnalyzer);

        Query query = parser.parse(queryString);
        TopDocs results = searcher.search(query, 10);
        System.out.println("Query text: " + query.toString());
        System.out.println("Hits for " + queryString + " -->" + results.totalHits);

        return results;
    }


    private static ArrayList<String[]> readQueryTsv(File queryFile){
        ArrayList<String[]> Data = new ArrayList<>(); //initializing a new ArrayList out of String[]'s

        try (BufferedReader TSVReader = new BufferedReader(new FileReader(queryFile))) {
            String line;
            while ((line = TSVReader.readLine()) != null) {
                String[] lineItems = line.split("\t"); //splitting the line and adding its items in String[]
               // System.out.println(lineItems.toString());
                Data.add(lineItems); //adding the splitted line array to the ArrayList
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        return Data;
    }


}
