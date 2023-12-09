package com.mmorrell.openbook.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderTreeNodes {

    public static OrderTreeNodes readOrderTreeNodes(byte[] data) {
        OrderTreeNodes orderTreeNodes = new OrderTreeNodes();

        return orderTreeNodes;
    }
}
