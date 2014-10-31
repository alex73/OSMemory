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

import java.util.HashSet;
import java.util.Set;

import org.alex73.osmemory.IOsmNode;
import org.alex73.osmemory.IOsmObject;
import org.alex73.osmemory.IOsmRelation;
import org.alex73.osmemory.IOsmWay;
import org.alex73.osmemory.MemoryStorage;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * AdaptiveFastArea works almost like FastArea, but creates cells dynamically in deep.
 */
public class AdaptiveFastArea {
    private static final int PARTS_COUNT_BYXY = 10;

    private final AdaptiveArea area;
    private final MemoryStorage storage;

    public AdaptiveFastArea(Geometry polygon, MemoryStorage storage) {
        area = new AdaptiveArea(polygon);
        if (storage == null) {
            throw new IllegalArgumentException();
        }
        this.storage = storage;
    }

    public Geometry getGeometry() {
        return area.polygon;
    }

    public boolean covers(IOsmObject obj) {
        switch (obj.getType()) {
        case IOsmObject.TYPE_NODE:
            return coversNode((IOsmNode) obj);
        case IOsmObject.TYPE_WAY:
            return coversWay((IOsmWay) obj);
        case IOsmObject.TYPE_RELATION:
            return coversRelation((IOsmRelation) obj, new HashSet<>());
        default:
            throw new RuntimeException("Unknown object type");
        }
    }

    public boolean mayCovers(Envelope box) {
        if (area.maxx < box.getMinX() / IOsmNode.DIVIDER) {
            return false;
        }
        if (area.minx > box.getMaxX() / IOsmNode.DIVIDER) {
            return false;
        }
        if (area.maxy < box.getMinY() / IOsmNode.DIVIDER) {
            return false;
        }
        if (area.miny > box.getMaxY() / IOsmNode.DIVIDER) {
            return false;
        }
        return true;
    }

    protected boolean coversNode(IOsmNode node) {
        return area.covers(node.getLat(), node.getLon());
    }

    protected boolean coversWay(IOsmWay way) {
        long[] nodeIds = way.getNodeIds();
        for (int i = 0; i < nodeIds.length; i++) {
            long nid = nodeIds[i];
            IOsmNode n = storage.getNodeById(nid);
            if (n != null && coversNode(n)) {
                return true;
            }
        }
        return false;
    }

    protected boolean coversRelation(IOsmRelation rel, Set<String> processedRelations) {
        processedRelations.add(rel.getObjectCode());
        for (int i = 0; i < rel.getMembersCount(); i++) {
            IOsmObject o = rel.getMemberObject(storage, i);
            if (o == null) {
                continue;
            }
            if (o.isRelation()) {
                if (processedRelations.contains(o.getObjectCode())) {
                    // check against circular relations
                    continue;
                } else if (coversRelation((IOsmRelation) o, processedRelations)) {
                    return true;
                }
            } else if (covers(o)) {
                return true;
            }
        }
        return false;
    }

    interface Covers {
        boolean covers(int lat, int lon);
    }

    static class AdaptiveArea implements Covers {
        private int minx, maxx, miny, maxy;
        private int stepx, stepy;
        private final Geometry polygon;
        private Covers[][] cached;
        private long checkCount;

        public AdaptiveArea(Geometry polygon) {
            if (polygon == null) {
                throw new IllegalArgumentException();
            }
            this.polygon = polygon;
            Envelope bo = polygon.getEnvelopeInternal();
            minx = (int) (bo.getMinX() / IOsmNode.DIVIDER) - 1;
            maxx = (int) (bo.getMaxX() / IOsmNode.DIVIDER) + 1;
            miny = (int) (bo.getMinY() / IOsmNode.DIVIDER) - 1;
            maxy = (int) (bo.getMaxY() / IOsmNode.DIVIDER) + 1;
        }

        private void split() {
            // can be more than 4-byte signed integer
            long dx = ((long) maxx) - ((long) minx);
            long dy = ((long) maxy) - ((long) miny);

            stepx = (int) (dx / PARTS_COUNT_BYXY + 1);
            stepy = (int) (dy / PARTS_COUNT_BYXY + 1);
            cached = new Covers[PARTS_COUNT_BYXY][];
            for (int i = 0; i < cached.length; i++) {
                cached[i] = new Covers[PARTS_COUNT_BYXY];
            }
        }

        @Override
        public boolean covers(int lat, int lon) {
            int x = lon;
            int y = lat;
            if (x < minx || x >= maxx || y < miny || y >= maxy) {
                return false;
            }
            checkCount++;
            if (cached == null) {
                if (checkCount > 30 && maxx - minx > 200 && maxy - miny > 200) {
                    split();
                } else {
                    return coversByPolygon(lat, lon);
                }
            }
            // already splitted

            int ix = (x - minx) / stepx;
            int iy = (y - miny) / stepy;
            Covers c = cached[ix][iy];
            if (c == null) {
                c = calcCache(ix, iy);
                cached[ix][iy] = c;
            }
            return c.covers(lat, lon);
        }

        private boolean coversByPolygon(int lat, int lon) {
            Point p = GeometryHelper.createPoint(lon * IOsmNode.DIVIDER, lat * IOsmNode.DIVIDER);
            if (polygon instanceof GeometryCollection) {
                // cached can be collection of point and polygon, but covers doesn't work with collection
                GeometryCollection cachedCollection = (GeometryCollection) polygon;
                for (int i = 0; i < cachedCollection.getNumGeometries(); i++) {
                    if (cachedCollection.getGeometryN(i).contains(p)) {
                        return true;
                    }
                }
                return false;
            } else {
                return polygon.covers(p);
            }
        }

        Covers calcCache(int ix, int iy) {
            int ulx = minx + ix * stepx;
            int uly = miny + iy * stepy;
            double mix = ulx * IOsmNode.DIVIDER;
            double max = (ulx + stepx - 1) * IOsmNode.DIVIDER;
            double miy = uly * IOsmNode.DIVIDER;
            double may = (uly + stepy - 1) * IOsmNode.DIVIDER;
            Polygon p = GeometryHelper.createBoxPolygon(mix, max, miy, may);
            Geometry intersection = polygon.intersection(p);
            if (intersection.isEmpty()) {
                return EMPTY_AREA;
            } else if (intersection.equalsExact(p)) {
                return FULL_AREA;
            } else {
                return new AdaptiveArea(intersection);
            }
        }
    }

    private static final Covers EMPTY_AREA = new Covers() {
        @Override
        public boolean covers(int lat, int lon) {
            return false;
        }
    };
    private static final Covers FULL_AREA = new Covers() {
        @Override
        public boolean covers(int lat, int lon) {
            return true;
        }
    };
}
