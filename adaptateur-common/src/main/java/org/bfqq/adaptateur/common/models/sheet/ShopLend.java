package org.bfqq.adaptateur.common.models.sheet;

import lombok.Data;

import java.util.Date;

@Data
public class ShopLend {
    private String productId;
    private String productName;
    private String SUK;
    private String productCode;
    private Double quantity;
    private String productImage;
    private Double turnover;
    private Double lend;
    private Double fee;
    private Double affiliate;
    private Double cashback;
    private Date settlementTime;
    private String orderId;
    private String fileName;

    private Double cost;
    private Double profit;
    private Double rate;

    // Getters and Setters for all the fields
}