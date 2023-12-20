package com.mmorrell.openbook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.core.PublicKey;

@Data
@Slf4j
@AllArgsConstructor
@Builder
public class OpenBookOrder {

    private double price;
    private double size;
    private PublicKey trader;

}
