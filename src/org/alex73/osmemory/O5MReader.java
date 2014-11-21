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
import java.util.Arrays;

import com.vividsolutions.jts.geom.Envelope;

/**
 * This class reads o5m file and stores all data into MemoryStorage.
 */
public class O5MReader extends BaseReader {

    public static void main(String[] aa) throws Exception {
        MemoryStorage st = new O5MReader().read(new File("tmp/belarus-updated.o5m"));
        st.showStat();
    }

    public O5MReader() {
        super(null);
    }

    public O5MReader(Envelope cropBox) {
        super(cropBox);
    }

    public O5MReader(MemoryStorage storage, double minLat, double maxLat, double minLon, double maxLon) {
        super(storage, new Envelope(minLon, maxLon, minLat, maxLat));
    }

    public MemoryStorage read(File file) throws Exception {
        new O5MDriver(this).read(file);
        storage.finishLoading();
        return storage;
    }

    void applyTags(O5MDriver driver, OsmBase obj) {
        for (int i = 0; i < driver.getObjectTagsCount(); i++) {
            obj.tagKeys[i] = storage.getTagsPack().getTagCode(driver.getObjectTagKeyString(i));
            obj.tagValues[i] = driver.getObjectTagValueBytes(i);
        }
    }

    /**
     * @param timestamp
     *            The Unit is seconds since Jan 01 1970.
     */
    protected void fileTimestamp(long timestamp) {
    }

    /**
     * Add nodes inside specified crop box.
     */
    void createNode(O5MDriver driver, long id, int lat, int lon, String user) {
        if (lat > 900000000 || lat < -900000000) {
            throw new RuntimeException("Wrong value for latitude: " + lat);
        }
        if (lon > 1800000000 || lon < -1800000000) {
            throw new RuntimeException("Wrong value for longitude: " + lon);
        }

        if (!isInsideCropBox(lat, lon)) {
            return;
        }

        if (driver.getObjectTagsCount() > 0) {
            short userCode = storage.getUsersPack().getTagCode(user);
            OsmNode result = new OsmNode(id, driver.getObjectTagsCount(), lat, lon, userCode);
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
    void createWay(O5MDriver driver, long id, long[] nodes, String user) {
        short userCode = storage.getUsersPack().getTagCode(user);
        OsmWay result = new OsmWay(id, driver.getObjectTagsCount(), nodes, userCode);
        boolean inside = false;
        for (int i = 0; i < nodes.length; i++) {
            if (storage.getNodeById(nodes[i]) != null) {
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
    void createRelation(O5MDriver driver, long id, long[] memberIds, byte[] memberTypes, String user) {
        short userCode = storage.getUsersPack().getTagCode(user);
        OsmRelation result = new OsmRelation(id, driver.getObjectTagsCount(), memberIds, memberTypes,
                userCode);
        for (int i = 0; i < result.memberRoles.length; i++) {
            result.memberRoles[i] = storage.getRelationRolesPack().getTagCode(driver.getMemberRoleString(i));
        }
        applyTags(driver, result);
        storage.relations.add(result);
    }
}
