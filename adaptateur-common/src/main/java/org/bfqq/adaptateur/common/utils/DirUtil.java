package org.bfqq.adaptateur.common.utils;

import org.bfqq.adaptateur.common.models.DirTypes;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public  class DirUtil {
    private static final String BASE_DIR = "D:\\我的坚果云\\数据采集";
    public static Collection< File> getNewFile(DirTypes dirTypes){
        List<File> list = new ArrayList<>();
        switch (dirTypes){
            case SHOP_FILENAME:{
                list.add( new File(Paths.get(BASE_DIR, "店铺信息.xlsx").toString()) );
            };break;
            case DAILY:{
                File[] dailyList = new File(Paths.get(BASE_DIR, "每日数据").toString()).listFiles();
                File[] dailyList1 = new File(Paths.get(dailyList[dailyList.length - 1].getAbsolutePath()).toString()).listFiles(((dir, name) -> name.endsWith("xlsx") && !name.startsWith("~")));
                list.addAll(Arrays.asList(dailyList1));
            };break;
            case SUBMIT:{
                File[] submitList = new File(Paths.get(BASE_DIR, "总表数据").toString()).listFiles();
                File[] submitList1 = new File(String.valueOf(Paths.get(submitList[submitList.length - 1].getAbsolutePath()))).listFiles((dir, name) -> name.startsWith("巴西采购单") && !name.startsWith("~"));
                list.addAll(Arrays.asList(submitList1));
            };break;
            default: break;

        }
        return list;
    }
}
