package com.xmldatamodifier.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContentReplaceRuleTest {

    @Test
    public void whenRegexIsStarThenConvertEverything() throws Exception {
        ContentReplaceRule replaceRule = new ContentReplaceRule("*", "0");
        assertThat(replaceRule.elaborate("ciao")).isEqualTo("0");
        assertThat(replaceRule.elaborate("asdrewnoe")).isEqualTo("0");
        assertThat(replaceRule.elaborate("")).isEqualTo("0");
        assertThat(replaceRule.elaborate(null)).isEqualTo("0");
        assertThat(replaceRule.elaborate("      ")).isEqualTo("0");
        assertThat(replaceRule.elaborate("jdoi joais oI OISH DO837298 2109 20921 h=)/)%%/(0")).isEqualTo("0");
    }

    @Test
    public void whenRegexIsPassedThenConvertOnlyTheMatchingRegex() throws Exception {
        ContentReplaceRule replaceRule = new ContentReplaceRule("\\d", "_");
        assertThat(replaceRule.elaborate("ciao 55")).isEqualTo("ciao __");
        assertThat(replaceRule.elaborate("asdrewnoe")).isEqualTo("asdrewnoe");
        assertThat(replaceRule.elaborate("")).isEqualTo("");
        assertThat(replaceRule.elaborate(null)).isEqualTo("");
        assertThat(replaceRule.elaborate("      ")).isEqualTo("      ");
        assertThat(replaceRule.elaborate("jdoi joais oI OISH DO837298 2109 20921 pppp /((=)")).isEqualTo("jdoi joais oI OISH DO______ ____ _____ pppp /((=)");
    }

    @Test(expected = NullPointerException.class)
    public void whenRegexIsNotPassedThenThrowException() throws Exception {
        new ContentReplaceRule(null, "_");
    }

    @Test(expected = NullPointerException.class)
    public void whenReplacementIsNotPassedThenThrowException() throws Exception {
        new ContentReplaceRule("*", null);
    }
}