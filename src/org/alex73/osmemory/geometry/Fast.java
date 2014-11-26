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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Some base operations for cells.
 */
public class Fast {
    public static final int PARTS_COUNT_BYXY = 20;

    protected final int minx, maxx, miny, maxy, stepx, stepy;
    protected final Geometry polygon;
    protected final Cell[][] cached;

    public Fast(Geometry polygon) {
        if (polygon == null) {
            throw new IllegalArgumentException();
        }
        this.polygon = polygon;

        Envelope bo = polygon.getEnvelopeInternal();
        minx = (int) (bo.getMinX() / IOsmNode.DIVIDER) - 1;
        maxx = (int) (bo.getMaxX() / IOsmNode.DIVIDER) + 1;
        miny = (int) (bo.getMinY() / IOsmNode.DIVIDER) - 1;
        maxy = (int) (bo.getMaxY() / IOsmNode.DIVIDER) + 1;

        // can be more than 4-byte signed integer
        long dx = ((long) maxx) - ((long) minx);
        long dy = ((long) maxy) - ((long) miny);

        stepx = (int) (dx / PARTS_COUNT_BYXY + 1);
        stepy = (int) (dy / PARTS_COUNT_BYXY + 1);
        cached = new Cell[PARTS_COUNT_BYXY][];
        for (int i = 0; i < cached.length; i++) {
            cached[i] = new Cell[PARTS_COUNT_BYXY];
        }
    }

    public Geometry getGeometry() {
        return polygon;
    }

    public synchronized Cell getCellForPoint(int lat, int lon) {
        int x = lon;
        int y = lat;
        if (x < minx || x >= maxx || y < miny || y >= maxy) {
            return null;
        }

        int ix = (x - minx) / stepx;
        int iy = (y - miny) / stepy;
        return getCell(ix, iy);
    }

    public int getCellXForPoint(int lon) {
        int x = lon;
        if (x < minx) {
            return -1;
        }
        if (x >= maxx) {
            return PARTS_COUNT_BYXY;
        }
        return (x - minx) / stepx;
    }

    public int getCellYForPoint(int lat) {
        int y = lat;
        if (y < miny) {
            return -1;
        }
        if (y >= maxy) {
            return PARTS_COUNT_BYXY;
        }
        return (y - miny) / stepy;
    }

    public Cell getCell(int ix, int iy) {
        if (cached[ix][iy] != null) {
            return cached[ix][iy];
        }

        synchronized (this) {
            if (cached[ix][iy] == null) {
                cached[ix][iy] = calcCell(ix, iy);
            }
        }

        return cached[ix][iy];
    }

    protected Cell calcCell(int ix, int iy) {
        int ulx = minx + ix * stepx;
        int uly = miny + iy * stepy;
        double mix = ulx * IOsmNode.DIVIDER;
        double max = (ulx + stepx - 1) * IOsmNode.DIVIDER;
        double miy = uly * IOsmNode.DIVIDER;
        double may = (uly + stepy - 1) * IOsmNode.DIVIDER;
        Polygon p = GeometryHelper.createBoxPolygon(mix, max, miy, may);
        final Geometry intersection = polygon.intersection(p);
        if (intersection.isEmpty()) {
            return new Cell(ix, iy, true, false, intersection);
        } else if (intersection.equalsExact(p)) {
            return new Cell(ix, iy, false, true, intersection);
        } else {
            return new Cell(ix, iy, false, false, intersection);
        }
    }

    public static class Cell {
        private final int x, y;
        private final boolean empty, full;
        private final Geometry geom;

        public Cell(int x, int y, boolean empty, boolean full, Geometry geom) {
            this.x = x;
            this.y = y;
            this.empty = empty;
            this.full = full;
            this.geom = geom;
        }

        public boolean isEmpty() {
            return empty;
        }

        public boolean isFull() {
            return full;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Geometry getGeom() {
            return geom;
        }
    }
}
