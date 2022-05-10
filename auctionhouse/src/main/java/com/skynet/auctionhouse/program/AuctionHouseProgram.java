package com.skynet.auctionhouse.program;

import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.Program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class AuctionHouseProgram extends Program {

    private static final PublicKey TOKEN_PROGRAM_ID =
            PublicKey.valueOf("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
    private static final PublicKey SYSTEM_PROGRAM_ID =
            PublicKey.valueOf("11111111111111111111111111111111");
    private static final PublicKey SYSVAR_RENT_PUBKEY =
            PublicKey.valueOf("SysvarRent111111111111111111111111111111111");

    public static TransactionInstruction sell(
            final PublicKey wallet,
            final PublicKey tokenAccount,
            final PublicKey metadata,
            final PublicKey authority,
            final PublicKey auctionHouse,
            final PublicKey auctionHouseFeeAccount,
            final PublicKey sellerTradeState,
            final PublicKey freeSellerTradeState,
            final PublicKey programAsSigner,
            final byte tradeStateBump,
            final byte freeTradeStateBump,
            final byte programAsSignerBump,
            final long buyerPrice,
            final long tokenSize
    ) {
        ByteBuffer buffer = ByteBuffer.allocate(27);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putLong(-5943767438166989261L); // anchor sighash for "sell"
        buffer.put(tradeStateBump);
        buffer.put(freeTradeStateBump);
        buffer.put(programAsSignerBump);
        buffer.putLong(buyerPrice);
        buffer.putLong(tokenSize);

        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(wallet, true, true));
        keys.add(new AccountMeta(tokenAccount, false, true));
        keys.add(new AccountMeta(metadata, false, false));
        keys.add(new AccountMeta(authority, true, true));
        keys.add(new AccountMeta(metadata, false, false));
        keys.add(new AccountMeta(auctionHouse, false, false));
        keys.add(new AccountMeta(auctionHouseFeeAccount, false, true));
        keys.add(new AccountMeta(sellerTradeState, false, true));
        keys.add(new AccountMeta(freeSellerTradeState, false, true));
        keys.add(new AccountMeta(TOKEN_PROGRAM_ID, false, false));
        keys.add(new AccountMeta(SYSTEM_PROGRAM_ID, false, false));
        keys.add(new AccountMeta(programAsSigner, false, false));
        keys.add(new AccountMeta(SYSVAR_RENT_PUBKEY, false, false));


        byte[] instructionData = buffer.array();

        return createTransactionInstruction(
                wallet,
                keys,
                instructionData
        );
    }
}