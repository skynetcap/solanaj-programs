package com.mmorrell.openbook.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
public class AnyNode {

    private byte tag;
    private byte[] data; // 87 bytes

    public static List<AnyNode> readAnyNodes(byte[] data) {
        List<AnyNode> nodes = new ArrayList<>();

        int offset = 0;
        for (int i = 0; i < 1024; i++) {
            byte newTag = data[offset];
            offset += 1;

            byte[] newData = Arrays.copyOfRange(data, offset, offset + 87);
            offset += 87;

            nodes.add(
                    AnyNode.builder()
                            .tag(newTag)
                            .data(newData)
                            .build()
            );
        }

        return nodes;
    }

}
