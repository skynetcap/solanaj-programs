package com.mmorrell.openbook.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class BookSide {

    // OrderTreeRoot x 2
    // OrderTreeRoot x 4 (reserved)
    // 256 bytes padding
    // OrderTreeNodes x 1

    List<OrderTreeRoot> roots;
    List<OrderTreeRoot> reservedRoots;

    public static BookSide readBookSide(byte[] data) {
        return BookSide.builder()
                .roots(new ArrayList<>())
                .reservedRoots(new ArrayList<>())
                .build();
    }


}
