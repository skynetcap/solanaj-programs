package com.mmorrell.jupiter.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;
import org.p2p.solanaj.core.PublicKey;

/**
 * Data Transfer Object for aggregating a user's DCA statistics.
 */
@Data
@AllArgsConstructor
public class JupiterUserDcaStats {
    private long totalOrders;
    private double totalVolumeUsd;
    private Set<PublicKey> uniqueInputTokens;
    private Set<PublicKey> uniqueOutputTokens;
}