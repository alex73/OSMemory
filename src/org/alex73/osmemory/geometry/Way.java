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
import org.alex73.osmemory.IOsmWay;
import org.alex73.osmemory.MemoryStorage;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

/**
 * Representation of way.
 */
public class Way {
    private final IOsmWay way;
    private final MemoryStorage storage;
    private Envelope boundingBox;

    public Way(IOsmWay way, MemoryStorage storage) {
        this.way = way;
        this.storage = storage;
    }

    public IOsmWay getWay() {
        return way;
    }

    public Envelope getBoundingBox() {
        if (boundingBox == null) {
            boundingBox = new Envelope();
            for (long nid : way.getNodeIds()) {
                IOsmNode n = storage.getNodeById(nid);
                if (n != null) {
                    boundingBox.expandToInclude(n.getLongitude(), n.getLatitude());
                }
            }
        }
        return boundingBox;
    }

    public boolean isAllPointsDefined() {
        for (long nid : way.getNodeIds()) {
            IOsmNode n = storage.getNodeById(nid);
            if (n == null) {
                return false;
            }
        }
        return true;
    }

    public boolean isLine() {
        long[] nids = way.getNodeIds();
        for (int i = 0; i < nids.length; i++) {
            boolean existTheSame = false;
            for (int j = 0; j < i; j++) {
                if (nids[i] == nids[j]) {
                    existTheSame = true;
                    break;
                }
            }
            if (existTheSame) {
                return false;
            }
        }
        return true;
    }

    public boolean isClosed() {
        long[] nids = way.getNodeIds();
        for (int i = 0; i < nids.length; i++) {
            boolean existTheSame = false;
            for (int j = 0; j < i; j++) {
                if (nids[i] == nids[j]) {
                    if (i == nids.length - 1 && j == 0) {
                        return true; // last equals first
                    }
                    existTheSame = true;
                    break;
                }
            }
            if (existTheSame) {
                return false;
            }
        }
        return false;
    }

    public Geometry getLineGeometry() {
        long[] nodeIds = way.getNodeIds();

        Coordinate[] points = new Coordinate[nodeIds.length];
        for (int i = 0; i < points.length; i++) {
            IOsmNode node = storage.getNodeById(nodeIds[i]);
            if (node == null) {
                throw new RuntimeException("Node #" + nodeIds[i] + " not exist for way #" + way.getId());
            }
            points[i] = GeometryHelper.coord(node.getLongitude(), node.getLatitude());
        }

        try {
            LineString line = GeometryHelper.createLine(points);
            if (!line.isValid()) {
                throw new Exception("not valid line");
            }
            if (!line.isSimple()) {
                throw new Exception("self-intersected");
            }
            return line;
        } catch (Throwable ex) {
            throw new RuntimeException("Impossible to create line from way #" + way.getId() + ": "
                    + ex.getMessage(), ex);
        }
    }
}
