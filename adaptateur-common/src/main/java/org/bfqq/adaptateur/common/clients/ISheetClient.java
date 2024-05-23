package org.bfqq.adaptateur.common.clients;

import org.bfqq.adaptateur.common.models.daily.DailyDetail;
import org.bfqq.adaptateur.common.models.sheet.PurchaseOrder;
import org.bfqq.adaptateur.common.models.sheet.Shop;

import java.util.List;

public interface ISheetClient {
    public List<PurchaseOrder> readPurchaseOrder(int type, String fileName);

    public String[] readSortFiles(String fileName);

    public List<Shop> readShopCatalog(String fileName);
    public DailyDetail readDaily(String filename);
}
