package com.mmorrell.openbook.model;

import lombok.Builder;
import lombok.Data;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.utils.ByteUtils;

import java.util.Arrays;
import java.util.List;

/**
 * The OpenBookEventHeap class represents the heap of open book events.
 * It contains information about the free and used heads, the count of events,
 * the padding, and the sequence number.
 * It also includes a list of event nodes and fill events.
 * The class provides methods to read the OpenBookEventHeap from data and retrieve the fill events,
 * out events, and event owners to consume.
 */
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

    /**
     * Reads an OpenBookEventHeap from the given byte array.
     *
     * @param data the byte array containing the OpenBookEventHeap data
     * @return the deserialized OpenBookEventHeap object
     */
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

    /**
     * Returns a list of OpenBookFillEvent objects.
     *
     * @return The list of OpenBookFillEvent objects.
     */
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

    /**
     * Returns a list of OpenBookOutEvent objects.
     *
     * @return The list of OpenBookOutEvent objects.
     */
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

    /**
     * Retrieves a list of public keys representing the owners of the events to be consumed.
     *
     * @return A list of public keys representing the owners of the events to be consumed.
     */
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
                .filter(publicKey -> !publicKey.equals(new PublicKey("11111111111111111111111111111111")))
                .toList();
    }
}
