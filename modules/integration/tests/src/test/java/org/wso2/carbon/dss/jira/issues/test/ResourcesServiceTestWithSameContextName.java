package org.wso2.carbon.dss.jira.issues.test;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.fileutils.FileManager;
import org.wso2.carbon.automation.utils.httpclient.HttpClientUtil;
import org.wso2.carbon.dataservices.samples.rdbms_sample.RDBMSSampleStub;
import org.wso2.carbon.dss.DSSIntegrationTest;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Random;

/**
 * This test is to verify the fix for https://wso2.org/jira/browse/DS-721
 */
public class ResourcesServiceTestWithSameContextName extends DSSIntegrationTest{
    private static final Log log = LogFactory.getLog(ResourcesServiceTestWithSameContextName.class);
    private final String serviceName = "ResourcesServiceTestWithSameContextName";
    private String serviceEndPoint;
    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
        serviceEndPoint = getServiceUrl(serviceName);
        deployService(serviceName,
                new DataHandler(new URL("file:///" + ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts"
                        + File.separator + "DSS" + File.separator + "samples"
                        + File.separator + "dbs" + File.separator
                        + "rdbms" + File.separator + "ResourcesServiceTestWithSameContextName.dbs")));
        serviceEndPoint = getServiceUrl(serviceName);
    }
    @AfterClass(alwaysRun = true)
    public void destroy() throws RemoteException {
        deleteService(serviceName);
        cleanup();
    }
    private void getProduct(String id) throws Exception {

        HttpClientUtil httpClient = new HttpClientUtil();
        OMElement result = httpClient.get(serviceEndPoint +  ".HTTPEndpoint/"+"product"+"/"+id);
        Assert.assertNotNull(result, "Response null");
        System.out.println("Result "+result.toString());
        Assert.assertTrue(result.toString().contains("<productName>product" + id + "</productName>"), "Expected result not found");


    }
    private void getProducts() throws Exception {

        HttpClientUtil httpClient = new HttpClientUtil();
        OMElement result = httpClient.get(serviceEndPoint +".HTTPEndpoint/"+ "product");
        Assert.assertNotNull(result, "Response null");
        for (int i = 1; i < 6; i++) {
            Assert.assertTrue(result.toString().contains("<productName>product" + i + "</productName>"), "Expected result not found");
        }


    }
    private void addProduct()throws  Exception{
        HttpClientUtil httpClient = new HttpClientUtil();
        for (int i = 1; i < 6; i++) {

            String para = "productCode=" + i
                    + "&" + "productName=" + "product" + i
                    + "&" + "productLine=2"
                    + "&" + "quantityInStock=200"
                    + "&" + "buyPrice=10";
            httpClient.post(serviceEndPoint +".HTTPEndpoint/"+ "product", para);

        }
    }
    @Test(groups = {"wso2.dss"})
    public void addRequest() throws Exception {
    addProduct();
    log.info("Verified POST successfully");
    }

    @Test(groups = {"wso2.dss"},dependsOnMethods = {"addRequest"})
    public void getRequest() throws Exception {
         getProduct("1");
        log.info("Verified GET /product/{id} successfully");

    }
    @Test(groups = {"wso2.dss"},dependsOnMethods = {"addRequest"})
    public void getAllRequest() throws Exception {
        getProducts();
        log.info("Verified GET /product successfully");
    }
}
