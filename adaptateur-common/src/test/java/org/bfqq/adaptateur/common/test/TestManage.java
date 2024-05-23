package org.bfqq.adaptateur.common.test;
import org.bfqq.adaptateur.common.clients.ManageClient;

import org.bfqq.adaptateur.common.models.manage.DebitRecord;
import org.bfqq.adaptateur.common.models.manage.Order;
import org.bfqq.adaptateur.common.models.manage.Recharge;
import org.bfqq.adaptateur.common.models.manage.User;
import org.junit.*;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class TestManage {

    private static ManageClient manageClient;
    private static String clientId = "5377003";
    private static String orderId = "8188180613547334";


    @BeforeClass
    public static void beforeClass() throws IOException {
        manageClient = new ManageClient();
        System.out.println("登录后台");
        assertTrue(manageClient.loginAdmin());
        System.out.println("登录用户: " + clientId);
        assertTrue(manageClient.loginUser(clientId));
    }
    @Before
    public void setUpClass() throws IOException {

    }

    @Test
    public  void testListUser() throws IOException {
        User[] users = manageClient.listUsers(null);
        long count = Arrays.stream(users).filter((x) -> clientId.equals(x.getClientId())).count();
        assertEquals(count, 1);
    }

    @Test
    public  void testListOrder() throws IOException {
        Order[] orders = manageClient.listOrder("8188180613547334", "8188036382099163");
        assertEquals(orders.length, 2);
    }

    @Test
    public  void testListDebitRecord() throws IOException {
        DebitRecord[] debitRecords = manageClient.listDebitRecord(clientId, null, null);
        assertTrue(debitRecords.length > 0);
    }

    @Test
    public void testListAllRecharge() throws IOException {
        Recharge[] recharges = manageClient.listAllRecharge();
        assertTrue(recharges.length > 0);
    }


}
