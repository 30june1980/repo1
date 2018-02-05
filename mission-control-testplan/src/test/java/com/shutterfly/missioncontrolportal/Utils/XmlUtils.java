package com.shutterfly.missioncontrolportal.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class XmlUtils {

    private XmlUtils() {
    }

    public static Map<String, String> readXml(@Nonnull String filePath) {
        Map<String, String> map = new LinkedHashMap<>();
        try {
            final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filePath);
            final XPathExpression xpath = XPathFactory.newInstance().newXPath().compile("//*[count(./*) = 0]");
            final NodeList nodeList = (NodeList) xpath.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                final Element el = (Element) nodeList.item(i);
                String key = el.getNodeName().trim();
                String value = el.getFirstChild() == null ? "" : el.getFirstChild().getNodeValue().trim();
                map.put(key, value);
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to parse XML file", exception);
        }
        return map;
    }

    public static String readXmlElement(@Nonnull String filePath, String elementName) {
        SAXParserFactory parserFactor = SAXParserFactory.newInstance();
        SAXHandler handler = new SAXHandler(elementName);
        SAXParser parser;
        try {
            parser = parserFactor.newSAXParser();
            parser.parse(new File(filePath),
                    handler);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new IllegalArgumentException("Failed to parse the XML file");
        }
        return handler.result;
    }

    private static class SAXHandler extends DefaultHandler {
        private StringBuilder buffer;
        private String result;
        private String elementName;

        SAXHandler(String elementName) {
            this.elementName = elementName;
        }

        @Override
        public void startElement(String uri, String localName,
                                 String qName, Attributes attributes) {
            if (elementName.equals(qName)) {
                buffer = new StringBuilder();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (buffer != null) {
                StringBuilder builder = new StringBuilder();
                for (int i = start; i < start + length; i++) {
                    builder.append(ch[i]);
                }
                String word = builder.toString().trim();
                if (!word.equals("")) {
                    buffer.append(word).append(", ");
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (elementName.equals(qName)) {
                result = buffer.toString().trim();
                if (!result.isEmpty() && result.charAt(result.length() - 1) == ',') {
                    result = result.substring(0, result.length() - 1);
                }
                buffer = new StringBuilder();
            }
        }
    }

}