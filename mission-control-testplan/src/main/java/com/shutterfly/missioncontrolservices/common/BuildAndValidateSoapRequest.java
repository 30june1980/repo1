/**
 * 
 */
package com.shutterfly.missioncontrolservices.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.shutterfly.missioncontrolservices.config.ConfigLoader;

/**
 * @author Diptman Gupta
 *
 */

public class BuildAndValidateSoapRequest extends ConfigLoader {
	private static final Logger logger = LoggerFactory.getLogger(BuildAndValidateSoapRequest.class);
public static SOAPMessage readXml(String fileName, Map<String, String> updateMap)
			throws ParserConfigurationException, SAXException, IOException, SOAPException {

		File file = new File(fileName);
		DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = builder.newDocumentBuilder();
		Document document = documentBuilder.parse(file);
		document.getDocumentElement().normalize();
		NodeList nodeList = document.getElementsByTagName("*");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node nNode = nodeList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;

				updateMap.forEach((key, value) -> {
					if (eElement.getTagName().equalsIgnoreCase(key)) {
						eElement.setTextContent(value);
					}
				});
			}
		}
		String myEnvelope = convertDocumentToString(document);
		InputStream in = new ByteArrayInputStream(myEnvelope.getBytes());
		return MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage(new MimeHeaders(), in);
	}

	public static SOAPMessage getSOAPResponse(SOAPMessage soapRequest, String strEndpoint) throws SOAPException {
		// Send the SOAP request to the given endpoint and return the
		// corresponding response
		SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection soapConnection = soapConnectionFactory.createConnection();

		return soapConnection.call(soapRequest, strEndpoint);

	}

	public static void validateSoapResponse(SOAPMessage soapResponse, Map<String, String> validateMap)
			throws SOAPException {

		// Get all elements with the requested element tag from the SOAP message
		SOAPBody soapBody = soapResponse.getSOAPBody();

		validateMap.forEach((key, value) -> {
			NodeList elements = soapBody.getElementsByTagName(key);
			if (elements.getLength() != 1) {

				logger.info("Expected exactly one element " + key + "in message, but found "
						+ Integer.toString(elements.getLength()));

				Assert.assertEquals(false, true);
			} else {
				// Validate the element value against the expected value

				String strActual = elements.item(0).getTextContent();
				Assert.assertEquals(strActual, value);
			}
		});
		// Check whether there is exactly one element with the given tag

	}

	private static String convertDocumentToString(Document doc) {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tf.newTransformer();
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));

			return writer.getBuffer().toString();
		} catch (TransformerException e) {
			logger.error("Error stack trace : ",e);
		}

		return null;
	}
}
