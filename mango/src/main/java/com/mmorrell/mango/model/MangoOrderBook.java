package com.mmorrell.mango.model;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * This class represents a Serum MangoOrderbook, that get deserialized from bytes.
 *
 * Note:
 *
 * buffer_layout_1.blob(5),
 * layout_1.accountFlagsLayout('accountFlags'),
 * slab_1.SLAB_LAYOUT.replicate('slab'),
 * buffer_layout_1.blob(7),
 *
 */
public class MangoOrderBook {

    private MangoSlab mangoSlab;
    private byte baseDecimals;
    private byte quoteDecimals;
    private long baseLotSize;
    private long quoteLotSize;

    public static MangoOrderBook readMangoOrderBook(byte[] data) {
        final MangoOrderBook mangoMangoOrderBook = new MangoOrderBook();

        final MangoSlab mangoSlab = MangoSlab.readOrderBookSlab(data);
        mangoMangoOrderBook.setSlab(mangoSlab);

        return mangoMangoOrderBook;

    }

    public ArrayList<MangoOrder> getMangoOrders() {
        if (mangoSlab == null) {
            return null;
        }

        final ArrayList<MangoOrder> mangoOrders = new ArrayList<>();

        mangoSlab.getMangoSlabNodes().forEach(slabNode -> {
            if (slabNode instanceof MangoSlabLeafNode) {
                MangoSlabLeafNode slabLeafNode = (MangoSlabLeafNode) slabNode;
                mangoOrders.add(MangoOrder.builder()
                        .price(slabLeafNode.getPrice())
                        .quantity(slabLeafNode.getQuantity())
                        .clientOrderId(slabLeafNode.getClientOrderId())
                        .floatPrice(MangoUtils.priceLotsToNumber(slabLeafNode.getPrice(), baseDecimals, quoteDecimals, baseLotSize, quoteLotSize))
                        .floatQuantity((float) ((slabLeafNode.getQuantity() * baseLotSize) / MangoUtils.getBaseSplTokenMultiplier(baseDecimals)))
                        .owner(slabLeafNode.getOwner())
                        .build()
                );
            }
        });

        return mangoOrders;


    }

    /**
     * Retrieves the top {@link MangoOrder} for bids (sorted by price descending).
     * @return
     */
    public MangoOrder getBestBid() {
        final ArrayList<MangoOrder> MangoOrders = getMangoOrders();
        MangoOrders.sort(Comparator.comparingLong(MangoOrder::getPrice).reversed());
        return MangoOrders.get(0);
    }

    public MangoOrder getBestAsk() {
        final ArrayList<MangoOrder> MangoOrders = getMangoOrders();
        MangoOrders.sort(Comparator.comparingLong(MangoOrder::getPrice));
        return MangoOrders.get(0);
    }

    public MangoSlab getSlab() {
        return mangoSlab;
    }

    public void setSlab(MangoSlab mangoSlab) {
        this.mangoSlab = mangoSlab;
    }

    public void setBaseDecimals(byte baseDecimals) {
        this.baseDecimals = baseDecimals;
    }

    public byte getBaseDecimals() {
        return baseDecimals;
    }

    public void setQuoteDecimals(byte quoteDecimals) {
        this.quoteDecimals = quoteDecimals;
    }

    public byte getQuoteDecimals() {
        return quoteDecimals;
    }


    public void setBaseLotSize(long baseLotSize) {
        this.baseLotSize = baseLotSize;
    }

    public long getBaseLotSize() {
        return baseLotSize;
    }

    public void setQuoteLotSize(long quoteLotSize) {
        this.quoteLotSize = quoteLotSize;
    }

    public long getQuoteLotSize() {
        return quoteLotSize;
    }

}
