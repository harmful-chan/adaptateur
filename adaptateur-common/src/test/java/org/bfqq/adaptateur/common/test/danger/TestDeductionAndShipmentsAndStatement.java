package org.bfqq.adaptateur.common.test.danger;

import org.bfqq.adaptateur.common.models.manage.ShipObject;
import org.bfqq.adaptateur.common.models.manage.ShipTypes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TestDeductionAndShipmentsAndStatement extends TestParameterizedBase {


    public TestDeductionAndShipmentsAndStatement(String clientId, String orderId, String tracking, String carrier, String ret) {
        super(clientId, orderId, tracking, carrier, ret);
    }

    @Test
    public void testDeductionAndShipmentsAndStatement() throws IOException {
        assertTrue(manageClient.loginAdmin());
        ShipObject so = new ShipObject();
        so.setClientID(clientId);
        so.setOrderID(orderId);
        so.setTrackingNumber(tracking);
        so.setCarrier(carrier);
        so.setStep(ShipTypes.DeductAndShipAndDeclare);
        assertTrue(manageClient.DeductShipDeclare(so));
    }
}
