package org.bfqq.adaptateur.common.test;

import org.bfqq.adaptateur.common.clients.ISheetClient;
import org.bfqq.adaptateur.common.clients.SheetClient;
import org.bfqq.adaptateur.common.models.daily.DailyDetail;
import org.bfqq.adaptateur.common.models.sheet.PurchaseOrder;
import org.bfqq.adaptateur.common.models.sheet.Shop;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.JVM)
public class TestSheetClient {

    final String BASE_DIR = "src/main/resources/test/数据采集";
    static ISheetClient sheet = null;
    @BeforeClass
    public static void beforeClass() throws IOException {
        sheet = new SheetClient();
    }

    @Test
    public void testReadShopCatalog(){
        String filename = Paths.get(BASE_DIR, "店铺信息.xlsx").toString();
        List<Shop> shops = sheet.readShopCatalog(filename);
        Assertions.assertTrue(!shops.isEmpty());
    }

    @Test
    public void testReadDaily(){
        File[] list = new File(Paths.get(BASE_DIR, "每日数据").toString()).listFiles();
        list = new File(Paths.get(list[list.length-1].getAbsolutePath()).toString()).listFiles(((dir, name) -> name.endsWith("xlsx") && !name.startsWith("~")));
        List<DailyDetail> dailyDetails = new ArrayList<DailyDetail>() {
        };
        for (File file : list){
            DailyDetail dailyDetail = sheet.readDaily(file.getAbsolutePath());
            dailyDetails.add(dailyDetail);
        }

        Assertions.assertTrue(!dailyDetails.isEmpty());
    }

    @Test
    public void testReadPurchaseOrder(){
        File[] list = new File(Paths.get(BASE_DIR, "总表数据").toString()).listFiles();
        String filename = Paths.get(list[list.length-1].getAbsolutePath(), "巴西采购单.xlsx").toString();

        int length = sheet.readSortFiles(filename).length;
        Assertions.assertEquals(4, length);

        List<PurchaseOrder> purchaseOrders = sheet.readPurchaseOrder(1, filename);
        Assertions.assertTrue(!purchaseOrders.isEmpty());
    }
}
