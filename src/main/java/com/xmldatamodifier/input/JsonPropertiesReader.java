package com.xmldatamodifier.input;

import com.google.gson.*;
import com.xmldatamodifier.core.ContentReplaceRule;
import com.xmldatamodifier.core.Rule;
import com.xmldatamodifier.core.SkipRule;
import com.xmldatamodifier.xml.manipulation.XPathRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class JsonPropertiesReader implements FilePropertiesReader {

    private final Logger log = LoggerFactory.getLogger(JsonPropertiesReader.class);

    private Gson gsonParser = new GsonBuilder().create();

    static final Rule SKIP_RULE = new SkipRule();

    @Override
    public Map<String, XPathRule> read(String fileName) throws IOException {
        log.info("Loading configuration file...");
        Map<String, XPathRule> xPathRules = new HashMap<>();
        try (FileReader reader = new FileReader(fileName)) {
            JsonObject jsonObject = gsonParser.fromJson(reader, JsonObject.class);
            JsonArray ruleSetJsonArray = jsonObject.get("rule_set").getAsJsonArray();
            for (JsonElement element : ruleSetJsonArray) {
                if(element instanceof JsonNull) {
                    throw new JsonParseException("Configuration file has errors, found a null rule!");
                }
                XPathRule rule = getXPathRulesFromJsonObject(element.getAsJsonObject());
                xPathRules.put(rule.getXPath(), rule);
            }
        }
        log.info("Loaded {} xPath rule sets", xPathRules.size());
        return xPathRules;
    }

    private XPathRule getXPathRulesFromJsonObject(JsonObject rulesJsonObject) {
        requireNonNull(rulesJsonObject, "Rules Node cannot be null");
        String xPath = rulesJsonObject.get("xpath").getAsString();
        requireNonNull(xPath, "XPath cannot be null");
        Set<Rule> ruleSet = new HashSet<>();
        for (JsonElement ruleJsonElement : rulesJsonObject.get("rules").getAsJsonArray()) {
            JsonObject rule = ruleJsonElement.getAsJsonObject();
            requireNonNull(rule, "Rule cannot be null");
            JsonElement ruleTypeJsonElement = rule.get("type");
            requireNonNull(ruleTypeJsonElement, "Rule type cannot be null");
            Rule.RuleType type = Rule.RuleType.valueOf(ruleTypeJsonElement.getAsString());

            switch (type) {
                case REPLACE:
                    String regex = rule.get("match").getAsString();
                    String replacement = rule.get("replacement").getAsString();
                    ruleSet.add(new ContentReplaceRule(regex, replacement));
                    break;
                case SKIP:
                    ruleSet.add(SKIP_RULE);
                    break;
                default:
                    throw new IllegalArgumentException("Rule type is not valid: " + type);
            }
        }
        return new XPathRule(xPath, ruleSet);
    }
}
