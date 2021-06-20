package ch.openserum.mango.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.p2p.solanaj.utils.ByteUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class U64F64 {
    public static final int U64F64_LENGTH = 16;

    private byte[] data;

    public float decode() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < 16; i++) {
            buffer.put(data[16 - i - 1]);
        }

        // divisor
        double divisor = Math.pow(2, 64);
        String newString = ByteUtils.bytesToHex(buffer.array());
        BigInteger result = new BigInteger(newString, 16);
        BigDecimal divided = new BigDecimal(result).divide(new BigDecimal(divisor));

        return divided.floatValue();
    }
}
