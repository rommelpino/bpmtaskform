package soadev.bpm.view.helper;

import com.oracle.xmlns.bpm.bpmobject.hellotypes.helloobject.HelloObjectType;
import com.oracle.xmlns.bpm.bpmobject.hellotypes.reviewobject.ReviewObjectType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HelloWorldPayloadHandler extends AbstractPayloadHandler {
    private JAXBContext jaxbContext;

    public HelloWorldPayloadHandler() {
        try {
            jaxbContext =
                    JAXBContext.newInstance(HelloObjectType.class, ReviewObjectType.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public Object unmarshal(Element element) throws Exception {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object obj = unmarshaller.unmarshal(element);
        return obj;
    }

    public Element marshal(Object object,
                                          String elementName) throws Exception {
        
        
        if(elementName == null){
            throw new IllegalArgumentException("elementName param is null");
        }
        DocumentBuilder docBuilder =
            DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = docBuilder.newDocument();
        if (object == null){
            return document.createElement(elementName);
        }
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(object, document);
        Element element = document.getDocumentElement();
        return element;
    }
}
