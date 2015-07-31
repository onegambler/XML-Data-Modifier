package com.onegambler.dataremover.xml.parsing;

import com.google.common.collect.ImmutableSet;
import com.onegambler.dataremover.core.ContentReplaceRule;
import com.onegambler.dataremover.core.ContentRule;
import com.onegambler.dataremover.xml.manipulation.XmlRuleSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.Attributes;

import java.io.Writer;
import java.util.Collections;
import java.util.Set;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SAXTransformationHandlerTest {

    public static final String DOCUMENT_START = "documentStart";
    public static final String ELEMENT_NAME = "nodeName";
    public static final String CONTENT_NODE = "contentNode";

    @Mock
    private XmlRuleSet ruleSet;

    @Mock
    private Writer writer;

    @Mock
    XPathHandler xPathHandler;

    private SAXTransformationHandler handler;

    @Before
    public void setUp() {
        handler = new SAXTransformationHandler(writer, DOCUMENT_START, ruleSet, xPathHandler, true);
    }

    @Test
    public void testStartDocument() throws Exception {
        handler.startDocument();

        verify(writer, times(1)).write(DOCUMENT_START + "\n");
        verifyNoMoreInteractions(writer, ruleSet, xPathHandler);
    }

    @Test
    public void testEndDocument() throws Exception {
        handler.endDocument();

        verifyZeroInteractions(writer, ruleSet, xPathHandler);
    }

    @Test
    public void testStartElementNoAttributes() throws Exception {
        Attributes mockAttribute = mock(Attributes.class);
        when(mockAttribute.getLength()).thenReturn(0);

        handler.startElement(null, null, ELEMENT_NAME, mockAttribute);

        verify(writer, times(1)).write(SAXTransformationHandler.START_TAG);
        verify(writer, times(1)).write(ELEMENT_NAME);
        verify(writer, times(1)).write(SAXTransformationHandler.END_TAG);
        verify(xPathHandler).enterNode(ELEMENT_NAME);
        verifyNoMoreInteractions(writer, ruleSet, xPathHandler);
    }

    @Test
    public void testStartElementWAttributes() throws Exception {
        Attributes mockAttribute = mock(Attributes.class);
        when(mockAttribute.getLength()).thenReturn(1);
        when(mockAttribute.getLocalName(0)).thenReturn("attributeName");
        when(mockAttribute.getValue(0)).thenReturn("attributeValue");

        handler.startElement(null, null, ELEMENT_NAME, mockAttribute);

        verify(writer, times(1)).write(SAXTransformationHandler.START_TAG);
        verify(writer, times(1)).write(ELEMENT_NAME);

        verify(writer, times(1)).write(" ");
        verify(writer, times(1)).write("attributeName");
        verify(writer, times(1)).write("=\"");
        verify(writer, times(1)).write(any(char[].class), anyInt(), anyInt());
        verify(writer, times(1)).write("\"");
        verify(writer, times(1)).write(SAXTransformationHandler.END_TAG);

        verify(xPathHandler, times(1)).enterNode(ELEMENT_NAME);
        verifyNoMoreInteractions(writer, ruleSet, xPathHandler);
    }

    @Test
    public void testEndElement() throws Exception {
        handler.endElement(null, null, ELEMENT_NAME);
        verify(writer, times(1)).write(format("</%s>", ELEMENT_NAME));
        verify(xPathHandler, times(1)).exitLastEnteredNode();
    }

    @Test
    public void testCharactersNoRules() throws Exception {

        when(xPathHandler.getCurrentXPath()).thenReturn("/");
        when(ruleSet.getRulesForXPath("/", ContentReplaceRule.class)).thenReturn(Collections.emptySet());

        handler.characters(CONTENT_NODE.toCharArray(), 0, CONTENT_NODE.length());

        verify(writer, times(1)).write(any(char[].class), anyInt(), anyInt());
        verify(xPathHandler, times(1)).getCurrentXPath();
        verify(ruleSet, times(1)).getRulesForXPath(anyString(), any());
        verifyNoMoreInteractions(writer, ruleSet, xPathHandler);
    }

    @Test
    public void testCharactersWRules() throws Exception {
        Set<ContentRule> rules = ImmutableSet.of(new ContentReplaceRule("*", "0"));
        when(xPathHandler.getCurrentXPath()).thenReturn("/");
        when(ruleSet.getRulesForXPath("/", ContentRule.class)).thenReturn(rules);

        handler.characters(CONTENT_NODE.toCharArray(), 0, CONTENT_NODE.length());

        ArgumentCaptor<char[]> contentCaptor = ArgumentCaptor.forClass(char[].class);
        ArgumentCaptor<Integer> startCaptor = ArgumentCaptor.forClass(int.class);
        ArgumentCaptor<Integer> endCaptor = ArgumentCaptor.forClass(int.class);

        verify(writer, times(1)).write(contentCaptor.capture(), startCaptor.capture(), endCaptor.capture());
        verify(xPathHandler, times(1)).getCurrentXPath();
        verify(ruleSet, times(1)).getRulesForXPath(anyString(), any());
        verifyNoMoreInteractions(writer, ruleSet, xPathHandler);
        String capturedString = new String(contentCaptor.getValue(), startCaptor.getValue(), endCaptor.getValue());

        assertThat(capturedString).isEqualTo("0");
    }

    @Test
    public void testIgnorableWhitespace() throws Exception {
        Set<ContentRule> rules = ImmutableSet.of(new ContentReplaceRule("*", "0"));
        when(xPathHandler.getCurrentXPath()).thenReturn("/");
        when(ruleSet.getRulesForXPath("/", ContentRule.class)).thenReturn(rules);

        handler.ignorableWhitespace(CONTENT_NODE.toCharArray(), 0, CONTENT_NODE.length());

        ArgumentCaptor<char[]> contentCaptor = ArgumentCaptor.forClass(char[].class);
        ArgumentCaptor<Integer> startCaptor = ArgumentCaptor.forClass(int.class);
        ArgumentCaptor<Integer> endCaptor = ArgumentCaptor.forClass(int.class);

        verify(writer, times(1)).write(contentCaptor.capture(), startCaptor.capture(), endCaptor.capture());
        verify(xPathHandler, times(1)).getCurrentXPath();
        verify(ruleSet, times(1)).getRulesForXPath(anyString(), any());
        verifyNoMoreInteractions(writer, ruleSet, xPathHandler);
        String capturedString = new String(contentCaptor.getValue(), startCaptor.getValue(), endCaptor.getValue());

        assertThat(capturedString).isEqualTo("0");
    }

    @Test
    public void testProcessingInstruction() throws Exception {
        handler.processingInstruction("target", "data");
        verify(writer, times(1)).write("<?target data?>");
        verifyNoMoreInteractions(writer, ruleSet, xPathHandler);
    }

    @Test
    public void testEscape() {
        testEscapedCharacter('&', "&amp;");
        testEscapedCharacter('<', "&lt;");
        testEscapedCharacter('>', "&gt;");
        testEscapedCharacter('\"', "&#34;");
        testEscapedCharacter('\'', "&#39;");

        testEscapedCharacter('2', "2");
        testEscapedCharacter('1', "1");
        testEscapedCharacter('9', "9");

        testEscapedCharacter((char) 128, "&#128;");
        testEscapedCharacter((char) 200, "&#200;");
    }

    private void testEscapedCharacter(char characterToEscape, String expectedOutput) {
        char[] input = String.format("test %s", characterToEscape).toCharArray();
        char[] output = new char[input.length * 8];
        int escape = handler.escape(input, 0, input.length, output);
        String outputString = new String(output, 0, escape);
        assertThat(outputString).isEqualTo("test " + expectedOutput);
    }
}