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
 * Way object representation.
 */
public class OsmWay extends OsmBase implements IOsmWay {
    private final long[] nodeIds;

    public OsmWay(long id, int tagsCount, long[] nodeIds) {
        super(id, tagsCount);
        this.nodeIds = nodeIds;
    }

    @Override
    public long[] getNodeIds() {
        return nodeIds;
    }

    @Override
    public int getType() {
        return TYPE_WAY;
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public boolean isWay() {
        return true;
    }

    @Override
    public boolean isRelation() {
        return false;
    }

    @Override
    public String getObjectCode() {
        return IOsmObject.getWayCode(id);
    }
}
