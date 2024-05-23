package org.bfqq.adaptateur.common.test.danger;

import org.bfqq.adaptateur.common.models.manage.ShipObject;
import org.bfqq.adaptateur.common.models.manage.ShipTypes;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class TestDeductionAndShip extends TestParameterizedBase {

    public TestDeductionAndShip(String clientId, String orderId, String tracking, String carrier, String ret) {
        super(clientId, orderId, tracking, carrier, ret);
    }

    @Test
    public void testDeductionAndShip() throws IOException {
        assertTrue(manageClient.loginAdmin());
        ShipObject so = new ShipObject();
        so.setClientID(clientId);
        so.setOrderID(orderId);
        so.setStep(ShipTypes.DeductAndShip);
        assertTrue(manageClient.DeductShipDeclare(so));
    }

}
