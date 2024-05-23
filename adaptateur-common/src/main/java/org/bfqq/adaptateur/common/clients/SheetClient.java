package org.bfqq.adaptateur.common.clients;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bfqq.adaptateur.common.models.daily.*;
import org.bfqq.adaptateur.common.models.sheet.*;
import org.bfqq.adaptateur.common.utils.ConvertCellValueUtil;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SheetClient implements ISheetClient {
    private static final Logger logger = Logger.getLogger(SheetClient.class.getName()); // 日志打印类
    ///
    public String[] readSortFiles(String fileName) {
        File file = new File(fileName);
        File parentDirectory = file.getParentFile();

        if (parentDirectory == null || !parentDirectory.isDirectory()) {
            return new String[0]; // 如果文件不存在或者不在目录中，则返回空数组
        }

        String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
        File[] files = parentDirectory.listFiles((dir, name1) -> name1.startsWith(name) && !name1.startsWith("~"));


        if (files == null) {
            return new String[0]; // 如果没有符合条件的文件，则返回空数组
        }

        Arrays.sort(files);

        return Arrays.stream(files)
                .map(File::getAbsolutePath)
                .toArray(String[]::new);
    }



    // region XLSX操作
    public List<PurchaseOrder> readPurchaseOrder(int type, String fileName) {
        List<PurchaseOrder> os = new ArrayList<>();
        String[] files = readSortFiles(fileName); // 读取相同文件
        int j1 = 0;

        for (int i = 0; i < files.length; i++) {
            try (FileInputStream fis = new FileInputStream(files[i])) {
                XSSFWorkbook workbook = new XSSFWorkbook(fis);
                System.out.println("Reading " + files[i]);
                Sheet sheet = workbook.getSheetAt(0);
                if (sheet != null) {
                    for (int j = 0; j < sheet.getLastRowNum(); j++) {
                        j1 = j;
                        XSSFRow row = (XSSFRow) sheet.getRow(j);
                        if (row == null) {
                            continue;
                        }
                        PurchaseOrder to = new PurchaseOrder();
                        // 去除表格 us A1空 B1=操作人
                        if (type == 1) { // 类型1巴西单
                            String A1 = ConvertCellValueUtil.cv(row.getCell(0), String.class);
                            String B1 = ConvertCellValueUtil.cv(row.getCell(1), String.class);
                            String C1 = ConvertCellValueUtil.cv(row.getCell(2), String.class);
                            String D1 = ConvertCellValueUtil.cv(row.getCell(3), String.class);
                            String E1 = ConvertCellValueUtil.cv(row.getCell(4), String.class);

                            // 去除表格 br A1=序号 B1=提单日期
                            if (StringUtils.isBlank(C1) || "下单时间".equals(A1) && "剩余".equals(B1)) {
                                continue;
                            }
                            to.setCountry("BR");
                            to.setOrderDate(ConvertCellValueUtil.cv(row.getCell(0), Date.class));

                            to.setOrderOverdue(ConvertCellValueUtil.cv(row.getCell(1), Double.class));
                            to.setSubmissionDate(ConvertCellValueUtil.cv(row.getCell(3), Date.class));
                            to.setSubmissionOverdue(ConvertCellValueUtil.cv(row.getCell(4), Double.class));

                            to.setOrderId(ConvertCellValueUtil.cv(row.getCell(8), String.class));
                            to.setStatus(ConvertCellValueUtil.cv(row.getCell(24), String.class));
                            String v = ConvertCellValueUtil.cv(row.getCell(12), String.class);
                            to.setUpdate(StringUtils.isBlank(v) ? false : "已更新".equals(v));
                            to.setBuyer(ConvertCellValueUtil.cv(row.getCell(18), String.class));
                            to.setIndex(C1);
                            os.add(to);
                        } else if (type == 2) { // 类型2美国单
                            String A1 = ConvertCellValueUtil.cv(row.getCell(0), String.class);
                            String B1 = ConvertCellValueUtil.cv(row.getCell(1), String.class);
                            if (StringUtils.isBlank(A1) && "操作人".equals(B1) || "序号".equals(A1) && "日期".equals(B1)) {
                                continue;
                            }
                            to.setCountry("US");
                            to.setOrderId(ConvertCellValueUtil.cv(row.getCell(5), String.class));
                            to.setStatus(ConvertCellValueUtil.cv(row.getCell(23), String.class));
                            os.add(to);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return os;
    }

    public List<Shop> readShopCatalog(String fileName) {
        List<Shop> shops = new ArrayList<>();
        String[] files = readSortFiles(fileName);

        for (String file : files) {
            try (FileInputStream fis = new FileInputStream(file)) {
                System.out.println("Reading " + file);
                Workbook workbook = new XSSFWorkbook(fis);
                Sheet sheet = workbook.getSheetAt(0);

                if (sheet == null) {
                    continue;
                }

                for (int j = 1; j <= sheet.getLastRowNum(); j++) {
                    Row row = sheet.getRow(j);

                    Shop shop = new Shop();
                    shop.setCompanyNumber(ConvertCellValueUtil.cv(row.getCell(0), String.class));
                    shop.setClientId(ConvertCellValueUtil.cv(row.getCell(1), String.class));
                    shop.setCompanyName(ConvertCellValueUtil.cv(row.getCell(2), String.class));
                    shop.setNick(ConvertCellValueUtil.cv(row.getCell(3), String.class));
                    shop.setCN(ConvertCellValueUtil.cv(row.getCell(4), String.class));
                    shop.setCategory(ConvertCellValueUtil.cv(row.getCell(5), String.class));
                    shop.setStatus(ConvertCellValueUtil.cv(row.getCell(7), String.class));

                    shops.add(shop);
                }
            } catch (IOException e) {
                // e.printStackTrace();
            }
        }

        return shops;
    }

    public DailyDetail readDaily(String filename) {
        String[] headerFirst = new String[] { "时间", "订单号", "订单信息", "订单详情", "日期" };

        DailyDetail daily = new DailyDetail();
        List<NotShip> notShips = new ArrayList<>();
        List<OrderDetail> orderDetails = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filename)) {
            System.out.println("Reading " + filename);
            Workbook workbook = WorkbookFactory.create(fis);
            Sheet sheet = workbook.getSheetAt(0);

            // Company data
            String date = new File(new File(filename).getParent()).getName();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
            try {
                daily.setCollectionDate(dateFormat.parse(date));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            daily.setCompanyNumber(new File(filename).getName().substring(0, 4));
            daily.setCompany(sheet.getRow(0).getCell(0).toString());
            daily.setNick(sheet.getRow(0).getCell(1).toString());
            daily.setOperator(sheet.getRow(1).getCell(0).toString());
            daily.setCN(ShopFileInfo.convert(filename).getCN());

            // Other data processing
            // 在售数据
            String u = sheet.getRow(1).getCell(1).toString();
            daily.setInStockNumber(ConvertCellValueUtil.parseInteger(sheet.getRow(1).getCell(1)));


            String m = sheet.getRow(1).getCell(2).toString();
            daily.setReviewNumber(ConvertCellValueUtil.parseInteger(sheet.getRow(1).getCell(2)));


            String d = sheet.getRow(1).getCell(3).toString();
            Integer i1 = ConvertCellValueUtil.parseInteger(sheet.getRow(1).getCell(3));
            daily.setRemovedNumber(i1);
            // 店铺数据
            Row r0 = sheet.getRow(2);

            daily.setIM24(ConvertCellValueUtil.cv(r0.getCell(0), Double.class));
            daily.setNotSell(ConvertCellValueUtil.cv(r0.getCell(1), Double.class));
            daily.setWrongGoods(ConvertCellValueUtil.cv(r0.getCell(2), Double.class));
            daily.setDispute(ConvertCellValueUtil.cv(r0.getCell(3), Double.class));
            daily.setGoodReviews(ConvertCellValueUtil.cv(r0.getCell(4), Double.class));
            daily.setCollect72(ConvertCellValueUtil.cv(r0.getCell(5), Double.class));

            // 资金数据
            Row r1 = sheet.getRow(3);
            daily.setLend(splitAmount(r1.getCell(0).toString()).getValue());
            daily.setFreeze(splitAmount(r1.getCell(1).toString()).getValue());
            daily.setOnWay(splitAmount(r1.getCell(2).toString()).getValue());
            daily.setArrears(splitAmount(r1.getCell(3).toString()).getValue());

            // 直通车数据
            daily.setConsume(splitAmount(r1.getCell(4) != null ? r1.getCell(4).toString() : null).getValue());
            daily.setPromotion(splitAmount(r1.getCell(5) != null ? r1.getCell(5).toString() : null).getValue());

            // 提现数据
            int[] ints = getHeaderLine(sheet, "时间", headerFirst);
            if (ints != null) {
                List<Withdraw> withdraws = new ArrayList<>();
                for (int i = ints[0]; i < ints[1]; i++) {
                    Row row = sheet.getRow(i);
                    String v = row.getCell(0).toString();
                    if (v.contains("时间") || v.contains("暂无数据")) {
                        continue;
                    }
                    Withdraw withdraw = new Withdraw();
                    withdraw.setWithdrawTime(row.getCell(0).getDateCellValue());
                    String[] splitValue = row.getCell(4).toString().split(" ");
                    double amount = Double.parseDouble(splitValue[0].replace(",", ""));
                    withdraw.setAmount(amount);
                    withdraws.add(withdraw);
                }
                daily.setWithdraws(withdraws);
            } else {
                daily.setWithdraws(Arrays.asList(new Withdraw[0]));
            }

            // 在途订单
            ints = getHeaderLine(sheet, "订单号", headerFirst);
            if (ints != null) {
                List<OnWayOrder> onWayOrders = new ArrayList<>();
                for (int j = ints[0]; j <= ints[1]; j++) {
                    Row row = sheet.getRow(j);
                    OnWayOrder onWayOrder = new OnWayOrder();

                    if (row.getCell(0).getCellTypeEnum() == CellType.STRING) {
                        String v = row.getCell(0).getStringCellValue();
                        if (v.contains("订单号")) {
                            continue;
                        }
                    }

                    onWayOrder.setOrderId(Double.toString(row.getCell(0).getNumericCellValue()));
                    onWayOrder.setPaymentTime(row.getCell(1).getDateCellValue());
                    onWayOrder.setShippingTime(row.getCell(2) != null ? row.getCell(2).getDateCellValue() : null);
                    onWayOrder.setReceiptTime(row.getCell(3) != null ? row.getCell(3).getDateCellValue() : null);
                    Map.Entry<String, Double> split = splitAmount(row.getCell(4).toString());
                    onWayOrder.setAmount(split.getValue());
                    onWayOrder.setReason(row.getCell(9).toString());
                    onWayOrders.add(onWayOrder);
                }
                daily.setOnWayOrders(onWayOrders);
            } else {
                daily.setOnWayOrders(Arrays.asList(new OnWayOrder[0]));
            }

            // 纠纷订单
            ints = getHeaderLine(sheet, "订单信息", headerFirst);
            if (ints != null) {
                List<Dispute> disputes = new ArrayList<>();
                for (int j = ints[0]; j <= ints[1]; j++) {
                    Row row = sheet.getRow(j);
                    String v = row.getCell(0).toString();
                    if (v.equals("订单信息") || v.equals("暂无数据")) {
                        continue;
                    }

                    Dispute dispute = new Dispute();
                    String c0 = row.getCell(0).toString();
                    // 订单
                    // 订单号
                    Matcher matcher = Pattern.compile("订单信息\\s*(\\d+)").matcher(c0);
                    if (matcher.find()) {
                        dispute.setOrderId(matcher.group(1));
                    }
                    // 下单时间
                    String time = "";
                    matcher = Pattern.compile("下单时间\\s*([\\d-]+\\s[\\d:]+)").matcher(c0);
                    if (matcher.find()) {
                        time = matcher.group(1);
                    }
                    try {
                        dispute.setOrderTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(time));
                    } catch (ParseException e) {
                        // e.printStackTrace(); // Handle parsing exception if necessary
                    }
                    dispute.setDisputeTime(row.getCell(4).getDateCellValue());

                    String c5 = row.getCell(5) != null ? row.getCell(5).toString() : null;
                    if (c5 != null) {
                        dispute.setStatus(c5.split("\\n")[0]);
                        if (c5.contains("剩余：")) {
                            int index = c5.indexOf("剩余：");
                            dispute.setLastTime(c5.substring(index + 4));
                        }
                    }

                    disputes.add(dispute);
                }
                daily.setDisputeOrders(disputes);
            } else {
                daily.setDisputeOrders(Arrays.asList(new Dispute[0]) );
            }

            // 订单详情
            ints = getHeaderLine(sheet, "订单详情", headerFirst);
            if (ints != null) {
                for (int j = ints[0]; j <= ints[1]; j++) {
                    Row row = sheet.getRow(j);
                    OrderDetail od = new OrderDetail();
                    String c0 = row.getCell(0) != null ? row.getCell(0).toString() : null;
                    if (c0 != null && c0.equals("订单详情")) {
                        continue;
                    }

                    // 订单
                    // 订单号
                    Matcher matcher = Pattern.compile("订单号:\\s*(\\d+)").matcher(c0);
                    if (matcher.find()) {
                        od.setOrderId(matcher.group(1));
                    }
                    // 下单时间
                    String time = "";
                    matcher = Pattern.compile("下单时间:\\s*([\\d-]+\\s[\\d:]+)").matcher(c0);
                    if (matcher.find()) {
                        time = matcher.group(1);
                    }
                    try {
                        od.setOrderTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(time));
                    } catch (ParseException e) {
                        e.printStackTrace(); // Handle parsing exception if necessary
                    }

                    // 标题
                    String c2 = row.getCell(2) != null ? row.getCell(2).toString() : null;
                    String[] raw = c2 != null ? c2.split("\\n") : new String[0];
                    od.setTitle(raw.length > 0 ? raw[0] : "");
                    od.setIsAssess(Arrays.asList(raw).contains("上网时效考核订单"));
                    for (int i = 0; i < raw.length; i++) {
                        if (raw[i].equals("商品属性:") && raw[i + 1].toLowerCase().contains("united states")) {
                            od.setShipsFrom(OrderShipsFromTypes.UnitedStates);
                        }
                        if (raw[i].equals("商品属性:") && raw[i + 1].toLowerCase().contains("brazil")) {
                            od.setShipsFrom(OrderShipsFromTypes.Brazil);
                        }
                    }

                    // 数量
                    String c3 = row.getCell(3) != null ? row.getCell(3).toString() : null;
                    int d1 = 1;
                    try {
                        d1 = Integer.parseInt(c3 != null && !c3.isEmpty() ? c3.substring(1) : "1");
                    } catch (NumberFormatException e) {
                        e.printStackTrace(); // Handle parsing exception if necessary
                    }
                    od.setQuantity(d1);

                    //售后
                    String c4 = row.getCell(4) != null ? row.getCell(4).toString() : null;
                    od.setAfter(c4);

                    // 金额
                    String c5 = row.getCell(5) != null ? row.getCell(5).toString() : null;
                    if (c5 != null) {
                        String[] split = c5.split("\\r?\\n");
                        od.setRMB(splitAmount(split[0]).getValue());
                        if (split.length > 1) {
                            Map.Entry<String, Double> kv = splitAmount(split[1]);
                            od.setSymbol(kv.getKey());
                            od.setAmount(kv.getValue());
                        } else {
                            od.setAmount(0.0);
                        }
                    }

                    // 状态
                    String c6 = row.getCell(6) != null ? row.getCell(6).toString() : null;
                    if (c6 != null) {
                        od.setStatus(c6.split("\\n")[0]);
                        if (c6.contains("剩余时间：")) {
                            int index = c6.indexOf("剩余时间：");
                            od.setLastTime(c6.substring(index + 6));
                        }
                    }
                    orderDetails.add(od);

                }

                daily.setNotShips(notShips);
                daily.setOrderDetails(orderDetails);
            }
            else {
                daily.setOrderDetails(Arrays.asList(new OrderDetail[0]));
            }

            // 直通车消耗
            ints = getHeaderLine(sheet, "日期", headerFirst);
            if (ints != null) {
                List<PromotionDetail> list = new ArrayList<>();

                for (int j = ints[0]; j <= ints[1]; j++) {
                    Row row = sheet.getRow(j);
                    PromotionDetail pd = new PromotionDetail();
                    String c0 = row.getCell(0) != null ? row.getCell(0).toString() : null;
                    if (c0 != null && c0.equals("日期")) {
                        continue;
                    }

                    pd.setTime(row.getCell(0).getDateCellValue());
                    pd.setExpenses(row.getCell(1).getNumericCellValue());
                    pd.setInCome(row.getCell(2).getNumericCellValue());

                    list.add(pd);
                }

                daily.setPromotionDetails(list);
            } else {
                daily.setPromotionDetails(Arrays.asList(new PromotionDetail[0]));
            }


            // Return the populated DailyDetail object
            return daily;
        } catch (IOException | EncryptedDocumentException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<SubmitOrder> readSubmitOrder(String fileName) {
        List<SubmitOrder> orders = new ArrayList<>();
        String[] files = readSortFiles(fileName);
        int i, j =0;
        for (i = 0; i < files.length; i++) {
            try (FileInputStream fis = new FileInputStream(files[i])) {
                logger.info("Reading " + files[i]);
                Workbook workbook = WorkbookFactory.create(fis);
                Sheet sheet = workbook.getSheetAt(0);
                if (sheet != null) {
                    for (j = 0; j <= sheet.getLastRowNum(); j++) {
                        Row row = sheet.getRow(j);

                        SubmitOrder order = new SubmitOrder();
                        order.setStoreName(ConvertCellValueUtil.cv(row.getCell(0), String.class));
                        // Skip header
                        if (order.getStoreName() != null && order.getStoreName().contains("店铺")) {
                            continue;
                        }

                        order.setOperator(ConvertCellValueUtil.cv(row.getCell(1), String.class));
                        order.setClientId(ConvertCellValueUtil.cv(row.getCell(2), String.class));

                        order.setOrderId(ConvertCellValueUtil.cv(row.getCell(4), String.class));
                        order.setOrderStatus(ConvertCellValueUtil.cv(row.getCell(5), String.class));

                        order.setOrderTime((Date) ConvertCellValueUtil.cv(row.getCell(7), Data.class));

                        order.setCost(ConvertCellValueUtil.cv(row.getCell(13), Double.class));

                        order.setOrderPrice(ConvertCellValueUtil.cv(row.getCell(14), String.class));
                        order.setTradeId(ConvertCellValueUtil.cv(row.getCell(17), String.class).trim().replace("\r", "").replace("\n", ""));

                        double amount = -1.1;
                        try {
                            amount = Double.parseDouble(ConvertCellValueUtil.cv(row.getCell(18), String.class).trim().replace("RMB", "").replace("R", "").replace("$", ""));
                        } catch (NumberFormatException e) {
                            amount = -1.1;
                        }
                        order.setDeductionAmount(amount);

                        orders.add(order);
                    }
                }
            } catch (IOException | EncryptedDocumentException e) {
                logger.warning(MessageFormat.format("warning: line:{0} file:{1}", j, files[i]));
            }
        }
        return orders;
    }


    public List<ShopLend> readShopLend(String fileName) {
        List<ShopLend> os = new ArrayList<>();
        String[] ss = readSortFiles(fileName);
        int c, i = 0;
        for ( c = 0; c < ss.length; c++) {
            try (FileInputStream fis = new FileInputStream(ss[c])) {
                Workbook workbook = WorkbookFactory.create(fis);
                logger.info("Reading " + ss[c]);
                Sheet sheet = workbook.getSheetAt(0);

                if (sheet != null && sheet.getLastRowNum() > 0) {
                    for (i = 1; i < sheet.getLastRowNum() + 1; i++) {
                        Row row = sheet.getRow(i);

                        ShopLend to = new ShopLend();

                        to.setProductId(ConvertCellValueUtil.cv(row.getCell(0), String.class));
                        to.setProductName(ConvertCellValueUtil.cv(row.getCell(1), String.class));
                        to.setSUK(ConvertCellValueUtil.cv(row.getCell(2), String.class));
                        to.setProductCode(ConvertCellValueUtil.cv(row.getCell(3), String.class));
                        to.setQuantity(ConvertCellValueUtil.cv(row.getCell(4), Double.class));
                        to.setProductImage(ConvertCellValueUtil.cv(row.getCell(5), String.class));
                        to.setTurnover(ConvertCellValueUtil.cv(row.getCell(6), Double.class));
                        to.setLend(ConvertCellValueUtil.cv(row.getCell(7), Double.class));
                        to.setFee(ConvertCellValueUtil.cv(row.getCell(8), Double.class));
                        to.setAffiliate(ConvertCellValueUtil.cv(row.getCell(9), Double.class));
                        to.setCashback(ConvertCellValueUtil.cv(row.getCell(10), Double.class));

                        if (row.getPhysicalNumberOfCells() == 13) {
                            to.setSettlementTime((Date) ConvertCellValueUtil.cv(row.getCell(11), Data.class));
                            to.setOrderId(ConvertCellValueUtil.cv(row.getCell(12), String.class));
                        } else {
                            to.setSettlementTime((Date) ConvertCellValueUtil.cv(row.getCell(12), Data.class));
                            to.setOrderId(ConvertCellValueUtil.cv(row.getCell(13), String.class));
                        }

                        to.setFileName(ss[c]);
                        os.add(to);
                    }
                }
            } catch (IOException | EncryptedDocumentException e) {
                logger.warning(MessageFormat.format("warning: line:{0} file:{1}", i, ss[c]));
            }
        }
        return os;
    }

    public List<ShopRefund> readShopRefund(String fileName) {
        List<ShopRefund> os = new ArrayList<>();
        String[] ss = readSortFiles(fileName);
        int c, i = 0;

        for ( c= 0; c < ss.length; c++) {
            try (FileInputStream fis = new FileInputStream(ss[c])) {
                Workbook workbook = WorkbookFactory.create(fis);
                logger.info("Reading " + ss[c]);
                if (workbook.getNumberOfSheets() > 0) {
                    Sheet sheet = workbook.getSheetAt(0);
                    if (sheet != null && sheet.getLastRowNum() + 1 >= 2) {
                        for (i = 1; i < sheet.getLastRowNum() + 1; i++) {
                            Row row = sheet.getRow(i);

                            ShopRefund to = new ShopRefund();

                            to.setOrderId(ConvertCellValueUtil.cv(row.getCell(0), String.class));
                            to.setProductId(ConvertCellValueUtil.cv(row.getCell(1), String.class));
                            to.setProductName(ConvertCellValueUtil.cv(row.getCell(2), String.class));
                            to.setSUK(ConvertCellValueUtil.cv(row.getCell(3), String.class));
                            to.setProductCode(ConvertCellValueUtil.cv(row.getCell(4), String.class));
                            to.setQuantity(ConvertCellValueUtil.cv(row.getCell(5), Integer.class));
                            to.setTurnover(ConvertCellValueUtil.cv(row.getCell(6), Double.class));
                            to.setRefund(ConvertCellValueUtil.cv(row.getCell(7), Double.class));
                            to.setSources(ConvertCellValueUtil.cv(row.getCell(8), String.class));
                            to.setFee(ConvertCellValueUtil.cv(row.getCell(9), Double.class));
                            to.setFee(ConvertCellValueUtil.cv(row.getCell(10), Double.class));
                            to.setAlliance(ConvertCellValueUtil.cv(row.getCell(11), Double.class));
                            to.setCashback(ConvertCellValueUtil.cv(row.getCell(12), Double.class));

                            if (row.getPhysicalNumberOfCells() == 13) {
                                to.setRefundTime(ConvertCellValueUtil.cv(row.getCell(12), Date.class));
                            } else {
                                to.setRefundTime(ConvertCellValueUtil.cv(row.getCell(13), Date.class));
                            }
                            to.setFileName(ss[c]);

                            os.add(to);
                        }
                    }
                }
            } catch (IOException e) {
                logger.warning(MessageFormat.format("warning: line:{0} file:{1}", i, ss[c]));
            }
        }
        return os;
    }

    public void saveShopLend(String fileName, ShopLend[] shopLends, String demoFileName, boolean overwrite) {
        try {
            Files.copy(Paths.get(demoFileName), Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);

            Workbook workbook;
            try (FileInputStream fis = new FileInputStream(fileName)) {
                workbook = WorkbookFactory.create(fis);
            }

            Sheet sheet = workbook.getSheetAt(0);
            CellStyle style = workbook.createCellStyle();
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);

            // region 设置数据
            for (int i = 0; i < shopLends.length; i++) {
                Row row = sheet.createRow(i + 1);

                Cell cell0 = row.createCell(0);
                cell0.setCellType(CellType.NUMERIC);
                cell0.setCellStyle(style);
                cell0.setCellValue(i + 1);

                Cell cell1 = row.createCell(1);
                cell1.setCellType(CellType.STRING);
                cell1.setCellStyle(style);
                cell1.setCellValue(shopLends[i].getProductId());

                Cell cell2 = row.createCell(2);
                cell2.setCellType(CellType.STRING);
                cell2.setCellValue(shopLends[i].getProductName());

                Cell cell3 = row.createCell(3);
                cell3.setCellType(CellType.STRING);
                cell3.setCellValue(shopLends[i].getSUK());

                Cell cell4 = row.createCell(4);
                cell4.setCellType(CellType.STRING);
                cell4.setCellValue(shopLends[i].getProductCode());

                Cell cell5 = row.createCell(5);
                cell5.setCellType(CellType.NUMERIC);
                cell5.setCellStyle(style);
                cell5.setCellValue(shopLends[i].getQuantity());

                Cell cell6 = row.createCell(6);
                cell6.setCellType(CellType.STRING);
                cell6.setCellValue(shopLends[i].getProductImage());

                Cell cell7 = row.createCell(7);
                cell7.setCellType(CellType.NUMERIC);
                cell7.setCellStyle(style);
                cell7.setCellValue(shopLends[i].getTurnover());

                Cell cell8 = row.createCell(8);
                cell8.setCellType(CellType.NUMERIC);
                cell8.setCellStyle(style);
                cell8.setCellValue(shopLends[i].getLend());

                Cell cell9 = row.createCell(9);
                cell9.setCellType(CellType.NUMERIC);
                cell9.setCellStyle(style);
                cell9.setCellValue(shopLends[i].getCost());

                Cell cell10 = row.createCell(10);
                cell10.setCellType(CellType.NUMERIC);
                cell10.setCellStyle(style);
                cell10.setCellValue(shopLends[i].getProfit());

                Cell cell11 = row.createCell(11);
                cell11.setCellType(CellType.NUMERIC);
                cell11.setCellStyle(style);
                cell11.setCellValue(shopLends[i].getRate());

                Cell cell12 = row.createCell(12);
                CellStyle style1 = workbook.createCellStyle();
                style1.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
                style1.setAlignment(HorizontalAlignment.CENTER);
                style1.setVerticalAlignment(VerticalAlignment.CENTER);
                cell12.setCellStyle(style1);
                cell12.setCellValue(shopLends[i].getSettlementTime());

                Cell cell13 = row.createCell(13);
                cell13.setCellType(CellType.STRING);
                cell13.setCellStyle(style);
                cell13.setCellValue(shopLends[i].getOrderId());

            }
            //endregion

            //region 设置公式统计
            Row totalRow = sheet.createRow(shopLends.length + 2);
            Cell c0 = totalRow.createCell(7);
            c0.setCellType(CellType.STRING);
            c0.setCellStyle(style);
            c0.setCellValue("总额:");
            Cell c1 = totalRow.createCell(8);
            c1.setCellType(CellType.NUMERIC);
            c1.setCellStyle(style);
            c1.setCellFormula("SUBTOTAL(9, I2:I" + (shopLends.length + 1) + ")");
            Cell c2 = totalRow.createCell(9);
            c2.setCellType(CellType.NUMERIC);
            c2.setCellStyle(style);
            c2.setCellFormula("SUBTOTAL(9, J2:J" + (shopLends.length + 1) + ")");
            Cell c3 = totalRow.createCell(10);
            c3.setCellType(CellType.NUMERIC);
            c3.setCellStyle(style);
            c3.setCellFormula("SUBTOTAL(9, K2:K" + (shopLends.length + 1) + ")");
            //endregion

            double lend = 0.0;
            double profit = 0.0;
            double cost = 0.0;
            for (ShopLend item : shopLends) {
                lend += item.getLend();
                cost += item.getCost();
                profit += item.getProfit();
            }

            Cell c4 = totalRow.createCell(11);
            c4.setCellType(CellType.NUMERIC);
            c4.setCellStyle(style);
            c4.setCellValue(Math.round(profit / cost));

            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                workbook.write(fos);
            }

            System.out.println("Saved " + fileName);
            workbook.close();
        } catch (IOException | EncryptedDocumentException e) {
            logger.warning(MessageFormat.format("warning: line:{0} file:{1}", ""));

        }
    }

    public void saveShopRefund(String fileName, ShopRefund[] shopRefunds, String demoFileName, boolean overwrite) {
        try {
            Files.copy(Paths.get(demoFileName), Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);

            try (FileInputStream fis = new FileInputStream(fileName)) {
                Workbook workbook = WorkbookFactory.create(fis);
                Sheet sheet = workbook.getSheetAt(0);

                for (int i = 0; i < shopRefunds.length; i++) {
                    Row row = sheet.createRow(i + 1);

                    row.createCell(0, CellType.NUMERIC).setCellValue(i + 1);
                    row.createCell(1, CellType.STRING).setCellValue(shopRefunds[i].getOrderId());
                    row.createCell(2, CellType.STRING).setCellValue(shopRefunds[i].getProductId());
                    row.createCell(3, CellType.STRING).setCellValue(shopRefunds[i].getProductName());
                    row.createCell(4, CellType.STRING).setCellValue(shopRefunds[i].getSUK());
                    row.createCell(5, CellType.STRING).setCellValue(shopRefunds[i].getProductCode());
                    row.createCell(6, CellType.NUMERIC).setCellValue(shopRefunds[i].getQuantity());
                    row.createCell(7, CellType.NUMERIC).setCellValue(shopRefunds[i].getTurnover());
                    row.createCell(8, CellType.NUMERIC).setCellValue(shopRefunds[i].getRefund());
                    row.createCell(9, CellType.STRING).setCellValue(shopRefunds[i].getSources());
                    row.createCell(10, CellType.NUMERIC).setCellValue(shopRefunds[i].getFee());
                    row.createCell(11, CellType.NUMERIC).setCellValue(shopRefunds[i].getAlliance());
                    row.createCell(12, CellType.NUMERIC).setCellValue(shopRefunds[i].getCashback());

                    Cell cell13 = row.createCell(13);
                    CellStyle style = workbook.createCellStyle();
                    style.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
                    cell13.setCellStyle(style);
                    cell13.setCellValue(shopRefunds[i].getRefundTime());

                    row.createCell(14, CellType.STRING).setCellValue(shopRefunds[i].getRefundReason());
                    row.createCell(15, CellType.STRING).setCellValue(shopRefunds[i].getDebitOperation() != null ? shopRefunds[i].getDebitOperation() : "");
                    row.createCell(16, CellType.NUMERIC).setCellValue(Math.round(shopRefunds[i].getDeduction() * 100.0) / 100.0);
                    row.createCell(17, CellType.STRING).setCellValue(shopRefunds[i].getRefundOperation() != null ? shopRefunds[i].getRefundOperation() : "");
                    row.createCell(18, CellType.STRING).setCellValue(shopRefunds[i].getRefundId() != null ? shopRefunds[i].getRefundId() : "");
                    row.createCell(19, CellType.STRING).setCellValue(shopRefunds[i].getTrade() != null ? shopRefunds[i].getTrade() : "");
                }

                Row totalRow = sheet.createRow(shopRefunds.length + 2);
                totalRow.createCell(15, CellType.STRING).setCellValue("总额:");
                Cell c1 = totalRow.createCell(16, CellType.FORMULA);
                c1.setCellFormula("SUBTOTAL(9, Q2:Q" + (shopRefunds.length + 1) + ")");

                try (FileOutputStream fos = new FileOutputStream(fileName)) {
                    workbook.write(fos);
                    logger.info("save " + fileName);
                }
            }
        } catch (IOException | EncryptedDocumentException e) {
            // e.printStackTrace(); // Handle exceptions as needed
        }
    }

    //endregion

    private Date parseDate(String dateString, String[] possibleDateFormats) {
        for (String format : possibleDateFormats) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                return dateFormat.parse(dateString);
            } catch (ParseException e) {
                // 如果解析失败，继续尝试下一个日期格式
            }
        }
        return null; // 如果所有日期格式都无法解析，返回 null
    }
    public Map.Entry<String, Double> splitAmount(String raw) {
        if (raw != null && !raw.trim().isEmpty()) {
            Matcher matcher = Pattern.compile("(\\p{Sc}?)([\\d.,]+)").matcher(raw);
            if (matcher.find()) {
                String currency = matcher.group(1).trim();
                String amountStr = matcher.group(2).replaceAll("[.,]", "");
                double amount = parseAmount(amountStr);
                return new AbstractMap.SimpleEntry<>(currency, amount);
            }
        }
        return new AbstractMap.SimpleEntry<>("", 0.00);

    }
    private double parseAmount(String amountStr) {
        try {
            return Double.parseDouble(amountStr) / 100; // Assuming the amount is in cents
        } catch (NumberFormatException e) {
            // Handle parsing error, return 0.00 as default
            return 0.00;
        }
    }
    private int[] getHeaderLine(Sheet sheet, String header, String[] headerList) {
        int start = -1, end = -1;

        // Search for the start index
        for (int i = 0; i < sheet.getLastRowNum(); i++) {
            Cell cell =  sheet.getRow(i).getCell(0);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                if (header.equals(cell.getStringCellValue()) && Arrays.asList(headerList).contains(cell.getStringCellValue())) {
                    start = i;
                    break;
                }
            }
        }

        // Search for the end index, use the last row of the table if not found
        if (start != -1) {
            List<String> arrayList = new ArrayList<>(Arrays.asList(headerList));
            arrayList.remove(header);
            for (int i = start + 1; i < sheet.getLastRowNum(); i++) {
                Cell cell = sheet.getRow(i).getCell(0);
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    if (arrayList.contains(cell.getStringCellValue())) {
                        end = i;
                        break;
                    }
                }
            }

            if (end == -1) {
                end = sheet.getLastRowNum();
            }
        }

        return start == -1 ? null : new int[] { start, end - 1 };
    }




}
