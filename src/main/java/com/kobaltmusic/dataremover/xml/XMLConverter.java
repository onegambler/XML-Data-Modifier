package com.kobaltmusic.dataremover.xml;

import com.kobaltmusic.dataremover.xml.manipulation.XPathRule;
import com.kobaltmusic.dataremover.input.JsonPropertiesReader;
import com.kobaltmusic.dataremover.xml.manipulation.XmlRuleSet;
import com.kobaltmusic.dataremover.xml.parsing.SAXTransformationHandler;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileWriter;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class XMLConverter {

    private String inputFile;
    private String outputFile;
    private Map<String, XPathRule> xPathRules;

    public XMLConverter(String inputFile, String outputFile, String configurationFile) throws Exception {
        requireNonNull(inputFile, "Input file cannot be null");
        requireNonNull(outputFile, "Output file cannot be null");
        requireNonNull(configurationFile, "Configuration file cannot be null");

        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.xPathRules = new JsonPropertiesReader().read(configurationFile);
    }

    public void convert(boolean escapeCharacters) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();

        try (FileWriter fileWriter = new FileWriter(outputFile)) {
            xmlReader.setContentHandler(new SAXTransformationHandler(fileWriter, new XmlRuleSet(xPathRules), escapeCharacters));
            xmlReader.parse(inputFile);
        }

    }
}
