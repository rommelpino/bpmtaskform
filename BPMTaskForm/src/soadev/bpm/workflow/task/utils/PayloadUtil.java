package soadev.bpm.workflow.task.utils;

import commonj.sdo.DataObject;
import commonj.sdo.Type;
import commonj.sdo.helper.DataFactory;
import commonj.sdo.helper.XMLDocument;
import commonj.sdo.helper.XMLHelper;
import commonj.sdo.helper.XSDHelper;

import java.io.IOException;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.dom.DOMResult;

import javax.xml.transform.dom.DOMSource;

import oracle.adf.share.logging.ADFLogger;

import oracle.bpel.services.common.util.XMLUtil;

import oracle.jbo.common.sdo.SDOTypeHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PayloadUtil {
    private static final String TASK_NS =
        "http://xmlns.oracle.com/bpel/workflow/task";
    private static ADFLogger _logger =
        ADFLogger.createADFLogger(PayloadUtil.class);
    
    public static Element getPayloadDataElementByName(Element payload, String name) {
        if (payload != null) {
                NodeList nodes = payload.getChildNodes();
                for(int i= 0; i < nodes.getLength(); i++){
                    Node node = nodes.item(i);
                    String nodeName = node.getLocalName();
                    if(nodeName == null){
                        nodeName = node.getNodeName();
                    }
                    if(node instanceof Element && nodeName.equals(name)){
                        return (Element)node;
                    }
                }
        }
        return null;
    }
    
    public static List<Element> getPayloadElements(Element payload){
        List<Element> payloadElements = new ArrayList<Element>();
        if (payload != null) {
                NodeList nodes = payload.getChildNodes();
                for(int i= 0; i < nodes.getLength(); i++){
                    Node node = nodes.item(i);
                    if(node instanceof Element){
                        payloadElements.add((Element)node);
                    }
                }
        }
        return payloadElements;
    }

//    public static Element createElement(String xmlString) throws IOException, SAXException,
//                                                  ParserConfigurationException {
//        if (xmlString == null){
//            throw new IllegalArgumentException();
//        }
//        DocumentBuilder docBuilder = getDocumentBuilder();
//        Element element =
//            docBuilder.parse(new InputSource(new StringReader(xmlString))).getDocumentElement();
//        return element;
//    }

    public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilder docBuilder =
            DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return docBuilder;
    }
    
    public static DataObject convertElementToSDO(XMLHelper xmlHelper, Element element)throws Exception {
        XMLDocument xmlDocument = 
                      xmlHelper.load(new DOMSource(element), null, null);
        return xmlDocument.getRootObject();
    }
    
    public static Element replacePayloadElementData(Element payloadElement,
                                              Node oldNode,
                                              Node newNode) throws Exception {
        Document doc;
        if (payloadElement == null) {
            doc = XMLUtil.createDocument();
            payloadElement = doc.createElementNS(TASK_NS, "payload");      
        }
        doc = payloadElement.getOwnerDocument();
        newNode = doc.importNode(newNode, true);
        if (oldNode != null) {
            payloadElement.replaceChild(newNode, oldNode);
        } else {
            payloadElement.appendChild(newNode);
        }
        return payloadElement;
    }
    
    public static Element replaceOrAppendPayloadElementData(Element payloadElement, Node newNode) throws Exception {
        Element oldNode = getPayloadDataElementByName(payloadElement, newNode.getLocalName());
        return replacePayloadElementData(payloadElement, oldNode, newNode);
    }
    
    public static String convertSDOToXMLString(DataObject data, String name) {
        if (data == null || name == null){
            throw new IllegalArgumentException();
        }
        return XMLHelper.INSTANCE.save(data, data.getType().getURI(), name );
    }
    
    public static Element convertSDOToElement(XMLHelper xmlHelper, DataObject sdo, String rootQName) throws IOException,
                                                                  SAXException,
                                                                  ParserConfigurationException {
        if (xmlHelper == null || sdo == null || rootQName == null){
            throw new IllegalArgumentException();
        }
        Document document = getDocumentBuilder().newDocument();
        XMLDocument xmlDocument = xmlHelper.createDocument(sdo, sdo.getType().getURI(), rootQName);
        xmlHelper.save(xmlDocument, new DOMResult(document), null);
        return document.getDocumentElement();
    }
    
    public static boolean isSimpleType(Class<?> clazz) {
            return clazz.equals(Boolean.class) || 
                    clazz.equals(Integer.class) ||
                    clazz.equals(Character.class) ||
                    clazz.equals(Byte.class) ||
                    clazz.equals(Short.class) ||
                    clazz.equals(Double.class) ||
                    clazz.equals(Long.class) ||
                    clazz.equals(Float.class);
    }
    
    public static DataObject wrapSimpleTypes(Object value) {
        if(!isSimpleType(value.getClass())){
            throw new RuntimeException("Trying to wrap a complex object.");
        }
        Type type =
            SDOTypeHandler.getSDOType(value.getClass().getName());
        DataObject wrapper = DataFactory.INSTANCE.create(type);
        wrapper.set("value", value);
        return wrapper;
    }
    public static List define(XSDHelper xsdHelper, ClassLoader loader, String path, String location){
        return xsdHelper.define(loader.getResourceAsStream(path), location);
    }
}
