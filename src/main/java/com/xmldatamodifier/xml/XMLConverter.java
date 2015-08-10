package com.xmldatamodifier.xml;

import com.xmldatamodifier.input.JsonPropertiesReader;
import com.xmldatamodifier.xml.manipulation.XPathRule;
import com.xmldatamodifier.xml.manipulation.XmlRuleSet;
import com.xmldatamodifier.xml.parsing.SAXTransformationHandler;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

    public void convert() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();

        try (Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) {
            xmlReader.setContentHandler(new SAXTransformationHandler(fileWriter, new XmlRuleSet(xPathRules)));
            xmlReader.parse(inputFile);
        }

    }
}
