package com.mmorrell.serum.model;

import com.mmorrell.common.model.GenericOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.p2p.solanaj.core.PublicKey;

/**
 * Class that represents a Serum order.
 */
@Getter
@Setter
public class Order extends GenericOrder {

    private OrderTypeLayout orderTypeLayout;
    private SelfTradeBehaviorLayout selfTradeBehaviorLayout;

    @Builder
    public Order(long price, long quantity, long clientOrderId, float floatPrice, float floatQuantity, PublicKey owner, long maxQuoteQuantity, long clientId, boolean buy, OrderTypeLayout orderTypeLayout, SelfTradeBehaviorLayout selfTradeBehaviorLayout) {
        super(price, quantity, clientOrderId, floatPrice, floatQuantity, owner, maxQuoteQuantity, clientId, buy);
        this.orderTypeLayout = orderTypeLayout;
        this.selfTradeBehaviorLayout = selfTradeBehaviorLayout;
    }
}
