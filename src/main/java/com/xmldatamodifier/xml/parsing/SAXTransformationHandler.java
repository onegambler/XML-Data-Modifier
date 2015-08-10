package com.xmldatamodifier.xml.parsing;

import com.xmldatamodifier.core.ContentRule;
import com.xmldatamodifier.core.Rule;
import com.xmldatamodifier.core.SkipRule;
import com.xmldatamodifier.xml.manipulation.XmlRuleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;


public class SAXTransformationHandler extends DefaultHandler {

    private final Writer writer;
    private final String documentStart;

    private final Logger log = LoggerFactory.getLogger(SAXTransformationHandler.class);

    private final XPathHandler xPathHandler;
    private final XmlRuleSet ruleSet;

    private boolean skipMode;
    private String skippedPath;

    public SAXTransformationHandler(Writer writer, XmlRuleSet transformer) {
        this(writer, XML_DOCUMENT_START_DEFAULT, transformer);
    }

    public SAXTransformationHandler(Writer writer, String documentStart, XmlRuleSet transformer) {
        this(writer, documentStart, transformer, new XPathHandler());
    }

    public SAXTransformationHandler(Writer writer, String documentStart, XmlRuleSet transformer, XPathHandler xPathHandler) {
        this.writer = writer;
        this.documentStart = documentStart;
        this.ruleSet = transformer;
        this.xPathHandler = xPathHandler;
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

        String currentXPath = xPathHandler.getCurrentXPath();

        try {
            if (skipMode || getRule(currentXPath, SkipRule.class).isPresent()) {
                skippedPath = skipMode ? skippedPath : currentXPath;
                skipMode = true;
            } else {
                writer.write(START_TAG);
                writer.write(qName);

                for (int i = 0; i < attributes.getLength(); i++) {
                    writer.write(" ");
                    writeAttribute(attributes.getLocalName(i), attributes.getValue(i));
                }
                writer.write(END_TAG);
            }

        } catch (Exception err) {

            throw new SAXException(err);

        } finally {

            xPathHandler.enterNode(qName);

        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        try {

            if (!skipMode || skippedPath.equals(xPathHandler.getCurrentXPath())) {
                skipMode = false;
                writer.write(format("</%s>", qName));
            }

        } catch (IOException err) {

            throw new SAXException(err);

        } finally {

            xPathHandler.exitLastEnteredNode();
        }
    }

    protected void writeAttribute(String attributeName, String value) throws SAXException {
        if (!skipMode) {
            try {
                writer.write(attributeName);
                writer.write("=\"");
                char[] attributeValue = value.toCharArray();
                String escapedString = escape(attributeValue, 0, value.length());
                writer.write(escapedString);
                writer.write("\"");
            } catch (IOException err) {
                throw new SAXException(err);
            }
        }
    }

    @Override
    public void characters(char[] input, int start, int length) throws SAXException {
        if (!skipMode) {
            try {

                String escapedString = escape(input, start, length);

                String currentXPath = xPathHandler.getCurrentXPath();
                Set<ContentRule> rulesForXPath = getRules(currentXPath, ContentRule.class);

                if (!rulesForXPath.isEmpty()) {
                    for (ContentRule rule : rulesForXPath) {
                        log.info("Applying rule [{}] to xpath {}", rule, currentXPath);
                        escapedString = rule.elaborate(escapedString);
                    }
                }

                writer.write(escapedString);


            } catch (IOException err) {
                throw new SAXException(err);
            }
        }
    }

    private <T extends Rule> Set<T> getRules(String currentXPath, Class<T> ruleClass) {
        log.debug("Trying to look for rules to apply to current xpath {}", currentXPath);
        Set<T> rulesForXPath = ruleSet.getRulesForXPath(currentXPath, ruleClass);
        log.debug("Found {} rules", rulesForXPath.size());
        return rulesForXPath;
    }

    private <T extends Rule> Optional<T> getRule(String currentXPath, Class<T> ruleClass) {
        Set<T> rules = getRules(currentXPath, ruleClass);
        checkArgument(rules.size() <= 1, "More than one rule of type {} has been defined for path {}. Please specify only rule of that type per path ", ruleClass.getSimpleName(), currentXPath);
        return rules.stream().findFirst();
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

    protected String escape(char ch[], int start, int length) {
        int newLength = 0;
        char[] out = new char[length * 8]; //worst case scenario
        for (int i = start; i < start + length; i++) {
            if (ch[i] == '<') {
                ("&lt;").getChars(0, 4, out, newLength);
                newLength += 4;
            } else if (ch[i] == '>') {
                ("&gt;").getChars(0, 4, out, newLength);
                newLength += 4;
            } else if (ch[i] == '&') {
                ("&amp;").getChars(0, 5, out, newLength);
                newLength += 5;
            } else if (ch[i] == '\"') {
                ("&#34;").getChars(0, 5, out, newLength);
                newLength += 5;
            } else if (ch[i] == '\'') {
                ("&#39;").getChars(0, 5, out, newLength);
                newLength += 5;
            } else {
                out[newLength++] = ch[i];
            }
        }

        return new String(out, 0, newLength).intern();
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
