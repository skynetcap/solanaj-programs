package com.mmorrell.openbook.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AnyNode class represents a node in a data structure.
 */
@Data
@Builder
public class AnyNode {

    private byte tag;
    private byte[] data; // 87 bytes
    private NodeTag nodeTag;

    /**
     * Reads the AnyNode objects from the given byte array data.
     *
     * @param data the byte array from which to read the AnyNode objects
     * @return a list of AnyNode objects read from the data
     */
    public static List<AnyNode> readAnyNodes(byte[] data) {
        List<AnyNode> nodes = new ArrayList<>(1024);

        int offset = 0;
        for (int i = 0; i < 1024; i++) {
            byte newTag = data[offset];
            offset += 1;

            byte[] newData = Arrays.copyOfRange(data, offset, offset + 87);
            offset += 87;

            nodes.add(
                    AnyNode.builder()
                            .tag(newTag)
                            .nodeTag(NodeTag.getNodeTag(newTag))
                            .data(newData)
                            .build()
            );
        }

        return nodes;
    }

}
