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
        // Add signer to AccountMeta keys
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(tokenAccount, false, false));
        keys.add(new AccountMeta(auctionHouse, true, true));
        keys.add(new AccountMeta(metadata, false, true));
        keys.add(new AccountMeta(authority, false, false));


        byte[] instructionData = null; //buffer.array();

        return createTransactionInstruction(
                wallet,
                keys,
                instructionData
        );
    }
}