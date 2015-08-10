package com.xmldatamodifier.xml.manipulation;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.xmldatamodifier.core.Rule;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;


public class XmlRuleSet {

    private final Map<String, XPathRule> xPathRulesMap;

    public XmlRuleSet(Map<String, XPathRule> xPathRulesMap) {
        this.xPathRulesMap = xPathRulesMap;
    }

    public <T extends Rule> Set<T> getRulesForXPath(String xPath, Class<T> ruleClassType) {
        requireNonNull(xPath, "XPath cannot be null");
        requireNonNull(ruleClassType, "Rule class cannot be null");

        XPathRule xPathRule = xPathRulesMap.get(xPath);
        if (xPathRule != null) {
            return FluentIterable
                    .from(xPathRule.getRuleSet())
                    .filter(ruleClassType).toSet();
        } else {
            return ImmutableSet.of();
        }
    }
}
