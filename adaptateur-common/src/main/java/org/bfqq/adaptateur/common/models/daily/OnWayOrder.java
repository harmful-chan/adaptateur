package org.bfqq.adaptateur.common.models.daily;

import lombok.Data;

import java.util.Date;

@Data
public class OnWayOrder {
    private int ID;
    private String OrderId;
    private Date PaymentTime;
    private Date ShippingTime;
    private Date ReceiptTime;
    private Double Amount;
    private String Reason;
}
