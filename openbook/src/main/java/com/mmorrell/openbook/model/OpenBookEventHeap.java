package com.mmorrell.openbook.model;

import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.utils.ByteUtils;

import java.util.Arrays;
import java.util.List;

@Data
@Builder
public class OpenBookEventHeap {

    private static final int FREE_HEAD_OFFSET = 8;
    private static final int USED_HEAD_OFFSET = 10;
    private static final int COUNT_OFFSET = 12;
    private static final int PADDING_OFFSET = 14;
    private static final int SEQ_NUM_OFFSET = 16;
    private static final int EVENT_NODES_OFFSET = 24;

    // EventHeapHeader x 1
    private short freeHead;
    private short usedHead;
    private short count;
    private short padding;
    private long seqNum;

    // + EventNode x 600 (starts at offset 24)
    private static final int NUM_EVENT_NODES = 600;
    private List<OpenBookEventNode> eventNodes;
    private List<OpenBookFillEvent> fillEvents;

    // + 64 bytes reserved

    public static OpenBookEventHeap readOpenBookEventHeap(byte[] data) {
        return OpenBookEventHeap.builder()
                .freeHead((short) Utils.readUint16(data, FREE_HEAD_OFFSET))
                .usedHead((short) Utils.readUint16(data, USED_HEAD_OFFSET))
                .count((short) Utils.readUint16(data, COUNT_OFFSET))
                .padding((short) Utils.readUint16(data, PADDING_OFFSET))
                .seqNum(ByteUtils.readUint64(data, SEQ_NUM_OFFSET).longValue())
                .eventNodes(
                        OpenBookEventNode.readEventNodes(
                                Arrays.copyOfRange(
                                        data,
                                        EVENT_NODES_OFFSET,
                                        EVENT_NODES_OFFSET + (NUM_EVENT_NODES * OpenBookEventNode.SIZE)
                                )
                        )
                )
                .build();
    }

    public List<OpenBookFillEvent> getFillEvents() {
        byte[] eventType = {0x00};
        return eventNodes.stream()
                .filter(openBookEventNode -> openBookEventNode.getEvent().getEventType() == (byte) 0)
                .map(openBookEventNode -> {
                    byte[] combined = new byte[eventType.length + openBookEventNode.getEvent().getPadding().length];
                    System.arraycopy(eventType, 0, combined, 0, eventType.length);
                    System.arraycopy(openBookEventNode.getEvent().getPadding(), 0, combined, eventType.length, openBookEventNode.getEvent().getPadding().length);
                    return OpenBookFillEvent.readOpenBookFillEvent(combined);
                })
                .filter(openBookFillEvent -> !openBookFillEvent.getMaker().equals(new PublicKey(
                        "11111111111111111111111111111111")))
                .toList();

    }

    public List<OpenBookOutEvent> getOutEvents() {
        byte[] eventType = {0x01};
        return eventNodes.stream()
                .filter(openBookEventNode -> openBookEventNode.getEvent().getEventType() == (byte) 1)
                .map(openBookEventNode -> {
                    byte[] combined = new byte[eventType.length + openBookEventNode.getEvent().getPadding().length];
                    System.arraycopy(eventType, 0, combined, 0, eventType.length);
                    System.arraycopy(openBookEventNode.getEvent().getPadding(), 0, combined, eventType.length, openBookEventNode.getEvent().getPadding().length);
                    return OpenBookOutEvent.readOpenBookOutEvent(combined);
                })
                .filter(openBookOutEvent -> !openBookOutEvent.getOwner().equals(new PublicKey(
                        "11111111111111111111111111111111")))
                .toList();
    }

    public List<PublicKey> getEventOwnersToConsume() {
        return eventNodes.stream()
                .map(openBookEventNode -> {
                    if (openBookEventNode.getEvent().getEventType() == (byte) 0) {
                        byte[] eventType = {0x00};
                        byte[] combined = new byte[eventType.length + openBookEventNode.getEvent().getPadding().length];
                        System.arraycopy(eventType, 0, combined, 0, eventType.length);
                        System.arraycopy(openBookEventNode.getEvent().getPadding(), 0, combined, eventType.length, openBookEventNode.getEvent().getPadding().length);
                        return OpenBookFillEvent.readOpenBookFillEvent(combined).getMaker();
                    } else {
                        byte[] eventType = {0x01};
                        byte[] combined = new byte[eventType.length + openBookEventNode.getEvent().getPadding().length];
                        System.arraycopy(eventType, 0, combined, 0, eventType.length);
                        System.arraycopy(openBookEventNode.getEvent().getPadding(), 0, combined, eventType.length, openBookEventNode.getEvent().getPadding().length);
                        return OpenBookOutEvent.readOpenBookOutEvent(combined).getOwner();
                    }
                })
                .toList();
    }
}
