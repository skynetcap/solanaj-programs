package com.mmorrell.zeta.program;

import com.mmorrell.common.model.Market;
import com.mmorrell.common.model.Order;
import com.mmorrell.zeta.model.ZetaOrderType;
import com.mmorrell.zeta.model.ZetaSide;
import com.mmorrell.zeta.util.ZetaUtil;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.Program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ZetaProgram extends Program {

    private static final PublicKey TOKEN_PROGRAM_ID =
            PublicKey.valueOf("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
    private static final PublicKey SYSVAR_RENT_PUBKEY =
            PublicKey.valueOf("SysvarRent111111111111111111111111111111111");

    private static final byte[] CANCEL_ALL_MARKET_ORDERS_SIGHASH = new byte[] {
            (byte) 0x8b, (byte) 0xbe, (byte) 0xe6, (byte) 0xf9, (byte) 0x4d, (byte) 0xa0, (byte) 0xce, (byte) 0x04
    };

    private static final byte[] PLACE_ORDER_SIGHASH = new byte[] {
            (byte) 0x92, (byte) 0x5D, (byte) 0x0E, (byte) 0xA7, (byte) 0x9F, (byte) 0x14, (byte) 0x06, (byte) 0x3A
    };

    private static final String PLACE_ORDER_TAG = "sky";

    public static TransactionInstruction cancelAllMarketOrders(PublicKey authority,
                                                               PublicKey zetaGroup,
                                                               PublicKey state,
                                                               PublicKey marginAccount,
                                                               PublicKey serumAuthority,
                                                               PublicKey openOrders,
                                                               Market market) {
        final ArrayList<AccountMeta> keys = new ArrayList<>();

        // Signer
        final AccountMeta authorityMeta = new AccountMeta(authority, true, false);

        // Cancel Accounts
        final AccountMeta zetaGroupMeta = new AccountMeta(zetaGroup, false, false);
        final AccountMeta stateMeta = new AccountMeta(state, false, false);
        final AccountMeta marginAccountMeta = new AccountMeta(marginAccount, false, true);
        final AccountMeta dexProgram = new AccountMeta(ZetaUtil.ZETA_SERUM_PROGRAM_ID, false, false);
        final AccountMeta serumAuthorityMeta = new AccountMeta(serumAuthority, false, false);
        final AccountMeta openOrdersMeta = new AccountMeta(openOrders, false, true);
        final AccountMeta marketMeta = new AccountMeta(market.getOwnAddress(), false, true);
        final AccountMeta bids = new AccountMeta(market.getBids(), false, true);
        final AccountMeta asks = new AccountMeta(market.getAsks(), false, true);
        final AccountMeta eventQueue = new AccountMeta(market.getEventQueueKey(), false, true);

        keys.add(authorityMeta);
        keys.add(zetaGroupMeta);
        keys.add(stateMeta);
        keys.add(marginAccountMeta);
        keys.add(dexProgram);
        keys.add(serumAuthorityMeta);
        keys.add(openOrdersMeta);
        keys.add(marketMeta);
        keys.add(bids);
        keys.add(asks);
        keys.add(eventQueue);

        return createTransactionInstruction(
                ZetaUtil.ZETA_ROOT_PROGRAM_ID,
                keys,
                CANCEL_ALL_MARKET_ORDERS_SIGHASH
        );
    }

    public static TransactionInstruction placeOrder(PublicKey authority,
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
        final AccountMeta authorityMeta = new AccountMeta(authority, true, false);
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

        ByteBuffer result = ByteBuffer.allocate(35);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.put(0, PLACE_ORDER_SIGHASH);
        result.putLong(8, price);
        result.putLong(16, size);
        result.put(24, side.getValue());
        result.put(25, orderType.getValue());
        result.put(26, new byte[]{0x00, 0x01, 0x03, 0x00, 0x00, 0x00});
        result.put(32, PLACE_ORDER_TAG.getBytes());

        System.out.println("Placed order: Size " + order.getQuantity() + " @ Price " + order.getPrice());
        // System.out.println("placeOrder Zeta hex: " + ByteUtils.bytesToHex(result.array()));

        return result.array();
    }
}
