package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.utils.ByteUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ch.openserum.mango.model.MangoUtils.U64_SIZE_BYTES;

/**
 * Represents a v3 Mango Perp group
 */
@Builder
@Getter
@Setter
@ToString
public class MangoPerpGroup {

    // Constants
    private static final int MAX_TOKENS = 32;
    private static final int MAX_PAIRS = MAX_TOKENS - 1;

    // Offsets
    private static final int METADATA_OFFSET = 0;
    private static final int NUM_ORACLES_OFFSET = METADATA_OFFSET + MangoAccountMetadata.METADATA_LAYOUT_SIZE;
    private static final int TOKENS_OFFSET = NUM_ORACLES_OFFSET + U64_SIZE_BYTES;
    private static final int SPOT_MARKETS_OFFSET = TOKENS_OFFSET
            + (MAX_TOKENS * MangoTokenInfo.MANGO_TOKEN_INFO_LAYOUT_SIZE);
    private static final int PERP_MARKETS_OFFSET = SPOT_MARKETS_OFFSET
            + (MAX_PAIRS * MangoSpotMarketInfo.MANGO_SPOT_MARKET_INFO_LAYOUT_SIZE);
    private static final int ORACLES_OFFSET = PERP_MARKETS_OFFSET
            + (MAX_PAIRS * MangoPerpMarketInfo.MANGO_PERP_MARKET_INFO_LAYOUT_SIZE);
    private static final int SIGNER_NONCE_OFFSET = ORACLES_OFFSET + (MAX_PAIRS * PublicKey.PUBLIC_KEY_LENGTH);
    private static final int SIGNER_KEY_OFFSET = SIGNER_NONCE_OFFSET + U64_SIZE_BYTES;
    private static final int ADMIN_OFFSET = SIGNER_KEY_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;
    private static final int DEX_PROGRAM_ID_OFFSET = ADMIN_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;
    private static final int MANGO_CACHE_OFFSET = DEX_PROGRAM_ID_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;
    private static final int VALID_INTERVAL_OFFSET = MANGO_CACHE_OFFSET + PublicKey.PUBLIC_KEY_LENGTH;

    // Member Variables
    private PublicKey publicKey;
    private MangoAccountMetadata metadata;
    private long numOracles;
    private List<MangoTokenInfo> tokens;
    private List<MangoSpotMarketInfo> spotMarkets;
    private List<MangoPerpMarketInfo> perpMarkets;
    private List<PublicKey> oracles;
    private long signerNonce;
    private PublicKey signerKey;
    private PublicKey admin;
    private PublicKey dexProgramId;
    private PublicKey mangoCache;
    private long validInterval;

    public static MangoPerpGroup readMangoPerpGroup(final PublicKey publicKey, byte[] data) {
        final MangoPerpGroup mangoPerpGroup = MangoPerpGroup.builder()
                .publicKey(publicKey)
                .build();

        mangoPerpGroup.setMetadata(
                MangoAccountMetadata.readMangoAccountMetadata(
                        Arrays.copyOfRange(data, METADATA_OFFSET, MangoAccountMetadata.METADATA_LAYOUT_SIZE)
                )
        );

        mangoPerpGroup.setNumOracles(
                ByteUtils.readUint64(data, NUM_ORACLES_OFFSET).longValue()
        );

        mangoPerpGroup.setTokens(new ArrayList<>());
        for (int i = 0; i < MAX_TOKENS; i++) {
            final MangoTokenInfo mangoTokenInfo = MangoTokenInfo.readMangoTokenInfo(
                    Arrays.copyOfRange(
                            data,
                            TOKENS_OFFSET + (i * MangoTokenInfo.MANGO_TOKEN_INFO_LAYOUT_SIZE),
                            TOKENS_OFFSET + (i * MangoTokenInfo.MANGO_TOKEN_INFO_LAYOUT_SIZE)
                                    + MangoTokenInfo.MANGO_TOKEN_INFO_LAYOUT_SIZE
                    )
            );

            if (mangoTokenInfo.getDecimals() != 0) {
                mangoPerpGroup.getTokens().add(mangoTokenInfo);
            }
        }

        mangoPerpGroup.setSpotMarkets(new ArrayList<>());
        for (int i = 0; i < MAX_PAIRS; i++) {
            int start = SPOT_MARKETS_OFFSET + (i * MangoSpotMarketInfo.MANGO_SPOT_MARKET_INFO_LAYOUT_SIZE);
            int end = SPOT_MARKETS_OFFSET + (i * MangoSpotMarketInfo.MANGO_SPOT_MARKET_INFO_LAYOUT_SIZE)
                    + MangoSpotMarketInfo.MANGO_SPOT_MARKET_INFO_LAYOUT_SIZE;

            final MangoSpotMarketInfo mangoSpotMarketInfo = MangoSpotMarketInfo.readMangoSpotMarketInfo(
                    Arrays.copyOfRange(
                            data,
                            start,
                            end
                    )
            );

            String spotMarketString = mangoSpotMarketInfo.getSpotMarket().toBase58();

            if (!spotMarketString.equalsIgnoreCase("11111111111111111111111111111111")) {
                mangoPerpGroup.getSpotMarkets().add(mangoSpotMarketInfo);
            }
        }

        mangoPerpGroup.setPerpMarkets(new ArrayList<>());
        for (int i = 0; i < MAX_PAIRS; i++) {
            int start = PERP_MARKETS_OFFSET + (i * MangoPerpMarketInfo.MANGO_PERP_MARKET_INFO_LAYOUT_SIZE);
            int end = PERP_MARKETS_OFFSET + (i * MangoPerpMarketInfo.MANGO_PERP_MARKET_INFO_LAYOUT_SIZE)
                    + MangoPerpMarketInfo.MANGO_PERP_MARKET_INFO_LAYOUT_SIZE;

            final MangoPerpMarketInfo mangoPerpMarketInfo = MangoPerpMarketInfo.readMangoPerpMarketInfo(
                    Arrays.copyOfRange(
                            data,
                            start,
                            end
                    )
            );

            String perpMarketString = mangoPerpMarketInfo.getPerpMarket().toBase58();

            if (!perpMarketString.equalsIgnoreCase("11111111111111111111111111111111")) {
                mangoPerpGroup.getPerpMarkets().add(mangoPerpMarketInfo);
            }
        }

        mangoPerpGroup.setOracles(new ArrayList<>());
        for (int i = 0; i < MAX_PAIRS; i++) {
            final PublicKey oracle = PublicKey.readPubkey(
                    data,
                    ORACLES_OFFSET + (i * PublicKey.PUBLIC_KEY_LENGTH)
            );

            if (!oracle.toBase58().equalsIgnoreCase("11111111111111111111111111111111")) {
                mangoPerpGroup.getOracles().add(oracle);
            }
        }

        mangoPerpGroup.setSignerNonce(Utils.readInt64(data, SIGNER_NONCE_OFFSET));
        mangoPerpGroup.setSignerKey(PublicKey.readPubkey(data, SIGNER_KEY_OFFSET));
        mangoPerpGroup.setAdmin(PublicKey.readPubkey(data, ADMIN_OFFSET));
        mangoPerpGroup.setDexProgramId(PublicKey.readPubkey(data, DEX_PROGRAM_ID_OFFSET));
        mangoPerpGroup.setMangoCache(PublicKey.readPubkey(data, MANGO_CACHE_OFFSET));
        mangoPerpGroup.setValidInterval(Utils.readInt64(data, VALID_INTERVAL_OFFSET));

        return mangoPerpGroup;
    }

}
