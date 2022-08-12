package com.mmorrell.pyth.model;

import com.mmorrell.pyth.utils.PythUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
/**
 * Represents a Pyth mapping account
 */
public class MappingAccount {

    private static final int MAGIC_NUMBER_OFFSET = 0;
    private static final int VERSION_OFFSET = MAGIC_NUMBER_OFFSET + PythUtils.INT32_SIZE;
    private static final int TYPE_OFFSET = VERSION_OFFSET + PythUtils.INT32_SIZE;
    private static final int SIZE_OFFSET = TYPE_OFFSET + PythUtils.INT32_SIZE;
    private static final int NUM_PRODUCTS_OFFSET = SIZE_OFFSET + PythUtils.INT32_SIZE;
    private static final int NEXT_MAPPING_ACCOUNT_OFFSET = NUM_PRODUCTS_OFFSET + (2 * PythUtils.INT32_SIZE);
    private static final int PRODUCT_ACCOUNT_KEYS_OFFSET = NEXT_MAPPING_ACCOUNT_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;

    private int magicNumber;
    private int version;
    private int type;
    private int size;
    private int numProducts;
    private PublicKey nextMappingAccount;
    private List<PublicKey> productAccountKeys;

    public static MappingAccount readMappingAccount(byte[] data) {
        final MappingAccount mappingAccount = MappingAccount.builder()
                .magicNumber(PythUtils.readInt32(data, MAGIC_NUMBER_OFFSET))
                .version(PythUtils.readInt32(data, VERSION_OFFSET))
                .type(PythUtils.readInt32(data, TYPE_OFFSET))
                .size(PythUtils.readInt32(data, SIZE_OFFSET))
                .numProducts(PythUtils.readInt32(data, NUM_PRODUCTS_OFFSET))
                .build();

        final PublicKey nextMappingAccount = PublicKey.readPubkey(data, NEXT_MAPPING_ACCOUNT_OFFSET);
        if (!nextMappingAccount.toBase58().equalsIgnoreCase(PythUtils.EMPTY_PUBKEY)){
            mappingAccount.setNextMappingAccount(nextMappingAccount);
        }

        mappingAccount.setProductAccountKeys(new ArrayList<>());
        for (int i = 0; i < mappingAccount.getNumProducts(); i++) {
            final PublicKey productAccountKey = PublicKey.readPubkey(
                    data,
                    PRODUCT_ACCOUNT_KEYS_OFFSET + (i * 32)
            );
            mappingAccount.getProductAccountKeys().add(productAccountKey);
        }

        return mappingAccount;
    }

}
