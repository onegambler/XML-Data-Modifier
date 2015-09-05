package com.xmldatamodifier.xml.manipulation;


import com.xmldatamodifier.core.Rule;

import java.util.Set;

public class XPathRule {
    private final String xPath;
    private final Set<Rule> ruleSet;

    public XPathRule(String xPath, Set<Rule> ruleSet) {
        this.xPath = xPath;
        this.ruleSet = ruleSet;
    }

    public Set<Rule> getRuleSet() {
        return ruleSet;
    }

    public String getXPath() {
        return xPath;
    }

    @Override
    public String toString() {
        return "XPathRule{" + "xPath='" + xPath + '\'' + ", ruleSet=" + ruleSet + '}';
    }
}
