package org.bfqq.adaptateur.common.models.manage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarkOrder {
    @JsonProperty("order_code")
    private String orderId;

    @JsonProperty("tracking_number")
    private String trackingNumber;

    @JsonProperty("tracking_number_old")
    private String trackingNumberOld;

    @JsonProperty("platform_carrier_code")
    private String carrier;

    @JsonProperty("platform_carrier_code_old")
    private String carrierOld;

    @JsonProperty("sync_status_text")
    private String statusText;

    @JsonProperty("tracking_tips")
    private String tips;

    public int getStep() {
        int ret = 0;
        if (orderId != null && !orderId.isEmpty()) {
            ret = 1;
            if (trackingNumber != null && trackingNumberOld != null && trackingNumber.equals(trackingNumberOld)) {
                ret = 2;
            }

            if (trackingNumber != null && trackingNumberOld != null && !trackingNumber.equals(trackingNumberOld) && tips != null && !tips.isEmpty()) {
                ret = 3;
            }
        }
        return ret;
    }
}
