package com.mmorrell.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;

import java.util.ArrayList;
import java.util.List;

import static com.mmorrell.mango.model.MangoUtils.ACCOUNT_FLAGS_SIZE_BYTES;
import static org.p2p.solanaj.core.PublicKey.PUBLIC_KEY_LENGTH;

@Builder
@Getter
@Setter
@ToString
public class MarginAccount {
    // Constants
    private static final int NUM_TOKENS = 5;
    private static final int NUM_MARKETS = NUM_TOKENS - 1;

    // Offsets
    private static final int MANGO_GROUP_OFFSET = ACCOUNT_FLAGS_SIZE_BYTES;
    private static final int OWNER_OFFSET = MANGO_GROUP_OFFSET + PUBLIC_KEY_LENGTH;
    private static final int DEPOSITS_OFFSET = OWNER_OFFSET + PUBLIC_KEY_LENGTH;
    private static final int BORROWS_OFFSET = DEPOSITS_OFFSET + (U64F64.U64F64_LENGTH * NUM_TOKENS);

    private PublicKey publicKey;
    private MangoFlags accountFlags;
    private PublicKey mangoGroup;
    private PublicKey owner;
    private List<U64F64> deposits;
    private List<U64F64> borrows;

    public static MarginAccount readMarginAccount(final PublicKey publicKey, byte[] data) {
        if (data == null) {
            return MarginAccount.builder().build();
        }

        final MarginAccount marginAccount = MarginAccount.builder()
                .publicKey(publicKey)
                .build();

        byte marginAccountFlags = data[0];

        marginAccount.setAccountFlags(
                MangoFlags.builder()
                        .initialized((marginAccountFlags & 1) == 1)
                        .mangoGroup((marginAccountFlags & 2) == 2)
                        .marginAccount((marginAccountFlags & 4) == 4)
                        .mangoSrmAccount((marginAccountFlags & 8) == 8)
                        .build()
        );

        marginAccount.setMangoGroup(PublicKey.readPubkey(data, MANGO_GROUP_OFFSET));
        marginAccount.setOwner(PublicKey.readPubkey(data, OWNER_OFFSET));

        marginAccount.setDeposits(new ArrayList<>());
        for (int i = 0; i < NUM_TOKENS; i++) {
            marginAccount.getDeposits().add(
                    U64F64.readU64F64(data, DEPOSITS_OFFSET + (i * U64F64.U64F64_LENGTH))
            );
        }

        marginAccount.setBorrows(new ArrayList<>());
        for (int i = 0; i < NUM_TOKENS; i++) {
            marginAccount.getBorrows().add(
                    U64F64.readU64F64(data, BORROWS_OFFSET + (i * U64F64.U64F64_LENGTH))
            );
        }

        return marginAccount;
    }

    public void loadOpenOrders(final PublicKey dexProgramId) {
        // Load open orders
    }
}
