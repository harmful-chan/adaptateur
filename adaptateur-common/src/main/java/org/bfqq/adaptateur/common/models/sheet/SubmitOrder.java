package org.bfqq.adaptateur.common.models.sheet;

import lombok.Data;

import java.util.Date;

@Data
public class SubmitOrder {
    private String storeName; // A
    private String operator; // B
    private String clientId;
    private String orderStatus;
    private String orderId; // E
    private Date orderTime;

    private Double cost;
    private String orderPrice;
    private String tradeId;
    private Double deductionAmount;
}
