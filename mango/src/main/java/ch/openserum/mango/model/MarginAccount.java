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

    private PublicKey publicKey;
    private MangoAccountFlags accountFlags;

    public static MarginAccount readMarginAccount(final PublicKey publicKey, byte[] data) {
        if (data == null) {
            return MarginAccount.builder().build();
        }

        final MarginAccount marginAccount = MarginAccount.builder()
                .publicKey(publicKey)
                .build();

        byte marginAccountFlags = data[0];

        marginAccount.setAccountFlags(
                MangoAccountFlags.builder()
                        .initialized((marginAccountFlags & 1) == 1)
                        .mangoGroup((marginAccountFlags & 2) == 2)
                        .marginAccount((marginAccountFlags & 4) == 4)
                        .mangoSrmAccount((marginAccountFlags & 8) == 8)
                        .build()
        );

        return marginAccount;
    }

    public void loadOpenOrders(final PublicKey dexProgramId) {
        // Load open orders
    }
}
