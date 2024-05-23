package org.bfqq.adaptateur.common.models.sheet;

import lombok.Data;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Data
public class ShopFileInfo {


    private String companyNumber;
    private String companyName;
    private String nick;
    private String CN;
    private String fileName;
    private DateRange collectRange;

    public static ShopFileInfo convert(String filename) {
        ShopFileInfo shop = new ShopFileInfo();
        shop.setFileName(filename);
        String name = new File(filename).getName().substring(0, new File(filename).getName().lastIndexOf('.')) ;
        String fixName = name;

        if (name.contains("_")) {
            String[] splits = name.split("_");
            fixName = splits[0];
            shop.setCN(Arrays.stream(splits).filter(x -> x.startsWith("cn")).findFirst().orElse(null));

            List<String> list = Arrays.stream(splits).collect(Collectors.toList());
            list.remove(fixName);
            list.remove(shop.getCN());
            splits = list.toArray(new String[0]);

            if (splits.length > 0) {
                String[] formats = { "yyyy", "yyyyMM", "yyyyMMdd", "yyyyMMddHH", "yyyyMMddHHmm", "yyyyMMddHHmmss" };
                DateRange dateRange = new DateRange();

                try {
                    Calendar calendar = Calendar.getInstance(); //创建Calendar 的实例
                    Date date = null;

                    for (String format : formats) {
                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                            date = dateFormat.parse(splits[0]);
                            break;
                        } catch (ParseException e) {
                            // Ignore and try next format
                        }
                    }
                    calendar.setTime(date);

                    // 设置日期为本月的第一天
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    Date firstDayOfMonth = calendar.getTime();
                    dateRange.setStart(firstDayOfMonth);
                    // 获取本月最后一天
                    calendar.add(Calendar.MONTH, 1); // 增加一个月
                    calendar.add(Calendar.DAY_OF_MONTH, -1); // 减去一天
                    Date lastDayOfMonth = calendar.getTime();
                    dateRange.setEnd(lastDayOfMonth);
                } catch (DateTimeParseException e) {
                    // Handle parsing exception
                }
                if (splits.length >1) {
                    try {
                        Calendar calendar = Calendar.getInstance(); //创建Calendar 的实例
                        Date date = null;

                        for (String format : formats) {
                            try {
                                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                                date = dateFormat.parse(splits[1]);
                                break;
                            } catch (ParseException e) {
                                // Ignore and try next format
                            }
                        }
                        calendar.setTime(date);

                        // 设置日期为本月的第一天
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        Date firstDayOfMonth = calendar.getTime();
                        // 获取本月最后一天
                        calendar.add(Calendar.MONTH, 1); // 增加一个月
                        calendar.add(Calendar.DAY_OF_MONTH, -1); // 减去一天
                        Date lastDayOfMonth = calendar.getTime();
                        dateRange.setEnd(lastDayOfMonth);
                    } catch (DateTimeParseException e) {
                        // Handle parsing exception
                    }
                }

                shop.setCollectRange(dateRange);
            } else {
                shop.setCollectRange(null);
            }
        }

        shop.setCompanyNumber(fixName.substring(0, 4));
        shop.setCompanyName(fixName.substring(4, fixName.length() - 6));
        shop.setNick(fixName.substring(fixName.length() - 2));

        return shop;
    }
}
