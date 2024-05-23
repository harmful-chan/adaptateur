package org.bfqq.adaptateur.common.models.manage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tag {
    @JsonProperty("count")
    private int count;

    @JsonProperty("tags")
    private String[] tags;

    @JsonProperty("purchaseStatu")
    private PurchaseStatus[] purchaseStatus;
}

