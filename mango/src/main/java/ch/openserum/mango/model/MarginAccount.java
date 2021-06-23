package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;

@Builder
@Getter
@Setter
@ToString
public class MarginAccount {
    private MangoAccountFlags accountFlags;

    public static MarginAccount readMarginAccount(byte[] data) {
        if (data == null) {
            return null;
        }

        return MarginAccount.builder().build();
    }

    public void loadOpenOrders(final PublicKey dexProgramId) {
        // Load open orders
    }
}
