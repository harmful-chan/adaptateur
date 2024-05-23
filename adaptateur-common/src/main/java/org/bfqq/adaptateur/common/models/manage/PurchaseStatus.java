package org.bfqq.adaptateur.common.models.manage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseStatus {
    @JsonProperty("count")
    private int count;

    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;
}
