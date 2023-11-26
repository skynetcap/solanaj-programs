package com.mmorrell.phoenix.model;

import lombok.Builder;
import lombok.Data;
import org.p2p.solanaj.core.PublicKey;

@Data
@Builder
public class PhoenixOrder {

    private double price;
    private double size;
    private PublicKey trader;

}
