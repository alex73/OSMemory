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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Driver for OSM XML read. Format described at the http://wiki.openstreetmap.org/wiki/OSM_XML.
 */
public class XMLDriver {
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
                        members.add(new Member(attributes.getValue("type"), attributes.getValue("id"),
                                attributes.getValue("role")));
                        break;
                    case "tag":
                        tags.add(new Tag(attributes.getValue("k"), attributes.getValue("v")));
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

    public static class Tag {
        public final String k, v;

        public Tag(String k, String v) {
            this.k = k;
            this.v = v;
        }
    }

    public static class Member {
        public final byte type;
        public final long id;
        public final String role;

        public Member(String type, String id, String role) {
            switch (type) {
            case "node":
                this.type = IOsmObject.TYPE_NODE;
                break;
            case "way":
                this.type = IOsmObject.TYPE_WAY;
                break;
            case "relation":
                this.type = IOsmObject.TYPE_RELATION;
                break;
            default:
                throw new RuntimeException();
            }
            this.id = Long.parseLong(id);
            this.role = role;
        }
    }
}
