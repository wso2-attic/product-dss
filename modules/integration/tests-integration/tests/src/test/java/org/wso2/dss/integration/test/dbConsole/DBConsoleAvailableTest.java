/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.dss.integration.test.dbConsole;

import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.dss.integration.test.DSSIntegrationTest;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.dss.integration.test.odata.ODataTestUtils.OK;
import static org.wso2.dss.integration.test.odata.ODataTestUtils.sendGET;

public class DBConsoleAvailableTest extends DSSIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
    }

    @Test(groups = "wso2.dss", description = "dbConsole available test case")
    public void dbConsoleAvailableTest() throws XPathExpressionException, IOException {
        String webAppUrl = dssContext.getContextUrls().getWebAppURL();
        String url = webAppUrl + "/" + "dbconsole/login.jsp?region=region5&item=dbconsole";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        Object[] response = sendGET(url, headers);
        Assert.assertEquals(OK, response[0]);
        Assert.assertTrue(response[1].toString().contains("Welcome to H2"));
    }
}


