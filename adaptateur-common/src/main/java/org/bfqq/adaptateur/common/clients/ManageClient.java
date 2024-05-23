package org.bfqq.adaptateur.common.clients;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import okhttp3.*;
import org.bfqq.adaptateur.common.models.manage.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Data
public class ManageClient {
    private Map<String, Map<String, String>> dicc = new HashMap<>();
    private boolean isLoginAdmin = false;
    private String currentClientID;

    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    private boolean isRunning;

    private String request(int protocol, String method, String url, String body) throws IOException {
        String backMsg;

        // 链接客户端
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        switch (protocol){
            case 1: builder.protocols(Collections.singletonList(Protocol.HTTP_1_1)); break;
            case 2: builder.protocols(Collections.singletonList(Protocol.HTTP_2));  break;

            default: break;
        }
        builder.cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                cookieStore.put(httpUrl.host(), list);
            }

            @NotNull
            @Override
            public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                List<Cookie> cookies = cookieStore.get(httpUrl.host());
                return cookies != null ? cookies : new ArrayList<>();
            }
        });
        OkHttpClient client = builder.build();

        // 请求头
        Request.Builder header = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("x-requested-with", "XMLHttpRequest")
                .header("dnt", "1");


//        // 设置Cookies
//        if(dicc.containsKey(new Uri(url).getHost())){
//            StringBuilder str = new StringBuilder();
//            for (Map.Entry<String, String> kv : dicc.get(new Uri(url).getHost()).entrySet()){
//                str.append(kv.getKey()).append("=").append(kv.getValue()).append(";");
//            }
//            header.header("Cookie", str.toString());
//        }


        // POST 请求设置请求体
        if("POST".equals(method) && body !=null && !body.trim().isEmpty()){
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), body);
            header.method("POST", requestBody);
        }
        Request request = header.build();

        Response response = client.newCall(request).execute();

