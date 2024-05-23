package org.bfqq.adaptateur.common.models.sheet;

import lombok.Data;

import java.util.Date;

@Data
public class ShopRefund {
    private String orderId;
    private String productId;
    private String productName;
    private String SUK;
    private String productCode;
    private Integer quantity;
    private Double turnover;
    private Double refund;
    private String sources;
    private Double fee;
    private Double alliance;
    private Double cashback;
    private Date refundTime;
    private String refundReason;
    private String debitOperation;
    private Double deduction;
    private String refundOperation;
    private String refundId;
    private String trade;
    private String fileName;

    // Getters and Setters for all the fields
}