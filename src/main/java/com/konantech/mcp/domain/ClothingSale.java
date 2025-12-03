package com.konantech.mcp.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ClothingSale {
    private UUID product_id;
    private String product_name;
    private String category;
    private int original_price;
    private int sale_price;
    private int discount_rate;
    private int stock;
    private boolean is_on_sale;
    private LocalDateTime created_at = LocalDateTime.now();
}

