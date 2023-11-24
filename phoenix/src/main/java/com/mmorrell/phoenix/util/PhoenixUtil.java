package com.mmorrell.phoenix.util;

import com.mmorrell.phoenix.program.PhoenixProgram;
import org.bitcoinj.core.Base58;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class PhoenixUtil {

    public static int readInt32(byte[] data, int offset) {
        // convert 4 bytes into an int.
        // create a byte buffer and wrap the array
        ByteBuffer bb = ByteBuffer.wrap(
                Arrays.copyOfRange(
                        data,
                        offset,
                        offset + 4
                )
        );

        // if the file uses little endian as apposed to network
        // (big endian, Java's native) format,
        // then set the byte order of the ByteBuffer
        bb.order(ByteOrder.LITTLE_ENDIAN);

        // read your integers using ByteBuffer's getInt().
        // four bytes converted into an integer!
        return bb.getInt(0);
    }

    public static String getDiscriminator(String input) {
        Keccak keccak = new Keccak(256);
        keccak.update(PhoenixProgram.PHOENIX_PROGRAM_ID.toByteArray());
        keccak.update(input.getBytes());

        ByteBuffer keccakBuffer = keccak.digest();
        keccakBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] keccakBytes = keccakBuffer.array();

        return Base58.encode(Arrays.copyOfRange(keccakBytes, 0, 8));
    }
}