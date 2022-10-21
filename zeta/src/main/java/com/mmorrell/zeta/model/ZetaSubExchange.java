package com.mmorrell.zeta.model;

import lombok.Builder;
import lombok.Data;
import org.p2p.solanaj.core.PublicKey;

@Data
@Builder
public class ZetaSubExchange {

    private PublicKey zetaGroupPubkey;
    private ZetaGroup zetaGroup;

}
