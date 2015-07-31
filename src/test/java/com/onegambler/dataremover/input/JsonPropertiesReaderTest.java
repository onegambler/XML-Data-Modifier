package com.onegambler.dataremover.input;

import com.onegambler.dataremover.core.ContentReplaceRule;
import com.onegambler.dataremover.core.Rule;
import com.onegambler.dataremover.xml.manipulation.XPathRule;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonPropertiesReaderTest {

    private final JsonPropertiesReader reader = new JsonPropertiesReader();
    private static final String PROPERTIES_PATH = "properties.json";

    @Test
    public void testRead() throws IOException, URISyntaxException {
        URL resource = getClass().getClassLoader().getResource(PROPERTIES_PATH);
        Objects.requireNonNull(resource);
        Map<String, XPathRule> properties = reader.read(Paths.get(resource.toURI()).toString());
        assertThat(properties).hasSize(4);

        assertThat(properties.get("/world/Europe/United Kingdom/London"))
                .isNotNull()
                .matches(xPathRule -> xPathRule.getRuleSet().size() == 1)
                .matches(xPathRule ->
                {
                    Rule rule = xPathRule.getRuleSet().stream().findFirst().get();
                    return rule instanceof ContentReplaceRule && rule.equals(new ContentReplaceRule("*", "0"));
                }, "Rule match");

        assertThat(properties.get("/wines/Italy/Piemonte/Barolo"))
                .isNotNull()
                .matches(xPathRule -> xPathRule.getRuleSet().size() == 1)
                .matches(xPathRule ->
                {
                    Rule rule = xPathRule.getRuleSet().stream().findFirst().get();
                    return rule instanceof ContentReplaceRule && rule.equals(new ContentReplaceRule("\\d", "rep"));
                }, "Rule match");

        assertThat(properties.get("/company/employees/ceo"))
                .isNotNull()
                .matches(xPathRule -> xPathRule.getRuleSet().size() == 1)
                .matches(xPathRule ->
                {
                    Rule rule = xPathRule.getRuleSet().stream().findFirst().get();
                    return rule instanceof ContentReplaceRule && rule.equals(new ContentReplaceRule("\\s", "newString"));
                }, "Rule match");

        assertThat(properties.get("/books/adventure/abook"))
                .isNotNull()
                .matches(xPathRule -> xPathRule.getRuleSet().size() == 1)
                .matches(xPathRule ->
                {
                    Rule rule = xPathRule.getRuleSet().stream().findFirst().get();
                    return rule instanceof ContentReplaceRule && rule.equals(new ContentReplaceRule("*", "0"));
                }, "Rule match");

        assertThat(properties.get("/non/existing/xpath"))
                .isNull();
    }

}