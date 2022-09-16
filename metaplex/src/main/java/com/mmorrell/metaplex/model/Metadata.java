package com.mmorrell.metaplex.model;

import lombok.Builder;
import lombok.Data;
import org.p2p.solanaj.core.PublicKey;

@Data
@Builder
public class Metadata {

    private PublicKey updateAuthority;
    private PublicKey tokenMint;
    private String name;
    private String symbol;
    private String uri;

}
