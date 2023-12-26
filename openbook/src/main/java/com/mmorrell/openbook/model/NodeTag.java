package com.mmorrell.openbook.model;

import lombok.Getter;

/**
 * NodeTag is an enumeration that represents tags for different types of nodes in a data structure.
 * Each tag has a corresponding byte value.
 */
@Getter
public enum NodeTag {
    Uninitialized((byte) 0), InnerNode((byte) 1), LeafNode((byte) 2), FreeNode((byte) 3), LastFreeNode((byte) 4);

    private final byte tag;

    NodeTag(byte tag) {
        this.tag = tag;
    }

    /**
     * Retrieves the NodeTag associated with the given tag value.
     *
     * @param tag the byte tag value
     * @return the NodeTag associated with the given tag value
     */
    public static NodeTag getNodeTag(byte tag) {
        for (NodeTag node : NodeTag.values()) {
            if (node.getTag() == tag) {
                return node;
            }
        }

        return Uninitialized;
    }
}
