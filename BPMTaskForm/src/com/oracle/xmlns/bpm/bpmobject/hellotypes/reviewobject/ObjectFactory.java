
package com.oracle.xmlns.bpm.bpmobject.hellotypes.reviewobject;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com.oracle.xmlns.bpm.bpmobject.hellotypes.reviewobject package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ReviewObject_QNAME =
        new QName("http://xmlns.oracle.com/bpm/bpmobject/HelloTypes/ReviewObject",
                  "ReviewObject");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.oracle.xmlns.bpm.bpmobject.hellotypes.reviewobject
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ReviewObjectType }
     *
     */
    public ReviewObjectType createReviewObjectType() {
        return new ReviewObjectType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReviewObjectType }{@code >}}
     *
     */
    @XmlElementDecl(namespace =
                    "http://xmlns.oracle.com/bpm/bpmobject/HelloTypes/ReviewObject",
                    name = "ReviewObject")
    public JAXBElement<ReviewObjectType> createReviewObject(ReviewObjectType value) {
        return new JAXBElement<ReviewObjectType>(_ReviewObject_QNAME,
                                                 ReviewObjectType.class, null,
                                                 value);
    }

}
