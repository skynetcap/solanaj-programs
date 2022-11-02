package com.mmorrell.zeta.program;

import com.mmorrell.common.SerumUtils;
import com.mmorrell.common.model.Market;
import com.mmorrell.common.model.MarketBuilder;
import com.mmorrell.common.model.OpenOrdersAccount;
import com.mmorrell.common.model.Order;
import com.mmorrell.common.model.SideLayout;
import com.mmorrell.zeta.model.ZetaOrderType;
import com.mmorrell.zeta.model.ZetaSide;
import com.mmorrell.zeta.util.ZetaUtil;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.Program;
import org.p2p.solanaj.utils.ByteUtils;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ZetaProgram extends Program {

    private static final PublicKey TOKEN_PROGRAM_ID =
            PublicKey.valueOf("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
    private static final PublicKey SYSVAR_RENT_PUBKEY =
            PublicKey.valueOf("SysvarRent111111111111111111111111111111111");

    private static final int MATCH_ORDERS_METHOD_ID = 2;
    private static final int CONSUME_EVENTS_METHOD_ID = 3;
    private static final int SETTLE_ORDERS_METHOD_ID = 5;
    private static final int CANCEL_ORDER_V2_METHOD_ID = 11;
    private static final int CANCEL_ORDER_BY_CLIENT_ID_V2_METHOD_ID = 12;

    /**
     * Builds a {@link TransactionInstruction} to match orders for a given Market and limit.
     * Might not be needed in Serum v3, since request queue handling changed.
     *
     * @param market market to crank
     * @param limit  number of orders to match
     * @return {@link TransactionInstruction} for the matchOrders call
     */
    public static TransactionInstruction matchOrders(Market market, int limit) {
        List<AccountMeta> accountMetas = new ArrayList<>();

        accountMetas.add(new AccountMeta(market.getOwnAddress(), false, true));
        accountMetas.add(new AccountMeta(market.getRequestQueue(), false, true));
        accountMetas.add(new AccountMeta(market.getEventQueueKey(), false, true));
        accountMetas.add(new AccountMeta(market.getBids(), false, true));
        accountMetas.add(new AccountMeta(market.getAsks(), false, true));
        accountMetas.add(new AccountMeta(market.getBaseVault(), false, true));
        accountMetas.add(new AccountMeta(market.getQuoteVault(), false, true));

        byte[] transactionData = encodeMatchOrdersTransactionData(
                limit
        );

        return createTransactionInstruction(
                ZetaUtil.ZETA_SERUM_PROGRAM_ID,
                accountMetas,
                transactionData
        );
    }

    /**
     * Encodes the limit parameter used in match orders instructions into a byte array
     *
     * @param limit number of orders to match
     * @return transaction data
     */
    private static byte[] encodeMatchOrdersTransactionData(int limit) {
        ByteBuffer result = ByteBuffer.allocate(7);
        result.order(ByteOrder.LITTLE_ENDIAN);

        result.put(1, (byte) MATCH_ORDERS_METHOD_ID);
        result.putShort(5, (short) limit);

        return result.array();
    }

    public static TransactionInstruction placeOrder(Account account,
                                                    PublicKey state,
                                                    PublicKey zetaGroup,
                                                    PublicKey marginAccount,
                                                    PublicKey serumAuthority,
                                                    PublicKey greeks,
                                                    PublicKey openOrders,
                                                    PublicKey orderPayer,
                                                    PublicKey coinWallet,
                                                    PublicKey pcWallet,
                                                    PublicKey oracle,
                                                    PublicKey marketNode,
                                                    PublicKey marketMint,
                                                    PublicKey mintAuthority,
                                                    Market market,
                                                    Order order,
                                                    ZetaSide side) {

        final AccountMeta stateMeta = new AccountMeta(state, false, false);
        final AccountMeta zetaGroupMeta = new AccountMeta(zetaGroup, false, false);
        final AccountMeta marginAccountMeta = new AccountMeta(marginAccount, false, true);
        final AccountMeta authorityMeta = new AccountMeta(account.getPublicKey(), true, false);
        final AccountMeta dexProgram = new AccountMeta(ZetaUtil.ZETA_SERUM_PROGRAM_ID, false, false);
        final AccountMeta tokenProgram = new AccountMeta(TOKEN_PROGRAM_ID, false, false);
        final AccountMeta serumAuthorityMeta = new AccountMeta(serumAuthority, false, false);
        final AccountMeta greeksMeta = new AccountMeta(greeks, false, false);
        final AccountMeta openOrdersMeta = new AccountMeta(openOrders, false, true);
        final AccountMeta rent = new AccountMeta(SYSVAR_RENT_PUBKEY, false, false);

        // marketAccounts
        final AccountMeta marketMeta = new AccountMeta(market.getOwnAddress(), false, true);
        final AccountMeta requestQueue = new AccountMeta(market.getRequestQueue(), false, true);
        final AccountMeta eventQueue = new AccountMeta(market.getEventQueueKey(), false, true);
        final AccountMeta bids = new AccountMeta(market.getBids(), false, true);
        final AccountMeta asks = new AccountMeta(market.getAsks(), false, true);
        final AccountMeta orderPayerTokenAccount = new AccountMeta(orderPayer, false, true);
        final AccountMeta coinVault = new AccountMeta(market.getBaseVault(), false, true);
        final AccountMeta pcVault = new AccountMeta(market.getQuoteVault(), false, true);
        final AccountMeta coinWalletMeta = new AccountMeta(coinWallet, false, true);
        final AccountMeta pcWalletMeta = new AccountMeta(pcWallet, false, true);

        // Last accounts
        final AccountMeta oracleMeta = new AccountMeta(oracle, false, false);
        final AccountMeta marketNodeMeta = new AccountMeta(marketNode, false, true);
        final AccountMeta marketMintMeta = new AccountMeta(marketMint, false, true);
        final AccountMeta mintAuthorityMeta = new AccountMeta(mintAuthority, false, false);

        ZetaOrderType orderType = ZetaOrderType.LIMIT;

        final List<AccountMeta> keys = new ArrayList<>(List.of(
                stateMeta,
                zetaGroupMeta,
                marginAccountMeta,
                authorityMeta,
                dexProgram,
                tokenProgram,
                serumAuthorityMeta,
                greeksMeta,
                openOrdersMeta,
                rent,
                marketMeta,
                requestQueue,
                eventQueue,
                bids,
                asks,
                orderPayerTokenAccount,
                coinVault,
                pcVault,
                coinWalletMeta,
                pcWalletMeta,
                oracleMeta,
                marketNodeMeta,
                marketMintMeta,
                mintAuthorityMeta
        ));

        byte[] transactionData = buildNewOrderv3InstructionData(
                order,
                side,
                orderType
        );

        return createTransactionInstruction(ZetaUtil.ZETA_ROOT_PROGRAM_ID, keys, transactionData);
    }

    /**
     * Encodes the {@link Order} object into a byte array usable with newOrderV3 instructions
     *
     * @param order {@link Order} object containing all required details
     * @return transaction data
     */
    public static byte[] buildNewOrderv3InstructionData(Order order, ZetaSide side, ZetaOrderType orderType) {
        long price = order.getPrice();
        long size = order.getQuantity();

        byte[] sigHash = new byte[]{
                (byte) 0x92, (byte) 0x5D, (byte) 0x0E, (byte) 0xA7, (byte) 0x9F, (byte) 0x14,
                (byte) 0x06, (byte) 0x3A
        };

        ByteBuffer result = ByteBuffer.allocate(35);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.put(0, sigHash);
        result.putLong(8, price);
        result.putLong(16, size);
        result.put(24, side.getValue());
        result.put(25, orderType.getValue());
        result.put(26, new byte[]{0x00, 0x01, 0x03, 0x00, 0x00, 0x00});
        result.put(32, "sky".getBytes());

        System.out.println("placeOrder Zeta hex: " + ByteUtils.bytesToHex(result.array()));

        byte[] arrayResult = result.array();
        return arrayResult;
    }

    /**
     * Builds a {@link TransactionInstruction} to cancel an existing Serum order by client ID.
     *
     * @param market     loaded market that we are trading on. this must be built by a {@link MarketBuilder}
     * @param openOrders open orders pubkey associated with this Account and market - look up using {@link SerumUtils}
     * @param owner      pubkey of your SOL wallet
     * @param clientId   identifier created before order creation that is associated with this order
     * @return {@link TransactionInstruction} for the cancelOrderByClientIdV2 call
     */
    public static TransactionInstruction cancelOrderByClientId(Market market,
                                                               PublicKey openOrders,
                                                               PublicKey owner,
                                                               long clientId) {
        List<AccountMeta> accountMetas = new ArrayList<>();

        accountMetas.add(new AccountMeta(market.getOwnAddress(), false, false));
        accountMetas.add(new AccountMeta(market.getBids(), false, true));
        accountMetas.add(new AccountMeta(market.getAsks(), false, true));
        accountMetas.add(new AccountMeta(openOrders, false, true));
        accountMetas.add(new AccountMeta(owner, true, false));
        accountMetas.add(new AccountMeta(market.getEventQueueKey(), false, true));

        byte[] transactionData = encodeCancelOrderByClientIdTransactionData(
                clientId
        );

        return createTransactionInstruction(
                ZetaUtil.ZETA_SERUM_PROGRAM_ID,
                accountMetas,
                transactionData
        );
    }

    /**
     * Encodes the clientId parameter used in cancelOrderByClientIdV2 instructions into a byte array
     *
     * @param clientId user-generated identifier associated with the order
     * @return transaction data
     */
    private static byte[] encodeCancelOrderByClientIdTransactionData(long clientId) {
        ByteBuffer result = ByteBuffer.allocate(13);
        result.order(ByteOrder.LITTLE_ENDIAN);

        result.put(1, (byte) CANCEL_ORDER_BY_CLIENT_ID_V2_METHOD_ID);
        result.putLong(5, clientId);

        return result.array();
    }

    /**
     * Builds a {@link TransactionInstruction} to cancel an existing Serum order by client ID.
     *
     * @param market        loaded market that we are trading on. this must be built by a {@link MarketBuilder}
     * @param openOrders    open orders pubkey associated with this Account and market - look up using {@link SerumUtils}
     * @param owner         pubkey of your SOL wallet
     * @param side          side of the order - buy or sell
     * @param clientOrderId byte array containing the clientOrderId - retrieved from an {@link OpenOrdersAccount}
     * @return {@link TransactionInstruction} for the cancelOrderByClientIdV2 call
     */
    public static TransactionInstruction cancelOrder(Market market,
                                                     PublicKey openOrders,
                                                     PublicKey owner,
                                                     SideLayout side,
                                                     byte[] clientOrderId) {
        List<AccountMeta> accountMetas = new ArrayList<>();

        accountMetas.add(new AccountMeta(market.getOwnAddress(), false, false));
        accountMetas.add(new AccountMeta(market.getBids(), false, true));
        accountMetas.add(new AccountMeta(market.getAsks(), false, true));
        accountMetas.add(new AccountMeta(openOrders, false, true));
        accountMetas.add(new AccountMeta(owner, true, false));
        accountMetas.add(new AccountMeta(market.getEventQueueKey(), false, true));

        byte[] transactionData = encodeCancelOrderTransactionData(
                side,
                clientOrderId
        );

        return createTransactionInstruction(
                ZetaUtil.ZETA_SERUM_PROGRAM_ID,
                accountMetas,
                transactionData
        );
    }

    /**
     * Encodes the clientOrderId and SideLayout params used in cancelOrderV2 instructions into a byte array
     *
     * @param side          side of the order - buy or sell
     * @param clientOrderId byte array containing the clientOrderId
     * @return transaction data
     */
    private static byte[] encodeCancelOrderTransactionData(SideLayout side, byte[] clientOrderId) {
        ByteBuffer result = ByteBuffer.allocate(25);
        result.order(ByteOrder.LITTLE_ENDIAN);

        result.put(1, (byte) CANCEL_ORDER_V2_METHOD_ID);
        result.put(5, (byte) side.getValue());
        for (int i = 0; i < 15; i++) {
            result.put(9 + i, clientOrderId[i]);
        }

        return result.array();
    }

    /**
     * Builds a {@link TransactionInstruction} used to settle funds on a given Serum {@link Market}
     *
     * @param market      loaded market that we are trading on. this must be built by a {@link MarketBuilder}
     * @param openOrders  open orders pubkey associated with this Account and market - look up using {@link SerumUtils}
     * @param owner       pubkey of your SOL wallet / the signer
     * @param baseWallet  coin fee receivable account
     * @param quoteWallet pc fee receivable account
     * @return {@link TransactionInstruction} for the settleFunds call
     */
    public static TransactionInstruction settleFunds(Market market,
                                                     PublicKey openOrders,
                                                     PublicKey owner,
                                                     PublicKey baseWallet,
                                                     PublicKey quoteWallet) {
        List<AccountMeta> accountMetas = new ArrayList<>();

        accountMetas.add(new AccountMeta(market.getOwnAddress(), false, true));
        accountMetas.add(new AccountMeta(openOrders, false, true));
        accountMetas.add(new AccountMeta(owner, true, false));
        accountMetas.add(new AccountMeta(market.getBaseVault(), false, true));
        accountMetas.add(new AccountMeta(market.getQuoteVault(), false, true));
        accountMetas.add(new AccountMeta(baseWallet, false, true));
        accountMetas.add(new AccountMeta(quoteWallet, false, true));
        accountMetas.add(new AccountMeta(SerumUtils.getVaultSigner(market), false, false));
        accountMetas.add(new AccountMeta(TOKEN_PROGRAM_ID, false, false));

        byte[] transactionData = encodeSettleOrdersTransactionData();

        return createTransactionInstruction(
                ZetaUtil.ZETA_SERUM_PROGRAM_ID,
                accountMetas,
                transactionData
        );
    }

    /**
     * Encodes the default SettleFunds transaction data
     *
     * @return transaction data
     */
    private static byte[] encodeSettleOrdersTransactionData() {
        ByteBuffer result = ByteBuffer.allocate(5);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.put(1, (byte) SETTLE_ORDERS_METHOD_ID);
        return result.array();
    }

    /**
     * Builds a {@link TransactionInstruction} to call Consume Events for a given market and {@link PublicKey}s
     *
     * @param signer             pubkey of account signing the transaction
     * @param openOrdersAccounts list of all open orders accounts to consume in the event queue
     * @param market             market with the event queue we want to process
     * @param baseWallet         coin fee receivable account (?)
     * @param quoteWallet        pc fee receivable account (?)
     * @return {@link TransactionInstruction} for the Consume Events call
     */
    public static TransactionInstruction consumeEvents(PublicKey signer,
                                                       List<PublicKey> openOrdersAccounts,
                                                       Market market,
                                                       PublicKey baseWallet,
                                                       PublicKey quoteWallet) {
        List<AccountMeta> accountMetas = new ArrayList<>();

        accountMetas.add(new AccountMeta(signer, true, false));
        accountMetas.addAll(openOrdersAccounts.stream()
                .map(publicKey -> new AccountMeta(publicKey, false, true))
                .collect(Collectors.toList()));

        accountMetas.add(new AccountMeta(market.getOwnAddress(), false, true));
        accountMetas.add(new AccountMeta(market.getEventQueueKey(), false, true));
        accountMetas.add(new AccountMeta(baseWallet, false, true));
        accountMetas.add(new AccountMeta(quoteWallet, false, true));

        int limit = 5;
        byte[] transactionData = encodeConsumeEventsTransactionData(
                limit
        );

        return createTransactionInstruction(
                ZetaUtil.ZETA_SERUM_PROGRAM_ID,
                accountMetas,
                transactionData
        );
    }

    /**
     * Encodes the limit parameter used in ConsumeEvents instructions into a byte array
     *
     * @param limit number of events to consume
     * @return transaction data
     */
    private static byte[] encodeConsumeEventsTransactionData(int limit) {
        ByteBuffer result = ByteBuffer.allocate(7);
        result.order(ByteOrder.LITTLE_ENDIAN);

        result.put(1, (byte) CONSUME_EVENTS_METHOD_ID);
        result.put(5, (byte) limit);

        return result.array();
    }
}
