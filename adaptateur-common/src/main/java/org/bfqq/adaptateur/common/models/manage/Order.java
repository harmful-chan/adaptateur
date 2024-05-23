package org.bfqq.adaptateur.common.models.manage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    private int status;

    @JsonProperty("id")
    private int id;

    @JsonProperty("order_code")
    private String orderId;

    @JsonProperty("distribution_costs")
    private double cost;

}
