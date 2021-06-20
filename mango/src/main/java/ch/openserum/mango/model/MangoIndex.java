package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MangoIndex {

    private long lastUpdate;
    private U64F64 borrow;
    private U64F64 deposit;

}
