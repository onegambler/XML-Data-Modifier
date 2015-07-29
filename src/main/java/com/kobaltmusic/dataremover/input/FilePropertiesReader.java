package com.kobaltmusic.dataremover.input;

import com.kobaltmusic.dataremover.xml.manipulation.XPathRule;

import java.io.IOException;
import java.util.Map;

public interface FilePropertiesReader {

    Map<String, XPathRule> read(String fileName) throws IOException;
}
