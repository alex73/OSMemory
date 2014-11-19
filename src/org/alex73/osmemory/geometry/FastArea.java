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

/**
 * Polygon.contains() call performance optimization. It can be used for checks like "if node inside country",
 * "if way inside city", etc.
 * 
 * This class creates 400(by default) cells above of polygon as cache. Each cell can have 3 states: fully
 * included into polygon, not included in polygon, included partially. For the fully included and not included
 * cells checking is very fast operation. For partially included - it checks only subpolygon inside cell, that
 * is also a little bit faster process.
 * 
 * Keep in mind, that only nodes inside polygon will be checked. That means, if you have way from upper left
 * corner into bottom right corner, i.e. overlaps polygon, but there is no nodes of way inside polygon, then
 * this way will be treated as 'not contains'.
 * 
 * 'Area contains node' means node should be inside area, but not on the border. That means border's nodes are
 * not contained in area.
 * 
 * 'Area covers node' means node should be inside area or on the border.
 */
public class FastArea extends Fast {
    private final MemoryStorage storage;

    public FastArea(Geometry polygon, MemoryStorage storage) {
        super(polygon);
        if (storage == null) {
            throw new IllegalArgumentException();
        }

        this.storage = storage;
    }

    public FastArea(IOsmObject areaObject, MemoryStorage storage) {
        this(OsmHelper.areaFromObject(areaObject, storage), storage);
    }

    public boolean mayCovers(Envelope box) {
        if (maxx < box.getMinX() / IOsmNode.DIVIDER) {
            return false;
        }
        if (minx > box.getMaxX() / IOsmNode.DIVIDER) {
            return false;
        }
        if (maxy < box.getMinY() / IOsmNode.DIVIDER) {
            return false;
        }
        if (miny > box.getMaxY() / IOsmNode.DIVIDER) {
            return false;
        }
        return true;
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

    public boolean covers(IExtendedObject ext) {
        if (!mayCovers(ext.getBoundingBox())) {
            return false;
        }
        // iterate by full cells first - it will be faster than check inside geometry
        Boolean covers = ext.iterateNodes(new ExtendedWay.NodesIterator() {
            public Boolean processNode(IOsmNode node) {
                if (isSkipped(node)) {
                    return null;
                }
                Cell c = getCellForPoint(node.getLat(), node.getLon());
                if (c != null && c.isFull()) {
                    return true;
                }
                return null;
            }
        });
        if (covers != null) {
            return covers;
        }

        // iterate by geometries because there are no points inside full cells
        covers = ext.iterateNodes(new ExtendedWay.NodesIterator() {
            public Boolean processNode(IOsmNode node) {
                if (isSkipped(node)) {
                    return null;
                }
                Cell c = getCellForPoint(node.getLat(), node.getLon());
                if (c != null && !c.isFull() && coversCellNode(c, node)) {
                    return true;
                }
                return null;
            }
        });
        if (covers != null) {
            return covers;
        }
        return false;
    }

    /**
     * Return true if node should be used for covers tests. May be useful for check borders.
     */
    protected boolean isSkipped(IOsmNode node) {
        return false;
    }

    protected boolean coversNode(IOsmNode node) {
        Cell c = getCellForPoint(node.getLat(), node.getLon());
        if (c == null) {
            return false;
        }
        if (c.isEmpty()) {
            return false;
        } else if (c.isFull()) {
            return true;
        } else {
            return coversCellNode(c, node);
        }
    }

    protected boolean coversCellNode(Cell c, IOsmNode node) {
        Point p = GeometryHelper.createPoint(node.getLongitude(), node.getLatitude());
        if (c.getGeom() instanceof GeometryCollection) {
            // cached can be collection of point and polygon, but covers doesn't work with collection
            GeometryCollection cachedCollection = (GeometryCollection) c.getGeom();
            for (int i = 0; i < cachedCollection.getNumGeometries(); i++) {
                if (cachedCollection.getGeometryN(i).contains(p)) {
                    return true;
                }
            }
            return false;
        } else {
            return c.getGeom().covers(p);
        }
    }

    protected boolean coversWay(IOsmWay way) {
        ExtendedWay ext = new ExtendedWay(way, storage);

        // iterate by full cells first - it will be faster than check inside geometry
        Boolean covers = ext.iterateNodes(new ExtendedWay.NodesIterator() {
            public Boolean processNode(IOsmNode node) {
                if (isSkipped(node)) {
                    return null;
                }
                Cell c = getCellForPoint(node.getLat(), node.getLon());
                if (c != null && c.isFull()) {
                    return true;
                }
                return null;
            }
        });
        if (covers != null) {
            return covers;
        }

        // iterate by geometries because there are no points inside full cells
        covers = ext.iterateNodes(new ExtendedWay.NodesIterator() {
            public Boolean processNode(IOsmNode node) {
                if (isSkipped(node)) {
                    return null;
                }
                Cell c = getCellForPoint(node.getLat(), node.getLon());
                if (c != null && !c.isFull() && coversCellNode(c, node)) {
                    return true;
                }
                return null;
            }
        });
        if (covers != null) {
            return covers;
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
}
