
package osm.xmldatatypes;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}tag" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{}ID" />
 *       &lt;attribute name="user" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="uid" use="required" type="{}ID" />
 *       &lt;attribute name="created_at" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="closed_at" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="open" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="num_changes" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="min_lat" use="required" type="{}Lat" />
 *       &lt;attribute name="min_lon" use="required" type="{}Lon" />
 *       &lt;attribute name="max_lat" use="required" type="{}Lat" />
 *       &lt;attribute name="max_lon" use="required" type="{}Lon" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "tag"
})
@XmlRootElement(name = "changeset")
public class Changeset {

    protected List<Tag> tag;
    @XmlAttribute(name = "id", required = true)
    protected long id;
    @XmlAttribute(name = "user", required = true)
    protected String user;
    @XmlAttribute(name = "uid", required = true)
    protected long uid;
    @XmlAttribute(name = "created_at", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar createdAt;
    @XmlAttribute(name = "closed_at")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar closedAt;
    @XmlAttribute(name = "open", required = true)
    protected boolean open;
    @XmlAttribute(name = "num_changes", required = true)
    protected int numChanges;
    @XmlAttribute(name = "min_lat", required = true)
    protected double minLat;
    @XmlAttribute(name = "min_lon", required = true)
    protected double minLon;
    @XmlAttribute(name = "max_lat", required = true)
    protected double maxLat;
    @XmlAttribute(name = "max_lon", required = true)
    protected double maxLon;

    /**
     * Gets the value of the tag property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tag property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTag().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Tag }
     * 
     * 
     */
    public List<Tag> getTag() {
        if (tag == null) {
            tag = new ArrayList<Tag>();
        }
        return this.tag;
    }

    /**
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUser(String value) {
        this.user = value;
    }

    /**
     * Gets the value of the uid property.
     * 
     */
    public long getUid() {
        return uid;
    }

    /**
     * Sets the value of the uid property.
     * 
     */
    public void setUid(long value) {
        this.uid = value;
    }

    /**
     * Gets the value of the createdAt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the value of the createdAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreatedAt(XMLGregorianCalendar value) {
        this.createdAt = value;
    }

    /**
     * Gets the value of the closedAt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getClosedAt() {
        return closedAt;
    }

    /**
     * Sets the value of the closedAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setClosedAt(XMLGregorianCalendar value) {
        this.closedAt = value;
    }

    /**
     * Gets the value of the open property.
     * 
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Sets the value of the open property.
     * 
     */
    public void setOpen(boolean value) {
        this.open = value;
    }

    /**
     * Gets the value of the numChanges property.
     * 
     */
    public int getNumChanges() {
        return numChanges;
    }

    /**
     * Sets the value of the numChanges property.
     * 
     */
    public void setNumChanges(int value) {
        this.numChanges = value;
    }

    /**
     * Gets the value of the minLat property.
     * 
     */
    public double getMinLat() {
        return minLat;
    }

    /**
     * Sets the value of the minLat property.
     * 
     */
    public void setMinLat(double value) {
        this.minLat = value;
    }

    /**
     * Gets the value of the minLon property.
     * 
     */
    public double getMinLon() {
        return minLon;
    }

    /**
     * Sets the value of the minLon property.
     * 
     */
    public void setMinLon(double value) {
        this.minLon = value;
    }

    /**
     * Gets the value of the maxLat property.
     * 
     */
    public double getMaxLat() {
        return maxLat;
    }

    /**
     * Sets the value of the maxLat property.
     * 
     */
    public void setMaxLat(double value) {
        this.maxLat = value;
    }

    /**
     * Gets the value of the maxLon property.
     * 
     */
    public double getMaxLon() {
        return maxLon;
    }

    /**
     * Sets the value of the maxLon property.
     * 
     */
    public void setMaxLon(double value) {
        this.maxLon = value;
    }

}
