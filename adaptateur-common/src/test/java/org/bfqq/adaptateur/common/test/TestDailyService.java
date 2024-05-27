package org.bfqq.adaptateur.common.test;


import org.apache.commons.lang3.tuple.Pair;
import org.bfqq.adaptateur.common.clients.ManageClient;
import org.bfqq.adaptateur.common.clients.SheetClient;
import org.bfqq.adaptateur.common.models.DirTypes;
import org.bfqq.adaptateur.common.models.daily.DailyDetail;
import org.bfqq.adaptateur.common.models.manage.Recharge;
import org.bfqq.adaptateur.common.models.manage.User;
import org.bfqq.adaptateur.common.models.sheet.Shop;
import org.bfqq.adaptateur.common.services.impl.DailyServiceImpl;
import org.bfqq.adaptateur.common.utils.DirUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class TestDailyService {

    static ManageClient manageClient = null;
    static SheetClient sheetClient = null;

    static DailyServiceImpl dailyService = null;
    @BeforeClass
    public static void beforeClass(){
        manageClient = new ManageClient();
        sheetClient = new SheetClient();
        dailyService = new DailyServiceImpl();
    }



    @Test
    public void test1() throws IOException {


        Assertions.assertTrue(manageClient.loginAdmin());
        Recharge[] recharges = manageClient.listAllRecharge();
        Assertions.assertTrue(recharges.length > 0);
        User[] users = manageClient.listUsers(null);
        Assertions.assertTrue(users.length > 0);

        // 获取文件路径
        String[] array = DirUtil.getNewFile(DirTypes.DAILY).stream().map(File::getAbsolutePath).toArray(String[]::new);
        Assertions.assertTrue(array.length > 0);


        // 读取表格数据
        List<DailyDetail> list = new ArrayList<DailyDetail>();
        for (String dir: array){
            list.add(sheetClient.readDaily(dir));
        }

        // 读取店铺目录
        String string = DirUtil.getNewFile(DirTypes.SHOP_FILENAME).stream().findFirst().get().toString();
        List<Shop> shops = sheetClient.readShopCatalog(string);
        Pair<Integer, Integer> ii = dailyService.listMissingStores(shops, LocalDate.now(), list);

        Assertions.assertEquals(ii.getKey(), ii.getValue());

        // 生成店铺数据
        String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        String path1 = Paths.get(new File(array[0]).getParent(),  format + "店铺数据.txt").toString();
        dailyService.BuildCompanyData(shops, LocalDate.now(), Arrays.asList(users) , Arrays.asList(recharges), list, path1);

        // 生成订单数据
        String path2 = Paths.get(new File(array[0]).getParent(), format + "订单.txt").toString();
        dailyService.buildOrders(shops, LocalDate.now(), list, path2);

        // 生成运营数据
        String path3 = Paths.get(new File(array[0]).getParent(), format+".txt").toString();
        dailyService.BuildOperData(shops, LocalDate.now(), Arrays.asList(users) , Arrays.asList(recharges), list, path3);

    }
}
