package com.xmldatamodifier.input;

import com.xmldatamodifier.core.ContentReplaceRule;
import com.xmldatamodifier.core.Rule;
import com.xmldatamodifier.xml.manipulation.XPathRule;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonPropertiesReaderTest {

    private final JsonPropertiesReader reader = new JsonPropertiesReader();
    private static final String PROPERTIES_PATH = "test/properties.json";

    @Test
    public void testRead() throws IOException, URISyntaxException {
        URL resource = getClass().getClassLoader().getResource(PROPERTIES_PATH);
        Objects.requireNonNull(resource);
        Map<String, XPathRule> properties = reader.read(Paths.get(resource.toURI()).toString());
        assertThat(properties).hasSize(6);

        testXPathRuleContent(
                properties.get("/world/Europe/United Kingdom/London"),
                1,
                Collections.<Rule>singleton(new ContentReplaceRule("*", "0")));

        testXPathRuleContent(
                properties.get("/wines/Italy/Piemonte/Barolo"),
                1,
                Collections.<Rule>singleton(new ContentReplaceRule("\\d", "rep")));

        testXPathRuleContent(
                properties.get("/company/employees/ceo"),
                1,
                Collections.<Rule>singleton(new ContentReplaceRule("\\s", "newString")));

        testXPathRuleContent(
                properties.get("/books/adventure/abook"),
                1,
                Collections.<Rule>singleton(new ContentReplaceRule("*", "0")));

        testXPathRuleContent(
                properties.get("/PATH/TO/SKIP"),
                1,
                Collections.singleton(JsonPropertiesReader.SKIP_RULE));

        assertThat(properties.get("/non/existing/xpath"))
                .isNull();
    }

    private void testXPathRuleContent(XPathRule xPathRule, int size, Set<Rule> expectedRule) {
        assertThat(xPathRule).isNotNull();
        assertThat(xPathRule.getRuleSet()).hasSize(size);
        assertThat(xPathRule.getRuleSet())
                .hasSameElementsAs(expectedRule);
    }

}