
package com.oracle.xmlns.bpm.bpmobject.hellotypes.reviewobject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReviewObjectType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ReviewObjectType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="review" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="reason" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlRootElement(name = "ReviewObject")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReviewObjectType", propOrder = { "review", "reason" })
public class ReviewObjectType {

    @XmlElement(required = true, nillable = true)
    protected String review;
    @XmlElement(required = true, nillable = true)
    protected String reason;

    /**
     * Gets the value of the review property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getReview() {
        return review;
    }

    /**
     * Sets the value of the review property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setReview(String value) {
        this.review = value;
    }

    /**
     * Gets the value of the reason property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the value of the reason property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setReason(String value) {
        this.reason = value;
    }

}
