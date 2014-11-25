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

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import osm.xmldatatypes.Member;

import com.vividsolutions.jts.geom.Envelope;

/**
 * This class reads XML file and stores all data into MemoryStorage.
 */
public class XMLReader extends BaseReader {
    public enum UPDATE_MODE {
        CREATE, MODIFY, DELETE
    };

    public static void main(String[] aa) throws Exception {
        MemoryStorage st = new XMLReader().read(new File(
                "src-test/org/alex73/osmemory/geometry/multipolygon1.xml"));
        st.showStat();
    }

    public XMLReader() {
        super(null);
    }

    public XMLReader(Envelope cropBox) {
        super(cropBox);
    }

    public XMLReader(MemoryStorage storage, double minLat, double maxLat, double minLon, double maxLon) {
        super(storage, new Envelope(minLon, maxLon, minLat, maxLat));
    }

    public MemoryStorage read(File file) throws Exception {
        new XMLDriver(this).read(file);
        storage.finishLoading();
        return storage;
    }

    void applyTags(XMLDriver driver, OsmBase obj) {
        for (int i = 0; i < driver.tags.size(); i++) {
            obj.tagKeys[i] = storage.getTagsPack().getTagCode(driver.tags.get(i).getK());
            obj.tagValues[i] = bytes(driver.tags.get(i).getV());
        }
    }

    void applyTags(Map<String, String> tags, OsmBase obj) {
        int i = 0;
        for (Map.Entry<String, String> en : tags.entrySet()) {
            obj.tagKeys[i] = storage.getTagsPack().getTagCode(en.getKey());
            obj.tagValues[i] = bytes(en.getValue());
            i++;
        }
    }

    /**
     * Add nodes inside specified crop box.
     */
    void createNode(XMLDriver driver, long id, double dlat, double dlon, String user) {
        if (dlat > 90 || dlat < -90) {
            throw new RuntimeException("Wrong value for latitude: " + dlat);
        }
        if (dlon > 180 || dlon < -180) {
            throw new RuntimeException("Wrong value for longitude: " + dlon);
        }
        int lat = (int) Math.round(dlat / IOsmNode.DIVIDER);
        int lon = (int) Math.round(dlon / IOsmNode.DIVIDER);

        if (!isInsideCropBox(lat, lon)) {
            return;
        }

        if (driver.tags.size() > 0) {
            short userCode = storage.getUsersPack().getTagCode(user);
            OsmNode result = new OsmNode(id, driver.tags.size(), lat, lon, userCode);
            applyTags(driver, result);
            storage.nodes.add(result);
        } else {
            if (storage.simpleNodeCount >= storage.simpleNodeIds.length) {
                // extend
                storage.simpleNodeIds = Arrays.copyOf(storage.simpleNodeIds,
                        storage.simpleNodeIds.length + 1024 * 1024);
                storage.simpleNodeLats = Arrays.copyOf(storage.simpleNodeLats,
                        storage.simpleNodeLats.length + 1024 * 1024);
                storage.simpleNodeLons = Arrays.copyOf(storage.simpleNodeLons,
                        storage.simpleNodeLons.length + 1024 * 1024);
            }
            int p = storage.simpleNodeCount++;
            storage.simpleNodeIds[p] = id;
            storage.simpleNodeLats[p] = lat;
            storage.simpleNodeLons[p] = lon;
        }
    }

    public void updateNode(UPDATE_MODE mode, long id, double dlat, double dlon, Map<String, String> tags,
            String user) {
        if (mode == UPDATE_MODE.DELETE) {
            storage.removeNode(id);
        } else {
            int lat = (int) Math.round(dlat / IOsmNode.DIVIDER);
            int lon = (int) Math.round(dlon / IOsmNode.DIVIDER);
            if (tags.isEmpty()) {
                storage.addSimpleNode(id, lat, lon);
            } else {
                short userCode = storage.getUsersPack().getTagCode(user);
                OsmNode n = new OsmNode(id, tags.size(), lat, lon, userCode);
                applyTags(tags, n);
                storage.addNode(n);
            }
        }
    }

    /**
     * Add ways that contains known nodes, i.e. inside specified crop box.
     */
    void createWay(XMLDriver driver, long id, List<Long> nodes, String user) {
        short userCode = storage.getUsersPack().getTagCode(user);
        long[] ns = new long[nodes.size()];
        for (int i = 0; i < ns.length; i++) {
            ns[i] = nodes.get(i);
        }
        OsmWay result = new OsmWay(id, driver.tags.size(), ns, userCode);
        boolean inside = false;
        for (int i = 0; i < ns.length; i++) {
            if (storage.getNodeById(ns[i]) != null) {
                inside = true;
                break;
            }
        }
        if (inside) {
            applyTags(driver, result);
            storage.ways.add(result);
        }
    }

    public void updateWay(UPDATE_MODE mode, long id, long[] nodes, Map<String, String> tags, String user) {
        if (mode == UPDATE_MODE.DELETE) {
            storage.removeWay(id);
        } else {
            short userCode = storage.getUsersPack().getTagCode(user);
            OsmWay w = new OsmWay(id, tags.size(), nodes, userCode);
            applyTags(tags, w);
            storage.addWay(w);
        }
    }

    /**
     * Add all relations.
     */
    void createRelation(XMLDriver driver, long id, List<Member> members, String user) {
        short userCode = storage.getUsersPack().getTagCode(user);
        long[] memberIds = new long[members.size()];
        byte[] memberTypes = new byte[members.size()];
        for (int i = 0; i < members.size(); i++) {
            memberIds[i] = members.get(i).getRef();
            switch (members.get(i).getType()) {
            case "node":
                memberTypes[i] = IOsmObject.TYPE_NODE;
                break;
            case "way":
                memberTypes[i] = IOsmObject.TYPE_WAY;
                break;
            case "relation":
                memberTypes[i] = IOsmObject.TYPE_RELATION;
                break;
            default:
                throw new RuntimeException();
            }
        }
        OsmRelation result = new OsmRelation(id, driver.tags.size(), memberIds, memberTypes, userCode);
        for (int i = 0; i < result.memberRoles.length; i++) {
            result.memberRoles[i] = storage.getRelationRolesPack().getTagCode(members.get(i).getRole());
        }
        applyTags(driver, result);
        storage.relations.add(result);
    }

    public void updateRelation(UPDATE_MODE mode, long id, long[] memberIDs, byte[] memberTypes,
            Map<String, String> tags, String user) {
        if (mode == UPDATE_MODE.DELETE) {
            storage.removeRelation(id);
        } else {
            short userCode = storage.getUsersPack().getTagCode(user);
            OsmRelation r = new OsmRelation(id, tags.size(), memberIDs, memberTypes, userCode);
            applyTags(tags, r);
            storage.addRelation(r);
        }
    }

    static final Charset UTF8 = Charset.forName("UTF-8");

    byte[] bytes(String str) {
        return str.getBytes(UTF8);
    }
}
