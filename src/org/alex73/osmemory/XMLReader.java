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

import com.vividsolutions.jts.geom.Envelope;

/**
 * This class reads XML file and stores all data into MemoryStorage.
 */
public class XMLReader extends BaseReader {

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
            obj.tagKeys[i] = storage.getTagsPack().getTagCode(driver.tags.get(i).k);
            obj.tagValues[i] = bytes(driver.tags.get(i).v);
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

    /**
     * Add all relations.
     */
    void createRelation(XMLDriver driver, long id, List<XMLDriver.Member> members, String user) {
        short userCode = storage.getUsersPack().getTagCode(user);
        long[] memberIds = new long[members.size()];
        byte[] memberTypes = new byte[members.size()];
        for (int i = 0; i < members.size(); i++) {
            memberIds[i] = members.get(i).id;
            memberTypes[i] = members.get(i).type;
        }
        OsmRelation result = new OsmRelation(id, driver.tags.size(), memberIds, memberTypes, userCode);
        for (int i = 0; i < result.memberRoles.length; i++) {
            result.memberRoles[i] = storage.getRelationRolesPack().getTagCode(members.get(i).role);
        }
        applyTags(driver, result);
        storage.relations.add(result);
    }

    static final Charset UTF8 = Charset.forName("UTF-8");

    byte[] bytes(String str) {
        return str.getBytes(UTF8);
    }
}
