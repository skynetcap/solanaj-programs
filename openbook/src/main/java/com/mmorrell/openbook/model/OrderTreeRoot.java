package com.mmorrell.openbook.model;

import com.mmorrell.openbook.OpenBookUtil;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * The OrderTreeRoot class represents a root node in an order tree.
 */
@Data
@Builder
public class OrderTreeRoot {

    public static final int SIZE = 4 + 4;

    private int maybeNode;
    private int leafCount;

    /**
     * Reads the order tree roots from a byte array.
     *
     * @param data     The byte array containing the order tree roots data
     * @param numRoots The number of order tree roots to read
     * @return The list of order tree roots
     */
    public static List<OrderTreeRoot> readOrderTreeRoots(byte[] data, int numRoots) {
        List<OrderTreeRoot> results = new ArrayList<>();

        int offset = 0;
        for (int i = 0; i < numRoots; i++) {
            results.add(
                    OrderTreeRoot.builder()
                            .maybeNode(OpenBookUtil.readInt32(data, offset += 4))
                            .leafCount(OpenBookUtil.readInt32(data, offset += 4))
                            .build()
            );
        }

        return results;
    }
}
