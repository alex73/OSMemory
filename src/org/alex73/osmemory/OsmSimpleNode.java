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

import java.util.Collections;
import java.util.Map;

/**
 * Simple node object representation. Simple nodes don't have tags, i.e. can be used only in ways and
 * relations.
 */
public class OsmSimpleNode implements IOsmNode {

    private final static short[] EMPTY_SHORT_LIST = new short[0];
    private final long id;

    /**
     * Latitude and longitude stored as integer, like in o5m. It allows to minimize memory and increase
     * performance in some cases. All coordinates in OSM stored with 7 digits after point precision.
     */
    private final int lat, lon;

    public OsmSimpleNode(MemoryStorage storage, int pos) {
        this.id = storage.simpleNodeIds[pos];
        this.lat = storage.simpleNodeLats[pos];
        this.lon = storage.simpleNodeLons[pos];
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getLat() {
        return lat;
    }

    @Override
    public int getLon() {
        return lon;
    }

    @Override
    public double getLatitude() {
        return lat * DIVIDER;
    }

    public double getLongitude() {
        return lon * DIVIDER;
    }

    @Override
    public boolean hasTag(short tagKey) {
        return false;
    }

    @Override
    public boolean hasTag(String tagName, MemoryStorage storage) {
        return false;
    }

    @Override
    public short[] getTags() {
        return EMPTY_SHORT_LIST;
    }

    @Override
    public String getTag(short tagKey) {
        return null;
    }

    @Override
    public String getTag(String tagName, MemoryStorage storage) {
        return null;
    }

    @Override
    public Map<String, String> extractTags(MemoryStorage storage) {
        return Collections.emptyMap();
    }

    @Override
    public int getType() {
        return IOsmObject.TYPE_NODE;
    }

    @Override
    public boolean isNode() {
        return true;
    }

    @Override
    public boolean isWay() {
        return false;
    }

    @Override
    public boolean isRelation() {
        return false;
    }

    @Override
    public String getObjectCode() {
        return IOsmObject.getNodeCode(id);
    }

    @Override
    public IOsmObjectID getObjectID() {
        return new OsmObjectID(TYPE_NODE, id);
    }

    @Override
    public short getUser() {
        return -1;
    }

    @Override
    public String getUser(MemoryStorage storage) {
        return null;
    }
}
