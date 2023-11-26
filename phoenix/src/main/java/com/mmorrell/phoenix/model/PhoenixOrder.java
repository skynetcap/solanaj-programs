package com.mmorrell.phoenix.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhoenixOrder {

    private double price;
    private double size;

}
