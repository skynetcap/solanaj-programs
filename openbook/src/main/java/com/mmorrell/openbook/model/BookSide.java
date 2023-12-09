package com.mmorrell.openbook.model;

import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * Represents an OpenBook v2 order book, stored in the `bid` and `asks` accounts separately.
 */
@Data
@Builder
public class BookSide {

    // OrderTreeRoot x 2
    // OrderTreeRoot x 4 (reserved)
    // 256 bytes padding
    // OrderTreeNodes x 1

    private static final int NUM_ROOTS = 2;
    private static final int NUM_RESERVED_ROOTS = 4;

    private List<OrderTreeRoot> roots;
    private List<OrderTreeRoot> reservedRoots;
    private OrderTreeNodes orderTreeNodes;

    public static BookSide readBookSide(byte[] data) {
        return BookSide.builder()
                .roots(OrderTreeRoot.readOrderTreeRoots(data, NUM_ROOTS))
                .reservedRoots(
                        OrderTreeRoot.readOrderTreeRoots(
                                Arrays.copyOfRange(
                                        data,
                                        NUM_ROOTS * OrderTreeRoot.SIZE,
                                        (NUM_ROOTS * OrderTreeRoot.SIZE) + (NUM_RESERVED_ROOTS * OrderTreeRoot.SIZE)
                                ),
                                NUM_RESERVED_ROOTS
                        )
                )
                .build();
    }
}
