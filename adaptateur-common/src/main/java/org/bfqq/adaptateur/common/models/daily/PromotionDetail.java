package org.bfqq.adaptateur.common.models.daily;

import lombok.Data;

import java.util.Date;

@Data
public class PromotionDetail {
    private int iD;
    private Date time;
    private double expenses;
    private double inCome;
}
