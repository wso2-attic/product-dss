/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.dss.fileservice.test;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.automation.utils.concurrency.ConcurrencyTest;
import org.wso2.carbon.automation.utils.concurrency.exception.ConcurrencyTestFailedError;
import org.wso2.carbon.dss.DSSIntegrationTest;

import java.io.File;

public class AGSpreadDataServiceTestCase extends DSSIntegrationTest {

    private static final Log log = LogFactory.getLog(AGSpreadDataServiceTestCase.class);
    private final String serviceName = "GSpreadDataService";

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {

        super.init();
        deployService(serviceName,
                      AXIOMUtil.stringToOM(FileManager.readFile(ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                                           + "artifacts" + File.separator + "DSS" + File.separator
                                           + "dbs" + File.separator + "gspread" + File.separator
                                           + "GSpreadDataService.dbs")));

    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        deleteService(serviceName);
        cleanup();
    }

    @Test(groups = {"wso2.dss"}, invocationCount = 5)
    public void selectOperation() throws AxisFault {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/gspread_sample_service", "ns1");
        OMElement payload = fac.createOMElement("getCustomers", omNs);

        OMElement result = new AxisServiceClient().sendReceive(payload, getServiceUrl(serviceName), "getcustomers");
        log.info("Response : " + result);
        Assert.assertTrue((result.toString().indexOf("Customers") == 1), "Expected Result not found on response message");
        Assert.assertTrue(result.toString().contains("<customerNumber>"), "Expected Result not found on response message");
        Assert.assertTrue(result.toString().contains("</Customer>"), "Expected Result not found on response message");


        log.info("Service Invocation success");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = {"selectOperation"}, timeOut = 1000 * 60 * 2)
    public void concurrencyTest() throws ConcurrencyTestFailedError, InterruptedException {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/gspread_sample_service", "ns1");
        OMElement payload = fac.createOMElement("getCustomers", omNs);
        ConcurrencyTest concurrencyTest = new ConcurrencyTest(2, 2);
        concurrencyTest.run(getServiceUrl(serviceName), payload, "getcustomers");
    }

}
