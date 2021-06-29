package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
public class MangoPerpAccount {

    // Constants
    private static final int MAX_TOKENS = 32;
    private static final int MAX_PAIRS = MAX_TOKENS - 1;

    // Offsets
    private static final int METADATA_OFFSET = 0;
    private static final int MANGO_GROUP_OFFSET = METADATA_OFFSET + MangoAccountMetadata.METADATA_LAYOUT_SIZE;
    private static final int OWNER_OFFSET = MANGO_GROUP_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;
    private static final int IN_MARGIN_BASKET_OFFSET = OWNER_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;
    private static final int NUM_IN_MARGIN_BASKET_OFFSET = IN_MARGIN_BASKET_OFFSET + MAX_PAIRS;
    private static final int DEPOSITS_OFFSET = NUM_IN_MARGIN_BASKET_OFFSET + 1; // numInMarginBasket is 1 byte
    private static final int BORROWS_OFFSET = DEPOSITS_OFFSET + (MAX_TOKENS * I80F48.I80F48_LENGTH);


    private PublicKey publicKey;
    private MangoAccountMetadata metaData;
    private PublicKey mangoGroup;
    private PublicKey owner;
    private List<Boolean> inMarginBasket;
    private byte numInMarginBasket;
    private List<I80F48> deposits;
    private List<I80F48> borrows;

    public static MangoPerpAccount readMangoPerpAccount(final PublicKey publicKey, byte[] data) {
        final MangoPerpAccount mangoPerpAccount = MangoPerpAccount.builder()
                .publicKey(publicKey)
                .metaData(
                        MangoAccountMetadata.readMangoAccountMetadata(
                                Arrays.copyOfRange(
                                        data,
                                        METADATA_OFFSET,
                                        METADATA_OFFSET + MangoAccountMetadata.METADATA_LAYOUT_SIZE
                                )
                        )
                )
                .mangoGroup(PublicKey.readPubkey(data, MANGO_GROUP_OFFSET))
                .owner(PublicKey.readPubkey(data, OWNER_OFFSET))
                .numInMarginBasket(data[NUM_IN_MARGIN_BASKET_OFFSET])
                .build();

        mangoPerpAccount.setInMarginBasket(new ArrayList<>());
        for (int i = 0; i < MAX_PAIRS; i++) {
            mangoPerpAccount.getInMarginBasket().add(data[IN_MARGIN_BASKET_OFFSET + i] == 1);
        }

        mangoPerpAccount.setNumInMarginBasket(data[NUM_IN_MARGIN_BASKET_OFFSET]);
        mangoPerpAccount.setDeposits(new ArrayList<>());
        for (int i = 0; i < MAX_TOKENS; i++) {
            final I80F48 i80F48 = I80F48.readI80F48(
                    data,
                    DEPOSITS_OFFSET + (i * I80F48.I80F48_LENGTH)
            );

            if (i80F48.decodeFloat() > 0) {
                mangoPerpAccount.getDeposits().add(i80F48);
            }
        }

        mangoPerpAccount.setBorrows(new ArrayList<>());
        for (int i = 0; i < MAX_TOKENS; i++) {
            final I80F48 i80F48 = I80F48.readI80F48(
                    data,
                    BORROWS_OFFSET + (i * I80F48.I80F48_LENGTH)
            );

            // TODO figure out good way to index by token mint, instead of "i"
            if (i80F48.decodeFloat() > 0) {
                mangoPerpAccount.getBorrows().add(i80F48);
            }
        }

        return mangoPerpAccount;
    }
}
