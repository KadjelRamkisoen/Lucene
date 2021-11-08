import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.search.spell.LuceneLevenshteinDistance;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
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
import java.util.List;


// https://www.baeldung.com/lucene-analyzers

public class IndexingLucene {

    /**
     * Print out all the tokens
     * Prints all the terms in the index
     * @param reader - Reader for the index file
     * @param fieldName - Name of the field to read from the index
     * @throws IOException - IOException
     */
    private static void getTokensForField(IndexReader reader, String fieldName) throws IOException {
        List<LeafReaderContext> list = reader.leaves();
        QuerySpellChecker query = new QuerySpellChecker(new LuceneLevenshteinDistance());
        String suggestion;

        for (LeafReaderContext lrc : list) {
            Terms terms = lrc.reader().terms(fieldName);
            if (terms != null) {
                TermsEnum termsEnum = terms.iterator();

                BytesRef term;
                while ((term = termsEnum.next()) != null) {
//                    LanguageDetector object = new OptimaizeLangDetector().loadModels();
//                    LanguageResult result = object.detect(term.utf8ToString());
                    suggestion = query.querySpellCheckerCreation(term.utf8ToString());
                    if (!term.utf8ToString().equals(suggestion)) {
                        //Print spell corrections
                        System.out.println("Term: " + term.utf8ToString());
                        System.out.println("Correction: " + suggestion);
                        System.out.println("Correction: " + suggestion);
                        // print language
                        //System.out.println("Language: " + result.getLanguage());
                    }
                }
            }
        }
    }

    /**
     * Calculates the number of tokens in stream
     * @param reader
     * @param fieldName
     * @throws IOException
     */
    private static void getLength(IndexReader reader, String fieldName) throws IOException {
        List<LeafReaderContext> list = reader.leaves();
        int i = 0;
        for (LeafReaderContext lrc : list) {
            Terms terms = lrc.reader().terms(fieldName);
            if (terms != null) {
                TermsEnum termsEnum = terms.iterator();

                BytesRef term;
                while ((term = termsEnum.next()) != null) {
                        System.out.println("Term: " + term.utf8ToString());
                        i++;
                    System.out.println("current i : " + i);
                }
            }
        }
        System.out.println("Length: " + i);
    }

    /**
     * queryDocuments
     * * The execution of a query in all the documents
     * @param directory - Where the indexed documents are
     * @param splitAnalyzer - Analyzer object
     * @param queryString - The query
     * @return Top documents with hits
     * @throws IOException - IOException
     * @throws TikaException - TikaException
     * @throws SAXException - SAXException
     * @throws ParseException - ParseException
     */
    private static TopDocs queryDocuments (Directory directory, Analyzer splitAnalyzer,  String queryString) throws IOException, TikaException, SAXException, ParseException {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher (reader);
        QueryParser parser = new QueryParser ("content", splitAnalyzer);
        //LanguageDetection languageDetection = new LanguageDetection();
        //String queryLanguage = languageDetection.Language(child);

        Query query = parser.parse(queryString);
        TopDocs results = searcher.search(query, 10);
        System.out.println("Hits for Science -->" + results.totalHits);

        List<ScoreDoc> hitsDocs = Arrays.stream((results.scoreDocs)).toList();

        for(ScoreDoc scoreDoc: hitsDocs){
            System.out.println(reader.document(scoreDoc.doc));
        }
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
     * Runs indexing of set of the documents
     * To execute the querying
     * @param args - arguments
     * @throws Exception - Exception
     */
    public static void main(String[] args) throws Exception {
        // time it
        long startTime = System.currentTimeMillis();
        System.out.println("start_date: " + startTime);

        Analyzer splitAnalyzer = new WordSplitter();
        // set the folder for indexes
        Path path = Paths.get(".//indexes_big");
        Directory directory = FSDirectory.open(path);
        IndexWriterConfig config = new IndexWriterConfig(splitAnalyzer);

        //Create a writer
        IndexWriter writer = new IndexWriter(directory, config);
        // set path to a set of gocuments
        String docsDirectoryPath = ".//full_docs//full_docs";
        File dir = new File(docsDirectoryPath);
        File[] directoryListing = dir.listFiles();
       // Hashtable<String, Integer> myDict = new Hashtable<String, Integer>();

        if (directoryListing != null) {
            for (File child : directoryListing) {
                Document document = new Document ();
                FileReader fileReader =  new FileReader(child);
                //LanguageDetection languageDetection = new LanguageDetection();

                //String docLanguage = languageDetection.Language(child);
                //System.out.println(child.getName());
                document.add(new TextField("content", fileReader));
                document.add(new TextField("path", child.getPath(), Field.Store.YES));
                document.add(new TextField("filename", child.getName(), Field.Store.YES));
                System.out.println(child.getName());
                //document.add(new TextField("language", docLanguage, Field.Store.YES));
//                System.out.println("File: " + child.getName());
//                System.out.println("Doc Language: " + docLanguage);
                writer.addDocument(document);
/*                if (!myDict.containsKey(docLanguage)){
                    myDict.put(docLanguage, 1);
                } else {
                    myDict.put(docLanguage, myDict.get(docLanguage) + 1);
                }

                if (!docLanguage.equals("en")){
                    System.out.println(docLanguage + ": " + child.getName());
                }*/
            }
        }
  //      System.out.println("Language dict: " + myDict);

        writer.close();
        long endTime = System.currentTimeMillis();
        System.out.println("end_date: " + endTime);
        System.out.println("That took " + (endTime - startTime)/1000 + " seconds");
    }
}