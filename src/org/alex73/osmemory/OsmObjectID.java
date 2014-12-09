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

public class OsmObjectID implements IOsmObjectID {
    private final int type;
    private final long id;

    public OsmObjectID(int type, long id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id) + type;
    }

    @Override
    public boolean equals(Object obj) {
        OsmObjectID o = (OsmObjectID) obj;
        return type == o.type && id == o.id;
    }

    @Override
    public String toString() {
        switch (type) {
        case IOsmObject.TYPE_NODE:
            return IOsmObject.getNodeCode(id);
        case IOsmObject.TYPE_WAY:
            return IOsmObject.getWayCode(id);
        case IOsmObject.TYPE_RELATION:
            return IOsmObject.getRelationCode(id);
        default:
            return "UNKNOWN TYPE: " + type;
        }
    }
}
