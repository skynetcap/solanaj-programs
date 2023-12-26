package com.mmorrell.openbook.program;

import com.mmorrell.openbook.OpenBookUtil;

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

import static com.mmorrell.openbook.OpenBookUtil.CONSUME_EVENTS_DISCRIMINATOR;

/**
 * The OpenbookProgram class extends the Program class and provides methods to interact with
 * the Openbook v2 program.
 */
public class OpenbookProgram extends Program {

    private static final PublicKey TOKEN_PROGRAM_ID =
            PublicKey.valueOf("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
    private static final PublicKey SYSVAR_RENT_PUBKEY =
            PublicKey.valueOf("SysvarRent111111111111111111111111111111111");
    public static final PublicKey OPENBOOK_V2_PROGRAM_ID = PublicKey.valueOf(
            "opnb2LAfJYbRMAHHvqjCwQxanZn7ReEHp1k81EohpZb");

    private static final int MATCH_ORDERS_METHOD_ID = 2;
    private static final int CONSUME_EVENTS_METHOD_ID = 3;
    private static final int SETTLE_ORDERS_METHOD_ID = 5;
    private static final int CANCEL_ORDER_V2_METHOD_ID = 11;
    private static final int CANCEL_ORDER_BY_CLIENT_ID_V2_METHOD_ID = 12;

    // Open Book methods to implement
    // CreateOpenOrdersIndexer
    // aka create_open_orders_indexer
    // https://github.com/openbook-dex/openbook-v2/blob/master/programs/openbook-v2/src/accounts_ix/create_open_orders_indexer.rs

    public static TransactionInstruction createOpenOrdersIndexer(Account caller) {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(caller.getPublicKey(),true, false));
        keys.add(new AccountMeta(caller.getPublicKey(),true, false));

        // open_orders_indexer
        try {
            PublicKey ourKey = PublicKey.findProgramAddress(
                    List.of(
                            "OpenOrdersIndexer".getBytes(),
                            caller.getPublicKey().toByteArray()
                    ),
                    OPENBOOK_V2_PROGRAM_ID
            ).getAddress();
            keys.add(new AccountMeta(ourKey, false, true));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // TODO change below key to `market`
        keys.add(new AccountMeta(SystemProgram.PROGRAM_ID, false, false));
        keys.add(new AccountMeta(SystemProgram.PROGRAM_ID, false, false));


        byte[] transactionData = OpenBookUtil.encodeNamespace("global:create_open_orders_indexer");

        return createTransactionInstruction(
                OPENBOOK_V2_PROGRAM_ID,
                keys,
                transactionData
        );
    }

    /**
     * Creates a transaction instruction for consuming events in OpenbookProgram.
     *
     * @param caller            The account initiating the transaction.
     * @param market            The public key of the market.
     * @param eventHeap         The public key of the event heap.
     * @param openOrdersAccounts A list of public keys representing open orders accounts.
     * @param limit             The maximum number of events to consume.
     * @return A TransactionInstruction object representing the consume events instruction.
     */
    public static TransactionInstruction consumeEvents(Account caller, PublicKey market, PublicKey eventHeap,
                                                       List<PublicKey> openOrdersAccounts, long limit) {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(OPENBOOK_V2_PROGRAM_ID, false, false));
        keys.add(new AccountMeta(market,false, true));
        keys.add(new AccountMeta(eventHeap,false, true));
        keys.addAll(openOrdersAccounts.stream()
                .map(publicKey -> new AccountMeta(publicKey, false, true))
                .toList()
        );
        keys.add(new AccountMeta(caller.getPublicKey(),true, false));

        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(CONSUME_EVENTS_DISCRIMINATOR);
        byteBuffer.putLong(8, limit);

        return createTransactionInstruction(
                OPENBOOK_V2_PROGRAM_ID,
                keys,
                byteBuffer.array()
        );
    }
}
