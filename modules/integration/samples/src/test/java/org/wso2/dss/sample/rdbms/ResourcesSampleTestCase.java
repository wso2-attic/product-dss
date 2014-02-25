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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.dss.sample.DSSTestUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import static org.testng.Assert.*;

/**
 * REST style Resource tests
 */
public class ResourcesSampleTestCase {

    private final String SERVICE_EPR_PRODUCT = DSSTestUtils.SERVICE_BASE_EPR + "ResourcesSample.HTTPEndpoint/product/";

    private final String SERVICE_EPR_PRODUCTS = DSSTestUtils.SERVICE_BASE_EPR + "ResourcesSample.HTTPEndpoint" +
            "/products/";

    private static final Log log = LogFactory.getLog(ResourcesSampleTestCase.class);

    @Test(groups = {"wso2.dss"})
    public void testResourceGet() throws IOException, XMLStreamException {
        log.info("Running ResourcesSampleTestCase#testResourceGet");
        URL url = new URL(SERVICE_EPR_PRODUCTS);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        InputStream in = httpCon.getInputStream();
        String xmlContent = DSSTestUtils.getStringFromInputStream(in);
        in.close();
        OMElement resultEl = AXIOMUtil.stringToOM(xmlContent);
        Iterator<OMElement> childEls = resultEl.getChildElements();
        assertNotNull(childEls.next(), "Element should not be null");
        assertNotNull(childEls.next(), " Element should not be null");
    }

    @Test(groups = {"wso2.dss"})
    public void testResourceGetWithParams() throws Exception {
        log.info("Running ResourcesSampleTestCase#testResourceGetWithParams");
        OMElement resultEl = this.executeGet(urlenc("S10_1678"));
        String buyPrice = ((OMElement) ((OMElement) resultEl
                .getChildrenWithLocalName("Product").next()).getChildrenWithLocalName(
                "buyPrice").next()).getText();
        assertTrue("48.81".equals(buyPrice), "Buy price should equal to 48.81");

    }

    private OMElement executeGet(String params) throws Exception {
        URL url = new URL(SERVICE_EPR_PRODUCT + params);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        InputStream in = httpCon.getInputStream();
        String xmlContent = DSSTestUtils.getStringFromInputStream(in);
        in.close();
        return AXIOMUtil.stringToOM(xmlContent);
    }

    private void executeDelete(String params) throws Exception {
        URL url = new URL(SERVICE_EPR_PRODUCT + params);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setRequestMethod("DELETE");
        httpCon.getResponseCode();
    }

    private void executePost(String params) throws Exception {
        URL url = new URL(SERVICE_EPR_PRODUCT);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
        out.write(params);
        out.close();
        httpCon.getInputStream().close();
    }

    private void executePut(String params) throws Exception {
        URL url = new URL(SERVICE_EPR_PRODUCT);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("PUT");
        httpCon.setRequestProperty("Content-Length", String.valueOf(params.length()));
        httpCon.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
        out.write(params);
        out.close();
        httpCon.getInputStream().close();
    }

    @Test(groups = {"wso2.dss"})
    public void testResourcePost() throws Exception {
        log.info("Running ResourcesSampleTestCase#testResourcePost");
        String params = "productCode=" + urlenc("X01_0001") + "&productName="
                + urlenc("Sony PS3") + "&productLine=" + urlenc("Game Consoles")
                + "&quantityInStock=" + urlenc("100") + "&buyPrice=" + urlenc("299.99");
        this.executePost(params);
        OMElement resultEl = this.executeGet("X01_0001");
        String productName = ((OMElement) ((OMElement) resultEl
                .getChildrenWithLocalName("Product").next()).getChildrenWithLocalName(
                "productName").next()).getText();
        String productLine = ((OMElement) ((OMElement) resultEl
                .getChildrenWithLocalName("Product").next()).getChildrenWithLocalName(
                "productLine").next()).getText();
        String quantityInStock = ((OMElement) ((OMElement) resultEl
                .getChildrenWithLocalName("Product").next()).getChildrenWithLocalName(
                "quantityInStock").next()).getText();
        String buyPrice = ((OMElement) ((OMElement) resultEl
                .getChildrenWithLocalName("Product").next()).getChildrenWithLocalName(
                "buyPrice").next()).getText();
        assertTrue("Sony PS3".equals(productName), "product name should be equal to PS3");
        assertTrue("Game Consoles".equals(productLine), "product line should be equal to Game Consoles");
        assertTrue("100".equals(quantityInStock), "Quantity in stock should be equal to 100");
        assertTrue("299.99".equals(buyPrice), "buy price should be equal to 299.99");
    }

    @Test(groups = {"wso2.dss"})
    public void testResourcePut() throws Exception {
        log.info("Running ResourcesSampleTestCase#testResourcePut");
        String params = "productCode=" + urlenc("X01_0002") + "&productName="
                + urlenc("A") + "&productLine=" + urlenc("B")
                + "&quantityInStock=" + urlenc("0") + "&buyPrice=" + urlenc("0.0");
        this.executePost(params);
        params = "productCode=" + urlenc("X01_0002") + "&productName=" + urlenc("XBox 360")
                + "&productLine=" + urlenc("Game Consoles") + "&quantityInStock=" + urlenc("2500")
                + "&buyPrice=" + urlenc("299.99");
        this.executePut(params);
        OMElement resultEl = this.executeGet("X01_0002");
        String productName = ((OMElement) ((OMElement) resultEl
                .getChildrenWithLocalName("Product").next()).getChildrenWithLocalName(
                "productName").next()).getText();
        String productLine = ((OMElement) ((OMElement) resultEl
                .getChildrenWithLocalName("Product").next()).getChildrenWithLocalName(
                "productLine").next()).getText();
        String quantityInStock = ((OMElement) ((OMElement) resultEl
                .getChildrenWithLocalName("Product").next()).getChildrenWithLocalName(
                "quantityInStock").next()).getText();
        String buyPrice = ((OMElement) ((OMElement) resultEl
                .getChildrenWithLocalName("Product").next()).getChildrenWithLocalName(
                "buyPrice").next()).getText();
        assertTrue("XBox 360".equals(productName), "product name should be equal to XBox 360");
        assertTrue("Game Consoles".equals(productLine), "Product line should be equal to Game Consoles");
        assertTrue("2500".equals(quantityInStock), "Quantity in stock should be equal to 2500");
        assertTrue("299.99".equals(buyPrice), " Buy price should be equal to  299.99");
    }

    @Test(groups = {"wso2.dss"})
    public void testResourceDelete() throws Exception {
        log.info("Running ResourcesSampleTestCase#testResourceDelete");
        String params = "productCode=" + urlenc("X01_0003") + "&productName="
                + urlenc("A") + "&productLine=" + urlenc("B")
                + "&quantityInStock=" + urlenc("0") + "&buyPrice=" + urlenc("0.0");
        this.executePost(params);
        OMElement resultEl = this.executeGet("X01_0003");
        String productName = ((OMElement) ((OMElement) resultEl.getChildrenWithLocalName(
                "Product").next()).getChildrenWithLocalName("productName").next()).getText();
        assertTrue("A".equals(productName), "Product name should equal to A");
        params = urlenc("X01_0003");
        this.executeDelete(params);
        resultEl = this.executeGet("X01_0003");
        assertFalse(resultEl.getChildElements().hasNext(), "Result set should not have any elements");

    }

    private String urlenc(String input) throws Exception {
        return URLEncoder.encode(input, "UTF-8");
    }

}
