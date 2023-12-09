package com.mmorrell.openbook.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mmorrell.openbook.OpenBookUtil;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderTreeNodes {

    private byte orderTreeType;
    // 3 bytes padding
    private int bumpIndex;
    private int freeListLen;
    private int freeListHead;
    // 512 bytes padding
    // AnyNode x 1024
    private List<AnyNode> nodes;

    public static OrderTreeNodes readOrderTreeNodes(byte[] data) {

        return OrderTreeNodes.builder()
                .orderTreeType(data[0])
                .bumpIndex(OpenBookUtil.readInt32(data, 4))
                .freeListLen(OpenBookUtil.readInt32(data, 8))
                .freeListHead(OpenBookUtil.readInt32(data, 12))
                .nodes(
                        AnyNode.readAnyNodes(
                                Arrays.copyOfRange(data, 16 + 512, data.length)
                        )
                )
                .build();
    }
}
