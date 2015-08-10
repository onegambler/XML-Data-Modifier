package com.xmldatamodifier.xml.parsing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

public class XPathHandler {

    private final Stack<String> xpath;
    private final Logger log = LoggerFactory.getLogger(XPathHandler.class);

    public XPathHandler() {
        xpath = new Stack<>();
    }

    public void enterNode(String node) {
        xpath.push(node);
    }

    public void exitLastEnteredNode() {
        if (xpath.size() == 0) {
            throw new IllegalStateException("Impossible to exit a node, the xpath was empty");
        }
        xpath.pop();
    }

    public String getCurrentXPath() {

        StringBuilder xPathBuilder = new StringBuilder();

        for (String node : xpath) {
            xPathBuilder.append("/");
            xPathBuilder.append(node);
        }

        return xPathBuilder.toString();
    }
}
