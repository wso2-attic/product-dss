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

package org.wso2.dss.sample.excel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.dataservices.samples.excel_sample_service.DataServiceFault;
import org.wso2.carbon.dataservices.samples.excel_sample_service.ExcelSampleService;
import org.wso2.carbon.dataservices.samples.excel_sample_service.ExcelSampleServiceStub;
import org.wso2.dss.sample.DSSTestUtils;

import java.rmi.RemoteException;

/**
 * Excel Sheet Sample Service Test
 */
public class ExcelSampleServiceTestCase {

    private final String SERVICE_EPR = DSSTestUtils.SERVICE_BASE_EPR + "ExcelSampleService";

    private static final Log log = LogFactory.getLog(ExcelSampleServiceTestCase.class);

    @Test(groups = {"wso2.dss"})
    public void testGetProducts() throws RemoteException, DataServiceFault {

            log.info("Running ExcelSampleServiceTestCase#testGetProducts");
            ExcelSampleService stub = new ExcelSampleServiceStub(SERVICE_EPR);
            assert stub.getProducts().length > 0 : "No of products should be greater than zero";

    }

}
