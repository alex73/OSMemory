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

import java.util.Map;

/**
 * Base interface for all OSM object.
 * 
 * Tag keys stored and processed as index instead string value.
 */
public interface IOsmObject {
    public static final int TYPE_NODE = 1;
    public static final int TYPE_WAY = 2;
    public static final int TYPE_RELATION = 3;

    /**
     * Object type: 1-node, 2-way,3-relation. It should be int instead enum for performance optimization.
     */
    int getType();

    boolean isNode();

    boolean isWay();

    boolean isRelation();

    /**
     * Object ID.
     */
    long getId();

    /**
     * Check if object has tag.
     */
    boolean hasTag(short tagKey);

    /**
     * Get tags list.
     */
    short[] getTags();

    /**
     * Check if object has tag. This operation is much slower than {@link #hasTag(short)}.
     */
    boolean hasTag(String tagName, MemoryStorage storage);

    /**
     * Get tag value.
     */
    String getTag(short tagKey);

    /**
     * Get tag value. This operation is much slower than {@link #getTag(short)}.
     */
    String getTag(String tagName, MemoryStorage storage);

    Map<String, String> extractTags(MemoryStorage storage);

    /**
     * Get object code, like n123, w75, r51.
     */
    String getObjectCode();

    /**
     * Get user code.
     */
    short getUser();

    /**
     * Get user name.
     */
    String getUser(MemoryStorage storage);

    static String getNodeCode(long nodeId) {
        return "n" + nodeId;
    }

    static String getWayCode(long wayId) {
        return "w" + wayId;
    }

    static String getRelationCode(long relationId) {
        return "r" + relationId;
    }
}
