package com.mmorrell.mango.model;

import lombok.Data;

@Data
public abstract class MangoSlabNode {

    public MangoSlabNode() {
    }

    // first 4 bytes
    private int tag;
    // bytes 5-72
    private byte[] blob;

    // TODO add getters for variants of the blob or make this an interface

    /**
     * returns the variant of this slabnode. 5 possible values [uninitialized (0), innerNode(1), leafNode(2), freeNode(3), lastFreeNode(4));
     * @return variant of the slabNode
     */
}
