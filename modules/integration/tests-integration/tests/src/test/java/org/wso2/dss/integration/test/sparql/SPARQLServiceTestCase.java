/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.dss.integration.test.sparql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.dss.integration.test.DSSIntegrationTest;

import javax.activation.DataHandler;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

public class SPARQLServiceTestCase extends DSSIntegrationTest {

    private static final Log log = LogFactory.getLog(SPARQLServiceTestCase.class);

    private final String serviceName = "SPARQLDataService";
    private  String serviceEndPoint;

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
        serviceEndPoint = getServiceUrlHttp(serviceName);
        String resourceFileLocation = getResourceLocation();
        deployService(serviceName,
                new DataHandler(new URL("file:///" + resourceFileLocation +File.separator + "dbs" + File.separator +
                        "sparql" + File.separator + "SPARQLDataService.dbs")));
        log.info(serviceName + " uploaded");
    }

    @Test(groups = "wso2.dss", description = "Check whether service deployed or not")
    public void testServiceDeployment() throws Exception {
        assertTrue(isServiceDeployed(serviceName));
        log.info(serviceName + " is deployed");
    }

    @Test(groups = {"wso2.dss"})
    public void getAllBookmarkData() throws IOException, XPathExpressionException {
        String endpoint = serviceEndPoint + ".SOAP11Endpoint/";
        String content ="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:dat=\"http://ws.wso2.org/dataservice\">\n" +
                        "   <soapenv:Header/>\n" +
                        "   <soapenv:Body>\n" +
                        "      <dat:getBookmarks/>\n" +
                        "   </soapenv:Body>\n" +
                        "</soapenv:Envelope>";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/xml");
        headers.put("Content-Type", "text/plain");
        headers.put("SOAPAction", "\"urn:getBookmarks\"");
        Object[] response = sendPOST(endpoint, content, headers);
        Assert.assertEquals(Integer.parseInt(response[0].toString()), 200);
        log.info("Response : " + response[1].toString());
        Assert.assertTrue(response[1].toString().contains("<bookmark>"), "Expected Result not found on response message");
        Assert.assertTrue(response[1].toString().contains("http://semantic.eea.europa.eu/home/roug/bookmarks"), "Expected Result not found on response message");

    }

    public Object[] sendPOST(String endpoint, String content, Map<String, String> headers) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(endpoint);
        httpClient.getParams().setParameter("http.socket.timeout", 300000);
        for (String headerType : headers.keySet()) {
            httpPost.setHeader(headerType, headers.get(headerType));
        }
        if (content != null) {
            HttpEntity httpEntity = new ByteArrayEntity(content.getBytes("UTF-8"));
            if (headers.get("Content-Type") == null) {
                httpPost.setHeader("Content-Type", "application/json");
            }
            httpPost.setEntity(httpEntity);
        }
        HttpResponse httpResponse = httpClient.execute(httpPost);
        if (httpResponse.getEntity() != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            reader.close();
            return new Object[] { httpResponse.getStatusLine().getStatusCode(), response.toString() };
        } else {
            return new Object[] { httpResponse.getStatusLine().getStatusCode() };
        }
    }

    @AfterClass(alwaysRun = true, groups = "wso2.dss", description = "delete service")
    public void deleteService() throws Exception {
        deleteService(serviceName);
        cleanup();
    }

}
