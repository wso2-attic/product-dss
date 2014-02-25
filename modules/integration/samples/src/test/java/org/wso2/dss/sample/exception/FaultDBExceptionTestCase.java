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
 *
 */

package org.wso2.dss.sample.exception;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.dataservices.samples.fault_dataservice.DataServiceFaultException;
import org.wso2.carbon.dataservices.samples.fault_dataservice.FaultDBServiceStub;
import org.wso2.dss.sample.DSSTestUtils;

import java.rmi.RemoteException;
import java.util.StringTokenizer;

import static org.testng.Assert.fail;

public class FaultDBExceptionTestCase {

    private final String SERVICE_EPR = DSSTestUtils.SERVICE_BASE_EPR + "FaultDBService";

    private static final Log log = LogFactory.getLog(FaultDBExceptionTestCase.class);

    @Test(groups = {"wso2.dss"}, description = "Checking the Exception returned from DSS when Database/table" +
            " does not exist for the data service")
    public void testExceptionForUnavailableDB() throws RemoteException {
        log.info("Running faultServiceTestCase#testExceptionForUnavailableDB");
        FaultDBServiceStub faultDBServiceStub = new FaultDBServiceStub(SERVICE_EPR);
        try {
            faultDBServiceStub.select_op_all_fields();
        } catch (DataServiceFaultException e) {
            StringTokenizer tokenizer = new StringTokenizer(e.getFaultMessage().getDataServiceFault(), "\n");
            while (tokenizer.hasMoreTokens()) {
               String token = tokenizer.nextToken();
                log.info("token:" + token);
                if (token.contains("DS Code")) {
                  String [] splittedStrings = token.split(":");
                    assert "DATABASE_ERROR".equals(splittedStrings[1].trim());
                    return;
                }
            }
            fail("No DS CODE found in exception");
        }
    }
}
