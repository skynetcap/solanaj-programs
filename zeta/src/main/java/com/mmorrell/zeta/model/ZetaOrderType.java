package com.mmorrell.zeta.model;

public enum ZetaOrderType {

    LIMIT(0), POST_ONLY(1), FILL_OR_KILL(2);

    private final int value;

    ZetaOrderType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
