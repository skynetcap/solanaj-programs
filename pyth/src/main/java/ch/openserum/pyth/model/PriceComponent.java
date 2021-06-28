package ch.openserum.pyth.model;

import lombok.*;
import org.p2p.solanaj.core.PublicKey;

@Builder
@Getter
@Setter
@ToString
public class PriceComponent {

    private PublicKey publisher;
    private PriceInfo aggregate;
    private PriceInfo latest;

}
