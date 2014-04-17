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
package org.wso2.dss.integration.test;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.extensions.XPathConstants;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.dss.integration.common.utils.DSSTestCaseUtils;
import org.wso2.dss.integration.common.utils.SqlDataSourceUtil;

import javax.activation.DataHandler;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.util.List;

public abstract class DSSIntegrationTest {
    //    protected Log log = LogFactory.getLog(getClass());
    protected AutomationContext dssContext = null;
    protected User userInfo;
    protected String sessionCookie;

    protected void init() throws Exception {
        init(TestUserMode.SUPER_TENANT_ADMIN);

    }

    protected void init(TestUserMode userType) throws Exception {
//        dssContext = new AutomationContext("DSS", "dss01", "carbon.supper", "admin");
        dssContext = new AutomationContext("DSS", userType);
        sessionCookie = dssContext.login();
        userInfo = dssContext.getUser();

    }

    protected void cleanup() {
        userInfo = null;
        dssContext = null;
    }

    protected String getServiceUrlHttp(String serviceName) throws XPathExpressionException {
        return dssContext.getContextUrls().getServiceUrl() + "/" + serviceName;
    }

    protected String getServiceUrlHttps(String serviceName) throws XPathExpressionException {
        return dssContext.getContextUrls().getSecureServiceUrl() + "/" + serviceName;
    }

    protected String getResourceLocation() throws XPathExpressionException {
        return TestConfigurationProvider.getResourceLocation("DSS");
    }

    protected void deployService(String serviceName, OMElement dssConfiguration) throws Exception {
        DSSTestCaseUtils dssTest = new DSSTestCaseUtils();
        Assert.assertTrue(dssTest.uploadArtifact(dssContext.getContextUrls().getBackEndUrl(), sessionCookie, serviceName,
                                                 new DataHandler(new ByteArrayDataSource(dssConfiguration.toString().getBytes()))),
                          "Service File Uploading failed");
        Assert.assertTrue(dssTest.isServiceDeployed(dssContext.getContextUrls().getBackEndUrl(), sessionCookie, serviceName),
                          "Service Not Found, Deployment time out ");
    }

    protected void deployService(String serviceName, DataHandler dssConfiguration)
            throws Exception {
        DSSTestCaseUtils dssTest = new DSSTestCaseUtils();
        Assert.assertTrue(dssTest.uploadArtifact(dssContext.getContextUrls().getBackEndUrl(), sessionCookie, serviceName,
                                                 dssConfiguration),
                          "Service File Uploading failed");
        Assert.assertTrue(dssTest.isServiceDeployed(dssContext.getContextUrls().getBackEndUrl(), sessionCookie, serviceName),
                          "Service Not Found, Deployment time out ");
    }

    protected void deleteService(String serviceName) throws Exception {
        DSSTestCaseUtils dssTest = new DSSTestCaseUtils();
        dssTest.deleteService(dssContext.getContextUrls().getBackEndUrl(), sessionCookie, serviceName);
        Assert.assertTrue(dssTest.isServiceDeleted(dssContext.getContextUrls().getBackEndUrl(), sessionCookie,
                                                   serviceName), "Service Deletion Failed");
    }

    protected DataHandler createArtifact(String path, List<File> sqlFile)
            throws Exception {
        SqlDataSourceUtil dataSource = new SqlDataSourceUtil(sessionCookie
                , dssContext.getContextUrls().getBackEndUrl());
        dataSource.createDataSource(sqlFile);
        return dataSource.createArtifact(path);
    }

    protected boolean isServiceDeployed(String serviceName) throws Exception {
        DSSTestCaseUtils dssTest = new DSSTestCaseUtils();
        return dssTest.isServiceDeployed(dssContext.getContextUrls().getBackEndUrl(), sessionCookie, serviceName);
    }

    protected boolean isServiceFaulty(String serviceName) throws Exception {
        DSSTestCaseUtils dssTest = new DSSTestCaseUtils();
        return dssTest.isServiceFaulty(dssContext.getContextUrls().getBackEndUrl(), sessionCookie, serviceName);
    }

    protected File selectSqlFile(String fileName) throws XPathExpressionException {

        String driver = dssContext.getConfigurationValue(XPathConstants.DATA_SOURCE_DRIVER_CLASS_NAME);;
        String type = "";
        if (driver.contains("h2")) {
            type = "h2";
        } else if (driver.contains("mysql")) {
            type = "MySql";
        } else if (driver.contains("oracle")) {
            type = "oracle";
        }

        return new File(TestConfigurationProvider.getResourceLocation() + "artifacts"
                        + File.separator + "DSS" + File.separator + "sql" + File.separator
                        + type + File.separator + fileName);
    }

}
