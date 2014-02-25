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
package org.wso2.carbon.dss.ui.test;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.wso2.carbon.automation.api.clients.server.admin.ServerAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.ServerGroupManager;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.dssutils.SqlDataSourceUtil;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.core.utils.environmentutils.ProductUrlGeneratorUtil;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.automation.utils.dss.DSSTestCaseUtils;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;

public abstract class DSSIntegrationUITest {
    //    protected Log log = LogFactory.getLog(getClass());
    protected EnvironmentVariables dssServer;
    protected UserInfo userInfo;

    protected void init() throws Exception {
        userInfo = UserListCsvReader.getUserInfo(2);
        EnvironmentBuilder builder = new EnvironmentBuilder().dss(2);
        dssServer = builder.build().getDss();
    }

    protected void init(int userId) throws Exception {
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().dss(userId);
        dssServer = builder.build().getDss();
    }

    protected void cleanup() {
        userInfo = null;
        dssServer = null;
    }

    protected String getServiceUrl(String serviceName) {
        return dssServer.getServiceUrl() + "/" + serviceName;
    }

    protected String getServiceUrlHttps(String serviceName) {
        return "https://" + dssServer.getProductVariables().getHostName() + ":"
               + dssServer.getProductVariables().getHttpsPort() + "/services/" + serviceName;
    }

    protected void deployService(String serviceName, OMElement dssConfiguration) throws Exception {
        DSSTestCaseUtils dssTest = new DSSTestCaseUtils();
        Assert.assertTrue(dssTest.uploadArtifact(dssServer.getBackEndUrl(), dssServer.getSessionCookie(), serviceName,
                                                 new DataHandler(new ByteArrayDataSource(dssConfiguration.toString().getBytes()))),
                          "Service File Uploading failed");
        Assert.assertTrue(dssTest.isServiceDeployed(dssServer.getBackEndUrl(), dssServer.getSessionCookie(), serviceName),
                          "Service Not Found, Deployment time out ");
    }

    protected void deployService(String serviceName, DataHandler dssConfiguration)
            throws Exception {
        DSSTestCaseUtils dssTest = new DSSTestCaseUtils();
        Assert.assertTrue(dssTest.uploadArtifact(dssServer.getBackEndUrl(), dssServer.getSessionCookie(), serviceName,
                                                 dssConfiguration),
                          "Service File Uploading failed");
        Assert.assertTrue(dssTest.isServiceDeployed(dssServer.getBackEndUrl(), dssServer.getSessionCookie(), serviceName),
                          "Service Not Found, Deployment time out ");
    }

    protected void deleteService(String serviceName) throws RemoteException {
        DSSTestCaseUtils dssTest = new DSSTestCaseUtils();
        dssTest.deleteService(dssServer.getBackEndUrl(), dssServer.getSessionCookie(), serviceName);
        Assert.assertTrue(dssTest.isServiceDeleted(dssServer.getBackEndUrl(), dssServer.getSessionCookie(),
                                                   serviceName), "Service Deletion Failed");
    }

    protected DataHandler createArtifact(String path, List<File> sqlFile)
            throws Exception, IOException, ClassNotFoundException, SQLException {
        SqlDataSourceUtil dataSource = new SqlDataSourceUtil(dssServer.getSessionCookie(), dssServer.getBackEndUrl(),
                                                             FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME),
                                                             Integer.parseInt(userInfo.getUserId()));
        dataSource.createDataSource(sqlFile);
        return dataSource.createArtifact(path);
    }

    protected void gracefullyRestartServer() throws Exception {
        ServerAdminClient serverAdminClient = new ServerAdminClient(dssServer.getBackEndUrl(),
                                                                    userInfo.getUserName(),
                                                                    userInfo.getPassword());
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME);
        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);
    }

    protected boolean isServiceDeployed(String serviceName) throws RemoteException {
        DSSTestCaseUtils dssTest = new DSSTestCaseUtils();
        return dssTest.isServiceDeployed(dssServer.getBackEndUrl(), dssServer.getSessionCookie(), serviceName);
    }

    protected boolean isServiceFaulty(String serviceName) throws RemoteException {
        DSSTestCaseUtils dssTest = new DSSTestCaseUtils();
        return dssTest.isServiceFaulty(dssServer.getBackEndUrl(), dssServer.getSessionCookie(), serviceName);
    }

    protected File selectSqlFile(String fileName) {

        String driver = FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME)
                .getDataSource().get_dbDriverName();
        String type = "";
        if (driver.contains("h2")) {
            type = "h2";
        } else if (driver.contains("mysql")) {
            type = "MySql";
        } else if (driver.contains("oracle")) {
            type = "oracle";
        }

        return new File(ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts"
                        + File.separator + "DSS" + File.separator + "sql" + File.separator
                        + type + File.separator + fileName);
    }

    protected String getLoginURL(String productName) {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();
        boolean isRunningOnStratos =
                environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos();

        if (isRunningOnStratos) {
            return ProductUrlGeneratorUtil.getServiceHomeURL(productName);
        } else {
            return ProductUrlGeneratorUtil.getProductHomeURL(productName);
        }
    }

    protected boolean isRunningOnCloud() {
        return FrameworkFactory.getFrameworkProperties(ProductConstant.APP_SERVER_NAME).getEnvironmentSettings().is_runningOnStratos();

    }

}
