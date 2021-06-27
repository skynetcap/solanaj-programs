package ch.openserum.pyth.model;

import ch.openserum.pyth.utils.PythUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;

@Builder
@Getter
@Setter
@ToString
public class MappingAccount {

    private static final int MAGIC_NUMBER_OFFSET = 0;
    private static final int VERSION_OFFSET = MAGIC_NUMBER_OFFSET + PythUtils.INT32_SIZE;
    private static final int TYPE_OFFSET = VERSION_OFFSET + PythUtils.INT32_SIZE;
    private static final int SIZE_OFFSET = TYPE_OFFSET + PythUtils.INT32_SIZE;
    private static final int NUM_PRODUCTS_OFFSET = SIZE_OFFSET + PythUtils.INT32_SIZE;
    private static final int NEXT_MAPPING_ACCOUNT_OFFSET = NUM_PRODUCTS_OFFSET + (2 * PythUtils.INT32_SIZE);

    private int magicNumber;
    private int version;
    private int type;
    private int size;
    private int numProducts;
    private PublicKey nextMappingAccount;

    public static MappingAccount readMappingAccount(byte[] data) {
        final MappingAccount mappingAccount = MappingAccount.builder()
                .magicNumber(PythUtils.readInt32(data, MAGIC_NUMBER_OFFSET))
                .version(PythUtils.readInt32(data, VERSION_OFFSET))
                .type(PythUtils.readInt32(data, TYPE_OFFSET))
                .size(PythUtils.readInt32(data, SIZE_OFFSET))
                .numProducts(PythUtils.readInt32(data, NUM_PRODUCTS_OFFSET))
                .build();

        final PublicKey nextMappingAccount = PublicKey.readPubkey(data, NEXT_MAPPING_ACCOUNT_OFFSET);
        if (!nextMappingAccount.toBase58().equalsIgnoreCase("11111111111111111111111111111111")){
            mappingAccount.setNextMappingAccount(nextMappingAccount);
        }

        return mappingAccount;
    }

}
