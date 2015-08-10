package com.xmldatamodifier.input;

import com.xmldatamodifier.xml.manipulation.XPathRule;

import java.io.IOException;
import java.util.Map;

public interface FilePropertiesReader {

    Map<String, XPathRule> read(String fileName) throws IOException;
}
