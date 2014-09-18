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
 * Relation object representation.
 */
public class OsmRelation extends OsmBase implements IOsmRelation {
    final long memberIDs[];
    final byte memberTypes[];
    final short memberRoles[];

    public OsmRelation(long id, int tagsCount, int memberCount) {
        super(id, tagsCount);
        memberIDs = new long[memberCount];
        memberTypes = new byte[memberCount];
        memberRoles = new short[memberCount];
    }

    public OsmRelation(long id, int tagsCount, long[] memberIDs, byte[] memberTypes) {
        super(id, tagsCount);
        this.memberIDs = memberIDs;
        this.memberTypes = memberTypes;
        memberRoles = new short[memberIDs.length];
    }

    @Override
    public int getType() {
        return TYPE_RELATION;
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public boolean isWay() {
        return false;
    }

    @Override
    public boolean isRelation() {
        return true;
    }

    @Override
    public String getObjectCode() {
        return IOsmObject.getRelationCode(id);
    }

    @Override
    public int getMembersCount() {
        return memberIDs.length;
    }

    @Override
    public IOsmObject getMemberObject(MemoryStorage storage, int memberIndex) {
        switch (memberTypes[memberIndex]) {
        case TYPE_NODE:
            return storage.getNodeById(memberIDs[memberIndex]);
        case TYPE_WAY:
            return storage.getWayById(memberIDs[memberIndex]);
        case TYPE_RELATION:
            return storage.getRelationById(memberIDs[memberIndex]);
        default:
            throw new RuntimeException("Unknown member type");
        }
    }

    @Override
    public String getMemberRole(MemoryStorage storage, int memberIndex) {
        return storage.getRelationRolesPack().getTagName(memberRoles[memberIndex]);
    }

    @Override
    public int getMemberType(int memberIndex) {
        return memberTypes[memberIndex];
    }

    @Override
    public long getMemberID(int memberIndex) {
        return memberIDs[memberIndex];
    }

    @Override
    public String getMemberCode(int memberIndex) {
        switch (memberTypes[memberIndex]) {
        case TYPE_NODE:
            return IOsmObject.getNodeCode(memberIDs[memberIndex]);
        case TYPE_WAY:
            return IOsmObject.getWayCode(memberIDs[memberIndex]);
        case TYPE_RELATION:
            return IOsmObject.getRelationCode(memberIDs[memberIndex]);
        default:
            throw new RuntimeException("Unknown member type");
        }
    }
}
