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

package org.wso2.carbon.dss.samples.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.dataservices.samples.gspread_sample_service.DataServiceFault;
import org.wso2.carbon.dataservices.samples.gspread_sample_service.GSpreadSample;
import org.wso2.carbon.dataservices.samples.gspread_sample_service.GSpreadSampleStub;
import org.wso2.carbon.dss.DSSIntegrationTest;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class GSpreadSampleTestCase extends DSSIntegrationTest {

    private static final Log log = LogFactory.getLog(GSpreadSampleTestCase.class);

    private static String serviceName = "GSpreadSample";
    private static String serverEpr;


    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init();
        String resourceFileLocation = null;
        serverEpr = dssServer.getBackEndUrl() + serviceName;
        resourceFileLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                               "DSS";
        deployService(serviceName,
                      new DataHandler(new URL("file:///" + resourceFileLocation +
                                              File.separator + "samples" + File.separator +
                                              "dbs" + File.separator + "gspread" + File.separator +
                                              "GSpreadSample.dbs")));
        log.info(serviceName + " uploaded");
    }

    @Test(groups = "wso2.dss", description = "Check whether fault service deployed or not")
    public void testServiceDeployment() throws RemoteException {
        assertTrue(isServiceDeployed(serviceName));
        log.info(serviceName + " is deployed");
    }


    @Test(groups = {"wso2.dss"}, dependsOnMethods = "testServiceDeployment", description = "invoke GSspread sheet test")
    public void testGSpreadQuery() throws DataServiceFault, RemoteException {

        if (this.isOnlineTestsEnabled()) {
            log.info("Running GSpreadSampleTestCase#testGSpreadQuery");
            GSpreadSample stub = new GSpreadSampleStub(serverEpr);
            assert stub.getCustomers().length > 0 : "No of customers should be greater than zero";

        }
    }

    @AfterClass(alwaysRun = true, groups = "wso2.dss", description = "delete service")
    public void deleteFaultyService() throws RemoteException {
        deleteService(serviceName);
        cleanup();
    }

    private boolean isOnlineTestsEnabled() {
        String gspreadProp = System.getProperty("online.tests");
        if (gspreadProp != null) {
            return Boolean.parseBoolean(gspreadProp);
        } else {
            return false;
        }
    }
}
