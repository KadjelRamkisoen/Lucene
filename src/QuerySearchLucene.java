import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class QuerySearchLucene {


    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        System.out.println("start_date: " + startTime);
        Analyzer splitAnalyzer = new WordSplitter();
        Path path = Paths.get(".//indexes_big");
        Directory directory = FSDirectory.open(path);
        IndexReader reader = DirectoryReader.open(directory);

        File queryFile = new File("queries.csv");
        ArrayList<String[]> queries = readQueryTsv(queryFile);
        TopDocs documents;
        String filename;
        Integer[] record = new Integer[2];
        String[] words;
        FileWriter writer = new FileWriter("results_queries.csv");
        writer.write( "Query_number" + "\t" + "Document_number" + "\n");
        for (String[] g: queries){
            System.out.println(g[1]);
            String regex_str = "[\\?\\*\\(\\)\\[]";
            documents = runSearch(directory,splitAnalyzer, g[1].replaceAll(regex_str, "").replaceAll("[\\/\\:\\;]", " "));
            // write information to a file
            int i = 0;
            if (documents != null) {
                for (ScoreDoc scoreDoc : documents.scoreDocs) {
                    i++;
                    int doc_id = scoreDoc.doc;
                    float score = scoreDoc.score;
                    Document doc = reader.document(doc_id);
                    System.out.println("File: " + doc.get("filename"));

                    filename = doc.get("filename");
                    words = Pattern.compile("[_.]").split(filename);

                    record[0] = Integer.parseInt(g[0]);
                    record[1] = Integer.parseInt(words[1]);
                    writer.write(+record[0] + "\t" + record[1] + "\n");
                }
            }
        }

        writer.close();
        long endTime = System.currentTimeMillis();
        System.out.println("end_date: " + endTime);
        System.out.println("That took " + (endTime - startTime)/1000 + " seconds");
    }

    //testing various of queries
    private static TopDocs testBooleanQueryDocuments(Directory directory, Analyzer splitAnalyzer,  String queryString) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher (reader);

        QueryBuilder builder = new QueryBuilder(splitAnalyzer);
        Query query = builder.createMinShouldMatchQuery("content", queryString, 0.75f);
        TopDocs results = null;
        if (query!=null){
            results = searcher.search(query, 10);
            System.out.println("Query text: " + query.toString());
            System.out.println("Hits for " + queryString + " -->" + results.totalHits);
        }
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
        TopDocs results = searcher.search(query, 10);
        System.out.println("Query text: " + query.toString());
        System.out.println("Hits for " + queryString + " -->" + results.totalHits);
        return results;
    }

    private static TopDocs testPhraseQueryDocuments(Directory directory, Analyzer splitAnalyzer,  String queryString) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher (reader);

        StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
        QueryBuilder builder = new QueryBuilder(splitAnalyzer);
        //Query query = new FuzzyQuery(new Term(queryString), 5);
//        BooleanQuery booleanQuery =  new BooleanQuery(5, BooleanClause.Occur.MUST);
        Query query = builder.createPhraseQuery("content", queryString, 5);
        TopDocs results = null;
        if (query!=null){
            results = searcher.search(query, 10);
            System.out.println("Query text: " + query.toString());
            System.out.println("Hits for " + queryString + " -->" + results.totalHits);
        }
        return results;
    }

    private static TopDocs testSimilarityQueryDocuments(Directory directory, Analyzer splitAnalyzer,  String queryString) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher (reader);

        Similarity similarity =  new BM25Similarity(1.2F, 0.1F);
        //Similarity[] sims = new Similarity[2];
        //sims[0] = new BM25Similarity(1.2F, 0.3F);
        //sims[1] = new LMDirichletSimilarity(2500);

        //Similarity similarity = new MultiSimilarity(sims);
        searcher.setSimilarity(similarity);

        QueryBuilder builder = new QueryBuilder(splitAnalyzer);
        Query query = builder.createMinShouldMatchQuery("content", queryString, 0.75f);

        TopDocs results = null;
        if (query!=null){
            results = searcher.search(query, 10);
            System.out.println("Query text: " + query.toString());
            System.out.println("Hits for " + queryString + " -->" + results.totalHits);
        }
        return results;
    }

    private static TopDocs runSearch(Directory directory, Analyzer splitAnalyzer,  String queryString) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher (reader);
        // set chosen parameters and similarity
        Similarity similarity =  new BM25Similarity(1.2f, 0.3f);
        searcher.setSimilarity(similarity);
        QueryBuilder builder = new QueryBuilder(splitAnalyzer);
        // set fraction value
        Query query = builder.createMinShouldMatchQuery("content", queryString, 0.75f);

        TopDocs results = null;
        if (query!=null){
            results = searcher.search(query, 10);
            System.out.println("Query text: " + query.toString());
            System.out.println("Hits for " + queryString + " -->" + results.totalHits);
        }
        return results;
    }
    private static ArrayList<String[]> readQueryTsv(File queryFile){
        ArrayList<String[]> Data = new ArrayList<>(); //initializing a new ArrayList out of String[]'s

        try (BufferedReader TSVReader = new BufferedReader(new FileReader(queryFile))) {
            String line;
            while ((line = TSVReader.readLine()) != null) {
                String[] lineItems = line.split("\t"); //splitting the line and adding its items in String[]
                Data.add(lineItems); //adding the splitted line array to the ArrayList
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        return Data;
    }
}
