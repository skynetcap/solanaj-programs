import ch.openserum.serum.manager.SerumManager;
import ch.openserum.serum.model.*;
import ch.openserum.serum.program.SerumProgram;
import org.bitcoinj.core.Base58;
import org.junit.Ignore;
import org.junit.Test;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.programs.MemoProgram;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.rpc.types.Memcmp;
import org.p2p.solanaj.rpc.types.ProgramAccount;
import org.p2p.solanaj.rpc.types.config.Commitment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OrderTest {

    private static final Logger LOGGER = Logger.getLogger(OrderTest.class.getName());
    private final RpcClient client = new RpcClient("https://node.openserum.io/");
    private final SerumManager serumManager = new SerumManager(client);
    private static final PublicKey SOL_USDC_MARKET_V3 = PublicKey.valueOf("9wFFyRfZBsuAha4YcuxcXLKwMxJR43S7fPfQLusDBzvT");

    /**
     * Places a sell order for 0.1 SOL on SOL/USDC and a buy order for 0.001 USDC on SOL/USDC.
     * This test does NOT cancel the orders, you'll need to do that manually.
     * <p>
     * Requires open orders accounts to already be manually created beforehand.
     * <p>
     * You'll need to configure your USDC wallet's pubkey in the "usdcPayer" variable.
     * The SOL wallet will have it's SOL wrapped automatically.
     */
    @Test
    @Ignore
    public void placeOrderTest() {
        // Replace with the public key of your USDC wallet
        final PublicKey usdcPayer = PublicKey.valueOf("A71WvME6ZhR4SFG3Ara7zQK5qdRSB97jwTVmB3sr7XiN");

        // Build account from secretkey.dat
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("secretkey.dat"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create account from private key
        final Account account = new Account(Base58.decode(new String(data)));

        // Get SOL/USDC market
        final Market solUsdcMarket = new MarketBuilder()
                .setPublicKey(SOL_USDC_MARKET_V3)
                .setClient(client)
                .setRetrieveDecimalsOnly(true)
                .build();

        long orderId = 11133711L;

        final Order order = Order.builder()
                .floatPrice(1337)
                .floatQuantity(0.1f)
                .clientOrderId(orderId)
                .orderTypeLayout(OrderTypeLayout.POST_ONLY)
                .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                .buy(false).build();

        // Place order
        String transactionId = serumManager.placeOrder(
                account,
                solUsdcMarket,
                order,
                account.getPublicKey(), //base wallet, sol address
                usdcPayer
        );

        assertNotNull(transactionId);
        LOGGER.info("Successfully placed offer for 0.1 SOL on SOL/USDC market.");

        // USDC order

        long usdcOrderId = 12321L;

        final Order usdcOrder = Order.builder()
                .floatPrice(0.001f)
                .floatQuantity(0.1f)
                .clientOrderId(usdcOrderId)
                .orderTypeLayout(OrderTypeLayout.POST_ONLY)
                .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                .buy(true).build();

        // Place order
        String usdcTransactionId = serumManager.placeOrder(
                account,
                solUsdcMarket,
                usdcOrder,
                account.getPublicKey(),
                usdcPayer
        );

        assertNotNull(usdcTransactionId);
        LOGGER.info("Successfully placed bid for 0.1 SOL on SOL/USDC market.");

        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Cancel the SOL order
        String cancelTransactionId = serumManager.cancelOrderByClientId(
                account,
                solUsdcMarket,
                orderId
        );

        assertNotNull(cancelTransactionId);
        LOGGER.info("Cancellation TX = " + cancelTransactionId);
        LOGGER.info("Successfully cancelled order by ID " + orderId);

        // Cancel the USDC order
        String usdcCancelTransactionId = serumManager.cancelOrderByClientId(
                account,
                solUsdcMarket,
                usdcOrderId
        );

        assertNotNull(usdcCancelTransactionId);
        LOGGER.info("USDC Cancellation TX = " + cancelTransactionId);
        LOGGER.info("Successfully cancelled order by ID " + usdcOrderId);

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Settle transaction
        LOGGER.info("Settling funds");
        final PublicKey baseWallet = account.getPublicKey();
        final PublicKey quoteWallet = usdcPayer;

        String settlementTransactionId = serumManager.settleFunds(
                solUsdcMarket,
                account,
                baseWallet,
                quoteWallet
        );

        assertNotNull(settlementTransactionId);
        LOGGER.info("Settlement TX = " + settlementTransactionId);
    }

    @Test
    @Ignore
    public void cancelOrderV2Test() {
        // Build account from secretkey.dat
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("secretkey.dat"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create account from private key
        final Account account = new Account(Base58.decode(new String(data)));

        final Market lqidUsdcMarket = new MarketBuilder()
                .setPublicKey(PublicKey.valueOf("4FPFh1iAiitKYMCPDBmEQrZVgA1DVMKHZBU2R7wjQWuu"))
                .setRetrieveDecimalsOnly(true)
                .setClient(client)
                .build();


        final OpenOrdersAccount openOrdersAccount = SerumUtils.findOpenOrdersAccountForOwner(
                client,
                lqidUsdcMarket.getOwnAddress(),
                PublicKey.valueOf("F459S1MFG2whWbznzULPkYff6TFe2QjoKhgHXpRfDyCj")
        );

        if (openOrdersAccount.getClientOrderIds().size() > 0) {
            openOrdersAccount.getClientOrderIds().forEach(clientOrderId -> {
                if (clientOrderId[0] != 0) {
                    LOGGER.info("Cancelling order: " + Arrays.toString(clientOrderId));

                    String transactionId = serumManager.cancelOrder(
                            account,
                            lqidUsdcMarket,
                            SideLayout.BUY,
                            clientOrderId,
                            openOrdersAccount
                    );

                    LOGGER.info("Cancel TX: " + transactionId);
                }
            });
        }
    }

    @Test
    @Ignore
    public void cancelAllOrdersAndSettleTest() {
        // Build account from secretkey.dat
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("secretkey.dat"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create account from private key
        final Account account = new Account(Base58.decode(new String(data)));

        final Market xrpBearUsdcMarket = new MarketBuilder()
                .setPublicKey(PublicKey.valueOf("G2aPyW7r3gfW8GnRumiXXp1567XzMZsfwvbgxDiaNR4U"))
                .setRetrieveDecimalsOnly(true)
                .setClient(client)
                .build();

        final PublicKey lqidWallet = PublicKey.valueOf("3Hbga31dmqqLauAUtHXyemNYXB1jYnS4t1ExmSdfe4sD");
        final PublicKey usdcWallet = PublicKey.valueOf("A71WvME6ZhR4SFG3Ara7zQK5qdRSB97jwTVmB3sr7XiN");

        final OpenOrdersAccount openOrdersAccount = SerumUtils.findOpenOrdersAccountForOwner(
                client,
                xrpBearUsdcMarket.getOwnAddress(),
                PublicKey.valueOf("F459S1MFG2whWbznzULPkYff6TFe2QjoKhgHXpRfDyCj")
        );

        String transactionId = serumManager.cancelAllOrdersAndSettle(
                account,
                xrpBearUsdcMarket,
                openOrdersAccount,
                lqidWallet,
                usdcWallet
        );

        LOGGER.info("Cancel all TX: " + transactionId);

        assertNotNull(transactionId);
    }

    @Test
    @Ignore
    public void openOrdersTest() {
        final Market lqidUsdcMarket = new MarketBuilder()
                .setPublicKey(PublicKey.valueOf("4FPFh1iAiitKYMCPDBmEQrZVgA1DVMKHZBU2R7wjQWuu"))
                .setRetrieveDecimalsOnly(true)
                .setClient(client)
                .build();


        final PublicKey owner = PublicKey.valueOf("F459S1MFG2whWbznzULPkYff6TFe2QjoKhgHXpRfDyCj");
        final OpenOrdersAccount openOrdersAccount = SerumUtils.findOpenOrdersAccountForOwner(
                client,
                lqidUsdcMarket.getOwnAddress(),
                owner
        );

        final List<OpenOrdersAccount.Order> orders = openOrdersAccount.getOrders();
        orders.forEach(order -> {
            float floatPrice = SerumUtils.priceLotsToNumber(
                    order.getPrice(),
                    lqidUsdcMarket.getBaseDecimals(),
                    lqidUsdcMarket.getQuoteDecimals(),
                    lqidUsdcMarket.getBaseLotSize(),
                    lqidUsdcMarket.getQuoteLotSize()
            );

            order.setFloatPrice(floatPrice);
            LOGGER.info(String.format("%s", order));
        });
    }

    @Test
    @Ignore
    public void placeOrderOxyTest() {
        // Replace with the public key of your OXY and USDC wallet
        final PublicKey oxyWallet = PublicKey.valueOf("DoecacoZMpqHT8RGusoJYcjDFZjZauaLrDQh8BxQUVdU");
        final PublicKey usdcPayer = PublicKey.valueOf("A71WvME6ZhR4SFG3Ara7zQK5qdRSB97jwTVmB3sr7XiN");

        // Build account from secretkey.dat
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("secretkey.dat"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create account from private key
        final Account account = new Account(Base58.decode(new String(data)));

        // Get OXY/USDC market
        final Market oxyUsdcMarket = new MarketBuilder()
                .setPublicKey(PublicKey.valueOf("GZ3WBFsqntmERPwumFEYgrX2B7J7G11MzNZAy7Hje27X"))
                .setClient(client)
                .setRetrieveDecimalsOnly(true)
                .build();

        long orderId = 11133711L;

        // 1 oxy bid @ $0.01
        final Order order = Order.builder()
                .floatPrice(0.01f)
                .floatQuantity(1f)
                .clientOrderId(orderId)
                .orderTypeLayout(OrderTypeLayout.POST_ONLY)
                .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                .buy(true).build();

        // Place order
        String transactionId = serumManager.placeOrder(
                account,
                oxyUsdcMarket,
                order,
                oxyWallet,
                usdcPayer
        );

        assertNotNull(transactionId);

        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Cancel the order
        String cancelTransactionId = serumManager.cancelOrderByClientId(
                account,
                oxyUsdcMarket,
                orderId
        );

        assertNotNull(cancelTransactionId);
        LOGGER.info("Cancellation TX = " + cancelTransactionId);
        LOGGER.info("Successfully cancelled order by ID " + orderId);

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Settle transaction
        LOGGER.info("Settling funds");
        final PublicKey baseWallet = oxyWallet;
        final PublicKey quoteWallet = usdcPayer;

        String settlementTransactionId = serumManager.settleFunds(
                oxyUsdcMarket,
                account,
                baseWallet,
                quoteWallet
        );

        assertNotNull(settlementTransactionId);
        LOGGER.info("Settlement TX = " + settlementTransactionId);
    }

//    @Test
//    public void cloneOrcaWallet() throws RpcException {
//        // clone this 5yHduya2yKQdZFPU4rTfi4cRG8M5tjK3wcVRSZ6CnafP
//        AccountInfo accountInfo = client.getApi().getAccountInfo(new PublicKey("5yHduya2yKQdZFPU4rTfi4cRG8M5tjK3wcVRSZ6CnafP"));
//        byte[] data = Base64.getDecoder().decode(accountInfo.getValue().getData().get(0));
//
//        Transaction createAccountTransaction = new Transaction();
//
//        createAccountTransaction.addInstruction(
//                SystemProgram.createAccount(
//
//                )
//        )
//    }

    @Test
    @Ignore
    public void btcBot() throws RpcException, InterruptedException {
        final PublicKey btcWallet = PublicKey.valueOf("GyFepQ1KrqoeiiejYWifZxrZkzwtWxu9KXzgQmzKNF7d");
        final PublicKey usdcPayer = PublicKey.valueOf("5yHduya2yKQdZFPU4rTfi4cRG8M5tjK3wcVRSZ6CnafP");
        final PublicKey ooa = PublicKey.valueOf("G5xANxunzHTR4tU63b18fh9PcdQkQFdiJH2Awrj2hqLz");

        for (int i = 0; i < 100; i++) {
            Transaction transaction = new Transaction();
            Account account = null;
            try {
                account = Account.fromJson(Files.readString(Paths.get("src/test/resources/mainnet.json")));
            } catch (IOException e) {
                e.printStackTrace();
            }

            final Market market = new MarketBuilder()
                    .setPublicKey(PublicKey.valueOf("A8YFbxQYFVqKZaoYJLLUVcQiWP7G2MeEgW5wsAQgMvFw"))
                    .setClient(client)
                    .setRetrieveOrderBooks(true)
                    .build();

            // get midpoint
            float midPoint = (market.getBidOrderBook().getBestBid().getFloatPrice() + market.getAskOrderBook().getBestAsk().getFloatPrice()) / 2;

            final Order order = Order.builder()
                    .floatPrice(midPoint)
                    .floatQuantity(0.0069f)
                    .clientOrderId(11133711L)
                    .orderTypeLayout(OrderTypeLayout.LIMIT)
                    .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                    .buy(true)
                    .build();

            serumManager.setOrderPrices(order, market);

            transaction.addInstruction(
                    SerumProgram.placeOrder(
                            account,
                            usdcPayer,
                            ooa,
                            market,
                            order
                    )
            );

            // Account 2
            Account account2 = null;
            try {
                account2 = Account.fromJson(Files.readString(Paths.get("src/test/resources/dev2.json")));
            } catch (IOException e) {
                e.printStackTrace();
            }

            final PublicKey btcWallet2 = PublicKey.valueOf("HkP6V9cQsk4HvpAn6HGefij9fQXd7roH7YvuzVcikmR9");
            final PublicKey usdcPayer2 = PublicKey.valueOf("J2284fx3US2k1a7hYoG9k3odSVadtZGrTEMfDkjcjXUN");
            final PublicKey ooa2 = PublicKey.valueOf("EQG3CD8Ld8mjXpY8aXHNo1xW3c75icz2Vc3kgAofxE5n");

            final Order sellOrder = Order.builder()
                    .floatPrice(midPoint)
                    .floatQuantity(0.0069f)
                    .clientOrderId(4201337L)
                    .orderTypeLayout(OrderTypeLayout.LIMIT)
                    .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                    .buy(false)
                    .build();

            serumManager.setOrderPrices(sellOrder, market);

            transaction.addInstruction(
                    SerumProgram.placeOrder(
                            account2,
                            btcWallet2,
                            ooa2,
                            market,
                            sellOrder
                    )
            );

            // crank before settle
            transaction.addInstruction(
                    SerumProgram.consumeEvents(
                            account.getPublicKey(),
                            List.of(
                                    ooa
                            ),
                            market,
                            btcWallet,
                            usdcPayer
                    )
            );

            transaction.addInstruction(
                    SerumProgram.settleFunds(
                            market,
                            ooa,
                            account.getPublicKey(),
                            btcWallet,
                            usdcPayer
                    )
            );

            transaction.addInstruction(
                    SerumProgram.consumeEvents(
                            account2.getPublicKey(),
                            List.of(
                                    ooa2
                            ),
                            market,
                            btcWallet2,
                            usdcPayer2
                    )
            );


            transaction.addInstruction(
                    SerumProgram.settleFunds(
                            market,
                            ooa2,
                            account2.getPublicKey(),
                            btcWallet2,
                            usdcPayer2
                    )
            );

            long btcAmount = 6900L;
            // dev 1 to dev 2 (send him btc)
            transaction.addInstruction(
                    TokenProgram.transfer(
                            btcWallet,
                            btcWallet2,
                            btcAmount,
                            account.getPublicKey()
                    )
            );

            long usdcAfterFees = (long)(midPoint * 0.0069f) * 999_700;
            // dev 2 to dev 1 (send him usdc)
            transaction.addInstruction(
                    TokenProgram.transfer(
                            usdcPayer2,
                            usdcPayer,
                            usdcAfterFees,
                            account2.getPublicKey()
                    )
            );

            transaction.addInstruction(
                    MemoProgram.writeUtf8(
                            account.getPublicKey(),
                            "Mess with the best, die like the rest."
                    )
            );
            String transactionId = client.getApi().sendTransaction(transaction, List.of(account, account2),
                    client.getApi().getRecentBlockhash(Commitment.CONFIRMED));
            LOGGER.info((i + 1) + " TX = " + transactionId);
            Thread.sleep(50L);
        }
    }


    @Test
    @Ignore
    public void orcaTrade() throws RpcException, InterruptedException {
        final PublicKey orcaWallet = PublicKey.valueOf("BgLAeRn8nt2APbCerWyYvGrGUDoRxT4o2FYP51MgX6Kn");
        final PublicKey usdcPayer = PublicKey.valueOf("5yHduya2yKQdZFPU4rTfi4cRG8M5tjK3wcVRSZ6CnafP");

        for (int i = 0; i < 50; i++) {
            Transaction transaction = new Transaction();
            Account account = null;
            try {
                account = Account.fromJson(Files.readString(Paths.get("src/test/resources/mainnet.json")));
            } catch (IOException e) {
                e.printStackTrace();
            }

            final Market market = new MarketBuilder()
                    .setPublicKey(PublicKey.valueOf("8N1KkhaCYDpj3awD58d85n973EwkpeYnRp84y1kdZpMX"))
                    .setClient(client)
                    .setRetrieveOrderBooks(true)
                    .build();

            final Order order = Order.builder()
                    .floatPrice(0.97f)
                    .floatQuantity(1f)
                    .clientOrderId(11133711L)
                    .orderTypeLayout(OrderTypeLayout.LIMIT)
                    .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                    .buy(true)
                    .build();

            serumManager.setOrderPrices(order, market);

            transaction.addInstruction(
                    SerumProgram.placeOrder(
                            account,
                            usdcPayer,
                            PublicKey.valueOf("5QNcrySBPvkVZhKZejGkneduCN2dJa5YHCqPhvo6ubDz"),
                            market,
                            order
                    )
            );

            // Account 2
            Account account2 = null;
            try {
                account2 = Account.fromJson(Files.readString(Paths.get("src/test/resources/dev2.json")));
            } catch (IOException e) {
                e.printStackTrace();
            }

            final PublicKey orcaWallet2 = PublicKey.valueOf("D5Xy5xnfT3jSc5tkgESvzsXL59MxNwf19R1XcCxp6UXi");
            final PublicKey usdcPayer2 = PublicKey.valueOf("J2284fx3US2k1a7hYoG9k3odSVadtZGrTEMfDkjcjXUN");
            final PublicKey ooa2 = PublicKey.valueOf("8ZAGtBLxircxFZUhs1L8zUiFyX6gKo8BdsGfum2w5gjC");

            final Order sellOrder = Order.builder()
                    .floatPrice(0.97f)
                    .floatQuantity(1f)
                    .clientOrderId(4201337L)
                    .orderTypeLayout(OrderTypeLayout.LIMIT) //try ioc as workaround?
                    .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                    .buy(false)
                    .build();

            serumManager.setOrderPrices(sellOrder, market);

            transaction.addInstruction(
                    SerumProgram.placeOrder(
                            account2,
                            orcaWallet2,
                            ooa2,
                            market,
                            sellOrder
                    )
            );

            // crank before settle
            transaction.addInstruction(
                    SerumProgram.consumeEvents(
                            account.getPublicKey(),
                            List.of(
                                    PublicKey.valueOf("5QNcrySBPvkVZhKZejGkneduCN2dJa5YHCqPhvo6ubDz")
                            ),
                            market,
                            orcaWallet,
                            usdcPayer
                    )
            );

            transaction.addInstruction(
                    SerumProgram.consumeEvents(
                            account2.getPublicKey(),
                            List.of(
                                    ooa2
                            ),
                            market,
                            orcaWallet2,
                            usdcPayer2
                    )
            );

            transaction.addInstruction(
                    SerumProgram.settleFunds(
                            market,
                            PublicKey.valueOf("5QNcrySBPvkVZhKZejGkneduCN2dJa5YHCqPhvo6ubDz"),
                            account.getPublicKey(),
                            orcaWallet,
                            usdcPayer
                    )
            );

            transaction.addInstruction(
                    SerumProgram.settleFunds(
                            market,
                            ooa2,
                            account2.getPublicKey(),
                            orcaWallet2,
                            usdcPayer2
                    )
            );

            long singleOrca = 1000000L;
            // dev 1 to dev 2 (send him orca)
            transaction.addInstruction(
                    TokenProgram.transfer(
                            orcaWallet,
                            orcaWallet2,
                            singleOrca,
                            account.getPublicKey()
                    )
            );

            long usdcAfterFees = 969612L;
            // dev 2 to dev 1 (send him usdc)
            transaction.addInstruction(
                    TokenProgram.transfer(
                            usdcPayer2,
                            usdcPayer,
                            usdcAfterFees,
                            account2.getPublicKey()
                    )
            );

            transaction.addInstruction(
                    MemoProgram.writeUtf8(
                            account.getPublicKey(),
                            "Mess With The Best, Die Like The Rest."
                    )
            );
            String transactionId = client.getApi().sendTransaction(transaction, List.of(account, account2), null);
            LOGGER.info((i + 1) + " TX = " + transactionId);
            Thread.sleep(50L);
        }
    }

    @Test
    @Ignore
    public void placeOrderSellMerTest() {
        // Replace with the public key of your OXY and USDC wallet
        final PublicKey merWallet = PublicKey.valueOf("FqZv3vNbLMcVXvV7yH8LmPKitn5nhLcpnd981JSFF7jf"); // needs 0.1 mer
        final PublicKey usdcWallet = PublicKey.valueOf("A71WvME6ZhR4SFG3Ara7zQK5qdRSB97jwTVmB3sr7XiN");

        // Build account from secretkey.dat
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("secretkey.dat"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create account from private key
        final Account account = new Account(Base58.decode(new String(data)));

        // Get OXY/USDC market
        final Market oxyUsdcMarket = new MarketBuilder()
                .setPublicKey(PublicKey.valueOf("HhvDWug3ftYNx5148ZmrQxzvEmohN2pKVNiRT4TVoekF")) // MER/USDC
                .setClient(client)
                .setRetrieveDecimalsOnly(true)
                .build();

        long orderId = 11133711L;

        // 0.1 mer offer @ $1337
        final Order order = Order.builder()
                .floatPrice(1337)
                .floatQuantity(0.1f)
                .clientOrderId(orderId)
                .orderTypeLayout(OrderTypeLayout.POST_ONLY)
                .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                .buy(false).build();

        // Place order
        String transactionId = serumManager.placeOrder(
                account,
                oxyUsdcMarket,
                order,
                merWallet,
                usdcWallet
        );

        assertNotNull(transactionId);

        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Cancel the order
        String cancelTransactionId = serumManager.cancelOrderByClientId(
                account,
                oxyUsdcMarket,
                orderId
        );

        assertNotNull(cancelTransactionId);
        LOGGER.info("Cancellation TX = " + cancelTransactionId);
        LOGGER.info("Successfully cancelled order by ID " + orderId);

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Settle transaction
        LOGGER.info("Settling funds");
        final PublicKey baseWallet = merWallet;
        final PublicKey quoteWallet = usdcWallet;

        String settlementTransactionId = serumManager.settleFunds(
                oxyUsdcMarket,
                account,
                baseWallet,
                quoteWallet
        );

        assertNotNull(settlementTransactionId);
        LOGGER.info("Settlement TX = " + settlementTransactionId);
    }

    // Requires 0.2 MER to offer
    @Test
    @Ignore
    public void placeOrderSellMerTestPreCalculatedOpenOrdersAccount() {
        // Replace with the public key of your OXY and USDC wallet
        final PublicKey merWallet = PublicKey.valueOf("FqZv3vNbLMcVXvV7yH8LmPKitn5nhLcpnd981JSFF7jf"); // needs 0.1 mer
        final PublicKey usdcWallet = PublicKey.valueOf("A71WvME6ZhR4SFG3Ara7zQK5qdRSB97jwTVmB3sr7XiN");

        // Build account from secretkey.dat
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("secretkey.dat"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create account from private key
        final Account account = new Account(Base58.decode(new String(data)));

        // Get OXY/USDC market
        final Market oxyUsdcMarket = new MarketBuilder()
                .setPublicKey(PublicKey.valueOf("HhvDWug3ftYNx5148ZmrQxzvEmohN2pKVNiRT4TVoekF")) // MER/USDC
                .setClient(client)
                .setRetrieveDecimalsOnly(true)
                .build();

        long orderId = 11133711L;


        // 0.1 mer offer @ $1337
        final Order order = Order.builder()
                .floatPrice(1337)
                .floatQuantity(0.1f)
                .clientOrderId(orderId)
                .orderTypeLayout(OrderTypeLayout.POST_ONLY)
                .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                .buy(false).build();

        final OpenOrdersAccount openOrdersAccount = SerumUtils.findOpenOrdersAccountForOwner(
                client,
                oxyUsdcMarket.getOwnAddress(),
                account.getPublicKey()
        );

        // Place order 1
        String transactionId = serumManager.placeOrder(
                account,
                oxyUsdcMarket,
                order,
                merWallet,
                usdcWallet,
                openOrdersAccount
        );

        LOGGER.info(String.format("TX 1: %s", transactionId));

        try {
            Thread.sleep(
                    1000L
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Cancel order 1
        String cancelTransactionId = serumManager.cancelOrderByClientId(
                account,
                oxyUsdcMarket,
                orderId,
                openOrdersAccount
        );

        LOGGER.info(String.format("Cancel TX 1: %s", cancelTransactionId));

        // Settle order 1
        String settleTransactionId = serumManager.settleFunds(
                oxyUsdcMarket,
                account,
                merWallet,
                usdcWallet,
                openOrdersAccount
        );

        LOGGER.info(String.format("Settle TX 1: %s", settleTransactionId));


        assertNotNull(transactionId);
    }

    @Test
    @Ignore
    public void placeOrderRandomClientIdTest() {
        final PublicKey xrpBearWallet = PublicKey.valueOf("3Hbga31dmqqLauAUtHXyemNYXB1jYnS4t1ExmSdfe4sD"); // XRPBEAR
        final PublicKey usdcWallet = PublicKey.valueOf("A71WvME6ZhR4SFG3Ara7zQK5qdRSB97jwTVmB3sr7XiN");

        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("secretkey.dat"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Account account = new Account(Base58.decode(new String(data)));

        final Market xrpBearUsdcMarket = new MarketBuilder()
                .setPublicKey(PublicKey.valueOf("G2aPyW7r3gfW8GnRumiXXp1567XzMZsfwvbgxDiaNR4U")) // XRPBEAR/USDC
                .setClient(client)
                .setRetrieveDecimalsOnly(true)
                .build();

        OpenOrdersAccount openOrdersAccount = SerumUtils.findOpenOrdersAccountForOwner(
                client,
                xrpBearUsdcMarket.getOwnAddress(),
                account.getPublicKey()
        );

        final Order order = Order.builder()
                .floatPrice(0.01f)
                .floatQuantity(1)
                .clientOrderId(new SecureRandom().nextLong())
                .orderTypeLayout(OrderTypeLayout.POST_ONLY)
                .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                .buy(true).build();

        String transactionId = serumManager.placeOrder(
                account,
                xrpBearUsdcMarket,
                order,
                xrpBearWallet,
                usdcWallet,
                openOrdersAccount
        );
        LOGGER.info(String.format("Order ID %d, Transaction ID %s", order.getClientOrderId(), transactionId));

        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String cancelTransactionId = serumManager.cancelOrderByClientIdAndSettle(
                account,
                xrpBearUsdcMarket,
                order.getClientOrderId(),
                openOrdersAccount,
                xrpBearWallet,
                usdcWallet
        );

        LOGGER.info(String.format("Cancellation TX %s", cancelTransactionId));
        assertNotNull(cancelTransactionId);
    }

    @Test
    @Ignore
    public void place20OrderSRandomClientIdTest() {
        final PublicKey xrpBearWallet = PublicKey.valueOf("3Hbga31dmqqLauAUtHXyemNYXB1jYnS4t1ExmSdfe4sD"); // XRPBEAR
        final PublicKey usdcWallet = PublicKey.valueOf("A71WvME6ZhR4SFG3Ara7zQK5qdRSB97jwTVmB3sr7XiN");

        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("secretkey.dat"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Account account = new Account(Base58.decode(new String(data)));

        final Market xrpBearUsdcMarket = new MarketBuilder()
                .setPublicKey(PublicKey.valueOf("G2aPyW7r3gfW8GnRumiXXp1567XzMZsfwvbgxDiaNR4U")) // XRPBEAR/USDC
                .setClient(client)
                .setRetrieveDecimalsOnly(true)
                .build();

        OpenOrdersAccount openOrdersAccount = SerumUtils.findOpenOrdersAccountForOwner(
                client,
                xrpBearUsdcMarket.getOwnAddress(),
                account.getPublicKey()
        );

        List<Order> orders = new ArrayList<>();
        Transaction transaction = new Transaction();

        for (int i = 1; i <= 9; i++) {
            final Order order = Order.builder()
                    .floatPrice(0.01F + (0.01f * 1 / 2 * i))
                    .floatQuantity(10 - i)
                    .clientOrderId(new SecureRandom().nextLong())
                    .orderTypeLayout(OrderTypeLayout.POST_ONLY)
                    .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                    .buy(true)
                    .build();

            serumManager.setOrderPrices(order, xrpBearUsdcMarket);

            transaction.addInstruction(
                    SerumProgram.placeOrder(
                            account,
                            usdcWallet,
                            openOrdersAccount.getOwnPubkey(),
                            xrpBearUsdcMarket,
                            order
                    )
            );

            LOGGER.info(String.format("Order ID %d", order.getClientOrderId()));
            orders.add(order);
        }

        try {
            String txId = client.getApi().sendTransaction(transaction, account);
            LOGGER.info("Place order TX = " + txId);
        } catch (RpcException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String cancelTx = serumManager.cancelOrdersByClientId(
                account,
                xrpBearUsdcMarket,
                orders.stream().map(Order::getClientOrderId).collect(Collectors.toList()),
                openOrdersAccount,
                xrpBearWallet,
                usdcWallet
        );

        LOGGER.info("Cancel TX = " + cancelTx);

        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        orders.clear();
        transaction = new Transaction();

        for (int i = 1; i <= 9; i++) {
            final Order order = Order.builder()
                    .floatPrice(0.01F + (0.01f * 1 / 2 * i))
                    .floatQuantity(10 - i)
                    .orderTypeLayout(OrderTypeLayout.POST_ONLY)
                    .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                    .clientOrderId(new SecureRandom().nextLong())
                    .buy(true)
                    .build();

            serumManager.setOrderPrices(order, xrpBearUsdcMarket);

            transaction.addInstruction(
                    SerumProgram.placeOrder(
                            account,
                            usdcWallet,
                            openOrdersAccount.getOwnPubkey(),
                            xrpBearUsdcMarket,
                            order
                    )
            );

            LOGGER.info(String.format("Order ID %d", order.getClientOrderId()));
            orders.add(order);
        }

        try {
            String txId = client.getApi().sendTransaction(transaction, account);
            LOGGER.info("Place order TX = " + txId);
        } catch (RpcException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cancelTx = serumManager.cancelOrdersByClientId(
                account,
                xrpBearUsdcMarket,
                orders.stream().map(Order::getClientOrderId).collect(Collectors.toList()),
                openOrdersAccount,
                xrpBearWallet,
                usdcWallet
        );

        LOGGER.info("Cancel TX = " + cancelTx);

    }

    @Test
    @Ignore
    public void srmFeeDiscountTest() {
        final PublicKey xrpBearWallet = PublicKey.valueOf("3Hbga31dmqqLauAUtHXyemNYXB1jYnS4t1ExmSdfe4sD"); // XRPBEAR
        final PublicKey usdcWallet = PublicKey.valueOf("A71WvME6ZhR4SFG3Ara7zQK5qdRSB97jwTVmB3sr7XiN");
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("secretkey.dat"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Account account = new Account(Base58.decode(new String(data)));
        final Market xrpBearUsdcMarket = new MarketBuilder()
                .setPublicKey(PublicKey.valueOf("G2aPyW7r3gfW8GnRumiXXp1567XzMZsfwvbgxDiaNR4U")) // XRPBEAR/USDC
                .setClient(client)
                .setRetrieveDecimalsOnly(true)
                .build();

        OpenOrdersAccount openOrdersAccount = SerumUtils.findOpenOrdersAccountForOwner(
                client,
                xrpBearUsdcMarket.getOwnAddress(),
                account.getPublicKey()
        );

        final Order order = Order.builder()
                .floatPrice(0.1f)
                .floatQuantity(1)
                .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                .orderTypeLayout(OrderTypeLayout.POST_ONLY)
                .clientOrderId(new SecureRandom().nextLong())
                .buy(true).build();

        String txId = serumManager.placeOrder(
                account,
                xrpBearUsdcMarket,
                order,
                xrpBearWallet,
                usdcWallet,
                openOrdersAccount,
                PublicKey.valueOf("CvviJtGxVhCJsnJ8akbFgZs1ARzxrf6rLxpBn8GGP3wG") // My SRM wallet
        );

        LOGGER.info("Discount TX = " + txId);
    }

    @Test
    @Ignore
    public void spoofyTest() {
        final PublicKey xrpBearWallet = PublicKey.valueOf("3Hbga31dmqqLauAUtHXyemNYXB1jYnS4t1ExmSdfe4sD"); // XRPBEAR
        final PublicKey usdcWallet = PublicKey.valueOf("A71WvME6ZhR4SFG3Ara7zQK5qdRSB97jwTVmB3sr7XiN");

        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("secretkey.dat"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Account account = new Account(Base58.decode(new String(data)));

        final Market xrpBearUsdcMarket = new MarketBuilder()
                .setPublicKey(PublicKey.valueOf("G2aPyW7r3gfW8GnRumiXXp1567XzMZsfwvbgxDiaNR4U")) // XRPBEAR/USDC
                .setClient(client)
                .setRetrieveDecimalsOnly(true)
                .build();

        OpenOrdersAccount openOrdersAccount = SerumUtils.findOpenOrdersAccountForOwner(
                client,
                xrpBearUsdcMarket.getOwnAddress(),
                account.getPublicKey()
        );

        for (int count = 0; count < 3; count++) {
            final Transaction transaction = new Transaction();

            for (int i = 1; i <= 10; i++) {
                long orderId = 10000L + i;

                final Order order = Order.builder()
                        .floatPrice(0.01f * i)
                        .floatQuantity(1)
                        .clientOrderId(orderId)
                        .orderTypeLayout(OrderTypeLayout.POST_ONLY)
                        .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                        .buy(true).build();

                serumManager.setOrderPrices(order, xrpBearUsdcMarket);

                transaction.addInstruction(
                        SerumProgram.placeOrder(
                                account,
                                usdcWallet,
                                openOrdersAccount.getOwnPubkey(),
                                xrpBearUsdcMarket,
                                order
                        )
                );
            }

            try {
                String transactionId = client.getApi().sendTransaction(transaction, account);

                LOGGER.info("Create orders TX = " + transactionId);

                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                final Transaction cancelTransaction = new Transaction();
                for (int i = 1; i <= 10; i++) {
                    cancelTransaction.addInstruction(
                            SerumProgram.cancelOrderByClientId(
                                    xrpBearUsdcMarket,
                                    openOrdersAccount.getOwnPubkey(),
                                    account.getPublicKey(),
                                    10000L + i
                            )
                    );
                }

                cancelTransaction.addInstruction(
                        SerumProgram.settleFunds(
                                xrpBearUsdcMarket,
                                openOrdersAccount.getOwnPubkey(),
                                account.getPublicKey(),
                                xrpBearWallet,
                                usdcWallet
                        )
                );
                // double cancel
                client.getApi().sendTransaction(cancelTransaction, account);
                String cancelTxId = client.getApi().sendTransaction(cancelTransaction, account);
                LOGGER.info("Cancel TX = " + cancelTxId);

                Thread.sleep(2000L);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    @Ignore
    public void cancelOrderByClientIdAndSettleLoopedTest() {
        final PublicKey lqidWallet = PublicKey.valueOf("5uRbRHoVD6EeBM3MLjx7GadMxbprvNvABZGfmS1hVVGG");
        final PublicKey usdcWallet = PublicKey.valueOf("A71WvME6ZhR4SFG3Ara7zQK5qdRSB97jwTVmB3sr7XiN");

        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("secretkey.dat"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Account account = new Account(Base58.decode(new String(data)));

        final Market lqidUsdcMarket = new MarketBuilder()
                .setPublicKey(PublicKey.valueOf("4FPFh1iAiitKYMCPDBmEQrZVgA1DVMKHZBU2R7wjQWuu")) // LQID/USDC
                .setClient(client)
                .setRetrieveDecimalsOnly(true)
                .build();

        final OpenOrdersAccount openOrdersAccount = SerumUtils.findOpenOrdersAccountForOwner(
                client,
                lqidUsdcMarket.getOwnAddress(),
                account.getPublicKey()
        );

        for (int i = 1; i <= 15; i++) {
            long orderId = 10000L + i;

            final Order order = Order.builder()
                    .floatPrice(0.18f - (i * .001f))
                    .floatQuantity(0.01f)
                    .clientOrderId(orderId)
                    .orderTypeLayout(OrderTypeLayout.POST_ONLY)
                    .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                    .buy(true).build();

            // Place order 1
            String transactionId = serumManager.placeOrder(
                    account,
                    lqidUsdcMarket,
                    order,
                    lqidWallet,
                    usdcWallet,
                    openOrdersAccount
            );

            LOGGER.info(String.format("TX %d: %s", orderId, transactionId));
            assertNotNull(transactionId);
        }

        for (int i = 1; i <= 15; i++) {
            long orderId = 10000L + i;

            String cancelTransactionId = serumManager.cancelOrderByClientIdAndSettle(
                    account,
                    lqidUsdcMarket,
                    orderId,
                    openOrdersAccount,
                    lqidWallet,
                    usdcWallet
            );

            LOGGER.info(String.format("Cancel TX %d: %s", orderId, cancelTransactionId));
            assertNotNull(cancelTransactionId);
        }
    }

    // Requires 0.1 MER to offer
    @Test
    @Ignore
    public void cancelOrderByClientIdAndSettleTest() {
        // Replace with the public key of your MER and USDC wallet
        final PublicKey merWallet = PublicKey.valueOf("FqZv3vNbLMcVXvV7yH8LmPKitn5nhLcpnd981JSFF7jf"); // needs 0.1 mer
        final PublicKey usdcWallet = PublicKey.valueOf("A71WvME6ZhR4SFG3Ara7zQK5qdRSB97jwTVmB3sr7XiN");

        // Build account from secretkey.dat
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("secretkey.dat"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create account from private key
        final Account account = new Account(Base58.decode(new String(data)));

        // Get OXY/USDC market
        final Market merUsdcMarket = new MarketBuilder()
                .setPublicKey(PublicKey.valueOf("HhvDWug3ftYNx5148ZmrQxzvEmohN2pKVNiRT4TVoekF")) // MER/USDC
                .setClient(client)
                .setRetrieveDecimalsOnly(true)
                .build();

        final OpenOrdersAccount openOrdersAccount = SerumUtils.findOpenOrdersAccountForOwner(
                client,
                merUsdcMarket.getOwnAddress(),
                account.getPublicKey()
        );

        long orderId = 10000L;

        // 0.1 mer offer @ $1337
        final Order order = Order.builder()
                .floatPrice(1000f)
                .floatQuantity(0.1f)
                .clientOrderId(orderId)
                .orderTypeLayout(OrderTypeLayout.POST_ONLY)
                .selfTradeBehaviorLayout(SelfTradeBehaviorLayout.DECREMENT_TAKE)
                .buy(false).build();

        // Place order 1
        String transactionId = serumManager.placeOrder(
                account,
                merUsdcMarket,
                order,
                merWallet,
                usdcWallet,
                openOrdersAccount
        );

        LOGGER.info(String.format("TX %d: %s", orderId, transactionId));

        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Cancel order 1
        String cancelTransactionId = serumManager.cancelOrderByClientIdAndSettle(
                account,
                merUsdcMarket,
                orderId,
                openOrdersAccount,
                merWallet,
                usdcWallet
        );

        LOGGER.info(String.format("Cancel TX %d: %s", orderId, cancelTransactionId));

        assertNotNull(transactionId);
    }

    @Test
    @Ignore
    public void testOpenOrdersAccounts() {
        final PublicKey skynetMainnetTestAccount = PublicKey.valueOf("F459S1MFG2whWbznzULPkYff6TFe2QjoKhgHXpRfDyCj");

        List<PublicKey> marketsToSearch = List.of(
                PublicKey.valueOf("9wFFyRfZBsuAha4YcuxcXLKwMxJR43S7fPfQLusDBzvT"),
                PublicKey.valueOf("HWHvQhFmJB3NUcu1aihKmrKegfVxBEHzwVX6yZCKEsi1"),
                PublicKey.valueOf("jyei9Fpj2GtHLDDGgcuhDacxYLLiSyxU4TY7KxB2xai"),
                PublicKey.valueOf("C6tp2RVZnxBPFbnAsfTjis8BN9tycESAT4SgDQgbbrsA"),
                PublicKey.valueOf("HCyhGnC77f7DaxQEvzj59g9ve7eJJXjsMYFWo4t7shcj")

        );

        LOGGER.info(
                String.format(
                        "Pubkey: %s\nSearching markets: %s",
                        skynetMainnetTestAccount.toBase58(),
                        marketsToSearch
                                .stream()
                                .map(PublicKey::toBase58)
                                .collect(Collectors.joining(", "))
                )
        );

        marketsToSearch.forEach(market -> {
            // get open orders account for a known pubkey
            int dataSize = 3228;

            List<ProgramAccount> programAccounts = null;

            Memcmp marketFilter = new Memcmp(SerumUtils.OWN_ADDRESS_OFFSET, market.toBase58());
            Memcmp ownerFilter = new Memcmp(45, skynetMainnetTestAccount.toBase58());

            List<Memcmp> memcmpList = List.of(marketFilter, ownerFilter);

            try {
                programAccounts = client.getApi().getProgramAccounts(SerumUtils.SERUM_PROGRAM_ID_V3, memcmpList, dataSize);
            } catch (RpcException e) {
                e.printStackTrace();
            }

            if (programAccounts != null) {
                programAccounts.forEach(programAccount -> {
                    // Get balance
                    // LOGGER.info("Open orders data = " + programAccount.getAccount().getData());

                    byte[] data = programAccount.getAccount().getDecodedData();
                    OpenOrdersAccount openOrdersAccount = OpenOrdersAccount.readOpenOrdersAccount(data);

                    boolean hasUnsettledFunds = false;

                    if (openOrdersAccount.getBaseTokenTotal() > 0 || openOrdersAccount.getQuoteTokenTotal() > 0) {
                        LOGGER.info(
                                String.format(
                                        "Found amount: %d on Market %s",
                                        openOrdersAccount.getBaseTokenTotal() + openOrdersAccount.getQuoteTokenTotal(),
                                        openOrdersAccount.getMarket()
                                )
                        );

                        // cancel orders before settlement
                        boolean isOrdersCancelled = false;

                        // cancel orders, set the bool
                        if (isOrdersCancelled) {
                            hasUnsettledFunds = true;
                        }
                    }

                    if (openOrdersAccount.getBaseTokenFree() > 0 || openOrdersAccount.getQuoteTokenFree() > 0) {
                        // settle funds
                        hasUnsettledFunds = true;
                    }

                    if (hasUnsettledFunds) {
                        LOGGER.info(String.format("Settling funds on market %s.", openOrdersAccount.getMarket()));
                    }

                    // Sleep so we don't get rate limited
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                });
            }
        });

        assertTrue(true);
    }

    @Test
    @Ignore
    public void singleOpenOrdersAccount() {
        final OpenOrdersAccount openOrdersAccount = SerumUtils.findOpenOrdersAccountForOwner(
                client,
                new PublicKey("G2aPyW7r3gfW8GnRumiXXp1567XzMZsfwvbgxDiaNR4U"),
                new PublicKey("F459S1MFG2whWbznzULPkYff6TFe2QjoKhgHXpRfDyCj")
        );

        LOGGER.info(openOrdersAccount.toString());
    }

    // Doesn't work yet
    @Test
    @Ignore
    public void consumeEventsTest() {
        LOGGER.info("Consuming events");

        // Build account from secretkey.dat
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(Paths.get("secretkey.dat"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create account from private key
        final Account account = new Account(Base58.decode(new String(data)));

        // Get SOL/USDC market
        final Market solUsdcMarket = new MarketBuilder()
                .setPublicKey(SOL_USDC_MARKET_V3)
                .setRetrieveOrderBooks(false)
                .setClient(client)
                .build();

        final PublicKey baseWallet = PublicKey.valueOf("Dc9tWM7oSgKDWcThGCnYhwQxLwnQ3e2J2WkyC6E2F1AG"); //wrapped sol
        final PublicKey quoteWallet = PublicKey.valueOf("A71WvME6ZhR4SFG3Ara7zQK5qdRSB97jwTVmB3sr7XiN");

        final OpenOrdersAccount openOrdersAccount = SerumUtils.findOpenOrdersAccountForOwner(
                client,
                solUsdcMarket.getOwnAddress(),
                account.getPublicKey()
        );

        final PublicKey openOrdersPubkey = openOrdersAccount.getOwnPubkey();

        // ConsumeEvents with our pubkey 5 times
        String transactionId = serumManager.consumeEvents(
                account,
                solUsdcMarket,
                List.of(
                        openOrdersPubkey,
                        openOrdersPubkey,
                        openOrdersPubkey,
                        openOrdersPubkey,
                        openOrdersPubkey
                ),
                baseWallet,
                quoteWallet
        );

        LOGGER.info("Consume Events TX = " + transactionId);

        // Verify we got a txId
        assertNotNull(transactionId);
    }

    @Test
    @Ignore
    public void lqidUsdcTest() {
        byte[] data = new byte[0];

        try {
            data = Files.readAllBytes(Paths.get("src/test/resources/lqidusdc.bin"));  // LQID/USDC
        } catch (IOException e) {
            e.printStackTrace();
        }

        OrderBook bidOrderBook = OrderBook.readOrderBook(data);
        System.out.println(bidOrderBook.getAccountFlags().toString());
        Slab slab = bidOrderBook.getSlab();

        assertNotNull(slab);

        /* C:\apps\solanaj\lqidusdc.bin (1/12/2021 8:55:59 AM)
            StartOffset(d): 00001709, EndOffset(d): 00001724, Length(d): 00000016 */

        // this rawData = key bytes for a 477.080 quantity bid at 0.0510 cents

        byte[] rawData = {
                (byte) 0xFC, (byte) 0xFD, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0x33, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        };


        slab.getSlabNodes().forEach(slabNode -> {
            if (slabNode instanceof SlabLeafNode) {
                SlabLeafNode slabLeafNode = (SlabLeafNode) slabNode;
                if (Arrays.equals(rawData, slabLeafNode.getKey())) {
                    System.out.println("Found the order");
                }
                System.out.println(slabNode);
            }
        });
    }
}
