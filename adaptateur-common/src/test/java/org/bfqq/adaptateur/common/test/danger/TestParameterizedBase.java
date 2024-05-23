package org.bfqq.adaptateur.common.test.danger;

import org.bfqq.adaptateur.common.clients.ManageClient;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class TestParameterizedBase {

    protected static ManageClient manageClient = new ManageClient();
    protected String ret;
    protected String clientId;
    protected String orderId;
    protected String tracking;
    protected String carrier;

    public TestParameterizedBase(String clientId, String orderId, String tracking, String carrier, String ret) {
        this.clientId = clientId;
        this.orderId = orderId;
        this.tracking = tracking;
        this.carrier  = carrier;
        this.ret = ret;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        List<Object[]> parameters = new ArrayList<>();
        String csvFile = "src/main/resources/test/test-capitalize.csv";
        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";

        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy);
                String i1 = data.length > 0 ? data[0]: null;
                String i2 = data.length > 1 ? data[1]: null;
                String i3 = data.length > 2 ? data[2]: null;
                String i4 = data.length > 3 ? data[3]: null;
                String i5 = data.length > 4 ? data[4]: null;
                parameters.add(new Object[]{i1, i2, i3, i4, i5});
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return parameters;
    }
}
