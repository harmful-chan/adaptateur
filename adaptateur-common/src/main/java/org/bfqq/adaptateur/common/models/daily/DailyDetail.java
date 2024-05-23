package org.bfqq.adaptateur.common.models.daily;

import lombok.Data;

import java.util.*;

@Data
public class DailyDetail implements Comparable<DailyDetail> {
    private int ID;
    private Date CollectionDate;
    private String CompanyNumber;
    private String Company;
    private String CN;
    private String Nick;
    private String Operator;
    private Double Promotion;
    private Double Consume;
    private Integer InStockNumber;
    private Integer ReviewNumber;
    private Integer RemovedNumber;
    private Double IM24;
    private Double WrongGoods;
    private Double NotSell;
    private Double Dispute;
    private Double GoodReviews;
    private Double Collect72;
    private Double Lend;
    private Double Freeze;
    private Double OnWay;
    private Double Arrears;
    private Collection<Withdraw> Withdraws;
    private Collection<NotShip> NotShips;
    private Collection<OrderDetail> OrderDetails;
    private Collection<Dispute> DisputeOrders;
    private Collection<OnWayOrder> OnWayOrders;
    private Collection<PromotionDetail> PromotionDetails;

    @Override
    public int compareTo(DailyDetail other) {
        int index = CollectionDate.compareTo(other.CollectionDate);
        if (index == 0) {
            index = CompanyNumber.compareTo(other.CompanyNumber);
            if (index == 0) {
                index = Company.compareTo(other.Company);
                if (index == 0) {
                    return Nick.compareTo(other.Nick);
                }
            }
        }
        return index;
    }
}
