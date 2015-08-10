package com.xmldatamodifier.xml;

import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;

public class XMLConverterTest {

    private static final String PROPERTIES_PATH = "test/properties.json";
    private static final String TEST_INPUT_PATH = "test/testInput.xml";
    private static final String ESCAPED_OUTPUT_PATH = "test/escapedExpectedOutput.xml";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testConvertEscape() throws Exception {
        testConversion(ESCAPED_OUTPUT_PATH);
    }

    private void testConversion(String expectedOutput) throws Exception {
        URL resource = getClass().getClassLoader().getResource(PROPERTIES_PATH);
        requireNonNull(resource);
        String configurationFilePath = Paths.get(resource.toURI()).toString();

        URL input = getClass().getClassLoader().getResource(TEST_INPUT_PATH);
        requireNonNull(input);
        String inputFilePath = Paths.get(input.toURI()).toString();

        String outputFilePath = folder.newFile("tempOutput.xml").getPath();

        XMLConverter converter = new XMLConverter(inputFilePath, outputFilePath, configurationFilePath);
        converter.convert();

        URL expectedOutputURL = getClass().getClassLoader().getResource(expectedOutput);
        requireNonNull(expectedOutputURL);

        List<String> expectedLines = Files.readLines(new File(expectedOutputURL.toURI()), Charset.defaultCharset());
        List<String> convertedLines = Files.readLines(new File(outputFilePath), Charset.defaultCharset());

        for(int i = 0; i<expectedLines.size(); i++) {
            assertEquals(expectedLines.get(i), convertedLines.get(i));
        }
    }
}