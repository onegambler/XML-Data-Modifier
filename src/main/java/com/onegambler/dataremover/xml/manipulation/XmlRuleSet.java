package com.onegambler.dataremover.xml.manipulation;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.onegambler.dataremover.core.Rule;

import java.util.Map;
import java.util.Optional;
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

        Optional<Set<Rule>> xPathRules = Optional.ofNullable(xPathRulesMap.get(xPath))
                .map(XPathRule::getRuleSet);
        return FluentIterable
                .from(xPathRules.orElse(ImmutableSet.of()))
                .filter(ruleClassType).toSet();
    }
}
