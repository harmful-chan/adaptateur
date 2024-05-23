package org.bfqq.adaptateur.common.models.daily;

import lombok.Data;

@Data
public class StoreOverview {
    private Integer ID;
    private String Company;
    private String CN;
    private String Opera;
    private Integer UP;
    private Integer Check;
    private Integer Down;
    private Double IM24;
    private Double Good;
    private Double Dispute;
    private Double Wrong;
    private String DisputeLine;
    private Integer F30;
    private Integer D30;
    private Integer Exp30;
    private Integer Fin;
    private Integer Dis;
    private Integer Close;
    private Integer Talk;
    private Integer Palt;
    private Integer All;
    private String ReadyLine;
    private Integer New;
    private Integer Ready;
    private Integer Wait;
    private Funds Funds;
}
