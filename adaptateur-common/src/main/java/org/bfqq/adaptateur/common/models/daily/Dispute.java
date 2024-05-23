package org.bfqq.adaptateur.common.models.daily;

import lombok.Data;

import java.util.Date;

@Data
public class Dispute {
    private int ID;
    private String OrderId;
    private Date OrderTime;
    private String Buyer;
    private Date DisputeTime;
    private String Status;
    private String LastTime;
}
