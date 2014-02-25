/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.dss.samples.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.dataservices.samples.batch_request_sample.BatchRequestSampleStub;
import org.wso2.carbon.dataservices.samples.batch_request_sample.DataServiceFault;
import org.wso2.carbon.dss.DSSIntegrationTest;
import org.wso2.ws.dataservice.samples.batch_request_sample.AddEmployee;
import org.wso2.ws.dataservice.samples.batch_request_sample.AddEmployee_batch_req;
import org.wso2.ws.dataservice.samples.batch_request_sample.AddEmployee_type0;
import org.wso2.ws.dataservice.samples.batch_request_sample.DeleteEmployee;
import org.wso2.ws.dataservice.samples.batch_request_sample.DeleteEmployee_batch_req;
import org.wso2.ws.dataservice.samples.batch_request_sample.DeleteEmployee_type0;
import org.wso2.ws.dataservice.samples.batch_request_sample.EmployeeExists;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotSame;


public class BatchRequestSampleTestCase extends DSSIntegrationTest {
    private static String serviceName = "BatchRequestSample";
    private static String serverEpr;

    private static final Log log = LogFactory.getLog(CSVSampleTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init();
        String resourceFileLocation = null;
        serverEpr = dssServer.getBackEndUrl() + serviceName;
        resourceFileLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                               "DSS";
        deployService(serviceName,
                      new DataHandler(new URL("file:///" + resourceFileLocation +
                                              File.separator + "samples" + File.separator +
                                              "dbs" + File.separator + "rdbms" + File.separator +
                                              "BatchRequestSample.dbs")));
        log.info(serviceName + " uploaded");
    }

    @Test(groups = "wso2.dss", description = "Check whether fault service deployed or not")
    public void testServiceDeployment() throws RemoteException {
        assertTrue(isServiceDeployed(serviceName));
        log.info(serviceName + " is deployed");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = "testServiceDeployment")
    public void testAddEmployee() throws RemoteException, DataServiceFault, InterruptedException {
        BatchRequestSampleStub stub = new BatchRequestSampleStub(serverEpr);
        AddEmployee addEmployee = new AddEmployee();
        AddEmployee_type0 type0 = new AddEmployee_type0();

        type0.setEmployeeNumber(20);
        type0.setEmail("wso2.test.automation@wso2.org");

        addEmployee.setAddEmployee(type0);
        stub.addEmployee(addEmployee);

        Thread.sleep(1000);
        EmployeeExists employeeExists = new EmployeeExists();

        employeeExists.setEmployeeNumber(20);
        assertEquals("1", stub.employeeExists(employeeExists).getEmployees().getEmployee()[0].getExists());

        DeleteEmployee deleteEmployee = new DeleteEmployee();
        DeleteEmployee_type0 deleteEmployee_type0 = new DeleteEmployee_type0();
        deleteEmployee_type0.setEmployeeNumber(20);
        deleteEmployee.setDeleteEmployee(deleteEmployee_type0);
        stub.deleteEmployee(deleteEmployee);

        assertNotSame("Employee was not deleted", stub.employeeExists(employeeExists).getEmployees().getEmployee()[0].getExists(), 1);
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = "testAddEmployee")
    public void testAddEmployee_batch() throws RemoteException, DataServiceFault {
        BatchRequestSampleStub stub = new BatchRequestSampleStub(serverEpr);
        AddEmployee_batch_req addEmployee_batch_req = new AddEmployee_batch_req();

        AddEmployee_type0 type0 = new AddEmployee_type0();
        type0.setEmployeeNumber(15);
        type0.setEmail("wso2.test.automation1@wso2.org");

        AddEmployee_type0 type1 = new AddEmployee_type0();
        type1.setEmployeeNumber(16);
        type1.setEmail("wso2.test.automation2@wso2.org");

        addEmployee_batch_req.addAddEmployee(type0);
        addEmployee_batch_req.addAddEmployee(type1);
        stub.addEmployee_batch_req(addEmployee_batch_req);

        EmployeeExists employeeExists = new EmployeeExists();

        employeeExists.setEmployeeNumber(15);
        assertEquals(stub.employeeExists(employeeExists).getEmployees().getEmployee()[0].getExists(), "1", "Employee not found");

        employeeExists.setEmployeeNumber(16);
        assertEquals(stub.employeeExists(employeeExists).getEmployees().getEmployee()[0].getExists(), "1", "Employee not found");

    }


    @Test(groups = {"wso2.dss"}, dependsOnMethods = "testAddEmployee_batch")
    public void testDeleteEmployee_batch() throws RemoteException, DataServiceFault {
        BatchRequestSampleStub stub = new BatchRequestSampleStub(serverEpr);
        DeleteEmployee_batch_req deleteEmployee_batch_req = new DeleteEmployee_batch_req();

        DeleteEmployee_type0 deleteEmployee_type0 = new DeleteEmployee_type0();
        deleteEmployee_type0.setEmployeeNumber(15);

        DeleteEmployee_type0 deleteEmployee_type1 = new DeleteEmployee_type0();
        deleteEmployee_type1.setEmployeeNumber(16);

        deleteEmployee_batch_req.addDeleteEmployee(deleteEmployee_type0);
        deleteEmployee_batch_req.addDeleteEmployee(deleteEmployee_type1);
        stub.deleteEmployee_batch_req(deleteEmployee_batch_req);

        EmployeeExists employeeExists = new EmployeeExists();
        employeeExists.setEmployeeNumber(15);
        assertNotSame("Employee was not deleted", stub.employeeExists(employeeExists).getEmployees().getEmployee()[0].getExists(), "1");

        employeeExists.setEmployeeNumber(16);
        assertNotSame("Employee was not deleted", stub.employeeExists(employeeExists).getEmployees().getEmployee()[0].getExists(), "1");
    }


    @AfterClass(alwaysRun = true, groups = "wso2.dss", description = "delete service")
    public void deleteFaultyService() throws RemoteException {
        deleteService(serviceName);
        cleanup();
    }

}
