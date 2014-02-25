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
package org.wso2.dss.sample.rdbms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.dataservices.samples.nested_query_sample.DataServiceFault;
import org.wso2.carbon.dataservices.samples.nested_query_sample.NestedQuerySample;
import org.wso2.carbon.dataservices.samples.nested_query_sample.NestedQuerySampleStub;
import org.wso2.dss.sample.DSSTestUtils;

import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * Nested Queries test
 */
public class NestedQuerySampleTestCase {
	
	private final String SERVICE_EPR = DSSTestUtils.SERVICE_BASE_EPR + "NestedQuerySample";

    private static final Log log = LogFactory.getLog(NestedQuerySampleTestCase.class);

    @Test(groups = {"wso2.dss"})
	public void testListOfficesNestedQuery() throws DataServiceFault, RemoteException {

			log.info("Running NestedQuerySampleTestCase#testListOfficesNestedQuery");
			NestedQuerySample stub = new NestedQuerySampleStub(SERVICE_EPR);
			assertTrue(stub.listOffices().length > 0, "No of offices listed should be greater than zero");
	}

    @Test(groups = {"wso2.dss"})
	public void testCustomerOrdersNestedQuery() throws DataServiceFault, RemoteException {

			log.info("Running NestedQuerySampleTestCase#testCustomerOrdersNestedQuery");
			NestedQuerySample stub = new NestedQuerySampleStub(SERVICE_EPR);
			assertTrue(stub.customerOrders().length > 0, "No of customer orders should be greater than zero");

	}

}
