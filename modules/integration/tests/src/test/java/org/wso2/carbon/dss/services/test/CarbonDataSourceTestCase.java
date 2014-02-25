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
package org.wso2.carbon.dss.services.test;

import org.apache.axiom.attachments.ByteArrayDataSource;
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
import org.wso2.carbon.automation.api.clients.dataservices.DataServiceAdminClient;
import org.wso2.carbon.automation.api.clients.ndatasource.NDataSourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.rssmanager.RSSManagerAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.dssutils.SqlDataSourceUtil;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.automation.utils.concurrency.ConcurrencyTest;
import org.wso2.carbon.automation.utils.concurrency.exception.ConcurrencyTestFailedError;
import org.wso2.carbon.automation.utils.dss.SampleDataServiceClient;
import org.wso2.carbon.dss.DSSIntegrationTest;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo_WSDataSourceDefinition;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CarbonDataSourceTestCase extends DSSIntegrationTest {
    private static final Log log = LogFactory.getLog(CarbonDataSourceTestCase.class);

    private final String serviceFile = "CarbonDSDataServiceTest.dbs";
    private final String serviceName = "CarbonDSDataServiceTest";

    private String carbonDataSourceName;
    private SqlDataSourceUtil sqlDataSource;
    private SampleDataServiceClient client;

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {

        super.init(3);
        carbonDataSourceName = createDataSource();
        log.info(carbonDataSourceName + " carbon Data Source Created");
        DataHandler dhArtifact = createArtifactWithDataSource(serviceFile);
        deployService(serviceName, dhArtifact);
        client = new SampleDataServiceClient(getServiceUrl(serviceName));
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws RemoteException {
        deleteService(serviceName);
        cleanup();
    }

    @Test(groups = {"wso2.dss"})
    public void selectOperation() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            client.getCustomerInBoston();
        }
        log.info("Select Operation Success");
    }

    @Test(groups = {"wso2.dss"})
    public void insertOperation() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            client.addEmployee(String.valueOf(i));
        }
        log.info("Insert Operation Success");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = {"insertOperation"})
    public void selectByNumber() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            OMElement result = client.getEmployeeById(String.valueOf(i));
            Assert.assertTrue(result.toString().contains("<first-name>AAA</first-name>"), "Expected Result Mismatched");
        }
        log.info("Select Operation with parameter Success");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = {"insertOperation"})
    public void updateOperation() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            client.increaseEmployeeSalary(String.valueOf(i), "10000");

            OMElement result = client.getEmployeeById(String.valueOf(i));
            Assert.assertTrue(result.toString().contains("<salary>60000.0</salary>"), "Expected Result Mismatched. update operation is not working fine");

        }
        log.info("Update Operation success");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = {"updateOperation"})
    public void deleteOperation() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            client.deleteEmployeeById(String.valueOf(i));
            verifyDeletion(String.valueOf(i));
        }
        log.info("Delete operation success");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = {"selectOperation"})
    public void concurrencyTest() throws ConcurrencyTestFailedError, InterruptedException {
        final OMFactory fac = OMAbstractFactory.getOMFactory();
        final OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/rdbms_sample", "ns1");
        ConcurrencyTest concurrencyTest = new ConcurrencyTest(5, 5);
        OMElement payload = fac.createOMElement("customersInBoston", omNs);
        concurrencyTest.run(getServiceUrl(serviceName), payload, "customersInBoston");
    }


    private String createDataSource() throws Exception {

        DataServiceAdminClient dataServiceAdminService =
                new DataServiceAdminClient(dssServer.getBackEndUrl(), dssServer.getSessionCookie());

        NDataSourceAdminServiceClient dataSourceAdminService =
                new NDataSourceAdminServiceClient(dssServer.getBackEndUrl(), dssServer.getSessionCookie());

        RSSManagerAdminServiceClient rSSAdminConsoleService =
                new RSSManagerAdminServiceClient(dssServer.getBackEndUrl(), dssServer.getSessionCookie());

        String[] list = dataServiceAdminService.getCarbonDataSources();
        String createDataSourceResponse = null;
        WSDataSourceMetaInfo dataSourceInfo;
        String carbonDataSourceName = null;
        FrameworkProperties frameworkProp = FrameworkFactory.getFrameworkProperties("DSS");

        sqlDataSource = new SqlDataSourceUtil(dssServer.getBackEndUrl(), dssServer.getSessionCookie(),
                                              FrameworkFactory.getFrameworkProperties("DSS"), 3);
        sqlDataSource.createDataSource(getSqlScript());
        String databaseName = sqlDataSource.getDatabaseName();
        if (frameworkProp.getEnvironmentSettings().is_runningOnStratos()) {
            if (list != null) {
                for (String ds : list) {
                    if (ds.startsWith(databaseName + "_")) {
                        dataSourceAdminService.deleteDataSource(ds);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            log.error("InterruptedException : " + e);
                            Assert.fail("InterruptedException : " + e);
                        }
                    }
                }
            }
            //todo
//            createDataSourceResponse = rSSAdminConsoleService.createCarbonDataSource(null);
//            Assert.assertTrue((createDataSourceResponse.indexOf(databaseName + "_") == 0), "Database name not found in create data source response message");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.error("InterruptedException : " + e);
                Assert.fail("InterruptedException : " + e);
            }

