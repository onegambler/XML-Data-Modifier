package com.xmldatamodifier.xml.parsing;

import com.google.common.collect.ImmutableSet;
import com.xmldatamodifier.core.ContentReplaceRule;
import com.xmldatamodifier.core.ContentRule;
import com.xmldatamodifier.core.SkipRule;
import com.xmldatamodifier.xml.manipulation.XmlRuleSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Writer;
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
        handler = new SAXTransformationHandler(writer, DOCUMENT_START, ruleSet, xPathHandler);
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
        when(xPathHandler.getCurrentXPath()).thenReturn("/");

        handler.startElement(null, null, ELEMENT_NAME, mockAttribute);

        verify(writer, times(1)).write(SAXTransformationHandler.START_TAG);
        verify(writer, times(1)).write(ELEMENT_NAME);
        verify(writer, times(1)).write(SAXTransformationHandler.END_TAG);
        verify(ruleSet, times(1)).getRulesForXPath("/", SkipRule.class);

        verifyNoMoreInteractions(writer, ruleSet);
    }

    @Test
    public void testStartElementWAttributes() throws Exception {
        Attributes mockAttribute = mock(Attributes.class);
        when(mockAttribute.getLength()).thenReturn(1);
        final String attributeName = "attributeName";
        final String attributeValue = "attributeValue";
        when(mockAttribute.getLocalName(0)).thenReturn(attributeName);
        when(mockAttribute.getValue(0)).thenReturn(attributeValue);
        when(xPathHandler.getCurrentXPath()).thenReturn("/");

        handler.startElement(null, null, ELEMENT_NAME, mockAttribute);

        verify(writer, times(1)).write(SAXTransformationHandler.START_TAG);
        verify(writer, times(1)).write(ELEMENT_NAME);

        verify(writer, times(1)).write(" ");
        verify(writer, times(1)).write(attributeName);
        verify(writer, times(1)).write("=\"");
        verify(writer, times(1)).write(attributeValue);
        verify(writer, times(1)).write("\"");
        verify(writer, times(1)).write(SAXTransformationHandler.END_TAG);
        verify(ruleSet, times(1)).getRulesForXPath("/", SkipRule.class);
        verify(xPathHandler, times(1)).enterNode(ELEMENT_NAME);
        verify(xPathHandler, times(1)).getCurrentXPath();

        verifyNoMoreInteractions(writer, ruleSet);
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
        when(ruleSet.getRulesForXPath("/", ContentReplaceRule.class)).thenReturn(ImmutableSet.<ContentReplaceRule>of());

        handler.characters(CONTENT_NODE.toCharArray(), 0, CONTENT_NODE.length());

        verify(writer, times(1)).write(any(String.class));
        verify(xPathHandler, times(1)).getCurrentXPath();
        verify(ruleSet, times(1)).getRulesForXPath(anyString(), any(Class.class));
        verifyNoMoreInteractions(writer, ruleSet, xPathHandler);
    }

    @Test
    public void testCharactersWRules() throws Exception {
        Set<ContentRule> rules = ImmutableSet.<ContentRule>of(new ContentReplaceRule("*", "0"));
        when(xPathHandler.getCurrentXPath()).thenReturn("/");
        when(ruleSet.getRulesForXPath("/", ContentRule.class)).thenReturn(rules);

        handler.characters(CONTENT_NODE.toCharArray(), 0, CONTENT_NODE.length());

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

        verify(writer, times(1)).write(stringArgumentCaptor.capture());
        verify(xPathHandler, times(1)).getCurrentXPath();
        verify(ruleSet, times(1)).getRulesForXPath(anyString(), any(Class.class));
        verifyNoMoreInteractions(writer, ruleSet, xPathHandler);
        assertThat(stringArgumentCaptor.getValue()).isEqualTo("0");
    }

    @Test
    public void testIgnorableWhitespace() throws Exception {
        Set<ContentRule> rules = ImmutableSet.<ContentRule>of(new ContentReplaceRule("*", "0"));
        when(xPathHandler.getCurrentXPath()).thenReturn("/");
        when(ruleSet.getRulesForXPath("/", ContentRule.class)).thenReturn(rules);

        handler.ignorableWhitespace(CONTENT_NODE.toCharArray(), 0, CONTENT_NODE.length());

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

        verify(writer, times(1)).write(stringArgumentCaptor.capture());
        verify(xPathHandler, times(1)).getCurrentXPath();
        verify(ruleSet, times(1)).getRulesForXPath(anyString(), any(Class.class));
        verifyNoMoreInteractions(writer, ruleSet, xPathHandler);

        assertThat(stringArgumentCaptor.getValue()).isEqualTo("0");
    }

    @Test(expected = SAXException.class)
    public void whenThereIsMoreThanOneSkipRuleThenThrowException() throws SAXException {
        Attributes mockAttribute = mock(Attributes.class);
        when(mockAttribute.getLength()).thenReturn(0);
        when(xPathHandler.getCurrentXPath()).thenReturn("/simple/path");
        when(ruleSet.getRulesForXPath("/simple/path", SkipRule.class)).thenReturn(ImmutableSet.of(new SkipRule(), new SkipRule()));
        handler.startElement(null, null, ELEMENT_NAME, mockAttribute);
    }

    @Test
    public void testSkip() throws SAXException, IOException {
        Attributes mockAttribute = mock(Attributes.class);
        when(mockAttribute.getLength()).thenReturn(0);
        when(xPathHandler.getCurrentXPath()).thenReturn("/path/to/skip");
        when(ruleSet.getRulesForXPath("/path/to/skip", SkipRule.class)).thenReturn(ImmutableSet.of(new SkipRule()));

        when(xPathHandler.getCurrentXPath()).thenReturn("/path/to/skip");
        handler.startElement(null, null, ELEMENT_NAME, mockAttribute);
        verifyZeroInteractions(writer);

        when(xPathHandler.getCurrentXPath()).thenReturn("/path/to/skip/this/too");
        handler.startElement(null, null, ELEMENT_NAME, mockAttribute);
        verifyZeroInteractions(writer);

        handler.characters(null, 0, 0);
        verifyZeroInteractions(writer);

        handler.writeAttribute(null, null);
        verifyZeroInteractions(writer);

        handler.endElement(null, null, null);
        verifyZeroInteractions(writer);
        verify(xPathHandler, times(1)).exitLastEnteredNode();

        when(xPathHandler.getCurrentXPath()).thenReturn("/path/to/skip");
        handler.endElement(null, null, ELEMENT_NAME);
        verify(writer, times(1)).write(format("</%s>", ELEMENT_NAME));
        verify(xPathHandler, times(2)).exitLastEnteredNode();

        when(xPathHandler.getCurrentXPath()).thenReturn("/another/path");
        handler.startElement(null, null, ELEMENT_NAME + "_START_1", mockAttribute);
        verify(writer, times(1)).write(ELEMENT_NAME + "_START_1");

        char[] characters = (ELEMENT_NAME + "_CHARS").toCharArray();
        handler.characters(characters, 0, characters.length);
        verify(writer, times(1)).write(new String(characters).intern());

        handler.writeAttribute("AttributeName", "AttributeValue");
        verify(writer, times(1)).write("AttributeName");
        verify(writer, times(1)).write("AttributeValue");
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

        testEscapedCharacter('g', "g");
    }

    private void testEscapedCharacter(char characterToEscape, String expectedOutput) {
        char[] input = String.format("test %s", characterToEscape).toCharArray();
        String escapedString = handler.escape(input, 0, input.length);
        assertThat(escapedString).isEqualTo("test " + expectedOutput);
    }
}