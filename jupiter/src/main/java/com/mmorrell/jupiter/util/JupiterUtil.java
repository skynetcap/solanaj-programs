package com.mmorrell.jupiter.util;

import org.p2p.solanaj.core.PublicKey;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class JupiterUtil {
    public static int readUint32(byte[] data, int offset) {
        return (data[offset] & 0xFF) |
                ((data[offset + 1] & 0xFF) << 8) |
                ((data[offset + 2] & 0xFF) << 16) |
                ((data[offset + 3] & 0xFF) << 24);
    }

    public static long readUint64(byte[] data, int offset) {
        return Long.parseUnsignedLong(Long.toUnsignedString(org.bitcoinj.core.Utils.readInt64(data, offset)));
    }

    public static long readInt64(byte[] data, int offset) {
        return org.bitcoinj.core.Utils.readInt64(data, offset);
    }

    public static Long readOptionalUint64(byte[] data, int offset) {
        boolean hasValue = data[offset] != 0;
        return hasValue ? readUint64(data, offset + 1) : null;
    }

    public static Boolean readOptionalBoolean(byte[] data, int offset) {
        boolean hasValue = data[offset] != 0;
        return hasValue ? data[offset + 1] != 0 : null;
    }

    public static PublicKey readOptionalPublicKey(byte[] data, int offset) {
        boolean hasValue = data[offset] != 0;
        return hasValue ? PublicKey.readPubkey(data, offset + 1) : null;
    }

    /**
     * Calculates the account discriminator for a given account name.
     *
     * @param accountName the name of the account.
     * @return the first 8 bytes of the SHA-256 hash of "account:<accountName>".
     */
    public static byte[] getAccountDiscriminator(String accountName) {
        String preimage = "account:" + accountName;
        try {
            MessageDigest hasher = MessageDigest.getInstance("SHA-256");
            hasher.update(preimage.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOfRange(hasher.digest(), 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found for discriminator calculation.");
        }
    }
}