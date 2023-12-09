package com.mmorrell.openbook.model;

import lombok.Getter;

@Getter
public enum NodeTag {
    Uninitialized((byte) 0), InnerNode((byte) 1), LeafNode((byte) 2), FreeNode((byte) 3), LastFreeNode((byte) 4);

    private final byte tag;

    NodeTag(byte tag) {
        this.tag = tag;
    }

    public static NodeTag getNodeTag(byte tag) {
        for (NodeTag node : NodeTag.values()) {
            if (node.getTag() == tag) {
                return node;
            }
        }

        return Uninitialized;
    }
}
