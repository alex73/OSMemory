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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alex73.osmemory.IOsmNode;
import org.alex73.osmemory.IOsmObject;
import org.alex73.osmemory.IOsmRelation;
import org.alex73.osmemory.IOsmWay;
import org.alex73.osmemory.MemoryStorage;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Class for cache some extended information about relation, like boundary, geometry, etc.
 */
public class ExtendedRelation implements IExtendedObject {
    private final IOsmRelation relation;
    private final MemoryStorage storage;

    private BoundingBox boundingBox;
    private boolean allPointsDefined;
    private Geometry area;

    // Area's border points for check 'isInBorder'
    private final Set<Long> borderNodes = new HashSet<>();

    public ExtendedRelation(IOsmRelation relation, MemoryStorage storage) {
        this.relation = relation;
        this.storage = storage;
    }

    @Override
    public IOsmObject getObject() {
        return relation;
    }

    public BoundingBox getBoundingBox() {
        checkProcessed();
        return boundingBox;
    }

    public boolean isAllPointsDefined() {
        checkProcessed();
        return allPointsDefined;
    }

    public Set<Long> getBorderNodes() {
        return borderNodes;
    }

    public synchronized Geometry getArea() {
        if (area == null) {
            List<Line> lines = new ArrayList<>();
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
                    lines.add(new Line(false, way2line(storage, (IOsmWay) m)));
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
                    lines.add(new Line(true, way2line(storage, (IOsmWay) m)));
                    break;
                case "border":
                    if (m == null) {
                        throw new RuntimeException("Object " + relation.getMemberCode(i)
                                + " not exist for relation #" + relation.getId());
                    }
                    throw new RuntimeException();
                }
            }
            Geometry g = null;
            while (!lines.isEmpty()) {
                List<LineString> outer = getAs(false, lines);
                List<LineString> inner = getAs(true, lines);
                Geometry go = polygonize(outer);
                if (!go.isValid()) {
                    throw new RuntimeException("Impossible to create polygon from relation #"
                            + relation.getId() + ": outer part is not valid");
                }
                Geometry gi = polygonize(inner);
                if (!gi.isValid()) {
                    throw new RuntimeException("Impossible to create polygon from relation #"
                            + relation.getId() + ": inner part is not valid");
                }
                if (g == null) {
                    g = GeometryHelper.substract(go, gi);
                } else {
                    g = g.union(GeometryHelper.substract(go, gi));
                }
            }
            if (!g.isValid()) {
                throw new RuntimeException("Impossible to create polygon from relation #" + relation.getId()
                        + ": it is not valid");
            }
            area = g;
        }
        return area;
    }

    LineString way2line(MemoryStorage storage, IOsmWay way) {
        long[] nodeIds = way.getNodeIds();

        Coordinate[] points = new Coordinate[nodeIds.length];
        for (int i = 0; i < points.length; i++) {
            IOsmNode node = storage.getNodeById(nodeIds[i]);
            if (node == null) {
                throw new RuntimeException("Node #" + nodeIds[i] + " not exist for way #" + way.getId());
            }
            borderNodes.add(nodeIds[i]);
            points[i] = GeometryHelper.coord(node.getLongitude(), node.getLatitude());
        }
        return GeometryHelper.createLine(points);
    }

    Geometry polygonize(List<LineString> lines) {
        if (lines.isEmpty()) {
            return GeometryHelper.emptyCollection();
        }
        List<Polygon> polygons = new ArrayList<>();

        while (!lines.isEmpty()) {
            LineString ring = fullClosedLine(lines);
            polygons.add(GeometryHelper.createPolygon(ring.getCoordinates()));
        }
        if (polygons.size() > 1) {
            return GeometryHelper.multipolygon(polygons);
        } else {
            return polygons.get(0);
        }
    }

    LineString fullClosedLine(List<LineString> lines) {
        List<Coordinate> tail = new ArrayList<>();

        boolean found;
        do {
            found = false;
            for (int i = 0; i < lines.size(); i++) {
                if (addToClosed(tail, lines.get(i))) {
                    lines.remove(i);
                    i--;
                    found = true;
                }
            }
        } while (found);

        LineString s = GeometryHelper.createLine(tail);
        if (!s.isClosed()) {
            throw new RuntimeException("Non-closed line starts from " + tail.get(0) + " ends to "
                    + tail.get(tail.size() - 1));
        }
        if (!s.isSimple()) {
            throw new RuntimeException("Self-intersected line: " + s);
        }
        return s;
    }

    boolean addToClosed(List<Coordinate> tail, LineString line) {
        boolean revert = false;
        int place = -1;
        if (tail.isEmpty()) {
            place = 0;
        } else {
            Coordinate tailFirst = tail.get(0);
            Coordinate tailLast = tail.get(tail.size() - 1);
            Coordinate lineFirst = line.getCoordinateN(0);
            Coordinate lineLast = line.getCoordinateN(line.getNumPoints() - 1);

            if (lineFirst.equals(tailLast)) {
                revert = false;
                place = tail.size();
            } else if (lineFirst.equals(tailFirst)) {
                revert = true;
                place = 0;
            } else if (lineLast.equals(tailFirst)) {
                revert = false;
                place = 0;
            } else if (lineLast.equals(tailLast)) {
                revert = true;
                place = tail.size();
            }
        }

        if (place >= 0) {
            List<Coordinate> cs = Arrays.asList(line.getCoordinates());
            if (revert) {
                Collections.reverse(cs);
            }
            tail.addAll(place, cs);
        }

        return place >= 0;
    }

    protected synchronized void checkProcessed() {
        if (boundingBox != null) {
            return;
        }
        boundingBox = new BoundingBox();
        allPointsDefined = true;
        iterateNodes(new NodesIterator() {
            @Override
            public Boolean processNode(IOsmNode n) {
                if (n != null) {
                    boundingBox.expandToInclude(n.getLat(), n.getLon());
                } else {
                    allPointsDefined = false;
                }
                return null;
            }
        });
    }

    public Boolean iterateNodes(NodesIterator iterator) {
        return iterateRelation(iterator, relation, new HashSet<>());
    }

    protected Boolean iterateWay(NodesIterator iterator, IOsmWay way) {
        for (int i = 0; i < way.getNodeIds().length; i++) {
            long nid = way.getNodeIds()[i];

            IOsmNode n = storage.getNodeById(nid);
            Boolean r = iterator.processNode(n);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    protected Boolean iterateRelation(NodesIterator iterator, IOsmRelation relation,
            Set<Long> processedRelations) {
        for (int i = 0; i < relation.getMembersCount(); i++) {
            IOsmObject m = relation.getMemberObject(storage, i);
            if (m == null) {
                continue;
            }
            Boolean r;
            switch (relation.getMemberType(i)) {
            case IOsmObject.TYPE_NODE:
                r = iterator.processNode((IOsmNode) m);
                break;
            case IOsmObject.TYPE_WAY:
                r = iterateWay(iterator, (IOsmWay) m);
                break;
            case IOsmObject.TYPE_RELATION:
                if (processedRelations.contains(m.getId())) {
                    continue;
                }
                processedRelations.add(relation.getId());
                r = iterateRelation(iterator, (IOsmRelation) m, processedRelations);
                break;
            default:
                throw new RuntimeException();
            }
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    List<LineString> getAs(boolean inner, List<Line> lines) {
        List<LineString> result = new ArrayList<>();
        while (!lines.isEmpty()) {
            if (lines.get(0).inner == inner) {
                result.add(lines.remove(0).line);
            } else {
                break;
            }
        }
        return result;
    }

    static class Line {
        boolean inner;
        LineString line;

        public Line(boolean inner, LineString line) {
            this.inner = inner;
            this.line = line;
        }
    }
}
