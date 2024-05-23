package org.bfqq.adaptateur.api.services;

import org.bfqq.adaptateur.common.models.manage.ShipObject;
import org.bfqq.adaptateur.common.models.manage.ShipTypes;
import org.springframework.stereotype.Service;

import java.io.IOException;

public interface IManageService {
    //public boolean Deduction(ShipTypes shipTypes, String clientId, String  orderId, String trackingId, String carrier) throws IOException;
    public boolean Deduction(ShipObject shipObject) throws IOException;
}
