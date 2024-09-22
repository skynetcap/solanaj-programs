package com.mmorrell.jupiter.model;

import com.mmorrell.jupiter.util.JupiterUtil;
import lombok.Builder;
import lombok.Data;
import org.p2p.solanaj.core.PublicKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Jupiter Perpetuals account in Jupiter Perpetuals.
 */
@Data
@Builder
public class JupiterPerpetuals {
    private Permissions permissions;
    private List<PublicKey> pools;
    private PublicKey admin;
    private byte transferAuthorityBump;
    private byte perpetualsBump;
    private long inceptionTime;

    @Data
    @Builder
    public static class Permissions {
        private boolean allowSwap;
        private boolean allowAddLiquidity;
        private boolean allowRemoveLiquidity;
        private boolean allowOpenPosition;
        private boolean allowClosePosition;
        private boolean allowPnlWithdrawal;
        private boolean allowCollateralWithdrawal;
        private boolean allowSizeChange;
    }

    /**
     * Deserializes a byte array into a JupiterPerpetuals object.
     *
     * @param data the byte array representing the account data.
     * @return a JupiterPerpetuals object.
     */
    public static JupiterPerpetuals fromByteArray(byte[] data) {
        int offset = 8; // Skip discriminator

        Permissions permissions = readPermissions(data, offset);
        offset += 8; // Adjust based on actual size

        List<PublicKey> pools = readPublicKeyList(data, offset);
        offset += 4 + (pools.size() * 32);

        PublicKey admin = PublicKey.readPubkey(data, offset);
        offset += 32;

        byte transferAuthorityBump = data[offset++];
        byte perpetualsBump = data[offset++];

        long inceptionTime = JupiterUtil.readInt64(data, offset);

        return JupiterPerpetuals.builder()
                .permissions(permissions)
                .pools(pools)
                .admin(admin)
                .transferAuthorityBump(transferAuthorityBump)
                .perpetualsBump(perpetualsBump)
                .inceptionTime(inceptionTime)
                .build();
    }

    // Add private static methods to read Permissions and PublicKey list
    private static Permissions readPermissions(byte[] data, int offset) {
        return Permissions.builder()
                .allowSwap(data[offset++] != 0)
                .allowAddLiquidity(data[offset++] != 0)
                .allowRemoveLiquidity(data[offset++] != 0)
                .allowOpenPosition(data[offset++] != 0)
                .allowClosePosition(data[offset++] != 0)
                .allowPnlWithdrawal(data[offset++] != 0)
                .allowCollateralWithdrawal(data[offset++] != 0)
                .allowSizeChange(data[offset] != 0)
                .build();
    }

    private static List<PublicKey> readPublicKeyList(byte[] data, int offset) {
        List<PublicKey> pools = new ArrayList<>();
        int poolsLength = JupiterUtil.readUint32(data, offset);
        offset += 4;
        for (int i = 0; i < poolsLength; i++) {
            pools.add(PublicKey.readPubkey(data, offset));
            offset += 32;
        }
        return pools;
    }
}