import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.tika.exception.TikaException;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;

import org.xml.sax.SAXException;

class LanguageDetection {

    public String Language(File file) throws IOException, SAXException, TikaException {
        Scanner sc = new Scanner(file);
        //new LanguageProfileReader();
        //Store the file in one string
        sc.useDelimiter("\\p{javaWhitespace}+");

        StringBuilder docContent = new StringBuilder();
        while (sc.hasNextLine()){
            docContent.append(sc.nextLine());
        }

        //Detect language
        LanguageDetector object = new OptimaizeLangDetector().loadModels();
        LanguageResult result = object.detect(docContent.toString());

        return result.getLanguage();
    }

    public String Language(String string) throws IOException, SAXException, TikaException {
        //Detect language
        LanguageDetector object = new OptimaizeLangDetector().loadModels();
        LanguageResult result = object.detect(string);

        return result.getLanguage();
    }
}