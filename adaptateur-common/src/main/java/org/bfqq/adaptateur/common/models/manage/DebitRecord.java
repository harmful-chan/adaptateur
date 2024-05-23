package org.bfqq.adaptateur.common.models.manage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DebitRecord {
    @JsonProperty("arn_id")
    private int recordId;

    @JsonProperty("transaction_no")
    private String tradeId;

    @JsonProperty("arn_amount")
    private double cost;

    @JsonProperty("arn_finish_time")
    private String createTime;

    @JsonProperty("cc_code")
    private String clientId;

    @JsonProperty("cu_name_en")
    private String clientName;
}
