package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;

import static ch.openserum.mango.model.MangoUtils.ACCOUNT_FLAGS_SIZE_BYTES;
import static org.p2p.solanaj.core.PublicKey.PUBLIC_KEY_LENGTH;

@Builder
@Getter
@Setter
@ToString
public class MarginAccount {

    // Offsets
    private static final int MANGO_GROUP_OFFSET = ACCOUNT_FLAGS_SIZE_BYTES;
    private static final int OWNER_OFFSET = MANGO_GROUP_OFFSET + PUBLIC_KEY_LENGTH;

    private PublicKey publicKey;
    private MangoAccountFlags accountFlags;
    private PublicKey mangoGroup;
    private PublicKey owner;

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

        marginAccount.setMangoGroup(PublicKey.readPubkey(data, MANGO_GROUP_OFFSET));
        marginAccount.setOwner(PublicKey.readPubkey(data, OWNER_OFFSET));

        return marginAccount;
    }

    public void loadOpenOrders(final PublicKey dexProgramId) {
        // Load open orders
    }
}
