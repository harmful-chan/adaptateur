package org.bfqq.adaptateur.common.models.daily;

import lombok.Data;

import java.util.Date;

@Data
public class OrderDetail {
    private int ID;
    private String OrderId;
    private Date OrderTime;
    private String Remark;
    private String Title;
    private int Quantity;
    private double RMB;
    private String Symbol;
    private double Amount;
    private String After;
    private String Status;
    private String LastTime;

    private OrderShipsFromTypes ShipsFrom;
    private boolean IsAssess;
}
