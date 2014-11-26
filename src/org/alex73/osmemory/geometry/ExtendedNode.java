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
import org.alex73.osmemory.IOsmObject;
import org.alex73.osmemory.MemoryStorage;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Class for cache some extended information about node.
 */
public class ExtendedNode implements IExtendedObject {
    private final IOsmNode node;
    private final MemoryStorage storage;

    private Envelope boundingBox;

    public ExtendedNode(IOsmNode node, MemoryStorage storage) {
        this.node = node;
        this.storage = storage;
    }

    @Override
    public IOsmObject getObject() {
        return node;
    }

    public Envelope getBoundingBox() {
        checkProcessed();
        return boundingBox;
    }

    protected synchronized void checkProcessed() {
        if (boundingBox != null) {
            return; // already loaded
        }
        boundingBox = new Envelope();
        boundingBox.expandToInclude(node.getLongitude(), node.getLatitude());
    }

    @Override
    public Boolean iterateNodes(NodesIterator iterator) {
        return iterator.processNode(node);
    }
}
