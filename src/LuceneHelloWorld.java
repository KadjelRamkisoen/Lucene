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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


// https://www.baeldung.com/lucene-analyzers

public class LuceneHelloWorld {

    /**
     * getTokensForField
     * Prints all the terms in the index
     * @param reader - Reader for the index file
     * @param fieldName - Name of the field to read from the index
     * @throws IOException - IOException
     */
    private static void getTokensForField(IndexReader reader, String fieldName) throws IOException {
        List<LeafReaderContext> list = reader.leaves();
        for (LeafReaderContext lrc : list) {
            Terms terms = lrc.reader().terms(fieldName);
            if (terms != null) {
                TermsEnum termsEnum = terms.iterator();

                BytesRef term;
                while ((term = termsEnum.next()) != null) {
//                    LanguageDetector object = new OptimaizeLangDetector().loadModels();
//                    LanguageResult result = object.detect(term.utf8ToString());

                    //Print language
                    System.out.println("Term: " + term.utf8ToString());
//                    System.out.println("Language: " + result.getLanguage());

                }
            }
        }
    }

    /**
     * queryDocuments
     * * The execution of a query in all the documents
     * @param directory - Where the indexed documents are
     * @param sptilAnalyzer - Analyzer object
     * @param child - Document in root directory of all documents
     * @param queryString - The query
     * @return Top documents with hits
     * @throws IOException - IOException
     * @throws TikaException - TikaException
     * @throws SAXException - SAXException
     * @throws ParseException - ParseException
     */
    private static TopDocs queryDocuments (Directory directory, Analyzer sptilAnalyzer, File child, String queryString) throws IOException, TikaException, SAXException, ParseException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher (reader);
        QueryParser parser = new QueryParser ("content", sptilAnalyzer);
        LanguageDetection languageDetection = new LanguageDetection();

        String queryLanguage = languageDetection.Language(child);

        Query query = parser.parse(queryString);
        TopDocs results = searcher.search(query, 10);
        System.out.println("Hits for Science -->" + results.totalHits);

        return results;
    }

    /**
     * readQueryTsv
     * To read the .tsv file that contains the queries
     * @param queryFile -  The tsv file with all the queries
     * @return array of queries
     */
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

    /**
     * main
     * To execute the querying
     * @param args - arguments
     * @throws Exception - Exception
     */
    public static void main(String[] args) throws Exception {
        //New index
        Analyzer sptilAnalyzer = new WordSplitter();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(sptilAnalyzer);

        //Create a writer
        IndexWriter writer = new IndexWriter(directory, config);
        String docsDirectoryPath = ".//docs_small//full_docs_small";
        File dir = new File(docsDirectoryPath);
        File[] directoryListing = dir.listFiles();
        int i = 0;
        Hashtable<String, Integer> myDict = new Hashtable<String, Integer>();

        if (directoryListing != null) {
            for (File child : directoryListing) {
                // Do something with child
                Document document = new Document ();
                FileReader fileReader =  new FileReader(child);
                LanguageDetection languageDetection = new LanguageDetection();

                String docLanguage = languageDetection.Language(child);

                document.add(new TextField("content", fileReader));
                document.add(new TextField("path", child.getPath(), Field.Store.YES));
                document.add(new TextField("filename", child.getName(), Field.Store.YES));
                document.add(new TextField("language", docLanguage, Field.Store.YES));
//                System.out.println("File: " + child.getName());
//                System.out.println("Doc Language: " + docLanguage);
                writer.addDocument(document);

                if (!myDict.containsKey(docLanguage)){
                    myDict.put(docLanguage, 1);
                }else{
                    myDict.put(docLanguage, myDict.get(docLanguage) + 1);
                }

                if (!docLanguage.equals("en")){
                    System.out.println(docLanguage + ": " + child.getName());
                }
            }
        }
        System.out.println("Language dict: " + myDict);

        writer.close();

        //Now let's try to query
        File queryFile = new File("dev_queries.tsv");
        ArrayList<String[]> queries = readQueryTsv(queryFile);

        Hashtable<String, Integer> myQueryDict = new Hashtable<String, Integer>();
        LanguageDetection languageDetection = new LanguageDetection();
        for (String[] g: queries){
            String queryLanguage = languageDetection.Language(g[1]);

            if (!myQueryDict.containsKey(queryLanguage)){
                myQueryDict.put(queryLanguage, 1);
            }else{
                myQueryDict.put(queryLanguage, myQueryDict.get(queryLanguage) + 1);
            }

            if (!queryLanguage.equals("en")){
                System.out.println(queryLanguage + ": " + g[0] + "-" + g[1]);
            }
        }
        System.out.println(myQueryDict);
//        reader = DirectoryReader.open(directory);
//        getTokensForField(reader, "content");
    }
}