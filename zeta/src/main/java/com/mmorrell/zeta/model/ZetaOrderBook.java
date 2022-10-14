package com.mmorrell.zeta.model;

import com.mmorrell.common.SerumUtils;
import com.mmorrell.common.model.AccountFlags;
import com.mmorrell.common.model.GenericOrder;
import com.mmorrell.common.model.GenericOrderBook;
import com.mmorrell.common.model.Order;
import com.mmorrell.common.model.Slab;
import com.mmorrell.common.model.SlabLeafNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ZetaOrderBook extends GenericOrderBook {

    public ZetaOrderBook() {
        super(0.001);
    }

    private Slab slab;

    /**
     * Reads values into an existing order book object, and returns it.
     * This is a pseudo-static replacement for the static readOrderBook method, given the abstract.
     *
     * @param data order book byte data
     * @return existing order book object, updated with read values from the bytes
     */
    public static GenericOrderBook readOrderBook(byte[] data) {
        final ZetaOrderBook orderBook = new ZetaOrderBook();

        final AccountFlags accountFlags = AccountFlags.readAccountFlags(data);
        orderBook.setAccountFlags(accountFlags);

        final Slab slab = Slab.readOrderBookSlab(data);
        orderBook.setSlab(slab);

        return orderBook;
    }

    /**
     * Build's an {@link Order} {@link ArrayList} from existing data.
     *
     * @return {@link List} containing {@link Order}s built from existing the {@link ZetaOrderBook} {@link Slab}.
     */
    public List<GenericOrder> getOrders() {
        if (slab == null) {
            return null;
        }

        final ArrayList<GenericOrder> orders = new ArrayList<>();

        slab.getSlabNodes().forEach(slabNode -> {
            if (slabNode instanceof SlabLeafNode slabLeafNode) {
                orders.add(Order.builder()
                        .price(slabLeafNode.getPrice())
                        .quantity(slabLeafNode.getQuantity())
                        .clientOrderId(slabLeafNode.getClientOrderId())
                        .floatPrice(SerumUtils.priceLotsToNumber(slabLeafNode.getPrice(), this.getBaseDecimals(),
                                this.getQuoteDecimals(), this.getBaseLotSize(), this.getQuoteLotSize()))
                        .floatQuantity((float) ((slabLeafNode.getQuantity() * this.getBaseLotSize()) / SerumUtils.getBaseSplTokenMultiplier(this.getBaseDecimals())) * (float) this.getMultiplier())
                        .owner(slabLeafNode.getOwner())
                        .build()
                );
            }
        });

        return orders;
    }

}
