package com.onegambler.dataremover.xml.parsing;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class XPathHandlerTest {

    private XPathHandler xPathHandler = new XPathHandler();

    @Test
    public void testHandler() throws Exception {
        xPathHandler.enterNode("rootNode");
        assertThat(xPathHandler.getCurrentXPath())
                .isEqualTo("/rootNode");
        xPathHandler.enterNode("firstChild");
        xPathHandler.enterNode("secondChild");

        assertThat(xPathHandler.getCurrentXPath())
                .isEqualTo("/rootNode/firstChild/secondChild");

        xPathHandler.exitLastEnteredNode();

        assertThat(xPathHandler.getCurrentXPath())
                .isEqualTo("/rootNode/firstChild");

        xPathHandler.exitLastEnteredNode();

        assertThat(xPathHandler.getCurrentXPath())
                .isEqualTo("/rootNode");
    }

    @Test(expected = IllegalStateException.class)
    public void whenXPathIsEmptyAndTryToExitNodeThenThrowException() {
        xPathHandler.exitLastEnteredNode();
    }
}