package com.mmorrell.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.p2p.solanaj.core.PublicKey;

/**
 * Class that represents a Serum order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericOrder {

    private long price;
    private long quantity;
    private long clientOrderId;
    private float floatPrice;
    private float floatQuantity;
    private PublicKey owner;

    // used in newOrderv3. no constructor, only setters/getters
    private long maxQuoteQuantity;
    private long clientId;
    private boolean buy;

}
