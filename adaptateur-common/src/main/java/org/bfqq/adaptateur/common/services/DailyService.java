package org.bfqq.adaptateur.common.services;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bfqq.adaptateur.common.models.daily.*;
import org.bfqq.adaptateur.common.models.manage.Recharge;
import org.bfqq.adaptateur.common.models.manage.User;
import org.bfqq.adaptateur.common.models.sheet.Shop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DailyService implements IDailyService {

    // region buildCompanyData
    private Map<DisputeTypes, List<Dispute>> classifiedDispute(Collection<Dispute> disputes) {
        Map<DisputeTypes, List<Dispute>> dic = new EnumMap<>(DisputeTypes.class);
        for (DisputeTypes type : DisputeTypes.values()) {
            dic.put(type, new ArrayList<>());
        }

        for (Dispute dispute : disputes) {
            for (DisputeTypes type : DisputeTypes.values()) {
                if (dispute.getStatus().contains("方案协商中")) {
                    dic.get(DisputeTypes.Talk).add(dispute);
                } else if (dispute.getStatus().contains("平台介入处理中")) {
                    dic.get(DisputeTypes.Platform).add(dispute);
                } else if (dispute.getStatus().contains("已结束")) {
                    dic.get(DisputeTypes.Close).add(dispute);
                }
            }
        }

        return dic;
    }
    private Map<OrderTypes, List<OrderDetail>> classifiedOrders(Collection<OrderDetail> orderDetails, LocalDate today) {
        LocalDate yesterday = today.minusDays(1);
        LocalDate before = today.minusDays(2);

        List<OrderDetail> yesterdayOrders = orderDetails.stream()
                .filter(o -> paresLocalDate(o.getOrderTime()).isEqual(yesterday))
                .collect(Collectors.toList());

        List<OrderDetail> beforeOrders = orderDetails.stream()
                .filter(o -> paresLocalDate(o.getOrderTime()).isEqual(before))
                .collect(Collectors.toList());

        List<OrderDetail> readyOrders = orderDetails.stream()
                .filter(o -> o.getStatus().contains("等待您发货") || o.getStatus().contains("等待发货"))
                .collect(Collectors.toList());

        List<OrderDetail> waitOrders = orderDetails.stream()
                .filter(o -> o.getStatus().contains("等待买家收货"))
                .collect(Collectors.toList());

        List<OrderDetail> notPayOrders = orderDetails.stream()
                .filter(o -> o.getStatus().contains("等待买家付款"))
                .collect(Collectors.toList());

        List<OrderDetail> cancelOrders = orderDetails.stream()
                .filter(o -> o.getAfter() != null && o.getAfter().equals("已取消"))
                .collect(Collectors.toList());

        List<OrderDetail> timeoutOrders = orderDetails.stream()
                .filter(o -> o.getAfter() == null && o.getStatus().equals("订单关闭"))
                .collect(Collectors.toList());

        List<OrderDetail> disputeOrders = orderDetails.stream()
                .filter(o -> o.getAfter() != null && o.getAfter().contains("纠纷"))
                .collect(Collectors.toList());

        List<OrderDetail> finishOrders = orderDetails.stream()
                .filter(o -> o.getStatus().contains("交易完成"))
                .collect(Collectors.toList());

        Map<OrderTypes, List<OrderDetail>> dic = new HashMap<>();
        dic.put(OrderTypes.BeforeOneDay, beforeOrders);
        dic.put(OrderTypes.Yesterday, yesterdayOrders);
        dic.put(OrderTypes.Wait, waitOrders);
        dic.put(OrderTypes.Ready, readyOrders);
        dic.put(OrderTypes.NotPay, notPayOrders);
        dic.put(OrderTypes.Cancel, cancelOrders);
        dic.put(OrderTypes.Timeout, timeoutOrders);
        dic.put(OrderTypes.Dispute, disputeOrders);
        dic.put(OrderTypes.Finish, finishOrders);

        return dic;
    }

    private LocalDate paresLocalDate(Date date){
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date paresDate(LocalDate localDate){
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public StoreOverview[] getStoreOverviews(LocalDate date, Collection<User> users, Collection<Recharge> recharges, Collection<DailyDetail> dailyDetails) {

        List<StoreOverview> overs = new ArrayList<>();

        for (DailyDetail daily : dailyDetails) {
            // 公司简称
            // String name = daily.getCompany().replace("市", "").replace("县", "").substring(2, 4);
            Map<OrderTypes, List<OrderDetail>> orderDic = classifiedOrders(daily.getOrderDetails(), date);
            Map<DisputeTypes, List<Dispute>> disputeDic = classifiedDispute(daily.getDisputeOrders());


            LocalDateTime max = LocalDateTime.now();
            LocalDateTime min = LocalDateTime.now().minusMonths(1);
            long disCount30 = daily.getDisputeOrders().stream().filter(o -> paresLocalDate(o.getDisputeTime()).isBefore(ChronoLocalDate.from(min))  && paresLocalDate(o.getDisputeTime()).isAfter(ChronoLocalDate.from(max))).count();
            long finCount30 = orderDic.get(OrderTypes.Finish).stream().filter(o -> paresLocalDate(o.getOrderTime()).isBefore(ChronoLocalDate.from(min))  && paresLocalDate(o.getOrderTime()).isAfter(ChronoLocalDate.from(max))).count();
            long disCount = daily.getDisputeOrders().size();
            long finCount = orderDic.get(OrderTypes.Finish).size();

            double exp30 = (disCount30 / 0.3) - disCount30 - finCount30;

            // 在途纠纷及拒付订单
            double loseAmount = daily.getOnWayOrders().stream().filter(o -> o.getReason().contains("纠纷中") || o.getReason().contains("拒付中")).mapToDouble(OnWayOrder::getAmount).sum();

            // 实际充值 不是索赔，不是返点
            double reality = recharges.stream().filter(r -> r.getCompanyName().contains(daily.getCompany()) && !r.getMark().contains("返点") && !r.getMark().contains("索赔") && !r.getMark().contains("海外仓")).mapToDouble(Recharge::getAmount).sum();

            // 云仓余额
            double balance = users.stream().filter(u -> u.getCompanyName().contains(daily.getCompany())).findFirst().get().getBalance();

            // 实际提现
            double withdraws = daily.getWithdraws().stream().mapToDouble(Withdraw::getAmount).sum();

            // 纠纷最小处理天数
            Optional<String> disputeTime = daily.getDisputeOrders().stream().filter(d -> d.getLastTime() != null).map(Dispute::getLastTime).min(Comparator.naturalOrder());

            // 待发货最小处理天数
            Optional<String> orderTime = daily.getOrderDetails().stream().filter(d -> d.getLastTime() != null).map(OrderDetail::getLastTime).min(Comparator.naturalOrder());

            // 昨天等待发货及等待收货
            List<OrderDetail> r1 = classifiedOrders(orderDic.get(OrderTypes.Yesterday), date).get(OrderTypes.Ready);
            List<OrderDetail> w1 = classifiedOrders(orderDic.get(OrderTypes.Yesterday), date).get(OrderTypes.Wait);

            StoreOverview over = new StoreOverview();
            // 再售数据
            over.setCompany(daily.getCompany());
            over.setCN(daily.getNick());
            over.setOpera(daily.getOperator());
            over.setUP(daily.getInStockNumber());
            over.setCheck(daily.getReviewNumber());
            over.setDown(daily.getRemovedNumber());
            // 服务数据
            over.setIM24(daily.getIM24());
            over.setGood(daily.getGoodReviews());
            over.setDispute(daily.getDispute());
            over.setWrong(daily.getWrongGoods());
            // 纠纷数据
            over.setDisputeLine(disputeTime.orElse(null));
            over.setF30((int) finCount30);
            over.setD30((int) disCount30);
            over.setExp30((int) exp30);
            over.setFin((int) finCount);
            over.setDis((int) disCount);
            over.setClose(disputeDic.get(DisputeTypes.Close).size());
            over.setTalk(disputeDic.get(DisputeTypes.Talk).size());
            over.setPalt(disputeDic.get(DisputeTypes.Platform).size());
            // 订单数据
            over.setAll(orderDic.get(OrderTypes.Ready).size());
            over.setReadyLine(orderTime.orElse(null));
            over.setNew(orderDic.get(OrderTypes.Yesterday).size());
            over.setReady(r1.size());
            over.setWait(w1.size());
            // 资金数据
            Funds funds = new Funds();
            funds.setLend(daily.getLend());
            funds.setFreeze(daily.getFreeze());
            funds.setOnWay(daily.getOnWay());
            funds.setArre(daily.getArrears());
            funds.setLose(loseAmount);
            funds.setGet(Math.abs(withdraws));
            funds.setReality(reality);
            funds.setBalance(balance);
            over.setFunds(funds);

            overs.add(over);
        }

        return overs.toArray(new StoreOverview[0]);
    }
    private Funds getCompanyFunds(StoreOverview[] storeOverviews) {
        double lend = 0.0;
        double freeze = 0.0;
        double onWay = 0.0;
        double arrears = 0.0;
        double lose = 0.0;
        double withdraws = 0.0;
        double reality = 0.0;
        double balance = 0.0;

        for (StoreOverview overview : storeOverviews) {
            lend += overview.getFunds().getLend();
            freeze += overview.getFunds().getFreeze();
            onWay += overview.getFunds().getOnWay();
            arrears += overview.getFunds().getArre();
            lose += overview.getFunds().getLose();
            withdraws += overview.getFunds().getGet();
            reality += overview.getFunds().getReality(); // 使用累加方式
            balance += overview.getFunds().getBalance(); // 使用累加方式
        }

        Funds funds = new Funds();
        funds.setLend(lend);
        funds.setFreeze(freeze);
        funds.setOnWay(onWay);
        funds.setArre(arrears);
        funds.setLose(lose);
        funds.setGet(withdraws);
        funds.setReality(reality);
        funds.setBalance(balance);

        return funds;
    }
    // endregion
    public void BuildCompanyData(Collection<Shop> shops, LocalDate date, Collection<User> users, Collection<Recharge> recharges, Collection<DailyDetail> dailyDetails, String filename){
        try {
            StoreOverview[] storeOverviews = getStoreOverviews(date, users, recharges, dailyDetails);
            Map<String, List<StoreOverview>> group = Arrays.stream(storeOverviews).collect(Collectors.groupingBy(StoreOverview::getCompany));

            for (Map.Entry<String, List<StoreOverview>> entry : group.entrySet()) {
                String company = entry.getKey();
                List<StoreOverview> storeOverviewList = entry.getValue();
                Funds funds = getCompanyFunds(storeOverviewList.toArray(new StoreOverview[0]));

                List<String> upload = new ArrayList<>();
                double total = funds.getLend() + funds.getOnWay() + funds.getGet() + funds.getBalance() - (funds.getArre() + funds.getLose() + funds.getReality());

                upload.add(String.format("%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                upload.add("-------------------------------");
                upload.add(String.format("  公司: %s", company));
                upload.add(String.format("（加）放款: %.2f", funds.getLend()));
                upload.add(String.format("（减）冻结: %.2f", funds.getFreeze()));
                upload.add(String.format("（加）在途: %.2f", funds.getOnWay()));
                upload.add(String.format("（减）欠款: %.2f", funds.getArre()));
                upload.add(String.format("（减）损耗: %.2f", funds.getLose()));
                upload.add(String.format("（加）回款: %.2f", funds.getGet()));
                upload.add(String.format("（减）实充: %.2f", funds.getReality()));
                upload.add(String.format("（加）余额: %.2f", funds.getBalance()));
                upload.add(String.format("（等）利润: %.2f", total));

                for (StoreOverview os : storeOverviewList) {
                    upload.add("-------------------------------");
                    upload.add(String.format("%s: %s",  os.getCN(), os.getOpera()));
                    upload.add(String.format("上架:%d 审核:%d 下架:%d", os.getUP(), os.getCheck(), os.getDown()));
                    upload.add(String.format("IM24:%.2f 好评:%.2f", os.getIM24(), os.getGood()));
                    upload.add(String.format("纠纷:%.2f 错发:%.2f", os.getDispute(), os.getWrong()));
                    upload.add(String.format("纠纷处理:%s", os.getDisputeLine()));
                    upload.add(String.format("发货处理:%s", os.getReadyLine()));
                    upload.add(String.format("F30:%d D30:%d ExpD30%%:%d", os.getF30(), os.getD30(), os.getExp30()));
                    upload.add(String.format("完成:%d 纠纷:%d 结束:%d", os.getFin(), os.getDis(), os.getClose()));
                    upload.add(String.format("协商:%d 介入:%d 总待:%d", os.getTalk(), os.getPalt(), os.getAll()));
                    upload.add(String.format("新单:%d 待发:%d 已发:%d", os.getNew(), os.getReady(), os.getWait()));
                    upload.add(String.format("放款:%.2f 冻结:%.2f", os.getFunds().getLend(), os.getFunds().getFreeze()));
                    upload.add(String.format("在途:%.2f 欠款:%.2f", os.getFunds().getOnWay(), os.getFunds().getArre()));
                    upload.add(String.format("损耗:%.2f 回款:%.2f", os.getFunds().getLose(), os.getFunds().getGet()));
                }
                upload.add("");

                String str = String.join("\r\n", upload) + "\r\n";
                Files.write(Paths.get(filename), str.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            }
            System.out.println("Build "+filename);
        } catch (IOException  e) {
            // e.printStackTrace(); // Handle exceptions as needed
        }
    }

    public void BuildOperData(Collection<Shop> shops, LocalDate date, Collection<User> users, Collection<Recharge> recharges, Collection<DailyDetail> dailyDetails, String filename){
        try {
            StoreOverview[] storeOverviews = getStoreOverviews(date, users, recharges, dailyDetails);
            Map<String, List<StoreOverview>> group = Arrays.stream(storeOverviews).collect(Collectors.groupingBy(StoreOverview::getOpera));

            for (Map.Entry<String, List<StoreOverview>> entry : group.entrySet()) {
                String opear = entry.getKey();
                List<StoreOverview> storeOverviewList = entry.getValue();
                Funds funds = getCompanyFunds(storeOverviewList.toArray(new StoreOverview[0]));

                List<String> upload = new ArrayList<>();

                for (StoreOverview os : storeOverviewList) {
                    upload.add("-------------------------------");
                    upload.add(String.format("%s: %s", os.getCN(), os.getOpera()));
                    upload.add(String.format("上架:%d 审核:%d 下架:%d", os.getUP(), os.getCheck(), os.getDown()));
                    upload.add(String.format("IM24:%.2f 好评:%.2f", os.getIM24(), os.getGood()));
                    upload.add(String.format("纠纷:%.2f 错发:%.2f", os.getDispute(), os.getWrong()));
                    upload.add(String.format("纠纷处理:%s", os.getDisputeLine()));
                    upload.add(String.format("发货处理:%s", os.getReadyLine()));
                    upload.add(String.format("F30:%d D30:%d ExpD30%%:%d", os.getF30(), os.getD30(), os.getExp30()));
                    upload.add(String.format("完成:%d 纠纷:%d 结束:%d", os.getFin(), os.getDis(), os.getClose()));
                    upload.add(String.format("协商:%d 介入:%d 总待:%d", os.getTalk(), os.getPalt(), os.getAll()));
                    upload.add(String.format("新单:%d 待发:%d 已发:%d", os.getNew(), os.getReady(), os.getWait()));
                    upload.add(String.format("放款:%.2f 冻结:%.2f", os.getFunds().getLend(), os.getFunds().getFreeze()));
                    upload.add(String.format("在途:%.2f 欠款:%.2f", os.getFunds().getOnWay(), os.getFunds().getArre()));
                    upload.add(String.format("损耗:%.2f 回款:%.2f", os.getFunds().getLose(), os.getFunds().getGet()));
                }
                upload.add("");

                String str = String.join("\r\n", upload) + "\r\n";
                String dir = FilenameUtils.getFullPath(filename);
                String name = FilenameUtils.getBaseName(filename);
                String ext = FilenameUtils.getExtension(filename);
                Path path = Paths.get(dir, name+opear + "."+ ext );
                File file = new File(path.toString());
                // 如果文件不存在，则创建文件
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter writer = new FileWriter(file);
                writer.write(str);
                writer.close();
                System.out.println("Build "+path.toString());
            }
        } catch (IOException  e) {
             e.printStackTrace(); // Handle exceptions as needed
        }
    }


    public Pair<Integer, Integer> listMissingStores(Collection<Shop> shops, LocalDate date, Collection<DailyDetail> dailyDetails){

        List<Shop> runCatalogShops = new ArrayList<>();
        for (Shop shop : shops) {
            if (shop.getStatus().equals("运营中")) {
                runCatalogShops.add(shop);
            }
        }

        System.out.println("运营中：" + runCatalogShops.size() + " 读取数量：" + dailyDetails.size());

        for (Shop run : runCatalogShops) {
            boolean flag = false;
            for (DailyDetail daily : dailyDetails) {
                if (daily.getCN().equals(run.getCN())) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                System.out.println(run.getCompanyNumber() + run.getCN() + run.getCompanyName() + run.getNick());
            }
        }
        return Pair.of(runCatalogShops.size(), dailyDetails.size());
    }


    // region buildOrders
    public static List<Pair<String, String>> createDetail(Collection<OrderDetail>... orderDetailss) {
        List<Pair<String, String>> detailList = new ArrayList<>();
        for (Collection<OrderDetail> orderDetails : orderDetailss){
            for (OrderDetail item : orderDetails) {
                Pair<String, String> pair;
                if (item.getSymbol() != null && !item.getSymbol().isEmpty()) {
                    pair = Pair.of(new SimpleDateFormat("yyyy-MM-dd").format(item.getOrderTime()), item.getSymbol() + " " + item.getAmount());
                } else {
                    String location;
                    if (item.getShipsFrom() == OrderShipsFromTypes.UnitedStates) {
                        location = "US";
                    } else if (item.getShipsFrom() == OrderShipsFromTypes.Brazil) {
                        location = "BR";
                    } else {
                        location = "None";
                    }
                    pair = Pair.of(new SimpleDateFormat("yyyy-MM-dd").format(item.getOrderTime()), location + " " + item.getRMB() + "RMB");
                }
                detailList.add(pair);
            }
        }

        return detailList;
    }

    public static Pair<String, String> createOverview(String name, Collection<OrderDetail>... orderDetailss) {
        double total = 0.0;
        int count = 0;
        for (Collection<OrderDetail> orderDetails : orderDetailss){
            for (OrderDetail item : orderDetails) {
                if ((item.getSymbol() != null && item.getSymbol().contains("R")) || item.getShipsFrom() == OrderShipsFromTypes.Brazil) {
                    total += item.getRMB();
                    count++;
                }
            }
        }

        return Pair.of(name, count + " 单 总BR " + java.lang.String.format("%.2f", total) + "RMB");
    }

    public static Pair<Integer, Double> createNumber(Collection<OrderDetail>... orderDetailss) {
        double total = 0.0;
        int count = 0;
        for (Collection<OrderDetail> orderDetails : orderDetailss){
            for (OrderDetail item : orderDetails) {
                if ((item.getSymbol() != null && item.getSymbol().contains("R")) || item.getShipsFrom() == OrderShipsFromTypes.Brazil) {
                    total += item.getRMB();
                    count++;
                }
            }
        }
        return Pair.of(count, total);
    }

    // endregion

    public void buildOrders(Collection<Shop> shops, LocalDate date, Collection<DailyDetail> dailyDetails, String filename)  {
        double total1 = 0.0;
        int count1 = 0;
        double total2 = 0.0;
        int count2 = 0;
        List<Pair<String, String >> list = new ArrayList<>();
        for (DailyDetail detail : dailyDetails) {
            Map<OrderTypes, List<OrderDetail>> orderDic = classifiedOrders(detail.getOrderDetails(), date);

            // 昨天等待发货及等待收货
            List<OrderDetail> r1 = classifiedOrders(orderDic.get(OrderTypes.Yesterday), date).get(OrderTypes.Ready);
            List<OrderDetail> w1 = classifiedOrders(orderDic.get(OrderTypes.Yesterday), date).get(OrderTypes.Wait);
            List<Pair<String, String>> kvs1 = createDetail(r1, w1);

            // 前天等待发货及等待收货
            List<OrderDetail> r2 = classifiedOrders(orderDic.get(OrderTypes.BeforeOneDay), date).get(OrderTypes.Ready);
            List<OrderDetail> w2 = classifiedOrders(orderDic.get(OrderTypes.BeforeOneDay), date).get(OrderTypes.Wait);
            List<Pair<String, String>> kvs2 = createDetail(r2, w2);


            String nick = shops.stream().filter(o -> o.getCompanyName().equals(detail.getCompany())).findFirst().get().getNick();
            Pair<String, String> kvs = createOverview(nick, r1, w1, r2, w2);
            Pair<Integer, Double> n1 = createNumber(r1, w1);
            Pair<Integer, Double> n2 = createNumber(r2, w2);

            if (n1.getKey() > 0 || n2.getKey() > 0) {
                count1 += n1.getKey();
                total1 += n1.getValue();
                count2 += n2.getKey();
                total2 += n2.getValue();
                list.add(kvs);
                list.addAll(kvs1);
                list.addAll(kvs2);
                list.add(Pair.of("", ""));
            }
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = formatter.format(paresDate(date));
        String yesterdat = formatter.format(paresDate(date.minusDays(1)));
        String bedore = formatter.format(paresDate(date.minusDays(2)));
        String kvt = currentDate;

        String kvt1 =  yesterdat +" " + count1 + "单";
        String kvt2 = bedore +" " + count2 + "单";
        list.add(Pair.of(kvt, "巴西订单"));
        list.add(Pair.of(kvt1, "BR " + String.format("%.2f", total1) + "RMB"));
        list.add(Pair.of(kvt2, "BR " + String.format("%.2f", total2) + "RMB"));

        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, String> entry : list) {
            str.append(entry.getKey()).append(" ").append(entry.getValue()).append("\r\n");
        }

        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
        try {
            Files.write(Paths.get(filename), str.toString().getBytes(), StandardOpenOption.CREATE);
            System.out.println("Build "+filename);
        } catch (IOException e) {
            // throw new RuntimeException(e);
        }
    }
}
