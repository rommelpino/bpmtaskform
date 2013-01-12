
package com.oracle.xmlns.bpm.bpmobject.hellotypes.helloobject;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;

import soadev.jaxb.adapters.DateAdapter;


/**
 * <p>Java class for HelloObjectType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="HelloObjectType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="date" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="greeting" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlRootElement(name = "HelloObject")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HelloObjectType",
         propOrder = { "date", "greeting", "message" })
public class HelloObjectType {

    @XmlElement(required = true, nillable = true)
    @XmlJavaTypeAdapter(DateAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected Date date;
    @XmlElement(required = true, nillable = true)
    protected String greeting;
    @XmlElement(required = true, nillable = true)
    protected String message;

    /**
     * Gets the value of the date property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setDate(Date value) {
        this.date = value;
    }

    /**
     * Gets the value of the greeting property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getGreeting() {
        return greeting;
    }

    /**
     * Sets the value of the greeting property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setGreeting(String value) {
        this.greeting = value;
    }

    /**
     * Gets the value of the message property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMessage(String value) {
        this.message = value;
    }

}
