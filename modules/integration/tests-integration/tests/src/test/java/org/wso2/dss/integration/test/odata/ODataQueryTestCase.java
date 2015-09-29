/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.dss.integration.test.odata;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.dss.integration.test.DSSIntegrationTest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.dss.integration.test.odata.ODataTestUtils.sendGET;

public class ODataQueryTestCase extends DSSIntegrationTest {
	private final String serviceName = "ODataBatchRequestSampleService";
	private final String configId = "default";
	private String webappURL;

	@BeforeClass(alwaysRun = true)
	public void serviceDeployment() throws Exception {
		super.init();
		List<File> sqlFileLis = new ArrayList<>();
		sqlFileLis.add(selectSqlFile("CreateODataTables.sql"));
		sqlFileLis.add(selectSqlFile("Customers.sql"));
		sqlFileLis.add(selectSqlFile("FIlesWithFIlesRecords.sql"));
		deployService(serviceName, createArtifact(getResourceLocation() +
		                                          File.separator + "dbs" +
		                                          File.separator + "odata" +
		                                          File.separator +
		                                          "ODataBatchRequestSampleService.dbs", sqlFileLis));
		webappURL = dssContext.getContextUrls().getWebAppURL();
	}

	@AfterClass(alwaysRun = true)
	public void destroy() throws Exception {
		deleteService(serviceName);
		cleanup();
	}

	@Test(groups = "wso2.dss", description = "select query test")
	public void validateSelectQueryTestCase() throws Exception {
		String endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/FILES?$select=TYPE";
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		Object[] response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		Assert.assertTrue(!response[0].toString().contains("FILENAME"));
	}

	@Test(groups = "wso2.dss", description = "top query test")
	public void validateTopQueryTestCase() throws Exception {
		String endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/CUSTOMERS?$top=1&$orderby=CONTACTFIRSTNAME%20desc";
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		Object[] response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		Assert.assertTrue(response[1].toString().contains("Zbyszek"));
		Assert.assertTrue(!response[1].toString().contains("Yu"));
	}

	@Test(groups = "wso2.dss", description = "order by query test")
	public void validateOrderByQueryTestCase() throws Exception {
		String endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/CUSTOMERS?$top=1&$orderby=length(ADDRESSLINE1)";
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		Object[] response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		Assert.assertTrue(response[1].toString().contains("Singapore"));
	}

	@Test(groups = "wso2.dss", description = "count query test")
	public void validateCountQueryTestCase() throws Exception {
		String endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/FILES/$count";
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		Object[] response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		Assert.assertTrue(response[1].toString().contains("4"));
	}

	@Test(groups = "wso2.dss", description = "filter query test")
	public void validateFilterQueryTestCase() throws Exception {
		String endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/CUSTOMERS?$filter=COUNTRY%20eq%20%27Singapore%27";
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		Object[] response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		Assert.assertTrue(response[1].toString().contains("Singapore"));
		Assert.assertTrue(!response[1].toString().contains("France"));
	}

	@Test(groups = "wso2.dss", description = "skip query test")
	public void validateSkipQueryTestCase() throws Exception {
		String endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/CUSTOMERS?$skip=1&$top=2&$orderby=CONTACTFIRSTNAME%20desc";
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		Object[] response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		Assert.assertTrue(!response[1].toString().contains("Zbyszek"));
		Assert.assertTrue(response[1].toString().contains("Yu"));
		Assert.assertTrue(response[1].toString().contains("Yoshi"));
	}
}
