
package osm.xmldatatypes;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *     &lt;extension base="{}osmBasicType">
 *       &lt;sequence>
 *         &lt;element ref="{}nd" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "nd"
})
@XmlRootElement(name = "way")
public class Way
    extends OsmBasicType
{

    @XmlElement(required = true)
    protected List<Nd> nd;

    /**
     * Gets the value of the nd property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nd property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNd().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Nd }
     * 
     * 
     */
    public List<Nd> getNd() {
        if (nd == null) {
            nd = new ArrayList<Nd>();
        }
        return this.nd;
    }

}
