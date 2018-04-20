package com.niksoftware.snapseed.util;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;

public final class XmlHelper {
    private XmlHelper() {
    }

    public static Document parseXml(String xmlContent) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource input = new InputSource();
            input.setCharacterStream(new StringReader(xmlContent));
            return builder.parse(input);
        } catch (Exception e) {
            return null;
        }
    }

    public static String generateXmlString(Document xmlDom) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StreamResult xmlStream = new StreamResult(new StringWriter());
            transformer.transform(new DOMSource(xmlDom), xmlStream);
            return xmlStream.getWriter().toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static Node lookupDocumentNode(Node rootNode, String nodeName) {
        if (rootNode == null || rootNode.getNodeName().equalsIgnoreCase(nodeName)) {
            return rootNode;
        }
        Node matchNode = null;
        Node currentNode = rootNode.getFirstChild();
        while (matchNode == null && currentNode != null) {
            matchNode = lookupDocumentNode(currentNode, nodeName);
            currentNode = currentNode.getNextSibling();
        }
        return matchNode;
    }

    public static String findAttributeValueForKey(String key, XmlPullParser parser) {
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (key.equalsIgnoreCase(parser.getAttributeName(i))) {
                return parser.getAttributeValue(i);
            }
        }
        return null;
    }
}
