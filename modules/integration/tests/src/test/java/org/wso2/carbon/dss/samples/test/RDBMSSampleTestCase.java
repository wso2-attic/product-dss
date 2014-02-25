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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.dataservices.samples.rdbms_sample.DataServiceFault;
import org.wso2.carbon.dataservices.samples.rdbms_sample.RDBMSSampleStub;
import org.wso2.carbon.dss.DSSIntegrationTest;
import org.wso2.ws.dataservice.samples.rdbms_sample.Customer;
import org.wso2.ws.dataservice.samples.rdbms_sample.Employee;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Random;

public class RDBMSSampleTestCase extends DSSIntegrationTest {
    private static final Log log = LogFactory.getLog(RDBMSSampleTestCase.class);

    private final String serviceName = "RDBMSSample";
    private RDBMSSampleStub stub;
    private int randomNumber;

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {
        super.init();
        String serviceEndPoint = getServiceUrl(serviceName);
        stub = new RDBMSSampleStub(serviceEndPoint);
        deployService(serviceName,
                      new DataHandler(new URL("file:///" + ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts"
                                              + File.separator + "DSS" + File.separator + "samples"
                                              + File.separator + "dbs" + File.separator
                                              + "rdbms" + File.separator + "RDBMSSample.dbs")));
        randomNumber = new Random().nextInt(1000);
    }


    @Test(groups = {"wso2.dss"})
    public void selectOperation() throws RemoteException, DataServiceFault {
        for (int i = 0; i < 5; i++) {
            Customer[] customers = stub.customersInBoston();
            for (Customer customer : customers) {
                Assert.assertEquals(customer.getCity(), "Boston", "City mismatched");
            }
        }
        log.info("Select Operation Success");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = "selectOperation")
    public void insertOperation() throws RemoteException, DataServiceFault {
        for (int i = 0; i < 5; i++) {
            stub.addEmployee(randomNumber + i, "FirstName", "LastName", "testmail@test.com", 50000.00);
        }
        log.info("Insert Operation Success");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = {"insertOperation"})
    public void selectByNumber() throws RemoteException, DataServiceFault {
        for (int i = 0; i < 5; i++) {
            Employee[] employees = stub.employeesByNumber(randomNumber + i);
            Assert.assertNotNull(employees, "Employee not found");
            Assert.assertEquals(employees.length, 1, "Employee count mismatched for given emp number");
        }
        log.info("Select operation with parameter success");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = {"selectByNumber"})
    public void updateOperation() throws RemoteException, DataServiceFault {
        for (int i = 0; i < 5; i++) {
            stub.incrementEmployeeSalary(20000.00, randomNumber + i);
            Employee[] employees = stub.employeesByNumber(randomNumber + i);
            Assert.assertNotNull(employees, "Employee not found");
            Assert.assertEquals(employees.length, 1, "Employee count mismatched for given emp number");
            Assert.assertEquals(employees[0].getSalary(), 70000.00, "Salary Increment not set");
        }
        log.info("Update Operation success");
    }

    @Test(groups = {"wso2.dss"}, dependsOnMethods = {"updateOperation"}, enabled = false)
    public void deleteOperation() throws RemoteException, DataServiceFault {
        for (int i = 0; i < 5; i++) {
            stub.begin_boxcar();
            stub.thousandFive();
            stub.incrementEmployeeSalaryEx(randomNumber + i);
            stub.end_boxcar();
            Employee[] employees = stub.employeesByNumber(randomNumber + i);
            Assert.assertNotNull(employees, "Employee not found");
            Assert.assertEquals(employees.length, 1, "Employee count mismatched for given emp number");
            Assert.assertEquals(employees[0].getSalary(), 71500.00, "Salary Increment not setby boxcaring");
        }
        log.info("Delete operation success");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws RemoteException {
        deleteService(serviceName);
        cleanup();
        stub = null;
    }
}
