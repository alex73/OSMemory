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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Driver for o5m read. Format described at the http://wiki.openstreetmap.org/wiki/O5m.
 */
public class O5MDriver {
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final byte MARK_BOF = (byte) 0xFF;
    public static final byte MARK_EOF = (byte) 0xFE;
    public static final byte MARK_RESET = (byte) 0xFF;
    public static final byte MARK_DATASET_NODE = (byte) 0x10;
    public static final byte MARK_DATASET_WAY = (byte) 0x11;
    public static final byte MARK_DATASET_RELATION = (byte) 0x12;
    public static final byte MARK_DATASET_BOUNDINGBOX = (byte) 0xDB;
    public static final byte MARK_DATASET_FILETIMESTAMP = (byte) 0xDC;
    public static final byte MARK_DATASET_HEADER = (byte) 0xE0;
    public static final byte MARK_DATASET_SYNC = (byte) 0xEE;
    public static final byte MARK_DATASET_JUMP = (byte) 0xEF;

    final O5MReader handler;
    private ByteBuffer buffer;

    private DeltaCoder deltaId = new DeltaCoder();
    private DeltaCoder deltaTimestamp = new DeltaCoder();
    private DeltaCoder deltaChangeset = new DeltaCoder();
    private DeltaCoderInt deltaLongitude = new DeltaCoderInt();
    private DeltaCoderInt deltaLatitude = new DeltaCoderInt();
    private DeltaCoder deltaReferenceWayNode = new DeltaCoder();
    private DeltaCoder deltaReferenceRelationMemberNode = new DeltaCoder();
    private DeltaCoder deltaReferenceRelationMemberWay = new DeltaCoder();
    private DeltaCoder deltaReferenceRelationMemberRelation = new DeltaCoder();

    private int[] stringPairPositions = new int[15001];
    private int[] stringPairFirstSizes = new int[15001];
    private int[] stringPairSecondSizes = new int[15001];
    int stringPairPos;

    private int datasetEndPos;
    private int objectTagsCount;
    private int[] objectTagKeyPositions = new int[1024];
    private int[] objectTagKeySizes = new int[1024];
    private int[] objectTagValuePositions = new int[1024];
    private int[] objectTagValueSizes = new int[1024];
    private long[] nodes = new long[8192];
    private byte[] memberTypes = new byte[8192];
    private int[] memberRolePositions = new int[8192];
    private int[] memberRoleSizes = new int[8192];

    private void resetDeltas() {
        deltaId.value = 0;
        deltaTimestamp.value = 0;
        deltaChangeset.value = 0;
        deltaLongitude.value = 0;
        deltaLatitude.value = 0;
        deltaReferenceWayNode.value = 0;
        deltaReferenceRelationMemberNode.value = 0;
        deltaReferenceRelationMemberWay.value = 0;
        deltaReferenceRelationMemberRelation.value = 0;
    }

    public O5MDriver(O5MReader handler) {
        this.handler = handler;
    }

    public void read(File file) throws Exception {
        try (RandomAccessFile aFile = new RandomAccessFile(file, "r")) {
            try (FileChannel inChannel = aFile.getChannel()) {
                buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
                buffer.order(ByteOrder.LITTLE_ENDIAN);

                if (buffer.get() != MARK_BOF) {
                    throw new IOException("This is not a .o5m file");
                }
                while (buffer.remaining() > 0) {
                    byte datasetType = buffer.get();
                    if (datasetType == MARK_RESET) {
                        resetDeltas();
                        continue;
                    }
                    if (datasetType == MARK_EOF && buffer.remaining() == 0) {
                        break;
                    }
                    long datasetLength = readUnsignedNumberAbsolute();
                    datasetEndPos = (int) (buffer.position() + datasetLength);
                    switch (datasetType) {
                    case MARK_DATASET_HEADER:
                        // should follow the first 0xff in the file; contents: 0x04 0x6f 0x35 0x6d 0x32
                        // ("o5m2"),
                        // or 0x04 0x6f 0x35 0x63 0x32 ("o5c2") for .o5m change files
                        break;
                    case MARK_DATASET_FILETIMESTAMP:
                        break;
                    case MARK_DATASET_BOUNDINGBOX:
                        break;
                    case MARK_DATASET_NODE:
                        readNode();
                        break;
                    case MARK_DATASET_WAY:
                        readWay();
                        break;
                    case MARK_DATASET_RELATION:
                        readRelation();
                        break;
                    default:
                        throw new RuntimeException("Unknown dataset : " + Integer.toHexString(datasetType));
                    }
                    objectTagsCount = 0;
                    buffer.position(datasetEndPos);
                }
            }
        }
    }

