package com.mmorrell.mango.model;

import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class MangoSlab {

    private static final int INT32_SIZE = 4;

    private static final int BUMP_INDEX_OFFSET = 8;
    private static final int FREE_LIST_LEN_OFFSET = 16;
    private static final int FREE_LIST_HEAD_OFFSET = 24;
    private static final int ROOT_OFFSET = 28;
    private static final int LEAF_COUNT_OFFSET = 32;
    private static final int SLAB_NODE_OFFSET = 40;

    private int bumpIndex;
    private int freeListLen;
    private int freeListHead;
    private int root;
    private int leafCount;
    private ArrayList<MangoSlabNode> mangoSlabNodes;

    public static MangoSlab readOrderBookSlab(byte[] data) {
        final MangoSlab mangoSlab = new MangoSlab();

        int bumpIndex = mangoSlab.readBumpIndex(data);
        mangoSlab.setBumpIndex(bumpIndex);

        int freeListLen = mangoSlab.readFreeListLen(data);
        mangoSlab.setFreeListLen(freeListLen);

        int freeListHead = mangoSlab.readFreeListHead(data);
        mangoSlab.setFreeListHead(freeListHead);

        int root = mangoSlab.readRoot(data);
        mangoSlab.setRoot(root);

        int leafCount = mangoSlab.readLeafcount(data);
        mangoSlab.setLeafCount(leafCount);

        ArrayList<MangoSlabNode> mangoSlabNodes;
        byte[] mangoSlabNodeBytes = ByteUtils.readBytes(data, SLAB_NODE_OFFSET, 1024 * 88);

        mangoSlabNodes = mangoSlab.readMangoSlabNodes(mangoSlabNodeBytes, bumpIndex);
        mangoSlab.setMangoSlabNodes(mangoSlabNodes);

        return mangoSlab;
    }

    /**
     * [tag 4 bytes][blob 84 bytes]
     * repeated for N times
     *
     * @param data
     * @return
     */
    private ArrayList<MangoSlabNode> readMangoSlabNodes(byte[] data, int bumpIndex) {
        ArrayList<MangoSlabNode> MangoSlabNodes = new ArrayList<>();

        for (int i = 0; i < bumpIndex; i++) {
            MangoSlabNodes.add(readMangoSlabNode(ByteUtils.readBytes(data, (88 * i), 88)));
        }

        // 88 instead of 72 length for mango
        return MangoSlabNodes;
    }

    public MangoSlabNode readMangoSlabNode(byte[] data) {
        int tag = readInt32(ByteUtils.readBytes(data, 0, INT32_SIZE));
        byte[] blobData = ByteUtils.readBytes(data, 4, 84);
        MangoSlabNode mangoSlabNode;

        if (tag == 0) {
            mangoSlabNode = null;
        } else if (tag == 1) {
            int prefixLen = readInt32(ByteUtils.readBytes(blobData, 0, INT32_SIZE));
            // Only the first prefixLen high-order bits of key are meaningful
            int numBytesToRead = (int) Math.ceil(prefixLen / 4.00);
            byte[] key = ByteUtils.readBytes(blobData, 4, numBytesToRead);
            int child1 = readInt32(ByteUtils.readBytes(blobData, 20, 4));
            int child2 = readInt32(ByteUtils.readBytes(blobData, 24, 4));
            mangoSlabNode = new MangoSlabInnerNode(prefixLen, key, child1, child2);
        } else if (tag == 2) {
            byte ownerSlot = ByteUtils.readBytes(blobData, 0, 1)[0];
            byte orderType = ByteUtils.readBytes(blobData, 1, 1)[0];
            byte version = ByteUtils.readBytes(blobData, 2, 1)[0];
            byte timeInForce = ByteUtils.readBytes(blobData, 3, 1)[0];

            // "(price, seqNum)"
            // key starts at byte 4, u128. u128 = 128 bits = 16 * 8
            byte[] key = ByteUtils.readBytes(blobData, 4, 16);
            long seqNum = Utils.readInt64(key, 0); // unused?
            long price = Utils.readInt64(key, 8);
            PublicKey owner = PublicKey.readPubkey(blobData, 20);
            long quantity = Utils.readInt64(blobData, 52);
            long clientOrderId = Utils.readInt64(blobData, 60);
            long bestInitial = Utils.readInt64(blobData, 68);
            long timeStamp = Utils.readInt64(blobData, 76);

            mangoSlabNode = new MangoSlabLeafNode(ownerSlot, orderType, version, timeInForce, key, owner, quantity,
                    clientOrderId, price, bestInitial, timeStamp);
        } else if (tag == 3) {
            // int next = readInt32(ByteUtils.readBytes(blobData, 0, 4));
            mangoSlabNode = new MangoSlabInnerNode();
        } else if (tag == 4) {
            mangoSlabNode = null;
        } else {
            throw new RuntimeException("unknown tag detected during slab deserialization = " + tag);
        }

        return mangoSlabNode;
    }

    private String getTagType(int tag) {
        if (tag == 1) {
            return "innerNode";
        } else {
            return "unknown";
        }
    }

    public int getLeafCount() {
        return leafCount;
    }

    public void setLeafCount(int leafCount) {
        this.leafCount = leafCount;
    }

    private int readLeafcount(byte[] data) {
        final byte[] bumpIndexBytes = ByteUtils.readBytes(data, LEAF_COUNT_OFFSET, INT32_SIZE);

        return readInt32(bumpIndexBytes);
    }

    public int getRoot() {
        return root;
    }

    public void setRoot(int root) {
        this.root = root;
    }

    private int readRoot(byte[] data) {
        final byte[] bumpIndexBytes = ByteUtils.readBytes(data, ROOT_OFFSET, INT32_SIZE);

        return readInt32(bumpIndexBytes);
    }

    public int getFreeListHead() {
        return freeListHead;
    }

    public void setFreeListHead(int freeListHead) {
        this.freeListHead = freeListHead;
    }

    private int readFreeListHead(byte[] data) {
        final byte[] bumpIndexBytes = ByteUtils.readBytes(data, FREE_LIST_HEAD_OFFSET, INT32_SIZE);

        return readInt32(bumpIndexBytes);
    }

    private int readFreeListLen(byte[] data) {
        final byte[] bumpIndexBytes = ByteUtils.readBytes(data, FREE_LIST_LEN_OFFSET, INT32_SIZE);

        return readInt32(bumpIndexBytes);
    }

    public int getFreeListLen() {
        return freeListLen;
    }

    public void setFreeListLen(int freeListLen) {
        this.freeListLen = freeListLen;
    }

    public int getBumpIndex() {
        return bumpIndex;
    }

    public void setBumpIndex(int bumpIndex) {
        this.bumpIndex = bumpIndex;
    }

    private int readBumpIndex(byte[] data) {
        final byte[] bumpIndexBytes = ByteUtils.readBytes(data, BUMP_INDEX_OFFSET, INT32_SIZE);

        return readInt32(bumpIndexBytes);
    }

    public int readInt32(byte[] data) {
        // convert 4 bytes into an int.

        //  create a byte buffer and wrap the array
        ByteBuffer bb = ByteBuffer.wrap(data);

        //  if the file uses little endian as apposed to network
        //  (big endian, Java's native) format,
        //  then set the byte order of the ByteBuffer
        bb.order(ByteOrder.LITTLE_ENDIAN);

        //  read your integers using ByteBuffer's getInt().
        //  four bytes converted into an integer!
        return bb.getInt(0);
    }

    public ArrayList<MangoSlabNode> getMangoSlabNodes() {
        return mangoSlabNodes;
    }

    public void setMangoSlabNodes(ArrayList<MangoSlabNode> mangoSlabNodes) {
        this.mangoSlabNodes = mangoSlabNodes;
    }
}
