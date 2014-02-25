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
import org.wso2.carbon.dataservices.samples.csv_sample_service.CSVSampleService;
import org.wso2.carbon.dataservices.samples.csv_sample_service.CSVSampleServiceStub;
import org.wso2.carbon.dataservices.samples.csv_sample_service.DataServiceFault;
import org.wso2.carbon.dss.DSSIntegrationTest;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class CSVSampleTestCase extends DSSIntegrationTest {
    private static String serviceName = "CSVSampleService";
    private static String serverEpr;

    private static final Log log = LogFactory.getLog(CSVSampleTestCase.class);

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
                                              "dbs" + File.separator + "csv" + File.separator +
                                              "CSVSampleService.dbs")));
        log.info(serviceName + " uploaded");
    }

    @Test(groups = "wso2.dss", description = "Check whether fault service deployed or not")
    public void testServiceDeployment() throws RemoteException {
        assertTrue(isServiceDeployed(serviceName));
        log.info(serviceName + " is deployed");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = "testServiceDeployment")
    public void testGetProducts() throws DataServiceFault, RemoteException {
        log.info("Running CSVSampleServiceTestCase#testGetProducts");
        CSVSampleService stub = new CSVSampleServiceStub(serverEpr);
        assert stub.getProducts().length > 0 : "No of products should be greater than zero";
    }

    @AfterClass(alwaysRun = true, groups = "wso2.dss", description = "delete service")
    public void deleteFaultyService() throws RemoteException {
        deleteService(serviceName);
        cleanup();
    }

}
