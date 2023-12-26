package com.mmorrell.openbook.model;

import lombok.Data;

import java.util.Arrays;

/**
 * The OpenBookAnyEvent class represents an event in the OpenBook system.
 * It stores the type of the event and additional padding data.
 */
@Data
public class OpenBookAnyEvent {

    public static final int SIZE = 144;

    // 0 = fill, 1 = out
    private byte eventType;
    private byte[] padding;

    /**
     * Reads a byte array and returns an OpenBookAnyEvent object.
     *
     * @param data the byte array to read
     * @return an OpenBookAnyEvent object
     */
    public static OpenBookAnyEvent readOpenBookAnyEvent(byte[] data) {
        OpenBookAnyEvent openBookAnyEvent = new OpenBookAnyEvent();
        openBookAnyEvent.setEventType(data[0]);
        openBookAnyEvent.setPadding(Arrays.copyOfRange(data, 1, SIZE));
        return openBookAnyEvent;
    }
}
