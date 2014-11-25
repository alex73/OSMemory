
package osm.xmldatatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="minlat" use="required" type="{}Lat" />
 *       &lt;attribute name="minlon" use="required" type="{}Lon" />
 *       &lt;attribute name="maxlat" use="required" type="{}Lat" />
 *       &lt;attribute name="maxlon" use="required" type="{}Lon" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "bounds")
public class Bounds {

    @XmlAttribute(name = "minlat", required = true)
    protected double minlat;
    @XmlAttribute(name = "minlon", required = true)
    protected double minlon;
    @XmlAttribute(name = "maxlat", required = true)
    protected double maxlat;
    @XmlAttribute(name = "maxlon", required = true)
    protected double maxlon;

    /**
     * Gets the value of the minlat property.
     * 
     */
    public double getMinlat() {
        return minlat;
    }

    /**
     * Sets the value of the minlat property.
     * 
     */
    public void setMinlat(double value) {
        this.minlat = value;
    }

    /**
     * Gets the value of the minlon property.
     * 
     */
    public double getMinlon() {
        return minlon;
    }

    /**
     * Sets the value of the minlon property.
     * 
     */
    public void setMinlon(double value) {
        this.minlon = value;
    }

    /**
     * Gets the value of the maxlat property.
     * 
     */
    public double getMaxlat() {
        return maxlat;
    }

    /**
     * Sets the value of the maxlat property.
     * 
     */
    public void setMaxlat(double value) {
        this.maxlat = value;
    }

    /**
     * Gets the value of the maxlon property.
     * 
     */
    public double getMaxlon() {
        return maxlon;
    }

    /**
     * Sets the value of the maxlon property.
     * 
     */
    public void setMaxlon(double value) {
        this.maxlon = value;
    }

}
