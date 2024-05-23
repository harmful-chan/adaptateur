package org.bfqq.adaptateur.common.models.manage;

import lombok.Data;

@Data
public class ShipObject {
    private int id;
    private String clientID;
    private String orderID;
    private String trackingNumber;
    private String trackingNumberOld;
    private String carrier;
    private String carrierOld;
    private ShipTypes step;
    private String tradeID;
    private String deductionAmount;
    private boolean isDeduction = false;
    private boolean isShipped;
}
