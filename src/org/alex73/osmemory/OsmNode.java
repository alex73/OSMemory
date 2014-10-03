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

/**
 * Node object representation.
 */
public class OsmNode extends OsmBase implements IOsmNode {
    public static final double DIVIDER = 0.0000001;
    /**
     * Latitude and longitude stored as integer, like in o5m. It allows to minimize memory and increase
     * performance in some cases. All coordinates in OSM stored with 7 digits after point precision.
     */
    private final int lat, lon;

    public OsmNode(long id, int tagsCount, int lat, int lon, short user) {
        super(id, tagsCount, user);
        this.lat = lat;
        this.lon = lon;
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

    @Override
    public double getLongitude() {
        return lon * DIVIDER;
    }

    @Override
    public int getType() {
        return TYPE_NODE;
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
}
