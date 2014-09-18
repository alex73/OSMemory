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

import java.util.HashMap;
import java.util.Map;

/**
 * Class for store some frequently used strings. Each string has own short id, that used in object instead
 * direct string usage.
 * 
 * That allow to minimize storage memory and use short type instead string.
 */
public class StringPack {
    final protected Map<String, Short> tagCodes = new HashMap<>();
    final protected Map<Short, String> tagNames = new HashMap<>();

    public synchronized short getTagCode(String tagName) {
        Short v = tagCodes.get(tagName);
        short result;
        if (v == null) {
            result = (short) tagCodes.size();
            if (result >= Short.MAX_VALUE) {
                throw new RuntimeException("Too many tag keys: more than " + Short.MAX_VALUE);
            }
            tagCodes.put(tagName, result);
            tagNames.put(result, tagName);
        } else {
            result = v;
        }
        return result;
    }

    public String getTagName(short tagKey) {
        return tagNames.get(tagKey);
    }
}