//            dataSourceInfo = dataSourceAdminService.getCarbonDataSources(createDataSourceResponse);
//            dataSourceInfo.getSecretInformation().setAliasSecret(sqlDataSource.getDatabasePassword());
//            dataSourceAdminService.editCarbonDataSources(createDataSourceResponse, dataSourceInfo);
        } else {
            String dataSourceName = databaseName + "DataSource";
            if (list != null) {
                for (String ds : list) {
                    if (dataSourceName.equalsIgnoreCase(ds)) {
                        dataSourceAdminService.deleteDataSource(ds);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            log.error("InterruptedException : " + e);
                            Assert.fail("InterruptedException : " + e);
                        }
                    }
                }
            }

            dataSourceInfo = getDataSourceInformation(dataSourceName);

            dataSourceAdminService.addDataSource(dataSourceInfo);
            createDataSourceResponse = dataSourceName;
        }

        list = dataServiceAdminService.getCarbonDataSources();
        Assert.assertNotNull(list, "Data Source list null");
        for (String ds : list) {
            if (ds.equals(createDataSourceResponse)) {
                carbonDataSourceName = ds;
                break;
            }
        }

        Assert.assertNotNull("DataSource Not found in DataSource List", carbonDataSourceName);
        return carbonDataSourceName;
    }

    private List<File> getSqlScript() {
        ArrayList<File> al = new ArrayList<File>();
        al.add(selectSqlFile("CreateTables.sql"));
        al.add(selectSqlFile("Customers.sql"));
        return al;
    }

    private DataHandler createArtifactWithDataSource(String serviceFileName)
            throws XMLStreamException, IOException {
        Assert.assertNotNull("Carbon data source name null. create carbon data source first", carbonDataSourceName);
        try {

            OMElement dbsFile = AXIOMUtil.stringToOM(FileManager.readFile(ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                                                                          + "artifacts" + File.separator + "DSS" + File.separator + "dbs" + File.separator
                                                                          + "rdbms" + File.separator + "MySql"
                                                                          + File.separator + serviceFileName).trim());
            OMElement dbsConfig = dbsFile.getFirstChildWithName(new QName("config"));

            Iterator configElement1 = dbsConfig.getChildElements();
            while (configElement1.hasNext()) {
                OMElement property = (OMElement) configElement1.next();
                String value = property.getAttributeValue(new QName("name"));
                if ("carbon_datasource_name".equals(value)) {
                    property.setText(carbonDataSourceName);

                }
            }
            if (log.isDebugEnabled()) {
                log.debug(dbsFile);
            }
            ByteArrayDataSource dbs = new ByteArrayDataSource(dbsFile.toString().getBytes());
            return new DataHandler(dbs);

        } catch (XMLStreamException e) {
            log.error("XMLStreamException when Reading Service File", e);
            throw new XMLStreamException("XMLStreamException when Reading Service File", e);
        } catch (IOException e) {
            log.error("IOException when Reading Service File", e);
            throw new IOException("IOException  when Reading Service File", e);
        }
    }

    private WSDataSourceMetaInfo getDataSourceInformation(String dataSourceName)
            throws XMLStreamException {
        WSDataSourceMetaInfo dataSourceInfo = new WSDataSourceMetaInfo();

        dataSourceInfo.setName(dataSourceName);

        WSDataSourceMetaInfo_WSDataSourceDefinition dataSourceDefinition = new WSDataSourceMetaInfo_WSDataSourceDefinition();

        dataSourceDefinition.setType("RDBMS");
        OMElement dsConfig = AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                                  "<configuration>\n" +
                                                  "<driverClassName>" + sqlDataSource.getDriver() + "</driverClassName>\n" +
                                                  "<url>" + sqlDataSource.getJdbcUrl() + "</url>\n" +
                                                  "<username>" + sqlDataSource.getDatabaseUser() + "</username>\n" +
                                                  "<password encrypted=\"true\">" + sqlDataSource.getDatabasePassword() + "</password>\n" +
                                                  "</configuration>");


        dataSourceDefinition.setDsXMLConfiguration(dsConfig.toString());

        dataSourceInfo.setDefinition(dataSourceDefinition);

        return dataSourceInfo;
    }


    private void verifyDeletion(String employeeNumber) throws AxisFault {
        OMElement result = client.getEmployeeById(employeeNumber);
        Assert.assertFalse(result.toString().contains("<employee>"), "Employee record found. deletion is now working fine");
    }
}
