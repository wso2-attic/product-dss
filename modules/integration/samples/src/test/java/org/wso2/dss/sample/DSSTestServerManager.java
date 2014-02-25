/*
 *
 *   Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.dss.sample;/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.integration.framework.TestServerManager;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.IOException;

/**
 * Prepares the WSO2 DSS for test runs, starts the server, and stops the server after
 * test runs
 */
public class DSSTestServerManager extends TestServerManager {
    private static final Log log = LogFactory.getLog(DSSTestServerManager.class);

    @Override
    @BeforeSuite(timeOut = 300000)
    public String startServer() throws IOException {
        String carbonHome = super.startServer();
        System.setProperty("carbon.home", carbonHome);
        return carbonHome;
    }

    @Override
    @AfterSuite(timeOut = 120000)
    public void stopServer() throws Exception {
        super.stopServer();
    }

    protected void copyArtifacts(String carbonHome) throws IOException {

        String deploymentDir = computeDestDirPath(carbonHome);

        /* CSV sample */
        copySampleFile(computeSourcePath("csv", "CSVSampleService.dbs"), deploymentDir);

        /* DTP sample */
        copySampleFile(computeSourcePath("rdbms", "DTPSampleService.dbs"), deploymentDir);

        /* RDBMS sample */
        copySampleFile(computeSourcePath("rdbms", "RDBMSSample.dbs"), deploymentDir);

        /* Batch request sample */
        copySampleFile(computeSourcePath("rdbms", "BatchRequestSample.dbs"), deploymentDir);

        /* Nested query sample */
        copySampleFile(computeSourcePath("rdbms", "NestedQuerySample.dbs"), deploymentDir);

        /* Excel sample */
        copySampleFile(computeSourcePath("excel", "ExcelSampleService.dbs"), deploymentDir);

        /* Resources sample */
        copySampleFile(computeSourcePath("rdbms", "ResourcesSample.dbs"), deploymentDir);

        /* Eventing sample */
     //   copySampleFile(computeSourcePath("rdbms", "EventingSample.dbs"), deploymentDir);

        /* File service sample */
        copySampleFile(computeSourcePath("rdbms", "FileService.dbs"), deploymentDir);


        copySampleFile("../src/test/resources/dbs/FaultDBService.dbs", deploymentDir);

        /* GSpread service sample */
        if (this.isOnlineTestsEnabled()) {
            copySampleFile(computeSourcePath("gspread", "GSpreadSample.dbs"), deploymentDir);
        }
    }

    protected boolean isOnlineTestsEnabled() {
        String gspreadProp = System.getProperty("online.tests");
        if (gspreadProp != null) {
            return Boolean.parseBoolean(gspreadProp);
        } else {
            return false;
        }
    }

    private void copySampleFile(String sourceFilePath, String destDirPath) {
        File sourceFile = new File(sourceFilePath);
        File destFile = new File(destDirPath);
        log.info("Copying " + sourceFile.getAbsolutePath() + " => " + destFile.getAbsolutePath());
        try {
            FileManipulator.copyFileToDir(sourceFile, destFile);
        } catch (IOException e) {
            log.error("Error while copying " + sourceFilePath + " to " + destDirPath, e);
        }
    }

    private String computeSourcePath(String sampleFolder, String fileName) {
        String samplesDir = System.getProperty("samples.dir");
        return samplesDir + File.separator + sampleFolder + File.separator + fileName;
    }

    private String computeDestDirPath(String carbonHome) {
        /* First create the deployment folder in the server if it doesn't already exist */
        String deploymentPath = carbonHome + File.separator + "repository" + File.separator
                + "deployment" + File.separator + "server" + File.separator + "dataservices" +
                File.separator + "samples";
        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdirs()) {
            log.error("Error while creating the deployment folder : " + deploymentPath);
        }
        return deploymentPath;
    }
}
