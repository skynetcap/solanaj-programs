package com.skynet.auctionhouse.manager;

import lombok.RequiredArgsConstructor;
import org.p2p.solanaj.rpc.RpcClient;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class AuctionHouseManager {
    private final RpcClient client;
    private static final Logger LOGGER = Logger.getLogger(AuctionHouseManager.class.getName());

    public void listExampleNft() {

    }
}
