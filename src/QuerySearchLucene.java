import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.TokenizerFactory;
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
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.regex.Pattern;

public class QuerySearchLucene {


    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        System.out.println("start_date: " + startTime);
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
        FileWriter writer = new FileWriter("output_multi_BM25_Dirih_sim.txt");
        writer.write( "Query_number" + "\t" + "Doc_number" + "\t" + "rating" + "\n");
        //System.out.println(Arrays.toString(queries.toArray()));
        for (String[] g: queries){
            System.out.println(g[1]);
            String regex_str = "[\\?\\*\\(\\)]";
            int i = 0;
            documents = queryDocuments(directory,splitAnalyzer, g[1].replaceAll(regex_str, "").replaceAll("[\\/\\:\\;]", " "));
            for(ScoreDoc scoreDoc : documents.scoreDocs) {
i++;
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
                writer.write( + record[0] + "\t" + record[1] + "\t" + i + "\n");
            }

        }

        writer.close();
       // saveList(query_results);
        //System.out.println(TokenizerFactory.availableTokenizers());
        long endTime = System.currentTimeMillis();
        System.out.println("end_date: " + endTime);
        System.out.println("That took " + (endTime - startTime)/1000 + " seconds");
    }

    private static TopDocs queryDocuments (Directory directory, Analyzer splitAnalyzer,  String queryString) throws IOException, TikaException, SAXException, ParseException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher (reader);
        //System.out.println(searcher.getSimilarity(true));
        Similarity[] sims = new Similarity[2];
        sims[0] = new BM25Similarity(1.2F, 0.1F);
        sims[1] = new LMDirichletSimilarity(2500);
        //Similarity similarity = new BM25Similarity(1.2F, 0.1F);

        Similarity similarity = new MultiSimilarity(sims);

        searcher.setSimilarity(similarity);
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
