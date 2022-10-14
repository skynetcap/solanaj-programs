package com.mmorrell.serum.model;

import com.mmorrell.common.model.GenericOrder;
import lombok.Getter;
import lombok.Setter;

/**
 * Class that represents a Serum order.
 */
@Getter
@Setter
public class Order extends GenericOrder {

    private OrderTypeLayout orderTypeLayout;
    private SelfTradeBehaviorLayout selfTradeBehaviorLayout;

}
