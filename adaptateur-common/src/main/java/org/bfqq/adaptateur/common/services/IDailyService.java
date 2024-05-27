package org.bfqq.adaptateur.common.services;

import org.apache.commons.lang3.tuple.Pair;
import org.bfqq.adaptateur.common.models.daily.DailyDetail;
import org.bfqq.adaptateur.common.models.manage.Recharge;
import org.bfqq.adaptateur.common.models.manage.User;
import org.bfqq.adaptateur.common.models.sheet.Shop;

import java.time.LocalDate;
import java.util.Collection;

public interface IDailyService {
    public void BuildCompanyData(Collection<Shop> shops, LocalDate date, Collection<User> users, Collection<Recharge> recharges, Collection<DailyDetail> dailyDetails, String filename);
    public void BuildOperData(Collection<Shop> shops, LocalDate date, Collection<User> users, Collection<Recharge> recharges, Collection<DailyDetail> dailyDetails, String filename);

    public Pair<Integer, Integer> listMissingStores(Collection<Shop> shops, LocalDate date, Collection<DailyDetail> dailyDetails);
    public void buildOrders(Collection<Shop> shops,  LocalDate date, Collection<DailyDetail> dailyDetails, String filename);
}
