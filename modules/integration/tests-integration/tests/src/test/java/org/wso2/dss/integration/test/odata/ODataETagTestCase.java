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

import static org.wso2.dss.integration.test.odata.ODataTestUtils.*;

public class ODataETagTestCase extends DSSIntegrationTest {
	private final String serviceName = "ODataETagSampleService";
	private final String configId = "default";
	private String webappURL;

	@BeforeClass(alwaysRun = true)
	public void serviceDeployment() throws Exception {
		super.init();
		List<File> sqlFileLis = new ArrayList<>();
		sqlFileLis.add(selectSqlFile("CreateODataTables.sql"));
		sqlFileLis.add(selectSqlFile("Customers.sql"));
		deployService(serviceName,
		              createArtifact(getResourceLocation() + File.separator + "dbs" + File.separator + "odata" +
		                             File.separator + "ODataETagSampleService.dbs", sqlFileLis));
		webappURL = dssContext.getContextUrls().getWebAppURL();
	}

	@AfterClass(alwaysRun = true)
	public void destroy() throws Exception {
		deleteService(serviceName);
		cleanup();
	}

	@Test(groups = "wso2.dss", description = "e tag retrieval test")
	public void validateE_TagRetrievalTestCase() throws Exception {
		String endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/FILES";
		String content = "{\"FILENAME\": \"M.K.H.Gunasekara\" ,\"TYPE\" : \"dss\"}";
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		Object[] response = sendPOST(endpoint, content, headers);
		Assert.assertEquals(response[0], 204);
		endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/FILES(\'M.K.H.Gunasekara')";
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		String etag = getETag(response[1].toString());
		headers.put("If-Match", "1212122");
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 412);
		headers.remove("If-Match");
		headers.put("If-None-Match", etag);
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 412);
		headers.remove("If-None-Match");
		headers.put("If-Match", etag);
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
	}

	@Test(groups = "wso2.dss", description = "etag generation test", dependsOnMethods = "validateE_TagRetrievalTestCase")
	public void validateE_TagGenerationTestCase() throws Exception {
		String endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/FILES";
		String content = "{\"FILENAME\": \"WSO2\" ,\"TYPE\" : \"bam\"}";
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		Object[] response = sendPOST(endpoint, content, headers);
		Assert.assertEquals(response[0], 204);
		endpoint = webappURL + "/odata/" + serviceName + "/" + configId +
		           "/FILES(\'WSO2\')";
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		String etag = getETag(response[1].toString());
		endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/FILES(\'WSO2\')";
		content = "{\"TYPE\" : \"USJP\"}";
		int responseCode = sendPUT(endpoint, content, headers);
		Assert.assertEquals(responseCode, 204);
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		String tempETag = getETag(response[1].toString());
		Assert.assertNotEquals(etag, tempETag);
	}

	@Test(groups = "wso2.dss", description = "etag concurrent handling with put method test", dependsOnMethods = "validateE_TagGenerationTestCase")
	public void validateE_TagConcurrentHandlingTestCaseForPutMethod() throws Exception {
		String endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/FILES(\'M.K.H.Gunasekara\')";
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		Object[] response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		String etag = getETag(response[1].toString());
		//modifying data - E-Tag should be changed after processing the below request
		headers.put("If-Match", etag);
		String content = "{\"TYPE\" : \"ml\"}";

		int responseCode = sendPUT(endpoint, content, headers);
		Assert.assertEquals(responseCode, 204);
		// Data has been modified therefore E-Tag has been changed, Then If-None-Match should be worked with previous E-Tag
		headers.remove("If-Match");
		headers.put("If-None-Match", etag);
		content = "{\"TYPE\" : \"test\"}";
		responseCode = sendPUT(endpoint, content, headers);
		Assert.assertEquals(responseCode, 204);

		//testing concurrent test with put method
		// get the E-Tag
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		etag = getETag(response[1].toString());
		content = "{\"TYPE\" : \"SriLanka\"}";
		headers.remove("If-None-Match");
		ODataRequestThreadExecutor threadExecutor = new ODataRequestThreadExecutor("PUT", content, headers, endpoint);
		threadExecutor.run();
		Thread.sleep(1000);
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		String tempETag = getETag(response[1].toString());
		Assert.assertNotEquals(etag, tempETag);
		headers.put("If-Match", etag);
		content = "{\"TYPE\" : \"MB\"}";
		responseCode = sendPUT(endpoint, content, headers);
		Assert.assertEquals(responseCode, 412);
		headers.put("If-Match", tempETag);
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		// Data validation
		Assert.assertFalse(response[1].toString().contains("MB"), "E-Tag with put method failed");
		Assert.assertTrue(response[1].toString().contains("SriLanka"), "E-Tag with put method failed");

		//testing concurrent test with delete method
		// get the E-Tag
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		headers.remove("If-Match");
		threadExecutor = new ODataRequestThreadExecutor("DELETE", null, headers, endpoint);
		threadExecutor.run();
		Thread.sleep(1000);
		headers.put("If-Match", etag);
		content = "{\"TYPE\" : \"MB\"}";
		responseCode = sendPUT(endpoint, content, headers);
		Assert.assertEquals(responseCode, 404);
	}

	@Test(groups = "wso2.dss", description = "etag concurrent handling with patch method test", dependsOnMethods = "validateE_TagConcurrentHandlingTestCaseForPutMethod")
	public void validateE_TagConcurrentHandlingTestCaseForPatchMethod() throws Exception {
		String endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/FILES(\'WSO2\')";
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		Object[] response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		String etag = getETag(response[1].toString());
		//modifying data - E-Tag should be changed after processing the below request
		headers.put("If-Match", etag);
		String content = "{\"TYPE\" : \"ml\"}";
		int responseCode = sendPATCH(endpoint, content, headers);
		Assert.assertEquals(responseCode, 204);
		// Data has been modified therefore E-Tag has been changed, Then If-None-Match should be worked with previous E-Tag
		headers.remove("If-Match");
		headers.put("If-None-Match", etag);
		content = "{\"TYPE\" : \"test\"}";
		responseCode = sendPATCH(endpoint, content, headers);
		Assert.assertEquals(responseCode, 204);

		//testing concurrent test with put method
		// get the E-Tag
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		etag = getETag(response[1].toString());
		content = "{\"TYPE\" : \"SriLanka\"}";
		headers.remove("If-None-Match");
		ODataRequestThreadExecutor threadExecutor = new ODataRequestThreadExecutor("PUT", content, headers, endpoint);
		threadExecutor.run();
		Thread.sleep(1000);
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		String tempETag = getETag(response[1].toString());
		Assert.assertNotEquals(etag, tempETag);
		headers.put("If-Match", etag);
		content = "{\"TYPE\" : \"MB\"}";
		responseCode = sendPATCH(endpoint, content, headers);
		Assert.assertEquals(responseCode, 412);
		headers.put("If-Match", tempETag);
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		// Data validation
		Assert.assertFalse(response[1].toString().contains("MB"), "E-Tag with put method failed");
		Assert.assertTrue(response[1].toString().contains("SriLanka"), "E-Tag with put method failed");

		//testing concurrent test with delete method
		// get the E-Tag
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		headers.remove("If-Match");
		threadExecutor = new ODataRequestThreadExecutor("DELETE", null, headers, endpoint);
		threadExecutor.run();
		Thread.sleep(1000);
		headers.put("If-Match", etag);
		content = "{\"TYPE\" : \"MB\"}";
		responseCode = sendPATCH(endpoint, content, headers);
		Assert.assertEquals(responseCode, 404);
	}

	@Test(groups = "wso2.dss", description = "etag concurrent handling with delete method test", dependsOnMethods = "validateE_TagConcurrentHandlingTestCaseForPatchMethod")
	public void validateE_TagConcurrentHandlingTestCaseForDeleteMethod() throws Exception {
		String endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/FILES";
		String content = "{\"FILENAME\": \"M.K.H.Gunasekara\" ,\"TYPE\" : \"dss\"}";
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		Object[] response = sendPOST(endpoint, content, headers);
		Assert.assertEquals(response[0], 204);
		endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/FILES(\'M.K.H.Gunasekara\')";
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		String etag = ODataTestUtils.getETag(response[1].toString());
		headers.put("If-None-Match", etag);
		int responseCode = sendDELETE(endpoint, headers);
		Assert.assertEquals(responseCode, 412);
		headers.remove("If-None-Match");
		headers.put("If-Match", etag);
		responseCode = sendDELETE(endpoint, headers);
		Assert.assertEquals(responseCode, 204);
		responseCode = sendDELETE(endpoint, headers);
		Assert.assertEquals(responseCode, 404);

		// To insert values
		validateE_TagRetrievalTestCase();

		//testing concurrent test with put method
		// get the E-Tag
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		etag = getETag(response[1].toString());
		content = "{\"TYPE\" : \"SriLanka\"}";
		headers.remove("If-Match");
		ODataRequestThreadExecutor threadExecutor = new ODataRequestThreadExecutor("PUT", content, headers, endpoint);
		threadExecutor.run();
		Thread.sleep(1000);
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		String tempETag = getETag(response[1].toString());
		Assert.assertNotEquals(etag, tempETag);
		headers.put("If-Match", etag);
		responseCode = sendDELETE(endpoint, headers);
		Assert.assertEquals(responseCode, 412);
		headers.put("If-Match", tempETag);
		responseCode = sendDELETE(endpoint, headers);
		Assert.assertEquals(responseCode, 204);
		responseCode = sendDELETE(endpoint, headers);
		Assert.assertEquals(responseCode, 404);
	}

	@Test(groups = "wso2.dss", description = "property modification using etag concurrent handling with put method test", dependsOnMethods = "validateE_TagConcurrentHandlingTestCaseForDeleteMethod")
	public void validateE_TagConcurrentHandlingTestCaseForUpdatePropertyWithPutMethod() throws Exception {
		// To insert values
		String endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/FILES";
		String content = "{\"FILENAME\": \"M.K.H.Gunasekara\" ,\"TYPE\" : \"dss\"}";
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		Object[] response = sendPOST(endpoint, content, headers);
		Assert.assertEquals(response[0], 204);
		String entityEndpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/FILES(\'M.K.H.Gunasekara\')";
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		String etag = getETag(response[1].toString());
		endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/FILES(\'M.K.H.Gunasekara\')/TYPE";
		content = "{\"value\" : \"Jayasooriya\"}";
		headers.put("If-None-Match", etag);
		int responseCode = sendPUT(endpoint, content, headers);
		Assert.assertEquals(responseCode, 412);
		headers.remove("If-None-Match");
		headers.put("If-Match", etag);
		responseCode = sendPUT(endpoint, content, headers);
		Assert.assertEquals(responseCode, 204);

		//testing concurrent test with put method
		// get the E-Tag
		headers.remove("If-Match");
		response = sendGET(entityEndpoint, headers);
		Assert.assertEquals(response[0], 200);
		etag = getETag(response[1].toString());
		content = "{\"value\" : \"SriLanka\"}";
		ODataRequestThreadExecutor threadExecutor = new ODataRequestThreadExecutor("PUT", content, headers, endpoint);
		threadExecutor.run();
		Thread.sleep(1000);
		response = sendGET(entityEndpoint, headers);
		Assert.assertEquals(response[0], 200);
		String tempETag = getETag(response[1].toString());
		Assert.assertNotEquals(etag, tempETag);
		headers.put("If-Match", etag);
		content = "{\"value\" : \"DSS Server\"}";
		responseCode = sendPUT(endpoint, content, headers);
		Assert.assertEquals(responseCode, 412);
		// Data validation
		headers.remove("If-Match");
		response = sendGET(endpoint, headers);
		Assert.assertEquals(response[0], 200);
		Assert.assertFalse(response[1].toString().contains("DSS Server"), "E-Tag with put method failed");
		Assert.assertTrue(response[1].toString().contains("SriLanka"), "E-Tag with put method failed");
		headers.put("If-Match", tempETag);
		responseCode = sendPUT(endpoint, content, headers);
		Assert.assertEquals(responseCode, 204);

	}

	@Test(groups = "wso2.dss", description = "property modification using etag concurrent handling with patch method test", dependsOnMethods = "validateE_TagConcurrentHandlingTestCaseForUpdatePropertyWithPutMethod")
	public void validateE_TagConcurrentHandlingTestCaseForUpdatePropertyWithPatchMethod() throws Exception {
		String entityEndpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/FILES(\'M.K.H.Gunasekara\')";
		String endpoint = webappURL + "/odata/" + serviceName + "/" + configId + "/FILES(\'M.K.H.Gunasekara\')/TYPE";
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		Object[] response = sendGET(entityEndpoint, headers);
		Assert.assertEquals(response[0], 200);
		String etag = getETag(response[1].toString());
		String content = "{\"value\" : \"Jayasooriya\"}";
		headers.put("If-None-Match", etag);
		int responseCode = sendPATCH(endpoint, content, headers);
		Assert.assertEquals(responseCode, 412);
		headers.remove("If-None-Match");
		headers.put("If-Match", etag);
		responseCode = sendPATCH(endpoint, content, headers);
		Assert.assertEquals(responseCode, 204);

		//testing concurrent test with put method
		// get the E-Tag
		headers.remove("If-Match");
		response = sendGET(entityEndpoint, headers);
		Assert.assertEquals(response[0], 200);
		etag = getETag(response[1].toString());
		content = "{\"value\" : \"SriLanka\"}";
		ODataRequestThreadExecutor threadExecutor = new ODataRequestThreadExecutor("PUT", content, headers, endpoint);
		threadExecutor.run();
		Thread.sleep(1000);
		response = sendGET(entityEndpoint, headers);
		Assert.assertEquals(response[0], 200);
		String tempETag = getETag(response[1].toString());
		Assert.assertNotEquals(etag, tempETag);
		headers.put("If-Match", etag);
		content = "{\"value\" : \"DSS Server\"}";
		responseCode = sendPATCH(endpoint, content, headers);
		Assert.assertEquals(responseCode, 412);
		// Data validation
		headers.remove("If-Match");
		response = sendGET(entityEndpoint, headers);
		Assert.assertEquals(response[0], 200);
		Assert.assertFalse(response[1].toString().contains("DSS Server"), "E-Tag with patch method failed");
		Assert.assertTrue(response[1].toString().contains("SriLanka"), "E-Tag with patch method failed");
		headers.put("If-Match", tempETag);
		responseCode = sendPATCH(endpoint, content, headers);
		Assert.assertEquals(responseCode, 204);

	}
}
