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
import java.util.Arrays;
import java.util.logging.Logger;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class U64F64 {
    private byte[] data;

    public float decode() {
        //Logger.getAnonymousLogger().info("Data = " + Arrays.toString(data));
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < 16; i++) {
            buffer.put(data[16 - i - 1]);
        }

        //Logger.getAnonymousLogger().info("Buffer = " + Arrays.toString(buffer.array()));

        // divisor
        double divisor = Math.pow(2, 64);

        String newString = ByteUtils.bytesToHex(buffer.array());

        //Logger.getAnonymousLogger().info("newString = " + newString);

        BigInteger result = new BigInteger(newString, 16);
        //System.out.println("bigInt Result = "  + result);

        BigDecimal divided = new BigDecimal(result).divide(new BigDecimal(divisor));

        Logger.getAnonymousLogger().info("deposit = "  + divided);

        return 0.0f;
    }

    private BigDecimal bytesToBigDecimal(byte[] buffer) {
        String string = new String(buffer);
        return new BigDecimal(string);
    }
}
