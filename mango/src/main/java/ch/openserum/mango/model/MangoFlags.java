package com.mmorrell.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class MangoFlags {
    boolean initialized;
    boolean mangoGroup;
    boolean marginAccount;
    boolean mangoSrmAccount;
}
