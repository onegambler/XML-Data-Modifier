package com.xmldatamodifier.xml.manipulation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.xmldatamodifier.core.ContentReplaceRule;
import com.xmldatamodifier.core.ContentRule;
import com.xmldatamodifier.core.Rule;
import com.xmldatamodifier.core.SkipRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlRuleSetTest {

    private static final String XPATH_1 = "/xpath_1";
    private static final String XPATH_2 = "/xpath_2";
    public static final ContentReplaceRule RULE_1 = new ContentReplaceRule("regex_1", "replacement_1");
    public static final ContentReplaceRule RULE_2 = new ContentReplaceRule("regex_2", "replacement_2");
    public static final ContentReplaceRule RULE_3 = new ContentReplaceRule("regex_3", "replacement_3");
    private final XPathRule XPATH_RULE_1 = new XPathRule(XPATH_1,
            ImmutableSet.<Rule>of(RULE_1, RULE_3));

    private final XPathRule XPATH_RULE_2 = new XPathRule(XPATH_2,
            ImmutableSet.of(RULE_2, new SkipRule()));

    private XmlRuleSet ruleSet;

    @Before
    public void setUp() {


        ImmutableMap<String, XPathRule> rulesImmutableMap = ImmutableMap.of(XPATH_1, XPATH_RULE_1, XPATH_2, XPATH_RULE_2);
        ruleSet = new XmlRuleSet(rulesImmutableMap);
    }

    @Test
    public void testGetRulesForXPathSameClass() throws Exception {
        Set<ContentReplaceRule> rulesForXPath = ruleSet.getRulesForXPath(XPATH_1, ContentReplaceRule.class);

        assertThat(rulesForXPath)
                .hasSize(2)
                .containsOnly(RULE_1, RULE_3);

        rulesForXPath = ruleSet.getRulesForXPath(XPATH_2, ContentReplaceRule.class);

        assertThat(rulesForXPath)
                .hasSize(1)
                .containsExactly(RULE_2);

    }

    @Test
    public void testGetRulesForXPathSuperClass() throws Exception {
        Set<ContentRule> rulesForXPath = ruleSet.getRulesForXPath(XPATH_1, ContentRule.class);

        assertThat(rulesForXPath)
                .hasSize(2)
                .containsOnly(RULE_1, RULE_3);

        rulesForXPath = ruleSet.getRulesForXPath(XPATH_2, ContentRule.class);

        assertThat(rulesForXPath)
                .hasSize(1)
                .containsExactly(RULE_2);

    }

    @Test
    public void testGetRulesForXPathNoClass() throws Exception {
        Set<SkipRule> rulesForXPath = ruleSet.getRulesForXPath(XPATH_1, SkipRule.class);

        assertThat(rulesForXPath)
                .isEmpty();
    }

    @Test(expected = NullPointerException.class)
    public void whenNoXpathIsPassedThenExceptionIsThrown() {
        ruleSet.getRulesForXPath(null, ContentRule.class);
    }

    @Test(expected = NullPointerException.class)
    public void whenNoClassIsPassedThenExceptionIsThrown() {
        ruleSet.getRulesForXPath("", null);
    }
}