package com.mmorrell.zeta.model;

public enum ZetaSide {

    UNINITIALIZED(0), BID(1), ASK(2);

    private final int value;

    ZetaSide(int value) {
        this.value = value;
    }

    public byte getValue() {
        return (byte) value;
    }
}
