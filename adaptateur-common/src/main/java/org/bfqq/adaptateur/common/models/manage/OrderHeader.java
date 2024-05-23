package org.bfqq.adaptateur.common.models.manage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderHeader {
    @JsonProperty("1")
    private Tag t1;

    @JsonProperty("2")
    private Tag t2;

    @JsonProperty("3")
    private Tag t3;

    @JsonProperty("4")
    private Tag t4;

    @JsonProperty("5")
    private Tag t5;

    @JsonProperty("6")
    private Tag t6;

    @JsonProperty("7")
    private Tag t7;

    @JsonProperty("8")
    private Tag t8;

    @JsonProperty("9")
    private Tag t9;
}
