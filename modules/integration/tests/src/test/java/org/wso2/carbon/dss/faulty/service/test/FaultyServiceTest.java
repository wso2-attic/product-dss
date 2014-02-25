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

package org.wso2.carbon.dss.faulty.service.test;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.dataservices.DataServiceFileUploaderClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.dss.DSSIntegrationTest;
import org.wso2.carbon.service.mgt.stub.ServiceAdminException;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;


public class FaultyServiceTest extends DSSIntegrationTest {

    private static final Log log = LogFactory.getLog(FaultyServiceTest.class);

    private static String serviceName = "FaultyDataService";

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {

        super.init();
        String resourceFileLocation;
        resourceFileLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                               "DSS";
        DataServiceFileUploaderClient dataServiceAdminClient =
                new DataServiceFileUploaderClient(dssServer.getBackEndUrl(),dssServer.getSessionCookie());
        dataServiceAdminClient.uploadDataServiceFile("FaultyDataService.dbs",
                                                     new DataHandler(new URL("file:///" + resourceFileLocation +
                                                                             File.separator + "dbs" + File.separator +
                                                                             "rdbms" + File.separator + "MySql" + File.separator +
                                                                             "FaultyDataService.dbs")));
        log.info(serviceName + " uploaded");
    }


    @Test(groups = "wso2.dss", description = "Check whether fault service deployed or not")
    public void isServiceFaulty() throws RemoteException {
        assertTrue(isServiceFaulty(serviceName));
        log.info(serviceName + " is faulty");
    }

    @AfterClass(alwaysRun = true)
    public void deleteService() throws ServiceAdminException, RemoteException,
                                       InterruptedException {
        deleteService(serviceName);
        log.info(serviceName + " deleted");
    }
}