package com.mmorrell.mango.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.p2p.solanaj.core.PublicKey;

/**
 * Class that represents a Serum order.
 */
@Data
@Builder
public class MangoOrder {

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
