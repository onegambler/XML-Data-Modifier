package com.onegambler.dataremover.input;

import com.onegambler.dataremover.xml.manipulation.XPathRule;

import java.io.IOException;
import java.util.Map;

public interface FilePropertiesReader {

    Map<String, XPathRule> read(String fileName) throws IOException;
}
