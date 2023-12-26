package com.mmorrell.openbook.model;

import com.mmorrell.openbook.OpenBookUtil;
import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The `BookSide` class represents a side of a book in a market. It contains information about the order tree structure and metadata for the book side.
 */
@Data
@Builder
public class BookSide {

    // OrderTreeRoot x 2
    // OrderTreeRoot x 4 (reserved)
    // 256 bytes padding
    // OrderTreeNodes x 1 (starts at offset 312)

    private static final int NUM_ROOTS = 2;
    private static final int NUM_RESERVED_ROOTS = 4;

    private List<OrderTreeRoot> roots;
    private List<OrderTreeRoot> reservedRoots;
    private OrderTreeNodes orderTreeNodes;

    // From the parent Market
    private byte baseDecimals;
    private byte quoteDecimals;
    private long baseLotSize;
    private long quoteLotSize;

    /**
     * Reads the BookSide data from a byte array.
     *
     * @param data The byte array containing the BookSide data
     * @return The BookSide object
     */
    public static BookSide readBookSide(byte[] data) {
        return BookSide.builder()
                .roots(
                        OrderTreeRoot.readOrderTreeRoots(
                                Arrays.copyOfRange(data, 8, 8 + (NUM_ROOTS * OrderTreeRoot.SIZE)),
                                NUM_ROOTS
                        )
                )
                .reservedRoots(
                        OrderTreeRoot.readOrderTreeRoots(
                                Arrays.copyOfRange(
                                        data,
                                        8 + (NUM_ROOTS * OrderTreeRoot.SIZE),
                                        8 + ((NUM_ROOTS * OrderTreeRoot.SIZE) + (NUM_RESERVED_ROOTS * OrderTreeRoot.SIZE))
                                ),
                                NUM_RESERVED_ROOTS
                        )
                )
                .orderTreeNodes(
                        OrderTreeNodes.readOrderTreeNodes(
                                Arrays.copyOfRange(
                                        data,
                                        256 + 8 + ((NUM_ROOTS * OrderTreeRoot.SIZE) + (NUM_RESERVED_ROOTS * OrderTreeRoot.SIZE)),
                                        data.length
                                )
                        )
                )
                .build();
    }


    /**
     * Retrieves the list of leaf nodes from the order tree.
     *
     * @return The list of leaf nodes
     */
    public List<LeafNode> getLeafNodes() {
        return orderTreeNodes.getNodes().stream()
                .filter(anyNode -> anyNode.getNodeTag() == NodeTag.LeafNode)
                .map(LeafNode::readLeafNode)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the list of OpenBookOrders representing the orders.
     *
     * @return The list of OpenBookOrders
     */
    public List<OpenBookOrder> getOrders() {
        return getLeafNodes().stream()
                .map(leafNode -> OpenBookOrder.builder()
                        .price(OpenBookUtil.priceLotsToNumber(leafNode.getPrice(), baseDecimals, quoteDecimals,
                                baseLotSize, quoteLotSize))
                        .size((leafNode.getQuantity() * baseLotSize) / OpenBookUtil.getBaseSplTokenMultiplier(baseDecimals))
                        .trader(leafNode.getOwner())
                        .build())
                .toList();
    }
}
