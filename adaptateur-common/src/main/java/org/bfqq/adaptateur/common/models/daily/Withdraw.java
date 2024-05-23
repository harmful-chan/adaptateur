package org.bfqq.adaptateur.common.models.daily;

import lombok.Data;

import java.util.Date;

@Data
public class Withdraw {
    private int ID;
    private Date WithdrawTime;
    private double Amount;
}