    void readNode() {
        long id = readSignedNumber(deltaId);

        if (buffer.position() >= datasetEndPos) {
            return;
        }
        readVersion();

        if (buffer.position() >= datasetEndPos) {
            return;
        }
        int longitude = readSignedNumberInt(deltaLongitude);
        int latitude = readSignedNumberInt(deltaLatitude);

        while (buffer.position() < datasetEndPos) {
            readObjectTag();
        }
        handler.createNode(this, id, latitude, longitude);
    }

    void readWay() {
        long id = readSignedNumber(deltaId);

        if (buffer.position() >= datasetEndPos) {
            return;
        }
        readVersion();

        if (buffer.position() >= datasetEndPos) {
            return;
        }

        int refSectionLength = (int) readUnsignedNumberAbsolute();
        int refSectionEnd = buffer.position() + refSectionLength;

        int c = 0;
        while (buffer.position() < refSectionEnd) {
            nodes[c] = readSignedNumber(deltaReferenceWayNode);
            c++;
        }

        while (buffer.position() < datasetEndPos) {
            readObjectTag();
        }
        handler.createWay(this, id, Arrays.copyOf(nodes, c));
    }

    void readRelation() {
        long id = readSignedNumber(deltaId);

        if (buffer.position() >= datasetEndPos) {
            return;
        }
        readVersion();

        if (buffer.position() >= datasetEndPos) {
            return;
        }

        int refSectionLength = (int) readUnsignedNumberAbsolute();
        int refSectionEnd = buffer.position() + refSectionLength;

        int c = 0;
        while (buffer.position() < refSectionEnd) {
            long idval = readUnsignedNumberAbsolute();
            memberTypes[c] = readMemberInfo(c);
            switch (memberTypes[c]) {
            case IOsmObject.TYPE_NODE:
                nodes[c] = readSignedNumber(idval, deltaReferenceRelationMemberNode);
                break;
            case IOsmObject.TYPE_WAY:
                nodes[c] = readSignedNumber(idval, deltaReferenceRelationMemberWay);
                break;
            case IOsmObject.TYPE_RELATION:
                nodes[c] = readSignedNumber(idval, deltaReferenceRelationMemberRelation);
                break;
            }
            c++;
            if (c >= memberTypes.length) {
                throw new RuntimeException("Too many members in relation");
            }
        }

        while (buffer.position() < datasetEndPos) {
            readObjectTag();
        }
        handler.createRelation(this, id, Arrays.copyOf(nodes, c), Arrays.copyOf(memberTypes, c));
    }

    void readVersion() {
        long version = readUnsignedNumberAbsolute();
        if (version == 0) {
            return;
        }
        long timestamp = readSignedNumber(deltaTimestamp);
        if (timestamp == 0) {
            return;
        }
        long changeset = readSignedNumber(deltaChangeset);
        readUidUserPair();
    }

    /**
     * To store numbers of different lengths we abandon bit 7 (most significant bit) of every byte and use
     * this bit as a length indicator. This indicator – when set to 1 – tells us that the next byte belongs to
     * the same number. The first byte of such a long number contains the least significant 7 bits, the last
     * byte the most significant 7 bits.
     */
    long readUnsignedNumberAbsolute() {
        long value = 0;
        long b = 0x80;

        for (int i = 0; (b & 0x80) != 0; i += 7) {
            b = buffer.get();
            long part = (b & 0x7F) << i;
            value |= part;
        }

        return value;
    }

    /**
     * If a number is stored as "signed", we will need 1 bit for the sign. For this purpose, the least
     * significant bit of the least significant byte is taken as sign bit. 0 means positive, 1 means negative.
     * We do not need the number -0, of course, so we can shift the range of negative numbers by one.
     */
    long readSignedNumber(DeltaCoder delta) {
        return readSignedNumber(readUnsignedNumberAbsolute(), delta);
    }

    long readSignedNumber(long value, DeltaCoder delta) {
        if ((value & 1) != 0) {
            // negative
            value = -(value >> 1) - 1;
        } else {
            // positive
            value = (value >> 1);
        }

        long result = delta.value + value;
        delta.value = result;

        return result;
    }

    int readSignedNumberInt(DeltaCoderInt delta) {
        long value = readUnsignedNumberAbsolute();

        if ((value & 1) != 0) {
            // negative
            value = -(value >> 1) - 1;
        } else {
            // positive
            value = (value >> 1);
        }

        int result = (int) (delta.value + value);
        delta.value = result;

        return result;
    }

