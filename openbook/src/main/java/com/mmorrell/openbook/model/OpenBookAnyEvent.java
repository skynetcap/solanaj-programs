package com.mmorrell.openbook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Arrays;

@Data
public class OpenBookAnyEvent {

    public static final int SIZE = 144;

    // 0 = fill, 1 = out
    private byte eventType;
    private byte[] padding;

    public static OpenBookAnyEvent readOpenBookAnyEvent(byte[] data) {
        OpenBookAnyEvent openBookAnyEvent = new OpenBookAnyEvent();
        openBookAnyEvent.setEventType(data[0]);
        openBookAnyEvent.setPadding(Arrays.copyOfRange(data, 1, SIZE));
        return openBookAnyEvent;
    }
}
