package com.mmorrell.openbook.model;

import lombok.Builder;
import lombok.Data;

import java.util.Arrays;

@Data
@Builder
public class OpenBookAnyEvent {

    public static final int SIZE = 144;

    // 0 = fill, 1 = out
    private byte eventType;
    private byte[] padding;

    public static OpenBookAnyEvent readOpenBookAnyEvent(byte[] data) {
        return OpenBookAnyEvent.builder()
                .eventType(data[0])
                .padding(Arrays.copyOfRange(data, 1, SIZE))
                .build();
    }
}
