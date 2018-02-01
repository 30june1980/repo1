package com.shutterfly.missioncontrolportal.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.LinkedHashMap;
import java.util.Map;

public class XmlUtils {

    private XmlUtils() {
    }

    public static Map<String, String> readXml(String fileName) {
        Map<String, String> map = new LinkedHashMap<>();
        try {
            final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fileName);
            final XPathExpression xpath = XPathFactory.newInstance().newXPath().compile("//*[count(./*) = 0]");
            final NodeList nodeList = (NodeList) xpath.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                final Element el = (Element) nodeList.item(i);
                String key = el.getNodeName().replace("sch:", "").trim();
                String value = el.getFirstChild() == null ? "" : el.getFirstChild().getNodeValue().trim();
                map.put(key, value);
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to parse XML file", exception);
        }
        return map;
    }
}