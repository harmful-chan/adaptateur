package org.bfqq.adaptateur.common.models.sheet;

import lombok.Data;

import java.util.Date;

@Data
public class PurchaseOrder {
    private String country;
    private String orderId;
    private String status;
    private boolean isUpdate;
    private String buyer;
    private Date orderDate;
    private Double orderOverdue;
    private Date submissionDate;
    private Double submissionOverdue;
    private String index;
}
