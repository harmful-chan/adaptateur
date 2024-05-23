package org.bfqq.adaptateur.api.controller;

import org.apache.commons.lang3.StringUtils;
import org.bfqq.adaptateur.api.services.IManageService;
import org.bfqq.adaptateur.common.models.manage.ShipObject;
import org.bfqq.adaptateur.common.models.manage.ShipTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class HomeController {

    @Autowired
    IManageService manageService;

    @PostMapping("/deduction")
    public void deduction(@RequestBody ShipObject shipObject) throws IOException {
        if(manageService != null && !StringUtils.isBlank(shipObject.getClientID()) ){
            manageService.Deduction(shipObject);
        }
    }
}
