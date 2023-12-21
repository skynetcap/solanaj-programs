package com.mmorrell.openbook.model;

import com.mmorrell.openbook.OpenBookUtil;
import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.utils.ByteUtils;

@Data
@Builder
public class OpenBookEventHeap {

    // EventHeapHeader x 1
    private short freeHead;
    private short usedHead;
    private short count;
    private short padding;
    private long seqNum;

    // + EventNode x 600 (starts at offset 24)

    // + 64 bytes reserved

    public static OpenBookEventHeap readOpenBookEventHeap(byte[] data) {
        return OpenBookEventHeap.builder()
                .freeHead((short) Utils.readUint16(data, 8))
                .usedHead((short) Utils.readUint16(data, 10))
                .count((short) Utils.readUint16(data, 12))
                .padding((short) Utils.readUint16(data, 14))
                .seqNum(ByteUtils.readUint64(data, 16).longValue())
                .build();
    }
}
