package com.mmorrell.jupiter.model;

import com.mmorrell.jupiter.util.JupiterUtil;
import lombok.Builder;
import lombok.Data;
import org.p2p.solanaj.core.PublicKey;

/**
 * Represents a Jupiter Perpetuals account in Jupiter Perpetuals.
 */
@Data
@Builder
public class JupiterPerpetuals {
    private Permissions permissions;
    private PublicKey pool; // Changed from List<PublicKey> to PublicKey
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
        private boolean allowIncreasePosition; // New field
        private boolean allowDecreasePosition; // New field
        private boolean allowCollateralWithdrawal;
        private boolean allowLiquidatePosition; // New field
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

        // Hardcoded offsets based on research
        offset = 19; // Set offset to the found pool offset
        PublicKey pool = PublicKey.readPubkey(data, offset);
        offset += 32; // Move to the next field

        offset = 51; // Set offset to the found admin offset
        PublicKey admin = PublicKey.readPubkey(data, offset);
        offset += 32;

        byte transferAuthorityBump = data[offset++];
        byte perpetualsBump = data[offset++];

        long inceptionTime = JupiterUtil.readInt64(data, offset);

        return JupiterPerpetuals.builder()
                .permissions(permissions)
                .pool(pool) // Set the single pool PublicKey
                .admin(admin)
                .transferAuthorityBump(transferAuthorityBump)
                .perpetualsBump(perpetualsBump)
                .inceptionTime(inceptionTime)
                .build();
    }

    // Add private static methods to read Permissions and PublicKey
    private static Permissions readPermissions(byte[] data, int offset) {
        return Permissions.builder()
                .allowSwap(data[offset++] != 0)
                .allowAddLiquidity(data[offset++] != 0)
                .allowRemoveLiquidity(data[offset++] != 0)
                .allowIncreasePosition(data[offset++] != 0) // New field
                .allowDecreasePosition(data[offset++] != 0) // New field
                .allowCollateralWithdrawal(data[offset++] != 0)
                .allowLiquidatePosition(data[offset] != 0) // New field
                .build();
    }
}