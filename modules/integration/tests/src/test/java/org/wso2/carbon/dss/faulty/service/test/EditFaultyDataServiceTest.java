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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.dataservices.DataServiceAdminClient;
import org.wso2.carbon.automation.api.clients.dataservices.DataServiceFileUploaderClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.dssutils.SqlDataSourceUtil;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.dss.DSSIntegrationTest;
import org.wso2.carbon.service.mgt.stub.ServiceAdminException;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

import static org.testng.Assert.assertTrue;


public class EditFaultyDataServiceTest extends DSSIntegrationTest {

    private static final Log log = LogFactory.getLog(EditFaultyDataServiceTest.class);

    private String serviceName = "FaultyDataService";
    private String resourceFileLocation;

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {

        super.init();
        resourceFileLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                               "DSS";
        DataServiceFileUploaderClient dataServiceAdminClient =
                new DataServiceFileUploaderClient(dssServer.getBackEndUrl(), dssServer.getSessionCookie());
        dataServiceAdminClient.uploadDataServiceFile("FaultyDataService.dbs",
                                                     new DataHandler(new URL("file:///" + resourceFileLocation + File.separator + "dbs" +
                                                                             File.separator + "rdbms" + File.separator +
                                                                             "MySql" + File.separator + "FaultyDataService.dbs")));
    }


    @Test(groups = "wso2.dss", description = "Check whether fault service deployed or not")
    public void isServiceFaulty() throws RemoteException {
        assertTrue(isServiceFaulty(serviceName));
        log.info(serviceName + " is faulty");
    }

    @Test(groups = "wso2.dss", dependsOnMethods = {"isServiceFaulty"}, description = "Fix the fault and redeploy")
    public void editFaultyService()
            throws Exception {
        DataServiceAdminClient dataServiceAdminService =
                new DataServiceAdminClient(dssServer.getBackEndUrl(),
                                           dssServer.getSessionCookie());
        String serviceContent;
        String newServiceContent;
        SqlDataSourceUtil dssUtil =
                new SqlDataSourceUtil(dssServer.getSessionCookie(),
                                      dssServer.getBackEndUrl(),
                                      FrameworkFactory.getFrameworkProperties("DSS"),
                                      Integer.parseInt(userInfo.getUserId()));

        dssUtil.createDataSource(getSqlScript());

        serviceContent = dataServiceAdminService.getDataServiceContent(serviceName);

        try {
            OMElement dbsFile = AXIOMUtil.stringToOM(serviceContent);
            OMElement dbsConfig = dbsFile.getFirstChildWithName(new QName("config"));
            Iterator configElement1 = dbsConfig.getChildElements();
            while (configElement1.hasNext()) {
                OMElement property = (OMElement) configElement1.next();
                String value = property.getAttributeValue(new QName("name"));
                if ("org.wso2.ws.dataservice.protocol".equals(value)) {
                    property.setText(dssUtil.getJdbcUrl());

                } else if ("org.wso2.ws.dataservice.driver".equals(value)) {
                    property.setText(dssUtil.getDriver());

                } else if ("org.wso2.ws.dataservice.user".equals(value)) {
                    property.setText(dssUtil.getDatabaseUser());

                } else if ("org.wso2.ws.dataservice.password".equals(value)) {
                    property.setText(dssUtil.getDatabasePassword());
                }
            }
            if (log.isDebugEnabled()) {
                log.debug(dbsFile);
            }
            newServiceContent = dbsFile.toString();
        } catch (XMLStreamException e) {
            log.error("XMLStreamException while handling data service content ", e);
            throw new XMLStreamException("XMLStreamException while handling data service content ", e);
        }
        Assert.assertNotNull("Could not edited service content", newServiceContent);
        dataServiceAdminService.editDataService(serviceName, "", newServiceContent);
        log.info(serviceName + " edited");

    }

    @Test(groups = "wso2.dss", dependsOnMethods = {"editFaultyService"},
          description = "Check whether service is redeployed")
    public void serviceReDeployment() throws RemoteException {
        Assert.assertTrue(isServiceDeployed(serviceName));
        log.info(serviceName + " redeployed");


    }

    @Test(groups = "wso2.dss", dependsOnMethods = {"serviceReDeployment"},
          description = "send requests to redeployed service")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void serviceInvocation() throws RemoteException, ServiceAdminException {
        OMElement response;
        String serviceEndPoint = dssServer.getServiceUrl()+"/" + serviceName+"/";
        AxisServiceClient axisServiceClient = new AxisServiceClient();
        for (int i = 0; i < 5; i++) {
            response = axisServiceClient.sendReceive(getPayload(), serviceEndPoint, "showAllOffices");
            Assert.assertTrue(response.toString().contains("<Office>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<officeCode>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<city>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("<phone>"), "Expected Result not Found");
            Assert.assertTrue(response.toString().contains("</Office>"), "Expected Result not Found");
        }
        log.info("service invocation success");
    }

    @AfterClass(alwaysRun = true)
    public void deleteService() throws ServiceAdminException, RemoteException {
        deleteService(serviceName);
        log.info(serviceName + " deleted");
    }

    private OMElement getPayload() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/faulty_dataservice", "ns1");
        return fac.createOMElement("showAllOffices", omNs);
    }

    private ArrayList<File> getSqlScript() {
        ArrayList<File> al = new ArrayList<File>();
        al.add(selectSqlFile("CreateTables.sql"));
        al.add(selectSqlFile("Offices.sql"));
        return al;
    }
}
