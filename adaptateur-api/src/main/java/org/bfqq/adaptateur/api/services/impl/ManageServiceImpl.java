package org.bfqq.adaptateur.api.services.impl;

import org.bfqq.adaptateur.api.services.IManageService;
import org.bfqq.adaptateur.common.clients.ManageClient;
import org.bfqq.adaptateur.common.models.manage.ShipObject;
import org.bfqq.adaptateur.common.models.manage.ShipTypes;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ManageServiceImpl implements IManageService {

    private static Map<String, ManageClient> concurrentMap = new ConcurrentHashMap<>();

    @Override
    public boolean Deduction(ShipObject shipObject) throws IOException {

        if(!concurrentMap.containsKey(shipObject.getClientID())){
            concurrentMap.put(shipObject.getClientID(), new ManageClient());
        }
        ManageClient manageClient = concurrentMap.get(shipObject.getClientID());
        synchronized (manageClient){
            manageClient.loginAdmin();
            return manageClient.DeductShipDeclare(shipObject);
        }

    }
}
