package com.mmorrell.zeta.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ZetaGroup {

    private List<ZetaProduct> zetaProducts;
    private List<ZetaExpiry> zetaExpiries;

}
