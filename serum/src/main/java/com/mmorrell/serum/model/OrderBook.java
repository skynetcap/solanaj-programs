package com.mmorrell.serum.model;

import com.mmorrell.common.model.AccountFlags;
import com.mmorrell.common.model.GenericOrder;
import com.mmorrell.common.model.GenericOrderBook;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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
@Getter
@Setter
public class OrderBook extends GenericOrderBook {

    private Slab slab;

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

        final ArrayList<GenericOrder> orders = new ArrayList<>();

        slab.getSlabNodes().forEach(slabNode -> {
            if (slabNode instanceof SlabLeafNode) {
                SlabLeafNode slabLeafNode = (SlabLeafNode) slabNode;
                orders.add(Order.builder()
                        .price(slabLeafNode.getPrice())
                        .quantity(slabLeafNode.getQuantity())
                        .clientOrderId(slabLeafNode.getClientOrderId())
                        .floatPrice(SerumUtils.priceLotsToNumber(slabLeafNode.getPrice(), this.getBaseDecimals(),
                                this.getQuoteDecimals(), this.getBaseLotSize(), this.getQuoteLotSize()))
                        .floatQuantity((float) ((slabLeafNode.getQuantity() * this.getBaseLotSize()) / SerumUtils.getBaseSplTokenMultiplier(this.getBaseDecimals())))
                        .owner(slabLeafNode.getOwner())
                        .build()
                );
            }
        });

        return orders;
    }

}
