package com.mmorrell.openbook.model;

import com.mmorrell.openbook.OpenBookUtil;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class OrderTreeRoot {

    public static final int SIZE = 8 + 8;

    private int maybeNode;
    private int leafCount;

    public static List<OrderTreeRoot> readOrderTreeRoots(byte[] data, int numRoots) {
        List<OrderTreeRoot> results = new ArrayList<>();

        int offset = 0;
        for (int i = 0; i < numRoots; i++) {
            results.add(
                    OrderTreeRoot.builder()
                            .maybeNode(OpenBookUtil.readInt32(data, offset += 8))
                            .leafCount(OpenBookUtil.readInt32(data, offset += 8))
                            .build()
            );
        }

        return results;
    }
}
