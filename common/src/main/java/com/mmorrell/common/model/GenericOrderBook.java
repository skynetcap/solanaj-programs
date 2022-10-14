package com.mmorrell.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public abstract class GenericOrderBook {

    private AccountFlags accountFlags;
    private byte baseDecimals;
    private byte quoteDecimals;
    private long baseLotSize;
    private long quoteLotSize;
    private double multiplier;

    public abstract List<GenericOrder> getOrders();

    public GenericOrder getBestBid() {
        final List<GenericOrder> orders = getOrders();
        orders.sort(Comparator.comparingLong(GenericOrder::getPrice).reversed());
        return orders.get(0);
    }

    public GenericOrder getBestAsk() {
        final List<GenericOrder> orders = getOrders();
        orders.sort(Comparator.comparingLong(GenericOrder::getPrice));
        return orders.get(0);
    }

}
