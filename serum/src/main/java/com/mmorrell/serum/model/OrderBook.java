package com.mmorrell.serum.model;

import com.mmorrell.common.model.GenericOrder;
import com.mmorrell.common.model.GenericOrderBook;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class represents a Serum orderbook, that get deserialized from bytes.
 *
 * Note:
 *
 * buffer_layout_1.blob(5),
 * layout_1.accountFlagsLayout('accountFlags'),
 * slab_1.SLAB_LAYOUT.replicate('slab'),
 * buffer_layout_1.blob(7),
 *
 */
public class OrderBook extends GenericOrderBook {

    private AccountFlags accountFlags;
    private Slab slab;
    private byte baseDecimals;
    private byte quoteDecimals;
    private long baseLotSize;
    private long quoteLotSize;

    /**
     * Reads values into an existing order book object, and returns it.
     * This is a pseudo-static replacement for the static readOrderBook method, given the abstract.
     *
     * @param data order book byte data
     * @return existing order book object, updated with read values from the bytes
     */
    public GenericOrderBook readOrderBook(byte[] data) {
        final AccountFlags accountFlags = AccountFlags.readAccountFlags(data);
        this.setAccountFlags(accountFlags);

        final Slab slab = Slab.readOrderBookSlab(data);
        this.setSlab(slab);

        return this;
    }

    /**
     * Build's an {@link Order} {@link ArrayList} from existing data.
     *
     * @return {@link List} containing {@link Order}s built from existing the {@link OrderBook} {@link Slab}.
     */
    public List<GenericOrder> getOrders() {
        if (slab == null) {
            return null;
        }

        final ArrayList<Order> orders = new ArrayList<>();

        slab.getSlabNodes().forEach(slabNode -> {
            if (slabNode instanceof SlabLeafNode) {
                SlabLeafNode slabLeafNode = (SlabLeafNode) slabNode;
                orders.add(Order.builder()
                        .price(slabLeafNode.getPrice())
                        .quantity(slabLeafNode.getQuantity())
                        .clientOrderId(slabLeafNode.getClientOrderId())
                        .floatPrice(SerumUtils.priceLotsToNumber(slabLeafNode.getPrice(), baseDecimals, quoteDecimals, baseLotSize, quoteLotSize))
                        .floatQuantity((float) ((slabLeafNode.getQuantity() * baseLotSize) / SerumUtils.getBaseSplTokenMultiplier(baseDecimals)))
                        .owner(slabLeafNode.getOwner())
                        .build()
                );
            }
        });

        return orders;
    }

    /**
     * Retrieves the top {@link Order} for bids (sorted by price descending).
     * @return
     */
    public Order getBestBid() {
        final ArrayList<Order> orders = getOrders();
        orders.sort(Comparator.comparingLong(Order::getPrice).reversed());
        return orders.get(0);
    }

    public Order getBestAsk() {
        final ArrayList<Order> orders = getOrders();
        orders.sort(Comparator.comparingLong(Order::getPrice));
        return orders.get(0);
    }

    public Slab getSlab() {
        return slab;
    }

    public void setSlab(Slab slab) {
        this.slab = slab;
    }

    public AccountFlags getAccountFlags() {
        return accountFlags;
    }

    public void setAccountFlags(AccountFlags accountFlags) {
        this.accountFlags = accountFlags;
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
