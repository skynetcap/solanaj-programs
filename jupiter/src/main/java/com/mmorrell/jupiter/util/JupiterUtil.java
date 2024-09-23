package com.mmorrell.jupiter.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.p2p.solanaj.core.PublicKey;

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

    public static byte[] getInstructionDiscriminator(String instructionName) {
        try {
            String discriminatorString = "global:" + instructionName;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(discriminatorString.getBytes());
            byte[] discriminator = new byte[8];
            System.arraycopy(hash, 0, discriminator, 0, 8);
            return discriminator;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}