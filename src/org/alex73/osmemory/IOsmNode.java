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
 * Node object.
 */
public interface IOsmNode extends IOsmObject {
    public static final double DIVIDER = 0.0000001;

    /**
     * Get latitude as int, i.e. multiplied by 10000000.
     */
    int getLat();

    /**
     * Get longitude as int, i.e. multiplied by 10000000.
     */
    int getLon();

    /**
     * Get latitude as double, like 53.9.
     */
    double getLatitude();

    /**
     * Get latitude as double, like 27.566667.
     */
    double getLongitude();
}
