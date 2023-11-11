package com.mmorrell.phoenix.program;

import com.mmorrell.serum.model.Market;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.Program;


public class PhoenixProgram extends Program {

    public static final PublicKey PHOENIX_PROGRAM_ID =
            PublicKey.valueOf("PhoeNiXZ8ByJGLkxNfZRnkUfjvmuYqLR89jjFHGqdXY");
    private static final PublicKey TOKEN_PROGRAM_ID =
            PublicKey.valueOf("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
    private static final PublicKey SYSVAR_RENT_PUBKEY =
            PublicKey.valueOf("SysvarRent111111111111111111111111111111111");


    public static TransactionInstruction matchOrders(Market market, int limit) {
        return null;
    }
}