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
package org.wso2.dss.sample.gspread;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.dataservices.samples.gspread_sample_service.DataServiceFault;
import org.wso2.carbon.dataservices.samples.gspread_sample_service.GSpreadSample;
import org.wso2.carbon.dataservices.samples.gspread_sample_service.GSpreadSampleStub;
import org.wso2.dss.sample.DSSTestUtils;

import java.rmi.RemoteException;

/**
 * Google Spread sheet test performed online.
 */
public class GSpreadSampleTestCase {

    private final String SERVICE_EPR = DSSTestUtils.SERVICE_BASE_EPR + "GSpreadSample";

    private static final Log log = LogFactory.getLog(GSpreadSampleTestCase.class);

    @Test(groups = {"wso2.dss"})
    public void testGSpreadQuery() throws DataServiceFault, RemoteException {

        if (this.isOnlineTestsEnabled()) {
            log.info("Running GSpreadSampleTestCase#testGSpreadQuery");
            GSpreadSample stub = new GSpreadSampleStub(SERVICE_EPR);
            assert stub.getCustomers().length > 0 : "No of customers should be greater than zero";

        }
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
