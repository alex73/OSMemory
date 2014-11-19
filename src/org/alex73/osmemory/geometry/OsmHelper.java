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

package org.alex73.osmemory.geometry;

import org.alex73.osmemory.IOsmNode;
import org.alex73.osmemory.IOsmObject;
import org.alex73.osmemory.IOsmRelation;
import org.alex73.osmemory.IOsmWay;
import org.alex73.osmemory.MemoryStorage;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Creates area geometry from way or relation.
 */
public class OsmHelper {
    public static Geometry areaFromObject(IOsmObject obj, MemoryStorage osm) {
        if (obj.isWay()) {
            return new ExtendedWay((IOsmWay) obj, osm).getArea();
        } else if (obj.isRelation()) {
            return new ExtendedRelation((IOsmRelation) obj, osm).getArea();
        } else {
            throw new RuntimeException("Area object must be way or relation");
        }
    }

    public static IExtendedObject extendedFromObject(IOsmObject obj, MemoryStorage osm) {
        switch (obj.getType()) {
        case IOsmObject.TYPE_NODE:
            return new ExtendedNode((IOsmNode) obj, osm);
        case IOsmObject.TYPE_WAY:
            return new ExtendedWay((IOsmWay) obj, osm);
        case IOsmObject.TYPE_RELATION:
            return new ExtendedRelation((IOsmRelation) obj, osm);
        default:
            throw new RuntimeException("Unknown object type");
        }
    }
}
