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

import com.vividsolutions.jts.geom.Envelope;

/**
 * Base class for readers from all formats.
 */
public class BaseReader {
    protected MemoryStorage storage;
    protected int minx, maxx, miny, maxy;

    /**
     * Reader can skip nodes outside of cropbox. If cropbox is not defined, all nodes will be loaded.
     */
    protected BaseReader(Envelope cropBox) {
        storage = new MemoryStorage();
        if (cropBox != null) {
            minx = (int) (cropBox.getMinX() / OsmNode.DIVIDER) - 1;
            maxx = (int) (cropBox.getMaxX() / OsmNode.DIVIDER) + 1;
            miny = (int) (cropBox.getMinY() / OsmNode.DIVIDER) - 1;
            maxy = (int) (cropBox.getMaxY() / OsmNode.DIVIDER) + 1;
        } else {
            minx = Integer.MIN_VALUE;
            maxx = Integer.MAX_VALUE;
            miny = Integer.MIN_VALUE;
            maxy = Integer.MAX_VALUE;
        }
    }

    /**
     * Check if node inside crop box.
     */
    protected boolean isInsideCropBox(int lat, int lon) {
        return lon >= minx && lon <= maxx && lat >= miny && lat <= maxy;
    }
}
