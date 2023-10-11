package com.mmorrell.openbook.program;

import com.google.common.primitives.Bytes;
import com.mmorrell.openbook.OpenBookUtil;

import com.syntifi.near.borshj.Borsh;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.Program;
import org.p2p.solanaj.programs.SystemProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for creating Serum v3 {@link TransactionInstruction}s
 */
public class OpenbookProgram extends Program {

    private static final PublicKey TOKEN_PROGRAM_ID =
            PublicKey.valueOf("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
    private static final PublicKey SYSVAR_RENT_PUBKEY =
            PublicKey.valueOf("SysvarRent111111111111111111111111111111111");
    public static final PublicKey OPENBOOK_V2_PROGRAM_ID = PublicKey.valueOf(
            "opnbkNkqux64GppQhwbyEVc3axhssFhVYuwar8rDHCu");


    // Open Book methods to implement
    // CreateOpenOrdersIndexer
    // aka create_open_orders_indexer
    // https://github.com/openbook-dex/openbook-v2/blob/master/programs/openbook-v2/src/accounts_ix/create_open_orders_indexer.rs

    public static TransactionInstruction createOpenOrdersIndexer(Account payer, PublicKey market) throws Exception {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(payer.getPublicKey(), true, true));
        keys.add(new AccountMeta(payer.getPublicKey(), false, true));

        PublicKey indexerPda = PublicKey.findProgramAddress(
                List.of(
                        "OpenOrdersIndexer".getBytes(),
                        payer.getPublicKey().toByteArray()
                ),
                OPENBOOK_V2_PROGRAM_ID
        ).getAddress();

        keys.add(new AccountMeta(indexerPda, false, true));
        keys.add(new AccountMeta(market, false, false));
        keys.add(new AccountMeta(SystemProgram.PROGRAM_ID, false, false));

        byte[] transactionData = OpenBookUtil.encodeNamespace("global:create_open_orders_indexer");
        return createTransactionInstruction(
                OPENBOOK_V2_PROGRAM_ID,
                keys,
                transactionData
        );
    }

    /**
     * Create an OpenBook v2 market.
     *
     * @param caller
     * @param baseMint
     * @param quoteMint
     * @param baseVault
     * @param quoteVault
     * @return
     * @throws Exception
     */
    public static TransactionInstruction createMarket(Account caller, Account market, PublicKey baseMint,
                                                      PublicKey quoteMint, PublicKey baseVault,
                                                      PublicKey quoteVault) throws Exception {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(market.getPublicKey(), true, true));

        // marketAuthority
        PublicKey marketAuthorityPda = PublicKey.findProgramAddress(
                List.of(
                        "Market".getBytes(),
                        market.getPublicKey().toByteArray()
                ),
                OPENBOOK_V2_PROGRAM_ID
        ).getAddress();

        keys.add(new AccountMeta(marketAuthorityPda, false, false)); //marketAuthority

        Account bidAccount = new Account();
        Account askAccount = new Account();
        Account heapAccount = new Account();

        keys.add(new AccountMeta(bidAccount.getPublicKey(), false, true)); //bids
        keys.add(new AccountMeta(askAccount.getPublicKey(), false, true)); //asks
        keys.add(new AccountMeta(heapAccount.getPublicKey(), false, true)); //eventHeap

        // payer
        keys.add(new AccountMeta(caller.getPublicKey(), true, true)); // payer

        // marketBaseVault
        keys.add(new AccountMeta(baseVault, false, false));

        // marketQuoteVault
        keys.add(new AccountMeta(quoteVault, false, false));

        // baseMint
        keys.add(new AccountMeta(baseMint, false, false));

        // quoteMint
        keys.add(new AccountMeta(quoteMint, false, false));

        // system
        keys.add(new AccountMeta(SystemProgram.PROGRAM_ID, false, false));

        // Remaining accounts: oracleA, oracleB (stub them)
        keys.add(new AccountMeta(OPENBOOK_V2_PROGRAM_ID, false, false));
        keys.add(new AccountMeta(OPENBOOK_V2_PROGRAM_ID, false, false));

        // stub collectFeeAdmin
        keys.add(new AccountMeta(OPENBOOK_V2_PROGRAM_ID, false, false));

        // stub openOrdersAdmin, consumeEventsAdmin, closeMarketAdmin
        keys.add(new AccountMeta(OPENBOOK_V2_PROGRAM_ID, false, false));
        keys.add(new AccountMeta(OPENBOOK_V2_PROGRAM_ID, false, false));
        keys.add(new AccountMeta(OPENBOOK_V2_PROGRAM_ID, false, false));

        // stub eventAuthority
        keys.add(new AccountMeta(OPENBOOK_V2_PROGRAM_ID, false, false));

        // program
        keys.add(new AccountMeta(OPENBOOK_V2_PROGRAM_ID, false, false));


        // sighash (first 8 bytes)
        byte[] transactionData = OpenBookUtil.encodeNamespace("global:create_market");

        // name (string)
        byte[] name = Borsh.serialize("Skynet's Den");

        // for OracleConfigParams, write "0" 64 bits wide, covers 2 i32 or just Borsh.serialize((long) 8)
        byte[] oracleConfigParams = Borsh.serialize(0L);
        byte[] quoteLotSize = Borsh.serialize(10L);
        byte[] baseLotSize = Borsh.serialize(100L);
        byte[] makerFee = Borsh.serialize(0L);
        byte[] takerFee = Borsh.serialize(20L);
        byte[] timeExpiry = Borsh.serialize(0L);

        byte[] anchorInstructionData = Bytes.concat(
                transactionData,
                name,
                oracleConfigParams,
                quoteLotSize,
                baseLotSize,
                makerFee,
                takerFee,
                timeExpiry
        );

        return createTransactionInstruction(
                OPENBOOK_V2_PROGRAM_ID,
                keys,
                anchorInstructionData
        );
    }
}
