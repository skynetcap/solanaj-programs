package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class MangoAccountFlags {
    boolean initialized;
    boolean mangoGroup;
    boolean marginAccount;
    boolean mangoSrmAccount;
}
