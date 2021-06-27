package ch.openserum.pyth.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class MappingAccount {

    private int magicNumber;
    private int version;

}
