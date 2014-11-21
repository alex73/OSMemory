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

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Some geometry operations.
 */
public class GeometryHelper {
    private static final GeometryFactory GEOM = new GeometryFactory(new PrecisionModel(10000000));

    public static Geometry fromWkt(String wkt) throws ParseException {
        return new WKTReader(GEOM).read(wkt);
    }

    public static String toWkt(Geometry geom) {
        return new WKTWriter().write(geom);
    }

    public static Point createPoint(double longitude, double latitude) {
        return GEOM.createPoint(coord(longitude, latitude));
    }

    public static Polygon createBoxPolygon(double mix, double max, double miy, double may) {
        return GEOM.createPolygon(new Coordinate[] { coord(mix, miy), coord(mix, may), coord(max, may),
                coord(max, miy), coord(mix, miy), });
    }

    public static Coordinate coord(double longitude, double latitude) {
        Coordinate result = new Coordinate(longitude, latitude);
        GEOM.getPrecisionModel().makePrecise(result);
        return result;
    }

    public static Polygon createPolygon(Coordinate[] coords) {
        return GEOM.createPolygon(coords);
    }

    public static LineString createLine(Coordinate[] coords) {
        return GEOM.createLineString(coords);
    }

    public static LineString createLine(List<Coordinate> coords) {
        return GEOM.createLineString(coords.toArray(new Coordinate[coords.size()]));
    }

    public static Geometry union(List<Geometry> list) {
        Geometry[] geoms = new Geometry[list.size()];
        list.toArray(geoms);
        return GEOM.createGeometryCollection(geoms).union();
    }

    public static Geometry multipolygon(List<Polygon> list) {
        Polygon[] pols = new Polygon[list.size()];
        list.toArray(pols);
        return GEOM.createMultiPolygon(pols);
    }

    public static Geometry substract(Geometry g1, Geometry g2) {
        return g1.difference(g2);
    }

    public static Geometry emptyCollection() {
        return GEOM.createGeometryCollection(null);
    }
}
