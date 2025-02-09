package com.charlyghislain.belcotax;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class XsdReader {

    public static final String XSD_BELCOTAX_2024_XSD = "/xsd/Belcotax-2024.xsd";
    public static final Map<String, Map<String, String>> XSD_DOCUMENTATION = parseXsdDocumentation();

    private static Map<String, Map<String, String>> parseXsdDocumentation() {
        try {
            Map<String, Map<String, String>> tagDocumentations = new HashMap<>();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(XsdReader.class.getResourceAsStream(XSD_BELCOTAX_2024_XSD));

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("xs:simpleType");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String elementName = eElement.getAttribute("name");

                    String[] docContent = Optional.ofNullable(eElement.getElementsByTagName("xs:annotation"))
                            .map(node -> node.item(0))
                            .map(node -> (Element) node)
                            .flatMap(e -> Optional.ofNullable(e.getElementsByTagName("xs:documentation")))
                            .map(node -> node.item(0).getTextContent())
                            .map(content -> content.split("\n"))
                            .orElse(new String[]{});

                    if (docContent.length == 0) {
                        continue;
                    }

                    Map<String, String> localizedContent = new HashMap<>();
                    if (docContent.length > 4) {
                        localizedContent.put("", docContent[1].strip());
                        localizedContent.put("fr", docContent[2].strip());
                        localizedContent.put("nl", docContent[3].strip());
                        localizedContent.put("de", docContent[4].strip());
                    }

                    if (localizedContent.isEmpty()) {
                        continue;
                    }
                    tagDocumentations.put(elementName, localizedContent);
                }
            }
            return tagDocumentations;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