    void readUidUserPair() {
        long v = readUnsignedNumberAbsolute();
        if (v != 0) {
            // refer to string pair
        } else {
            storeStringPair(stringPairPos);
            if (stringPairFirstSizes[stringPairPos] + stringPairSecondSizes[stringPairPos] <= 250) {
                // store for future
                stringPairPos++;
                if (stringPairPos >= stringPairPositions.length) {
                    stringPairPos -= stringPairPositions.length;
                }
            }
        }
    }

    byte readMemberInfo(int i) {
        long v = readUnsignedNumberAbsolute();
        int pairPos;
        if (v != 0) {
            // refer to string pair
            pairPos = (int) (stringPairPos - v);
            if (pairPos < 0) {
                pairPos += stringPairPositions.length;
            }
        } else {
            pairPos = stringPairPos;
            storeStringOnes(stringPairPos);
            if (stringPairFirstSizes[stringPairPos] + stringPairSecondSizes[stringPairPos] <= 250) {
                // store for future
                stringPairPos++;
                if (stringPairPos >= stringPairPositions.length) {
                    stringPairPos -= stringPairPositions.length;
                }
            }
        }
        memberRolePositions[i] = stringPairPositions[pairPos] + 1;
        memberRoleSizes[i] = stringPairFirstSizes[pairPos] - 1;
        switch (buffer.get(stringPairPositions[pairPos])) {
        case '0':
            return OsmNode.TYPE_NODE;
        case '1':
            return OsmNode.TYPE_WAY;
        case '2':
            return OsmNode.TYPE_RELATION;
        default:
            throw new RuntimeException("Unknown member type");
        }
    }

    void readObjectTag() {
        long v = readUnsignedNumberAbsolute();
        int pairPos;
        if (v != 0) {
            // refer to string pair
            pairPos = (int) (stringPairPos - v);
            if (pairPos < 0) {
                pairPos += stringPairPositions.length;
            }
        } else {
            pairPos = stringPairPos;
            storeStringPair(stringPairPos);
            if (stringPairFirstSizes[stringPairPos] + stringPairSecondSizes[stringPairPos] <= 250) {
                // store for future
                stringPairPos++;
                if (stringPairPos >= stringPairPositions.length) {
                    stringPairPos -= stringPairPositions.length;
                }
            }
        }
        objectTagKeyPositions[objectTagsCount] = stringPairPositions[pairPos];
        objectTagKeySizes[objectTagsCount] = stringPairFirstSizes[pairPos];
        objectTagValuePositions[objectTagsCount] = stringPairPositions[pairPos]
                + stringPairFirstSizes[pairPos] + 1;
        objectTagValueSizes[objectTagsCount] = stringPairSecondSizes[pairPos];
        objectTagsCount++;
        if (objectTagsCount >= objectTagKeyPositions.length) {
            throw new RuntimeException("Too many tags ib object");
        }
    }

    void storeStringPair(int pos) {
        stringPairPositions[pos] = buffer.position();
        int p1 = 0;
        while (buffer.get() != 0) {
            p1++;
        }
        int p2 = 0;
        while (buffer.get() != 0) {
            p2++;
        }
        stringPairFirstSizes[pos] = p1;
        stringPairSecondSizes[pos] = p2;
    }

    void storeStringOnes(int pos) {
        stringPairPositions[pos] = buffer.position();
        int p1 = 0;
        while (buffer.get() != 0) {
            p1++;
        }
        stringPairFirstSizes[pos] = p1;
        stringPairSecondSizes[pos] = 0;
    }

    void skip(long count) {
        for (int i = 0; i < count; i++) {
            buffer.get();
        }
    }

    int getObjectTagsCount() {
        return objectTagsCount;
    }

    String getObjectTagKeyString(int pos) {
        return getString(objectTagKeyPositions[pos], objectTagKeySizes[pos]);
    }

    byte[] getObjectTagValueBytes(int pos) {
        return getBytes(objectTagValuePositions[pos], objectTagValueSizes[pos]);
    }

    String getMemberRoleString(int pos) {
        return getString(memberRolePositions[pos], memberRoleSizes[pos]);
    }

    String getString(int pos, int len) {
        int p = buffer.position();

        buffer.position(pos);
        byte[] result = new byte[len];
        buffer.get(result);

        buffer.position(p);

        return new String(result, UTF8);
    }

    byte[] getBytes(int pos, int len) {
        int p = buffer.position();

        buffer.position(pos);
        byte[] result = new byte[len];
        buffer.get(result);

        buffer.position(p);
        return result;
    }

    static class DeltaCoder {
        long value;
    }

    static class DeltaCoderInt {
        int value;
    }
}
