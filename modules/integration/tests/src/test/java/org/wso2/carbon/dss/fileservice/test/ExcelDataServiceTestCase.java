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
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.automation.utils.concurrency.ConcurrencyTest;
import org.wso2.carbon.automation.utils.concurrency.exception.ConcurrencyTestFailedError;
import org.wso2.carbon.automation.utils.dss.DSSTestCaseUtils;
import org.wso2.carbon.dss.DSSIntegrationTest;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

import javax.activation.DataHandler;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

public class ExcelDataServiceTestCase extends DSSIntegrationTest {
    private static final Log log = LogFactory.getLog(ExcelDataServiceTestCase.class);
    private final String serviceName = "ExcelDataService";

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {

        super.init();
        addResource();
        deployService(serviceName,
                      AXIOMUtil.stringToOM(FileManager.readFile(ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                                                                + "artifacts" + File.separator + "DSS"
                                                                + File.separator + "dbs" + File.separator
                                                                + "excel" + File.separator
                                                                + "ExcelDataService.dbs")));

    }

    @Test(groups = "wso2.dss", description = "Check whether fault service deployed or not")
    public void testServiceDeployment() throws RemoteException {
        DSSTestCaseUtils dssTestCaseUtils = new DSSTestCaseUtils();
        assertTrue(dssTestCaseUtils.isServiceDeployed(dssServer.getBackEndUrl(),
                                                      dssServer.getSessionCookie(), serviceName));
        log.info(serviceName + " is deployed");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        deleteService(serviceName);
        deleteResource();
        cleanup();
    }

    @Test(groups = {"wso2.dss"}, invocationCount = 5, dependsOnMethods = "testServiceDeployment")
    public void selectOperation() throws AxisFault {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice", "ns1");
        OMElement payload = fac.createOMElement("getProducts", omNs);
        OMElement result = new AxisServiceClient().sendReceive(payload, getServiceUrl(serviceName), "getProducts");
        log.info("Response :" + result);
        Assert.assertTrue((result.toString().indexOf("Products") == 1), "Expected Result Not found");
        Assert.assertTrue(result.toString().contains("<Product>"), "Expected Result Not found");
        Assert.assertTrue(result.toString().contains("<ID>"), "Expected Result Not found");
        Assert.assertTrue(result.toString().contains("<Name>"), "Expected Result Not found");

        log.info("Service invocation success");
    }

    @Test(groups = {"wso2.dss"}, invocationCount = 5, dependsOnMethods = "testServiceDeployment")
    public void xsltTransformation() throws AxisFault {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice", "ns1");
        OMElement payload = fac.createOMElement("getProductClassifications", omNs);
        OMElement result = new AxisServiceClient().sendReceive(payload, getServiceUrl(serviceName), "getProductClassifications");
        if (log.isDebugEnabled()) {
            log.debug("Response :" + result);
        }
        Assert.assertTrue((result.toString().indexOf("Products") == 1), "Expected Result Not found");
        Assert.assertTrue(result.toString().contains("<Product>"), "Expected Result Not found");
        Assert.assertTrue(result.toString().contains("<Product-Name>"), "Expected Result Not found");
        Assert.assertTrue(result.toString().contains("<Product-Classification>"), "Expected Result Not found");

        log.info("XSLT Transformation Success");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = {"xsltTransformation"}, timeOut = 1000 * 60 * 1)
    public void concurrencyTest() throws ConcurrencyTestFailedError, InterruptedException {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice", "ns1");
        OMElement payload = fac.createOMElement("getProducts", omNs);
        ConcurrencyTest concurrencyTest = new ConcurrencyTest(5, 5);
        concurrencyTest.run(getServiceUrl(serviceName), payload, "getProducts");
    }

    private void addResource()
            throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException {
        ResourceAdminServiceClient resourceAdmin = new ResourceAdminServiceClient(dssServer.getBackEndUrl()
                , dssServer.getSessionCookie());

        resourceAdmin.deleteResource("/_system/config/automation/resources/");

        resourceAdmin.addResource("/_system/config/automation/resources/Products.xls",
                                  "application/vnd.ms-excel", "",
                                  new DataHandler(new URL("file:///" + ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                                                          + "artifacts" + File.separator + "DSS"
                                                          + File.separator + "resources"
                                                          + File.separator + "Products.xls")));

        resourceAdmin.addResource("/_system/config/automation/resources/transform.xslt",
                                  "application/xml", "",
                                  new DataHandler(new URL("file:///" + ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                                                          + "artifacts" + File.separator + "DSS"
                                                          + File.separator + "resources"
                                                          + File.separator + "transform.xslt")));
    }

    private void deleteResource()
            throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException {
        ResourceAdminServiceClient resourceAdmin = new ResourceAdminServiceClient(dssServer.getBackEndUrl()
                , dssServer.getSessionCookie());
        resourceAdmin.deleteResource("/_system/config/automation/resources/");
    }

}
