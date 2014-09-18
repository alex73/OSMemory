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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Storage for all nodes, ways, relations.
 */
public class MemoryStorage {
    static final Pattern RE_OBJECT_CODE = Pattern.compile("([nwr])([0-9]+)");

    // notes with tags sorted list
    protected final List<IOsmNode> nodes = new ArrayList<>();
    // ways sorted list
    protected final List<IOsmWay> ways = new ArrayList<>();
    // relations sorted list
    protected final List<IOsmRelation> relations = new ArrayList<>();

    // simple nodes, i.e. without tags
    protected long[] simpleNodeIds = new long[4 * 1024 * 1024];
    protected int[] simpleNodeLats = new int[4 * 1024 * 1024];
    protected int[] simpleNodeLons = new int[4 * 1024 * 1024];
    protected int simpleNodeCount;

    private final StringPack tagsPack = new StringPack();
    private final StringPack relationRolesPack = new StringPack();

    private long loadingStartTime, loadingFinishTime;

    public MemoryStorage() {
        loadingStartTime = System.currentTimeMillis();
    }

    /**
     * Must be called after load data for optimization and some internal processing.
     */
    void finishLoading() throws Exception {
        // check ID order
        long prev = 0;
        for (int i = 0; i < simpleNodeCount; i++) {
            long id = simpleNodeIds[i];
            if (id <= prev) {
                throw new Exception("Nodes must be ordered by ID");
            }
            prev = id;
        }
        prev = 0;
        for (int i = 0; i < nodes.size(); i++) {
            long id = nodes.get(i).getId();
            if (id <= prev) {
                throw new Exception("Nodes must be ordered by ID");
            }
            prev = id;
        }
        prev = 0;
        for (int i = 0; i < ways.size(); i++) {
            long id = ways.get(i).getId();
            if (id <= prev) {
                throw new Exception("Ways must be ordered by ID");
            }
            prev = id;
        }
        prev = 0;
        for (int i = 0; i < relations.size(); i++) {
            long id = relations.get(i).getId();
            if (id <= prev) {
                throw new Exception("Relations must be ordered by ID");
            }
            prev = id;
        }
        loadingFinishTime = System.currentTimeMillis();
    }

    public StringPack getTagsPack() {
        return tagsPack;
    }

    public StringPack getRelationRolesPack() {
        return relationRolesPack;
    }

    private static <T extends IOsmObject> T getById(List<T> en, long id) {
        int i = binarySearch(en, id);
        return i < 0 ? null : en.get(i);
    }

    private static <T extends IOsmObject> int binarySearch(List<T> en, long id) {
        int low = 0;
        int high = en.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            long midvalue = en.get(mid).getId();

            if (midvalue < id)
                low = mid + 1;
            else if (midvalue > id)
                high = mid - 1;
            else
                return mid;
        }
        return -1;
    }

    public IOsmNode getNodeById(long id) {
        int pos = Arrays.binarySearch(simpleNodeIds, 0, simpleNodeCount, id);
        if (pos >= 0) {
            return new OsmSimpleNode(this, pos);
        } else {
            return getById(nodes, id);
        }
    }

    public IOsmWay getWayById(long id) {
        return getById(ways, id);
    }

    public IOsmRelation getRelationById(long id) {
        return getById(relations, id);
    }

    /**
     * Get object by code like n123, w456, r789.
     */
    public IOsmObject getObject(String code) throws Exception {
        Matcher m = RE_OBJECT_CODE.matcher(code.trim());
        if (!m.matches()) {
            throw new Exception("Няправільны фарматы code: " + code);
        }
        long idl = Long.parseLong(m.group(2));
        switch (m.group(1)) {
        case "n":
            return getNodeById(idl);
        case "w":
            return getWayById(idl);
        case "r":
            return getRelationById(idl);
        default:
            throw new Exception("Wrong code format: " + code);
        }
    }

    /**
     * Show some loading statistics.
     */
    public void showStat() {
        DecimalFormat f = new DecimalFormat(",##0");
        System.out.println("Loading time       : " + f.format((loadingFinishTime - loadingStartTime)) + "ms");
        System.out.println("Simple nodes count : " + f.format(simpleNodeCount));
        System.out.println("Nodes count        : " + f.format(nodes.size()));
        System.out.println("Ways count         : " + f.format(ways.size()));
        System.out.println("Relations count    : " + f.format(relations.size()));
        System.out.println("Tags count         : " + f.format(tagsPack.tagCodes.size()));
        System.out.println("RelRoles count     : " + f.format(relationRolesPack.tagCodes.size()));
    }

    /**
     * Process objects with specific tag.
     */
    public void byTag(String tagName, Predicate<IOsmObject> predicate, Consumer<IOsmObject> consumer) {
        short tagKey = tagsPack.getTagCode(tagName);
        for (int i = 0; i < nodes.size(); i++) {
            IOsmNode n = nodes.get(i);
            if (n.hasTag(tagKey)) {
                if (predicate.test(n)) {
                    consumer.accept(n);
                }
            }
        }
        for (int i = 0; i < ways.size(); i++) {
            IOsmWay w = ways.get(i);
            if (w.hasTag(tagKey)) {
                if (predicate.test(w)) {
                    consumer.accept(w);
                }
            }
        }
        for (int i = 0; i < relations.size(); i++) {
            IOsmRelation r = relations.get(i);
            if (r.hasTag(tagKey)) {
                if (predicate.test(r)) {
                    consumer.accept(r);
                }
            }
        }
    }

    /**
     * Process all objects.
     */
    public void all(Predicate<IOsmObject> predicate, Consumer<IOsmObject> consumer) {
        for (int i = 0; i < nodes.size(); i++) {
            IOsmNode n = nodes.get(i);
            if (predicate.test(n)) {
                consumer.accept(n);
            }
        }
        for (int i = 0; i < ways.size(); i++) {
            IOsmWay w = ways.get(i);
            if (predicate.test(w)) {
                consumer.accept(w);
            }
        }
        for (int i = 0; i < relations.size(); i++) {
            IOsmRelation r = relations.get(i);
            if (predicate.test(r)) {
                consumer.accept(r);
            }
        }
    }

    /**
     * Process all objects.
     */
    public void all(Consumer<IOsmObject> consumer) {
        all(new Predicate<IOsmObject>() {
            @Override
            public boolean test(IOsmObject t) {
                return true;
            }
        }, consumer);
    }
}
