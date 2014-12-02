/**************************************************************************
 OSMemory library for OSM data processing.

 Copyright (C) 2014 Aleś Bułojčyk <alex73mail@gmail.com>

 This is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This software is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.alex73.osmemory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import osm.xmldatatypes.Member;
import osm.xmldatatypes.Nd;
import osm.xmldatatypes.Node;
import osm.xmldatatypes.Osm;
import osm.xmldatatypes.OsmBasicChange;
import osm.xmldatatypes.OsmBasicType;
import osm.xmldatatypes.OsmChange;
import osm.xmldatatypes.Relation;
import osm.xmldatatypes.Tag;
import osm.xmldatatypes.Way;

/**
 * Driver for OSM XML read. Format described at the http://wiki.openstreetmap.org/wiki/OSM_XML.
 */
public class XMLDriver {
    static JAXBContext CONTEXT;
    static {
        try {
            CONTEXT = JAXBContext.newInstance(OsmChange.class, Osm.class);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    final XMLReader handler;
    protected long id;
    protected double lat, lon;
    protected String user;
    protected List<Tag> tags = new ArrayList<>();
    protected List<Long> nds = new ArrayList<>();
    protected List<Member> members = new ArrayList<>();

    public XMLDriver(XMLReader handler) {
        this.handler = handler;
    }

    public void read(File file) throws Exception {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            parser.parse(in, new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws SAXException {
                    switch (qName) {
                    case "node":
                        id = Long.parseLong(attributes.getValue("id"));
                        lat = Double.parseDouble(attributes.getValue("lat"));
                        lon = Double.parseDouble(attributes.getValue("lon"));
                        user = attributes.getValue("user");
                        tags.clear();
                        break;
                    case "way":
                        id = Long.parseLong(attributes.getValue("id"));
                        user = attributes.getValue("user");
                        tags.clear();
                        nds.clear();
                        break;
                    case "nd":
                        nds.add(Long.parseLong(attributes.getValue("ref")));
                        break;
                    case "relation":
                        id = Long.parseLong(attributes.getValue("id"));
                        user = attributes.getValue("user");
                        tags.clear();
                        members.clear();
                        break;
                    case "member":
                        Member m = new Member();
                        m.setType(attributes.getValue("type"));
                        m.setRef(Long.parseLong(attributes.getValue("id")));
                        m.setRole(attributes.getValue("role"));
                        break;
                    case "tag":
                        Tag t = new Tag();
                        t.setK(attributes.getValue("k"));
                        t.setV(attributes.getValue("v"));
                        tags.add(t);
                        break;
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    switch (qName) {
                    case "node":
                        handler.createNode(XMLDriver.this, id, lat, lon, user);
                        break;
                    case "way":
                        handler.createWay(XMLDriver.this, id, nds, user);
                        break;
                    case "relation":
                        handler.createRelation(XMLDriver.this, id, members, user);
                        break;
                    }
                }
            });
        }
    }

    public void applyOsmChange(InputStream data, IApplyChangeCallback callback) throws Exception {
        OsmChange changes = (OsmChange) CONTEXT.createUnmarshaller().unmarshal(data);
        applyBasicChanges(XMLReader.UPDATE_MODE.CREATE, changes.getCreate(), callback);
        applyBasicChanges(XMLReader.UPDATE_MODE.MODIFY, changes.getModify(), callback);
        applyBasicChanges(XMLReader.UPDATE_MODE.DELETE, changes.getDelete(), callback);
    }

    static Map<String, String> tags(OsmBasicType obj) {
        Map<String, String> r = new TreeMap<>();
        for (Tag t : obj.getTag()) {
            r.put(t.getK(), t.getV());
        }
        return r;
    }

    static long[] nodes(List<Nd> nds) {
        long[] r = new long[nds.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = nds.get(i).getRef();
        }
        return r;
    }

    static long[] memberIds(List<Member> members) {
        long[] r = new long[members.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = members.get(i).getRef();
        }
        return r;
    }

    static byte[] memberTypes(List<Member> members) {
        byte[] r = new byte[members.size()];
        for (int i = 0; i < r.length; i++) {
            switch (members.get(i).getType()) {
            case "node":
                r[i] = IOsmObject.TYPE_NODE;
                break;
            case "way":
                r[i] = IOsmObject.TYPE_WAY;
                break;
            case "relation":
                r[i] = IOsmObject.TYPE_RELATION;
                break;
            default:
                throw new RuntimeException("Unknown member type: " + members.get(i).getType());
            }
        }
        return r;
    }

    static String[] memberRoles(List<Member> members) {
        String[] r = new String[members.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = members.get(i).getRole();
        }
        return r;
    }

    void applyBasicChanges(XMLReader.UPDATE_MODE mode, List<OsmBasicChange> changes,
            IApplyChangeCallback callback) {
        for (OsmBasicChange c : changes) {
            for (Node n : c.getNode()) {
                callback.beforeUpdateNode(n.getId());
                handler.updateNode(mode, n.getId(), n.getLat(), n.getLon(), tags(n), n.getUser());
                callback.afterUpdateNode(n.getId());
            }
            for (Way w : c.getWay()) {
                callback.beforeUpdateWay(w.getId());
                handler.updateWay(mode, w.getId(), nodes(w.getNd()), tags(w), w.getUser());
                callback.afterUpdateWay(w.getId());
            }
            for (Relation r : c.getRelation()) {
                callback.beforeUpdateRelation(r.getId());
                handler.updateRelation(mode, r.getId(), memberIds(r.getMember()), memberTypes(r.getMember()),
                        memberRoles(r.getMember()), tags(r), r.getUser());
                callback.afterUpdateRelation(r.getId());
            }
        }
    }

    /**
     * Application can use call back for each update. For define area of updates, for example.
     */
    public interface IApplyChangeCallback {
        void beforeUpdateNode(long id);

        void afterUpdateNode(long id);

        void beforeUpdateWay(long id);

        void afterUpdateWay(long id);

        void beforeUpdateRelation(long id);

        void afterUpdateRelation(long id);
    }
}
