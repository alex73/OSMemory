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

import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;

/**
 * Base class for node/way/relation objects with tags.
 */
public abstract class OsmBase implements IOsmObject {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    protected final long id;
    protected final short[] tagKeys;
    protected final byte[][] tagValues;
    protected final short user;

    public OsmBase(long id, int tagsCount, short user) {
        this.id = id;
        tagKeys = new short[tagsCount];
        tagValues = new byte[tagsCount][];
        this.user = user;
    }

    public long getId() {
        return id;
    }

    public boolean hasTag(short tagKey) {
        for (int i = 0; i < tagKeys.length; i++) {
            if (tagKeys[i] == tagKey) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTag(String tagName, MemoryStorage storage) {
        short tagKey = storage.getTagsPack().getTagCode(tagName);
        return hasTag(tagKey);
    }

    @Override
    public short[] getTags() {
        return tagKeys;
    }

    @Override
    public String getTag(short tagKey) {
        for (int i = 0; i < tagKeys.length; i++) {
            if (tagKeys[i] == tagKey) {
                return new String(tagValues[i], UTF8);
            }
        }
        return null;
    }

    @Override
    public String getTag(String tagName, MemoryStorage storage) {
        short tagKey = storage.getTagsPack().getTagCode(tagName);
        return getTag(tagKey);
    }

    /**
     * Get all tags for object into map. This operation is not so fast, i.e. shouldn't be used for all object.
     */
    public Map<String, String> extractTags(MemoryStorage storage) {
        Map<String, String> result = new TreeMap<>();
        for (int i = 0; i < tagKeys.length; i++) {
            String tagName = storage.getTagsPack().getTagName(tagKeys[i]);
            result.put(tagName, new String(tagValues[i], UTF8));
        }
        return result;
    }

    @Override
    public short getUser() {
        return user;
    }

    @Override
    public String getUser(MemoryStorage storage) {
        return storage.getUsersPack().getTagName(user);
    }
}
