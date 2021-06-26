import ch.openserum.mango.manager.MangoManager;
import ch.openserum.mango.model.*;
import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;

import java.util.Base64;
import java.util.logging.Logger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class MangoTest {

    private final RpcClient client = new RpcClient(Cluster.MAINNET);
    private final RpcClient devnetClient = new RpcClient("https://api.devnet.solana.com");
    private final MangoManager mangoManager = new MangoManager(client);
    private final MangoManager devnetMangoManager = new MangoManager(devnetClient);
    private static final Logger LOGGER = Logger.getLogger(MangoTest.class.getName());
    private static final PublicKey BTC_ETH_SOL_SRM_USDC_MANGO_GROUP =
            new PublicKey("2oogpTYm1sp6LPZAWD3bp2wsFpnV2kXL1s52yyFhW5vp");

    @Test
    public void mangoGroupTest() throws RpcException {
        LOGGER.info(
                String.format(
                        "Looking up Mango Group %s (%s)",
                        "BTC_ETH_SOL_SRM_USDC",
                        BTC_ETH_SOL_SRM_USDC_MANGO_GROUP
                )
        );

        // Retrieve MangoGroup from Solana
        final AccountInfo mangoGroupAccountInfo = client.getApi().getAccountInfo(
                BTC_ETH_SOL_SRM_USDC_MANGO_GROUP
        );

        // Deserialize MangoGroup
        byte[] mangoGroupData = Base64.getDecoder().decode(mangoGroupAccountInfo.getValue().getData().get(0));
        final MangoGroup mangoGroup = MangoGroup.readMangoGroup(mangoGroupData);

        LOGGER.info(
                String.format(
                        "Mango Group: Initialized (%b), MangoGroup (%b), MarginAccount (%b), MangoSrmAccount (%b)",
                        mangoGroup.getAccountFlags().isInitialized(),
                        mangoGroup.getAccountFlags().isMangoGroup(),
                        mangoGroup.getAccountFlags().isMarginAccount(),
                        mangoGroup.getAccountFlags().isMangoSrmAccount()
                )
        );

        // Deposits and Borrow indexes test
        for (int i = 0; i < 5; i++) {
            MangoIndex mangoIndex = mangoGroup.getIndexes().get(i);
            LOGGER.info(
                    String.format(
                            "Token Index %d (%s): %s",
                            i,
                            mangoGroup.getTokens().get(i).toBase58(),
                            mangoIndex.toString()
                    )
            );

            LOGGER.info(
                    String.format(
                            "Deposit = %.16f, Borrow = %.16f",
                            mangoIndex.getDeposit().decodeFloat(),
                            mangoIndex.getBorrow().decodeFloat()
                    )
            );
        }

        LOGGER.info(
                String.format(
                        "signerNonce = %d, signerKey = %s",
                        mangoGroup.getSignerNonce(),
                        mangoGroup.getSignerKey()
                )
        );

        LOGGER.info(
                String.format(
                        "DEX Program ID = %s",
                        mangoGroup.getDexProgramId().toBase58()
                )
        );

        // Total Deposits
        mangoGroup.getTotalDeposits().forEach(totalDeposit -> {
            LOGGER.info(
                    String.format(
                            "Total Deposit = %.2f",
                            totalDeposit.decodeFloat()
                    )
            );
        });

        // Total Borrows
        mangoGroup.getTotalBorrows().forEach(totalBorrow -> {
            LOGGER.info(
                    String.format(
                            "Total Borrow = %.2f",
                            totalBorrow.decodeFloat()
                    )
            );
        });

        LOGGER.info(
                String.format(
                        "maintCollRatio = %.2f, initCollRatio = %.2f",
                        mangoGroup.getMaintCollRatio().decodeFloat(),
                        mangoGroup.getInitCollRatio().decodeFloat()
                )
        );

        LOGGER.info(
                String.format(
                        "srmVault = %s, admin = %s",
                        mangoGroup.getSrmVault(),
                        mangoGroup.getAdmin()
                )
        );

        // Borrow Limits
        mangoGroup.getBorrowLimits().forEach(borrowLimit -> {
            LOGGER.info(
                    String.format(
                            "Borrow limit = %d",
                            borrowLimit
                    )
            );
        });
    }

    @Test
    public void getDefaultMangoGroupTest() {
        final MangoGroup defaultMangoGroup = mangoManager.getDefaultMangoGroup();

        LOGGER.info(
                String.format(
                        "defaultMangoGroup = %s",
                        defaultMangoGroup.toString()
                )
        );
    }

    @Test
    public void getMangoGroupAndMarginAccountTest() {
        final PublicKey marginAccountPk = PublicKey.valueOf("E95EaroWhpRnEtCpjH8HsFnzrQdcwxsXtVPwS8BbsXE");
        final MangoGroup defaultMangoGroup = mangoManager.getDefaultMangoGroup();
        final MarginAccount marginAccount = mangoManager.getMarginAccount(
                marginAccountPk,
                defaultMangoGroup.getDexProgramId()
        );

        LOGGER.info(
                String.format(
                        "Mango Group = %s",
                        defaultMangoGroup
                )
        );

        LOGGER.info(
                String.format(
                        "Margin Account = %s",
                        marginAccount.toString()
                )
        );


        for (int i = 0; i < 5; i++) {
            LOGGER.info(
                    String.format(
                            "Deposit = %.8f, BigDecimal = %s",
                            marginAccount.getDeposits().get(i).decodeFloat() /
                                    Math.pow(10, defaultMangoGroup.getMintDecimals().get(i)),
                            marginAccount.getDeposits().get(i).decodeBigDecimal()
                    )
            );
        }

        assertTrue(marginAccount.getAccountFlags().isMarginAccount());
    }

    @Test
    public void mangoV3Test() {
        final MangoPerpGroup mangoPerpGroup = devnetMangoManager.getMangoPerpGroup(
                PublicKey.valueOf("ECAikQUnS8HGLnzGrqEYA6Daz8nRRu9GsbfLbwMfK23P")
        );

        LOGGER.info(
                String.format(
                        "Mango Account = %s",
                        mangoPerpGroup
                )
        );

        assertTrue(mangoPerpGroup.getMetadata().isInitialized());

        final MangoSpotMarketInfo spotMarketInfo = mangoPerpGroup.getSpotMarketInfos().get(0);
        final I80F48 maintAssetWeight = spotMarketInfo.getMaintAssetWeight();
        final I80F48 initAssetWeight = spotMarketInfo.getInitAssetWeight();
        final I80F48 maintLiabWeight = spotMarketInfo.getMaintLiabWeight();
        final I80F48 initLiabWeight = spotMarketInfo.getInitLiabWeight();
        final I80F48 liquidationFee = spotMarketInfo.getLiquidationFee();

        LOGGER.info(
                String.format(
                        "maintAssetWeight = %.48f, bigDecimal = %s",
                        maintAssetWeight.decodeFloat(),
                        maintAssetWeight.decodeBigDecimal()
                )
        );

        LOGGER.info(
                String.format(
                        "initAssetWeight = %.48f, bigDecimal = %s",
                        initAssetWeight.decodeFloat(),
                        initAssetWeight.decodeBigDecimal()
                )
        );

        LOGGER.info(
                String.format(
                        "maintLiabWeight = %.48f, bigDecimal = %s",
                        maintLiabWeight.decodeFloat(),
                        maintLiabWeight.decodeBigDecimal()
                )
        );

        LOGGER.info(
                String.format(
                        "initLiabWeight = %.48f, bigDecimal = %s",
                        initLiabWeight.decodeFloat(),
                        initLiabWeight.decodeBigDecimal()
                )
        );

        LOGGER.info(
                String.format(
                        "liquidationFee = %.48f, bigDecimal = %s",
                        liquidationFee.decodeFloat(),
                        liquidationFee.decodeBigDecimal()
                )
        );

        assertArrayEquals(
                maintAssetWeight.getData(),
                new byte[]{102, 102, 102, 102, 102, -26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        );
    }

    @Test
    public void pubkeyReadTest() {
        byte[] rawData = {
                (byte)0x64, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x00, (byte)0x0A, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x00
        };

        PublicKey pubkey = PublicKey.readPubkey(rawData, 0);
        LOGGER.info("Pubkey = " + pubkey.toBase58());

        byte[] rawData2 = {
                (byte)0x51, (byte)0xA1, (byte)0x3F, (byte)0x14, (byte)0x0D, (byte)0x58,
                (byte)0x32, (byte)0x04, (byte)0x51, (byte)0x11, (byte)0x4A, (byte)0x84,
                (byte)0x91, (byte)0xF2, (byte)0x96, (byte)0x0B, (byte)0x62, (byte)0x0A,
                (byte)0x92, (byte)0x60, (byte)0xEA, (byte)0x50, (byte)0x21, (byte)0x4B,
                (byte)0x21, (byte)0x75, (byte)0x7E, (byte)0x29, (byte)0xEA, (byte)0x7C,
                (byte)0xD9, (byte)0xDB
        };

        PublicKey pubkey2 = PublicKey.readPubkey(rawData2, 0);
        LOGGER.info("Pubkey2 = " + pubkey2.toBase58());
    }
}
