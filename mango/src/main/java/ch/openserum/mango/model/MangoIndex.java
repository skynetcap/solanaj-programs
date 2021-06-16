package ch.openserum.mango.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MangoIndex {

    private long lastUpdate;
    private byte[] borrow;
    private byte[] deposit;

}
