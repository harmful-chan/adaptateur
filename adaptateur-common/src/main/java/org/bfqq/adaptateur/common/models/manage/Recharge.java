package org.bfqq.adaptateur.common.models.manage;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Recharge {
    @JsonProperty("ba_code")
    private String tradeId;

    @JsonProperty("pn_real_amount")
    private double amount;

    @JsonProperty("pn_update_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date paymentTime;

    @JsonProperty("pn_note")
    private String mark;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("customer_code")
    private String clientId;
}
