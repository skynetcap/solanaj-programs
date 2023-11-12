package com.mmorrell.phoenix.program;

import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.Program;
import org.p2p.solanaj.programs.SystemProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static com.mmorrell.phoenix.program.PhoenixProgram.PHOENIX_PROGRAM_ID;

@Slf4j
public class PhoenixSeatManagerProgram extends Program {

    public static final PublicKey PHOENIX_SEAT_MANAGER_PROGRAM_ID =
            PublicKey.valueOf("PSMxQbAoDWDbvd9ezQJgARyq6R9L5kJAasaLDVcZwf1");
    public static final PublicKey PHOENIX_LOG_AUTHORITY_ID = PublicKey.valueOf(
            "7aDTsspkQNGKmrexAN7FLx9oxU3iPczSSvHNggyuqYkR");
    private static final PublicKey TOKEN_PROGRAM_ID =
            PublicKey.valueOf("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
    private static final PublicKey SYSVAR_RENT_PUBKEY =
            PublicKey.valueOf("SysvarRent111111111111111111111111111111111");


    /**
     * #[account(0, name = "phoenix_program", desc = "Phoenix program")]
     * #[account(1, name = "log_authority", desc = "Phoenix log authority")]
     * #[account(2, writable, name = "market", desc = "This account holds the market state")]
     * #[account(3, writable, name = "seat_manager", desc = "The seat manager account is the market authority")]
     * #[account(4, writable, name = "seat_deposit_collector", desc = "Collects deposits for claiming new seats and refunds for evicting seats")]
     * #[account(5, signer, name = "trader")]
     * #[account(6, writable, signer, name = "payer")]
     * #[account(7, writable, name = "seat")]
     * #[account(8, name = "system_program", desc = "System program")]
     * ClaimSeat = 1,
     *
     */
    public static TransactionInstruction claimSeat(PublicKey market, PublicKey marketAuthority,
                                                   PublicKey seatDepositCollector, PublicKey trader,
                                                   PublicKey payer) {
        List<AccountMeta> accountMetas = new ArrayList<>();

        PublicKey seatPda = null;
        try {
            seatPda = PublicKey.findProgramAddress(
                    List.of(
                            "seat".getBytes(),
                            market.toByteArray(),
                            trader.toByteArray()
                    ),
                    PHOENIX_PROGRAM_ID
            ).getAddress();
            log.info("PDA found for Phoenix seat: {}", seatPda);
        } catch (Exception e) {
            log.error("Error claiming seat: {}", e.getMessage());
        }

        accountMetas.add(new AccountMeta(PHOENIX_PROGRAM_ID, false, false));
        accountMetas.add(new AccountMeta(PHOENIX_LOG_AUTHORITY_ID, false, false));
        accountMetas.add(new AccountMeta(market, false, true));
        accountMetas.add(new AccountMeta(marketAuthority, false, true));
        accountMetas.add(new AccountMeta(seatDepositCollector, false, true));
        accountMetas.add(new AccountMeta(trader, false, false));
        accountMetas.add(new AccountMeta(payer, true, true));
        accountMetas.add(new AccountMeta(seatPda, false, true));
        accountMetas.add(new AccountMeta(SystemProgram.PROGRAM_ID, false, false));

        ByteBuffer result = ByteBuffer.allocate(1);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.put(0, (byte) 1);

        byte[] transactionData = result.array();
        return createTransactionInstruction(
                PHOENIX_SEAT_MANAGER_PROGRAM_ID,
                accountMetas,
                transactionData
        );
    }
}