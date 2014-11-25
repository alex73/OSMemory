
package osm.xmldatatypes;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the osm.xmldatatypes package. 
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


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: osm.xmldatatypes
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Node }
     * 
     */
    public Node createNode() {
        return new Node();
    }

    /**
     * Create an instance of {@link OsmBasicType }
     * 
     */
    public OsmBasicType createOsmBasicType() {
        return new OsmBasicType();
    }

    /**
     * Create an instance of {@link Tag }
     * 
     */
    public Tag createTag() {
        return new Tag();
    }

    /**
     * Create an instance of {@link Nd }
     * 
     */
    public Nd createNd() {
        return new Nd();
    }

    /**
     * Create an instance of {@link Osm }
     * 
     */
    public Osm createOsm() {
        return new Osm();
    }

    /**
     * Create an instance of {@link Bounds }
     * 
     */
    public Bounds createBounds() {
        return new Bounds();
    }

    /**
     * Create an instance of {@link Way }
     * 
     */
    public Way createWay() {
        return new Way();
    }

    /**
     * Create an instance of {@link Relation }
     * 
     */
    public Relation createRelation() {
        return new Relation();
    }

    /**
     * Create an instance of {@link Member }
     * 
     */
    public Member createMember() {
        return new Member();
    }

    /**
     * Create an instance of {@link Changeset }
     * 
     */
    public Changeset createChangeset() {
        return new Changeset();
    }

    /**
     * Create an instance of {@link OsmChange }
     * 
     */
    public OsmChange createOsmChange() {
        return new OsmChange();
    }

    /**
     * Create an instance of {@link OsmBasicChange }
     * 
     */
    public OsmBasicChange createOsmBasicChange() {
        return new OsmBasicChange();
    }

}
