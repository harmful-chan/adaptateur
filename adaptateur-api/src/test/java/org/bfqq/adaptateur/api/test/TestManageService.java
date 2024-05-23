package org.bfqq.adaptateur.api.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bfqq.adaptateur.common.models.manage.ShipObject;
import org.bfqq.adaptateur.common.models.manage.ShipTypes;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class TestManageService {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @CsvFileSource(resources = "/testdata.csv", numLinesToSkip = 0)
    public void testGetHello(String clientId, String orderId, String  tracking, String carrier) throws Exception {

        ShipObject so = new ShipObject();
        so.setClientID(clientId);
        so.setOrderID(orderId);
        so.setTrackingNumber(tracking);
        so.setCarrier(carrier);
        so.setStep(ShipTypes.Deduct);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/deduction")
                        .content(new ObjectMapper().writeValueAsString(so))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        // 对接口返回的内容进行断言或其他操作
    }



}