//        // 获取Cookies
//        if (response.isSuccessful()) {
//            List<String> headers = response.headers("Set-Cookie");
//            for (String str : headers) {
//                Map<String, String> sm = dicc.get(new Uri(url).getHost());
//                if(sm == null){
//                    dicc.put(new Uri(url).getHost(), new HashMap<>());
//                    sm = dicc.get(new Uri(url).getHost());
//                }
//                String key = str.split(";")[0].split("=")[0];
//                String value = str.split(";")[0].split("=")[1];
//                sm.put(key, value);
//            }
//        }
        String responseBody = response.body().string();


        return responseBody;
    }

    private CompletableFuture<String> requestAsync(int protocol, String method, String url, String body) {
        CompletableFuture.supplyAsync(()-> {
            try {
                return request(protocol, method, url, body);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return null;
    }


    public boolean loginAdmin() throws IOException {
        if (!isLoginAdmin) {
            String ret = request(1, "POST", "https://gzbf-admin.goodhmy.com/default/index/login", "userName=globaltradeez&userPass=gzbf_aaabbb123456");
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jo = objectMapper.readTree(ret);
                String message = jo.get("message").asText();
                return isLoginAdmin = "登录成功".equals(message);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return isLoginAdmin;
    }

    public boolean loginUser(String companyCode) throws IOException {
        if (companyCode != null && !companyCode.equals(currentClientID)) {
            // 获取登录链接
            String raw = request(1,"POST", "https://gzbf-admin.goodhmy.com/customer/distributor/get-login-sass-url/", "company_code=" + companyCode + "&site_code=all_platform");
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jo = objectMapper.readTree(raw);

            // 登录sass
            String url = jo.get("data").asText();
            request(1,"GET", url, null);

            // 登录用户
            String code = url.substring(url.lastIndexOf("=") + 1);
            request(1,"GET", "https://gzbf-shop.goodhmy.com/login.html?code=" + code + "&redirect_url=https://erp.globaltradeez.com", null);

            currentClientID = companyCode;

        }
        return true;
    }

    public User[] listUsers(String clientId) throws IOException {
        int page = 0, pageSize = 20, total = 0;
        if(clientId == null){
            clientId = "";
        }
        String raw = request(1,"POST", "https://gzbf-admin.goodhmy.com/customer/distributor/list", encodeUser(1, 2000, clientId));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jo = objectMapper.readTree(raw);
            String p = jo.get("data").get("page").asText();
            String ps = jo.get("data").get("page_size").asText();
            String to = jo.get("data").get("total").asText();

            page = Integer.parseInt(p);
            pageSize = Integer.parseInt(ps);
            total = Integer.parseInt(to);

            String rows = jo.get("data").get("rows").toString();
            return objectMapper.readValue(rows, User[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // or handle exception and return appropriate value
    }

    public Order[] listOrder(String... orders) throws IOException {
        List<Order> list = new ArrayList<>();
        String raw = request(1,"POST", "https://gzbf-shop.goodhmy.com/auth/order/manage", encodeOrder(1, orders));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jo = objectMapper.readTree(raw);
            OrderHeader orderHeader = objectMapper.convertValue(jo.get("data").get("counts"), OrderHeader.class);
            if (orderHeader.getT1().getCount() > 0) {
                String rows = jo.get("data").get("rows").toString();
                Order[] orderList = objectMapper.readValue(rows, Order[].class);
                for (Order item : orderList) {
                    item.setStatus(1);
                    list.add(item);
                }
            }else if(orderHeader.getT3().getCount() > 0){
                raw = request(1,"POST", "https://gzbf-shop.goodhmy.com/auth/order/manage", encodeOrder(3, orders));
                jo = objectMapper.readTree(raw);
                String rows = jo.get("data").get("rows").toString();
                Order[] orderList = objectMapper.readValue(rows, Order[].class);
                for (Order item : orderList) {
                    item.setStatus(3);
                    list.add(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Similar code for the other part of the method...

        return list.toArray(new Order[0]);
    }
    public DebitRecord[] listDebitRecord(String clientId, String starTime, String endTime) throws IOException {
        if(starTime == null) starTime ="";
        if (endTime == null) endTime = "";


        String str = "arn_status=&customer_code=&cu_name=&cu_type=&start_add_time=&end_add_time=&start_finish_time=" +
                starTime + "&end_finish_time=" + endTime + "&page=1&limit=2000&transaction_no=";

        String raw = request(1, "GET", "https://gzbf-admin.goodhmy.com/payment/present/list", str);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jo = objectMapper.readTree(raw);
            String rows = jo.get("data").toString();
            DebitRecord[] debitRecords = objectMapper.readValue(rows, DebitRecord[].class);
            return debitRecords;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // or handle exception and return appropriate value
    }
    public String deduction(Order order) throws IOException {
        String str = "payment_method=BANK&payment_date=" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm:ss")) +
                "&payment_amount=" + order.getCost() + "&beneficiary_name=Wal-Mart+(China)+Inv&beneficiary_bank=中国建设银行股份有限&beneficiary_sub_bank=Community&beneficiary_account_number=44050110069700001060&payment_currency=RMB";
        String raw = request(1, "POST", "https://gzbf-shop.goodhmy.com/fee/account-refund-note/save", str);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jo = objectMapper.readTree(raw);
            String message = jo.get("message").asText();
            return message;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // or handle exception and return appropriate value
    }

    public String deductionYes(int checkId) throws IOException {
        String raw = request(1,"POST", "https://gzbf-admin.goodhmy.com/payment/present/yes?status=1", "checkId=" + checkId);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jo = objectMapper.readTree(raw);
            String message = jo.get("message").asText();
            return message;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // or handle exception and return appropriate value
    }

    public String deductionPassed(int checkId) throws IOException {
        String raw = request(1,"POST", "https://gzbf-admin.goodhmy.com/payment/present/passed", "checkId=" + checkId);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jo = objectMapper.readTree(raw);
            String message = jo.get("message").asText();
            return message;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // or handle exception and return appropriate value
    }
    public String shipments(Order order) throws IOException {
        String raw = request(1,"POST", "https://gzbf-shop.goodhmy.com/auth/order/manual-shipped", "send_data[ids][]=" + order.getId());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jo = objectMapper.readTree(raw);
            String successNum = jo.get("data").get("success_num").asText();
            return successNum;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // or handle exception and return appropriate value
    }

    public String statement(int id, int step, String carrier, String trackingNumber, String carrierOld, String trackingNumberOld) throws IOException {
        String param = "";
        String raw = null;
        if (step == 1) {
            param = "data[orders][0][id]=" + id +
                    "&data[orders][0][platform_carrier_code]=" + carrier.toUpperCase() +
                    "&data[orders][0][send_date]=" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                    "&data[orders][0][shipping_url]=https://t.17track.net/en#nums=" + trackingNumber +
                    "&data[orders][0][tracking_number]=" + trackingNumber;
            raw = request(1,"POST", "https://gzbf-shop.goodhmy.com/auth/shipping/save-service", param);
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jo = objectMapper.readTree(raw);
                raw = jo.get("success_num").asText();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (step == 2) {
            param = "data[orders][0][id]=" + id +
                    "&data[orders][0][platform_carrier_code]=" + carrier.toUpperCase() +
                    "&data[orders][0][send_date]=" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                    "&data[orders][0][shipping_url]=https://t.17track.net/en#nums=" + trackingNumber +
                    "&data[orders][0][tracking_number]=" + trackingNumber +
                    "&data[orders][0][is_all]=0" +
                    "&data[orders][0][platform_carrier_code_old]=" + carrierOld.toLowerCase() +
                    "&data[orders][0][tracking_number_old]=" + trackingNumberOld;
            raw = request(1,"POST", "https://gzbf-shop.goodhmy.com/auth/shipping/update-service", param);
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jo = objectMapper.readTree(raw);
                raw = jo.get("success_num").asText();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return raw;
    }

    public MarkOrder[] listStatements(String... orderIds) throws IOException {
        String raw = request(1,"POST", "https://gzbf-shop.goodhmy.com/auth/shipping/index", encodeMarkOrder(orderIds));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jo = objectMapper.readTree(raw);
            String rows = jo.get("data").get("rows").toString();
            return objectMapper.readValue(rows, MarkOrder[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // or handle exception and return appropriate value
    }


    public Recharge[] listAllRecharge() throws IOException {
        String str = "cu_type=&cu_id=&reference_no=&pm_code=&pn_fee_type=&pn_status=&customer_code=&cu_code=&cu_type=&pn_add_time=&pn_verify_time=&pending=&page=1&limit=2000";
        String raw = request(1,"POST", "https://gzbf-admin.goodhmy.com/payment/payment/list", str);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jo = objectMapper.readTree(raw);
            String rows = jo.get("data").get("rows").toString();
            return objectMapper.readValue(rows, Recharge[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Recharge[0];
    }

    public boolean DeductShipDeclare(ShipObject shipObject) {
        StringBuilder msg = new StringBuilder();
        this.isRunning = true;
        ShipObject so = shipObject;
        String clientId = so.getClientID();
        String orderId = so.getOrderID();
        String ret = null;
        boolean result = false;
        try {
            // 登录用户
            loginUser(clientId);
            System.out.print(msg.append("登录用户 ").append(clientId + ""));

            // 搜索订单号
            Order[] orders = listOrder(orderId);
            assert orders.length == 1 : orderId + "不存在";

            Order order = orders[0];
            so.setId(order.getId());

            if (so.getStep() == ShipTypes.Deduct || so.getStep() == ShipTypes.DeductAndShip || so.getStep() == ShipTypes.DeductAndShipAndDeclare) {
                // 申请扣款
                double cost = order.getCost();

                ret = deduction(order);
                System.out.print("\r" + msg.append(" "+orderId+" ").append("扣款 "+cost));
                assert "Success.".equals(ret) : "扣款失败";
                assert cost > 0 : "扣款金额为0";

                // 搜索订单
                DebitRecord[] debitRecords = listDebitRecord(clientId, null, null);
                DebitRecord debitRecord = debitRecords[0];
                System.out.print("\r" + msg.append(" 扣款金额 " + debitRecord.getCost() + " " + debitRecord.getTradeId()));
                assert (int)debitRecord.getCost() == (int)cost : "扣款金额" + debitRecord.getCost() + "成本" + cost + "不相同";    // 金额不相同

                so.setTradeID(debitRecord.getTradeId());
                so.setDeductionAmount(debitRecord.getCost() + "RMB");
                // 交易号重复则放弃订单
                int num = (int) Arrays.stream(debitRecords).filter(x -> x.getTradeId().equals(debitRecord.getTradeId())).count();
                System.out.print("\r" + msg.append(" 订单数量 "+num));
                assert num == 1 : "搜索不到订单或多个订单";    // 不是一个订单

                // 同意扣款
                ret = deductionYes(debitRecord.getRecordId());
                System.out.print("\r" + msg.append(" 审核成功"));
                assert "审核成功".equals(ret): "审核失败";

                // 同意出纳
                ret = deductionPassed(debitRecord.getRecordId());
                System.out.print("\r" + msg.append(" 出纳成功"));
                assert "success".equals(ret) : "出纳失败";

            }
            if (so.getStep() == ShipTypes.Ship || so.getStep() == ShipTypes.DeductAndShip || so.getStep() == ShipTypes.ShipAndDeclare || so.getStep() == ShipTypes.DeductAndShipAndDeclare) {
                // 订单转已发货
                ret = shipments(order);
                System.out.print("\r" + msg.append(" 转已发货"));
                so.setDeduction(true);
                so.setShipped(true);
            }
            if (so.getStep() == ShipTypes.Declare || so.getStep() == ShipTypes.ShipAndDeclare || so.getStep() == ShipTypes.DeductAndShipAndDeclare) {
                // 获取标发订单
                MarkOrder markOrder = listStatements(so.getOrderID())[0];

                // 标发
                if (markOrder != null && order.getOrderId().equals(markOrder.getOrderId())) {
                    String mark = statement(order.getId(), 1, so.getCarrier(), so.getTrackingNumber(), markOrder.getCarrierOld(), markOrder.getTrackingNumberOld());
                    System.out.print("\r" + msg.append(" 标发"));
                    assert "1".equals(mark) : "标发失败";
                    System.out.print("\r" + msg.append("成功"));
                }

                MarkOrder markOrder1 = listStatements(so.getOrderID())[0];
                so.setTrackingNumberOld(markOrder1.getTrackingNumberOld());
                so.setCarrierOld(markOrder1.getCarrierOld());
            }
            result = true;
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.print("\r" + msg.append("失败"));
        } finally {
            System.out.println();
            String dir = Paths.get(System.getProperty("user.dir"), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))).toString();
            File directory = new File(dir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String filename = Paths.get(dir, orderId + ".txt").toString();
            try {
                FileWriter writer = new FileWriter(filename);
                writer.write(msg.toString());
                writer.close();
            } catch (IOException e) {
                result = false;
                // e.printStackTrace();
            }
        }
        this.isRunning = false;
        return result;
    }







    private String encodeMarkOrder(String... orders) {
        String raw = String.join(" ", orders);
        String para = "data[0][name]=platform_id&data[0][value]=&data[1][name]=account_id&data[1][value]=&" +
                "data[2][name]=sync_status&data[2][value]=&data[3][name]=buyer_message&data[3][value]=&" +
                "data[4][name]=has_tracking_number&data[4][value]=&data[5][name]=is_split&data[5][value]=&" +
                "data[6][name]=is_update_mark&data[6][value]=&data[7][name]=status&data[7][value]=&" +
                "data[8][name]=search_type&data[8][value]=1&data[9][name]=search_val&data[9][value]=" + raw + "&" +
                "data[10][name]=page&data[10][value]=1&data[11][name]=page_size&data[11][value]=20&" +
                "data[12][name]=paid_time_start&data[12][value]=&data[13][name]=paid_time_end&data[13][value]=&" +
                "data[14][name]=over_time_left_start&data[14][value]=&data[15][name]=over_time_left_end&data[15][value]=&" +
                "data[16][name]=is_sub&data[16][value]=0&data[17][name]=sub_map_ids";
        return para;
    }

    private String encodeOrder(int status, String... orders) {
        String raw = String.join(" ", orders);
        String para = "data[0][name]=search_type&data[0][value]=1&data[1][name]=country&data[1][value]=&" +
                "data[2][name]=search_val&data[2][value]=&data[3][name]=order_code&data[3][value]=" + raw + "&" +
                "data[4][name]=time_type&data[4][value]=1&data[5][name]=time_start&data[5][value]=&" +
                "data[6][name]=time_end&data[6][value]=&data[7][name]=platform_id&data[7][value]=&" +
                "data[8][name]=platform_account_id&data[8][value]=&data[9][name]=type&data[9][value]=&" +
                "data[10][name]=ot_id&data[10][value]=&data[11][name]=quantity_type&data[11][value]=&" +
                "data[12][name]=can_combine&data[12][value]=&data[13][name]=buyer_message&data[13][value]=&" +
                "data[14][name]=has_tracking_number&data[14][value]=&data[15][name]=is_cancel&data[15][value]=&" +
                "data[16][name]=purchase_order_status&data[16][value]=&data[17][name]=is_ebay_message&data[17][value]=&" +
                "data[18][name]=exception_code&data[18][value]=&data[19][name]=platform_site_id&data[19][value]=&" +
                "data[20][name]=page&data[20][value]=1&data[21][name]=page_size&data[21][value]=2000&" +
                "data[22][name]=status&data[22][value]=" + status + "&data[23][name]=tag_id&data[23][value]=&" +
                "data[24][name]=is_submit&data[24][value]=&data[25][name]=return_1688_order_status&data[25][value]=&" +
                "data[26][name]=is_sub&data[26][value]=0&data[27][name]=sub_map_ids";
        return para;
    }

    private String encodeUser(int page, int pagesize, String clientId) {
        String para = "search_type=company_code_arr&search_val=" + clientId + "&cu_type=1&recommended_code=&referrer_code=&company_code=&role_type=&sales_agent_id=&business_person_id=&account_manager_id=&email=&phone=&company_name=&client_grade_id=&company_status=&createStartTime=&createEndTime=&verifyStartTime=&verifyEndTime=&loginStartTime=&loginEndTime=&cb_value_start=&cb_value_end=&erp_balance_value_from=&erp_balance_value_to=&content_desc_like=&page=" + page + "&pageSize=" + pagesize;
        //return WebUtility.UrlEncode(para);
        return para;
    }

}