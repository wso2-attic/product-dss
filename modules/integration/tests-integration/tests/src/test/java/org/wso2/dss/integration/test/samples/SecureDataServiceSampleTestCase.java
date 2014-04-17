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
package org.wso2.dss.integration.test.samples;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.axis2client.SecureAxisServiceClient;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.dataservices.samples.secure_dataservice.DataServiceFault;
import org.wso2.carbon.dataservices.samples.secure_dataservice.SecureDataServiceStub;
import org.wso2.carbon.integration.common.admin.client.SecurityAdminServiceClient;
import org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException;
import org.wso2.dss.integration.common.utils.DSSTestCaseUtils;
import org.wso2.dss.integration.test.DSSIntegrationTest;
import org.wso2.ws.dataservice.samples.secure_dataservice.Office;

import javax.activation.DataHandler;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class SecureDataServiceSampleTestCase extends DSSIntegrationTest {
    private static final Log log = LogFactory.getLog(SecureDataServiceSampleTestCase.class);

    private final String serviceName = "SecureDataService";

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {

        super.init();
        deployService(serviceName,
                      new DataHandler(new URL("file:///" + getResourceLocation() + File.separator + "samples"
                                              + File.separator + "dbs" + File.separator
                                              + "rdbms" + File.separator + "SecureDataService.dbs")));

    }

    @Test(groups = "wso2.dss", description = "check whether the service is deployed")
    public void testServiceDeployment() throws RemoteException, XPathExpressionException {
        DSSTestCaseUtils dssTestCaseUtils = new DSSTestCaseUtils();
        assertTrue(dssTestCaseUtils.isServiceDeployed(dssContext.getContextUrls().getBackEndUrl(), sessionCookie,
                                                      serviceName));
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        deleteService(serviceName);
        cleanup();
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = "testServiceDeployment")
    public void listOffices() throws DataServiceFault, RemoteException, XPathExpressionException {
        SecureDataServiceStub stub = new SecureDataServiceStub(dssContext.getContextUrls().getBackEndUrl()+serviceName);
        for (int i = 0; i < 5; i++) {
            Office[] offices = stub.showAllOffices();
            Assert.assertNotNull(offices, "Office List null");
            Assert.assertTrue(offices.length > 0, "Office list has no office");
        }
        log.info("Select Operation Success");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = {"listOffices"},
          description = "Service invocation after security engaged." +
                        "Provides Authentication. Clients have Username Tokens")
    public void securedListOffices() throws Exception {
        final int policyId = 1;

        secureService(policyId);
        SecureAxisServiceClient secureAxisServiceClient = new SecureAxisServiceClient();
        for (int i = 0; i < 5; i++) {
            OMElement response = secureAxisServiceClient.sendReceive(userInfo.getUserName(), userInfo.getPassword(),
                                                                     getServiceUrlHttps(serviceName), "showAllOffices",
                                                                     getPayload(), policyId);

            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("Select Operation Success");
    }

    private void secureService(int policyId)
            throws SecurityAdminServiceSecurityConfigExceptionException, RemoteException,
                   XPathExpressionException {
        SecurityAdminServiceClient securityAdminServiceClient = new SecurityAdminServiceClient(dssContext.getContextUrls().getBackEndUrl(), sessionCookie);
        if (TestConfigurationProvider.isPlatform()) {

            /*securityAdminServiceClient.applySecurity(serviceName, policyId + "", new String[]{userInfo.getUserName()},
                                                     new String[]{userInfo.getDomain().replace('.', '-') + ".jks"},
                                                     userInfo.getDomain().replace('.', '-') + ".jks");*/
        } else {
            securityAdminServiceClient.applySecurity(serviceName, Integer.toString(policyId) ,new String[] {"admin"},
                                                     new String[]{"wso2carbon.jks"}, "wso2carbon.jks");
        }
        log.info("Security Scenario " + policyId + " Applied");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Assert.fail("InterruptedException :" + e);

        }
    }

    private OMElement getPayload() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/secure_dataservice", "ns1");
        return fac.createOMElement("showAllOffices", omNs);
    }
}
