package com.kobaltmusic.dataremover.xml.parsing;

import com.kobaltmusic.dataremover.core.ContentRule;
import com.kobaltmusic.dataremover.core.Rule;
import com.kobaltmusic.dataremover.xml.manipulation.XmlRuleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import static java.lang.String.format;


public class SAXTransformationHandler extends DefaultHandler {

    private final Writer writer;
    private final String documentStart;

    private final Logger log = LoggerFactory.getLogger(SAXTransformationHandler.class);

    private XPathHandler xPathHandler;
    private final boolean escapeCharacters;
    private XmlRuleSet ruleSet;

    public SAXTransformationHandler(Writer writer, XmlRuleSet transformer, boolean escapeCharacters) {
        this(writer, XML_DOCUMENT_START_DEFAULT, transformer, escapeCharacters);
    }

    public SAXTransformationHandler(Writer writer, String documentStart, XmlRuleSet transformer, boolean escapeCharacters) {
        this(writer, documentStart, transformer, new XPathHandler(), escapeCharacters);
    }

    public SAXTransformationHandler(Writer writer, String documentStart, XmlRuleSet transformer, XPathHandler xPathHandler, boolean escapeCharacters) {
        this.writer = writer;
        this.documentStart = documentStart;
        this.ruleSet = transformer;
        this.xPathHandler = xPathHandler;
        this.escapeCharacters = escapeCharacters;
    }

    @Override
    public void startDocument() throws SAXException {
        try {
            log.info("START document transformation");
            writer.write(documentStart + "\n");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        log.info("END document transformation");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            writer.write(START_TAG);
            writer.write(qName);

            // output the attributes
            for (int i = 0; i < attributes.getLength(); i++) {
                writer.write(" ");
                writeAttribute(attributes.getLocalName(i), attributes.getValue(i));
            }
            writer.write(END_TAG);

            xPathHandler.enterNode(qName);

        } catch (IOException err) {
            throw new SAXException(err);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {

            writer.write(format("</%s>", qName));
            xPathHandler.exitLastEnteredNode();

        } catch (IOException err) {
            throw new SAXException(err);
        }
    }

    protected void writeAttribute(String attributeName, String value) throws
            SAXException {
        try {
            writer.write(attributeName);
            writer.write("=\"");
            char[] attributeValue = value.toCharArray();
            char[] attributeEscaped = new char[value.length() * 8];  // worst case scenario
            int newLength = escape(attributeValue, 0, value.length(), attributeEscaped);
            writer.write(attributeEscaped, 0, newLength);
            writer.write("\"");
        } catch (IOException err) {
            throw new SAXException(err);
        }
    }

    @Override
    public void characters(char[] input, int start, int length) throws SAXException {
        try {
            char[] output;
            if (escapeCharacters) {
                output = new char[length * 8]; // worst case scenario
                length = escape(input, start, length, output);
            } else {
                output = input;
            }

            String currentXPath = xPathHandler.getCurrentXPath();
            Set<ContentRule> rulesForXPath = getRules(currentXPath, ContentRule.class);

            if (!rulesForXPath.isEmpty()) {
                String stringContent = new String(output, 0, length);
                for (ContentRule rule : rulesForXPath) {
                    log.info("Applying rule [{}] to xpath {}", rule, currentXPath);
                    stringContent = rule.elaborate(stringContent);
                }

                output = stringContent.toCharArray();
                length = stringContent.length();
            }

            writer.write(output, 0, length);


        } catch (IOException err) {
            throw new SAXException(err);
        }
    }

    private <T extends Rule> Set<T> getRules(String currentXPath, Class<T> ruleClass) {
        log.debug("Trying to look for rules to apply to current xpath {}", currentXPath);
        Set<T> rulesForXPath = ruleSet.getRulesForXPath(currentXPath, ruleClass);
        log.debug("Found {} rules", rulesForXPath.size());
        return rulesForXPath;
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        characters(ch, start, length); //Ignorable whitespace: treat it as characters
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        try {
            writer.write(format("<?%s %s?>", target, data));
        } catch (IOException err) {
            throw new SAXException(err);
        }
    }

    protected int escape(char ch[], int start, int length, char[] out) {
        int output = 0;
        for (int i = start; i < start + length; i++) {
            if (ch[i] == '<') {
                ("&lt;").getChars(0, 4, out, output);
                output += 4;
            } else if (ch[i] == '>') {
                ("&gt;").getChars(0, 4, out, output);
                output += 4;
            } else if (ch[i] == '&') {
                ("&amp;").getChars(0, 5, out, output);
                output += 5;
            } else if (ch[i] == '\"') {
                ("&#34;").getChars(0, 5, out, output);
                output += 5;
            } else if (ch[i] == '\'') {
                ("&#39;").getChars(0, 5, out, output);
                output += 5;
            } else if (ch[i] < 127) {
                out[output++] = ch[i];
            } else {
                // output character reference
                out[output++] = '&';
                out[output++] = '#';
                String code = Integer.toString(ch[i]);
                int len = code.length();
                code.getChars(0, len, out, output);
                output += len;
                out[output++] = ';';
            }
        }

        return output;
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        log.warn("Exception during xml parsing", e);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        log.error("Exception during xml parsing", e);
    }

    public static final String XML_DOCUMENT_START_DEFAULT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    public static final String START_TAG = "<";
    public static final String END_TAG = ">";
}
