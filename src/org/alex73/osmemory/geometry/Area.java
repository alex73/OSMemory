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

import java.util.ArrayList;
import java.util.List;

import org.alex73.osmemory.IOsmNode;
import org.alex73.osmemory.IOsmObject;
import org.alex73.osmemory.IOsmRelation;
import org.alex73.osmemory.IOsmWay;
import org.alex73.osmemory.MemoryStorage;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

/**
 * Representation of polygon.
 */
public class Area {
    protected final Geometry geom;

    public Area(Geometry geom) {
        this.geom = geom;
    }

    public Geometry getGeometry() {
        return geom;
    }

    public static Area fromWKT(String wkt) throws Exception {
        return new Area(GeometryHelper.fromWkt(wkt));
    }

    public static Area fromOSM(MemoryStorage storage, IOsmObject obj) {
        return new Area(osm2poly(storage, obj));
    }

    static Geometry osm2poly(MemoryStorage storage, IOsmObject obj) {
        switch (obj.getType()) {
        case IOsmObject.TYPE_NODE:
            throw new RuntimeException("Node can't be area");
        case IOsmObject.TYPE_WAY:
            return way2poly(storage, (IOsmWay) obj);
        case IOsmObject.TYPE_RELATION:
            return relation2poly(storage, (IOsmRelation) obj);
        default:
            throw new RuntimeException("Unknown object type");
        }
    }

    static Geometry way2poly(MemoryStorage storage, IOsmWay way) {
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
            return GeometryHelper.createPolygon(points);
        } catch (Throwable ex) {
            throw new RuntimeException("Impossible to create area from way #" + way.getId() + ": "
                    + ex.getMessage(), ex);
        }
    }

    static LineString way2line(MemoryStorage storage, IOsmWay way) {
        long[] nodeIds = way.getNodeIds();

        Coordinate[] points = new Coordinate[nodeIds.length];
        for (int i = 0; i < points.length; i++) {
            IOsmNode node = storage.getNodeById(nodeIds[i]);
            if (node == null) {
                throw new RuntimeException("Node #" + nodeIds[i] + " not exist for way #" + way.getId());
            }
            points[i] = GeometryHelper.coord(node.getLongitude(), node.getLatitude());
        }
        return GeometryHelper.createLine(points);
    }

    static Geometry relation2poly(MemoryStorage storage, IOsmRelation relation) {
        List<LineString> outer = new ArrayList<>();
        List<LineString> inner = new ArrayList<>();
        Geometry border = null;

        for (int i = 0; i < relation.getMembersCount(); i++) {
            IOsmObject m = relation.getMemberObject(storage, i);
            switch (relation.getMemberRole(storage, i)) {
            case "outer":
            case "":
                if (m == null) {
                    throw new RuntimeException("Object " + relation.getMemberCode(i)
                            + " not exist for relation #" + relation.getId());
                }
                if (m.getType() != IOsmObject.TYPE_WAY) {
                    throw new RuntimeException("Not a way outer object " + m.getObjectCode()
                            + "  for relation #" + relation.getId());
                }
                outer.add(way2line(storage, (IOsmWay) m));
                break;
            case "inner":
                if (m == null) {
                    throw new RuntimeException("Object " + relation.getMemberCode(i)
                            + " not exist for relation #" + relation.getId());
                }
                if (m.getType() != IOsmObject.TYPE_WAY) {
                    throw new RuntimeException("Not a way inner object " + m.getObjectCode()
                            + "  for relation #" + relation.getId());
                }
                inner.add(way2line(storage, (IOsmWay) m));
                break;
            case "border":
                if (m == null) {
                    throw new RuntimeException("Object " + relation.getMemberCode(i)
                            + " not exist for relation #" + relation.getId());
                }
                border = osm2poly(storage, m);
                break;
            }
        }
        if (border != null) {
            if (!outer.isEmpty() || !inner.isEmpty()) {
                throw new RuntimeException("Impossible to create area for " + relation.getObjectCode());
            }
            return border;
        } else {
            if (outer.isEmpty()) {
                throw new RuntimeException("There is no outer members for " + relation.getObjectCode());
            }
            Polygonizer po = new Polygonizer();
            po.add(outer);
            Polygonizer pi = new Polygonizer();
            pi.add(inner);
            Geometry go = GeometryHelper.union((List<Geometry>) po.getPolygons());
            Geometry gi = GeometryHelper.union((List<Geometry>) pi.getPolygons());
            if (!po.getDangles().isEmpty()) {
                throw new RuntimeException("There are dangles for outer members for "
                        + relation.getObjectCode());
            }
            if (!po.getCutEdges().isEmpty()) {
                throw new RuntimeException("There are cut edges for outer members for "
                        + relation.getObjectCode());
            }

            if (!pi.getDangles().isEmpty()) {
                throw new RuntimeException("There are dangles for inner members for "
                        + relation.getObjectCode());
            }
            if (!pi.getCutEdges().isEmpty()) {
                throw new RuntimeException("There are cut edges for inner members for "
                        + relation.getObjectCode());
            }

            return GeometryHelper.substract(go, gi);
        }
    }

    public Envelope getBoundingBox() {
        return geom.getEnvelopeInternal();
    }
}
