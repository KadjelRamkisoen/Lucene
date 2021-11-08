import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.queryparser.ext.ExtendableQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
        Path path = Paths.get(".//indexes_big");
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
        FileWriter writer = new FileWriter("output_big_LMDirichletSim_4000.txt");
        writer.write( "Query_number" + "\t" + "Doc_number" + "\t" + "rating" + "\n");
        //System.out.println(Arrays.toString(queries.toArray()));
        for (String[] g: queries){
            System.out.println(g[1]);
            String regex_str = "[\\?\\*\\(\\)]";
//            documents = testComplexParserDocuments(directory,splitAnalyzer, g[1].replaceAll(regex_str, "").replaceAll("[\\/\\:\\;]", " "));
//            documents = testQueryParserDocuments(directory,splitAnalyzer, g[1].replaceAll(regex_str, "").replaceAll("[\\/\\:\\;]", " "));
//            documents = testExtenableParserDocuments(directory,splitAnalyzer, g[1].replaceAll(regex_str, "").replaceAll("[\\/\\:\\;]", " "));
//            documents = testPhraseQueryDocuments(directory,splitAnalyzer, g[1].replaceAll(regex_str, "").replaceAll("[\\/\\:\\;]", " "));
            documents = testSimilarityQueryDocuments(directory,splitAnalyzer, g[1].replaceAll(regex_str, "").replaceAll("[\\/\\:\\;]", " "));

            int i = 0;
//            if (documents != null) {
            for (ScoreDoc scoreDoc : documents.scoreDocs) {
                i++;
                int doc_id = scoreDoc.doc;
                //System.out.println(doc_id);
                Document doc = reader.document(doc_id);
                System.out.println("File: " + doc.get("filename"));

                filename = doc.get("filename");
                words = Pattern.compile("[_.]").split(filename);

                record[0] = Integer.parseInt(g[0]);
                record[1] = Integer.parseInt(words[1]);
                query_results.add(record);
                System.out.println("Record: " + record[0] + ", " + record[1]);
                writer.write(+record[0] + "\t" + record[1] + "\t" + i + "\n");
            }
//            }

        }

        writer.close();
       // saveList(query_results);
        //System.out.println(TokenizerFactory.availableTokenizers());
        long endTime = System.currentTimeMillis();
        System.out.println("end_date: " + endTime);
        System.out.println("That took " + (endTime - startTime)/1000 + " seconds");
    }

    private static TopDocs testComplexParserDocuments (Directory directory, Analyzer splitAnalyzer,  String queryString) throws IOException, TikaException, SAXException, ParseException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher (reader);
        QueryParser parser = new ComplexPhraseQueryParser("content", splitAnalyzer);

        Query query = parser.parse(queryString);
        TopDocs results = searcher.search(query, 20);
        System.out.println("Query text: " + query.toString());
        System.out.println("Hits for " + queryString + " -->" + results.totalHits);

        return results;
    }

    private static TopDocs testQueryParserDocuments(Directory directory, Analyzer splitAnalyzer,  String queryString) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher (reader);

        QueryParser parser = new QueryParser("content", splitAnalyzer);
        Query query = parser.parse(queryString);
        TopDocs results = searcher.search(query, 20);
        System.out.println("Query text: " + query.toString());
        System.out.println("Hits for " + queryString + " -->" + results.totalHits);
        return results;
    }

    private static TopDocs testExtenableParserDocuments(Directory directory, Analyzer splitAnalyzer,  String queryString) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher (reader);

        QueryParser parser = new ExtendableQueryParser("content", splitAnalyzer);
        Query query = parser.parse(queryString);
        TopDocs results = searcher.search(query, 20);
        System.out.println("Query text: " + query.toString());
        System.out.println("Hits for " + queryString + " -->" + results.totalHits);
        return results;
    }

    private static TopDocs testPhraseQueryDocuments(Directory directory, Analyzer splitAnalyzer,  String queryString) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher (reader);

        StandardAnalyzer standardAnalyzer = new StandardAnalyzer();

        QueryBuilder builder = new QueryBuilder(standardAnalyzer);
        Query query = builder.createPhraseQuery("content", queryString);
        TopDocs results = null;
        if (query!=null){
            results = searcher.search(query, 20);
            System.out.println("Query text: " + query.toString());
            System.out.println("Hits for " + queryString + " -->" + results.totalHits);
        }
        return results;
    }

    private static TopDocs tesClassicSimilarityQueryDocuments(Directory directory, Analyzer splitAnalyzer,  String queryString) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher (reader);
        Similarity similarity =  new ClassicSimilarity();
        searcher.setSimilarity(similarity);

        QueryParser parser = new QueryParser("content", splitAnalyzer);
        Query query = parser.parse(queryString);

        TopDocs results = searcher.search(query, 20);
        System.out.println("Query text: " + query.toString());
        System.out.println("Hits for " + queryString + " -->" + results.totalHits);

        return results;
    }

    private static TopDocs testSimilarityQueryDocuments(Directory directory, Analyzer splitAnalyzer,  String queryString) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher (reader);

        int mu = 4000;
        Similarity similarity =  new LMDirichletSimilarity(mu);
        searcher.setSimilarity(similarity);

        QueryParser parser = new QueryParser("content", splitAnalyzer);
        Query query = parser.parse(queryString);

        TopDocs results = searcher.search(query, 20);
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
