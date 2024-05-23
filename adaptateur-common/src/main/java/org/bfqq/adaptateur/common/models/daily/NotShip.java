package org.bfqq.adaptateur.common.models.daily;

import lombok.Data;

import java.util.Date;

@Data
public class NotShip {
    private int ID;
    private Date OrderTime;
    private String Symbol;
    private double Amount;
    private int LastDay;
}
