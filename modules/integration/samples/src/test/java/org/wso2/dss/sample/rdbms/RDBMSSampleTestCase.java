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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.dataservices.samples.rdbms_sample.DataServiceFault;
import org.wso2.carbon.dataservices.samples.rdbms_sample.RDBMSSample;
import org.wso2.carbon.dataservices.samples.rdbms_sample.RDBMSSampleStub;
import org.wso2.dss.sample.DSSTestUtils;
import org.wso2.ws.dataservice.samples.rdbms_sample.Employee;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test for Relational Databases
 */
public class RDBMSSampleTestCase {

    private final String SERVICE_EPR = DSSTestUtils.SERVICE_BASE_EPR + "RDBMSSample";

    private static final Log log = LogFactory.getLog(RDBMSSampleTestCase.class);

    @Test(groups = {"wso2.dss"})
    public void testCreateEmployee() throws DataServiceFault, RemoteException {
        log.info("Running RDBMSSampleTestCase#testCreateEmployee");
        RDBMSSample stub = new RDBMSSampleStub(SERVICE_EPR);
        stub.addEmployee(5001, "Cena", "John", "john@gmail.com", 100000.0);
        Employee employee = stub.employeesByNumber(5001)[0];
        assertTrue("John".equals(employee.getFirstName()), "first name should be John");
        assertTrue("Cena".equals(employee.getLastName()), "Last Name should be Cena");
        assertTrue("john@gmail.com".equals(employee.getEmail()), "Email should be John@gmail.com");
        assertEquals(employee.getSalary(), 100000.0);

    }

    @Test(groups = {"wso2.dss"}, expectedExceptions = DataServiceFault.class)
    public void testCreateEmployeeLastNameValidation() throws RemoteException, DataServiceFault {
        log.info("Running RDBMSSampleTestCase#testCreateEmployeeLastNameValidation");
        RDBMSSample stub = new RDBMSSampleStub(SERVICE_EPR);
        stub.addEmployee(5002, "X", "John", "john@gmail.com", 100000.0);

    }

    @Test(groups = {"wso2.dss"}, expectedExceptions = Exception.class)
    public void testCreateEmployeeEmailValidation() throws DataServiceFault, RemoteException {
        log.info("Running RDBMSSampleTestCase#testCreateEmployeeEmailValidation");
        RDBMSSample stub = new RDBMSSampleStub(SERVICE_EPR);
        stub.addEmployee(5003, "Cena", "John", "john.gmail.com", 100000.0);

    }

    @Test(groups = {"wso2.dss"}, expectedExceptions = Exception.class)
    public void testCreateEmployeePrimaryKeyConstrainCheck() throws DataServiceFault, RemoteException {
        log.info("Running RDBMSSampleTestCase#testCreateEmployeePrimaryKeyConstrainCheck");
        RDBMSSample stub = new RDBMSSampleStub(SERVICE_EPR);
        stub.addEmployee(5004, "Cena", "John", "john@gmail.com", 100000.0);
        stub.addEmployee(5004, "Cena", "John", "john@gmail.com", 100000.0);
    }

    @Test(groups = {"wso2.dss"})
    public void testCreateEmployeeNullValuesCheck() throws DataServiceFault, RemoteException {
        log.info("Running RDBMSSampleTestCase#testCreateEmployeeNullValuesCheck");
        RDBMSSample stub = new RDBMSSampleStub(SERVICE_EPR);
        stub.addEmployee(5005, "Cena", null, "john@gmail.com", 100000.0);
        Employee employee = stub.employeesByNumber(5005)[0];
        assertNull(employee.getFirstName(), "Employee first name should be null");
    }

    @Test(groups = {"wso2.dss"})
    public void testCustomersInBoston() throws DataServiceFault, RemoteException {
        log.info("Running RDBMSSampleTestCase#testCustomersInBoston");
        RDBMSSample stub = new RDBMSSampleStub(SERVICE_EPR);
        assertTrue(stub.customersInBoston().length > 0, "Customers in Boston should be greater than zero");

    }

    @Test(groups = {"wso2.dss"})
    public void testProductsInfo() throws DataServiceFault, RemoteException {
        log.info("Running RDBMSSampleTestCase#testProductsInfo");
        RDBMSSample stub = new RDBMSSampleStub(SERVICE_EPR);
        assertTrue(stub.productsInfo().length > 0, "product info should be greater than zero");
    }

    @Test(groups = {"wso2.dss"})
    public void testSetSalaryForEmployeesArrayTypesRequest() throws DataServiceFault, RemoteException {
        log.info("Running RDBMSSampleTestCase#testSetSalaryForEmployeesArrayTypesRequest");
        RDBMSSample stub = new RDBMSSampleStub(SERVICE_EPR);
        stub.addEmployee(5006, "Smith", "Will", "will@gmail.com", 0);
        stub.addEmployee(5007, "Potter", "Harry", "harry@gmail.com", 0);
        stub.addEmployee(5008, "Bond", "James", "james@gmail.com", 0);
        stub.addEmployee(5009, "Kent", "Clark", "clark@gmail.com", 0);
        stub.setSalaryForEmployees(25500.0, new int[]{5006, 5007, 5008, 5009});
        assertEquals(stub.employeesByNumber(5006)[0].getSalary(), 25500.0, "Employee 5006's salary should equal to " +
                "25500.0");
        assertEquals(stub.employeesByNumber(5007)[0].getSalary(), 25500.0, "Employee 5007's salary should equal to " +
                "25500.0");
        assertEquals(stub.employeesByNumber(5008)[0].getSalary(), 25500.0, "Employee 5008's salary should equal to " +
                "25500.0");
        assertEquals(stub.employeesByNumber(5009)[0].getSalary(), 25500.0, "Employee 5009's salary should equal to " +
                "25500.0");

    }

    @Test(groups = {"wso2.dss"})
    public void testSetSalaryForEmployee() throws DataServiceFault, RemoteException {
        log.info("Running RDBMSSampleTestCase#testSetSalaryForEmployee");
        RDBMSSample stub = new RDBMSSampleStub(SERVICE_EPR);
        stub.addEmployee(5010, "Smith", "Will", "will@gmail.com", 0);
        stub.setEmployeeSalary(14600.0, 5010);
        assertEquals(stub.employeesByNumber(5010)[0].getSalary(), 14600.0, "5010's salary should equal to 14600.0");

    }

    @Test(groups = {"wso2.dss"})
    public void testThousandFive() throws DataServiceFault, RemoteException {
        log.info("Running RDBMSSampleTestCase#testThousandFive");
        RDBMSSample stub = new RDBMSSampleStub(SERVICE_EPR);
        assertEquals(stub.thousandFive()[0].getValue().intValue(), 1500, "Thousand five should equal to 1500");

    }

    @Test(groups = {"wso2.dss"})
    public void testIncrementEmployeeSalary() throws DataServiceFault, RemoteException {
        log.info("Running RDBMSSampleTestCase#testIncrementEmployeeSalary");
        RDBMSSample stub = new RDBMSSampleStub(SERVICE_EPR);
        stub.addEmployee(5011, "Smith", "Will", "will@gmail.com", 1500.0);
        stub.incrementEmployeeSalary(2500.0, 5011);
        assertEquals(stub.employeesByNumber(5011)[0].getSalary(), 4000.0, "Employee 5011's salary should equal to" +
                "4000.0");

    }

    @Test(groups = {"wso2.dss"})
    public void testIncrementEmployeeSalaryExBoxcarringResultExport() throws RemoteException,
            DataServiceFault {
        log.info("Running RDBMSSampleTestCase#testIncrementEmployeeSalaryExBoxcarringResultExport");
        RDBMSSampleStub stub = new RDBMSSampleStub(SERVICE_EPR);
        stub._getServiceClient().getOptions().setManageSession(true);
        stub.begin_boxcar();
        stub.addEmployee(5012, "Smith", "Will", "will@gmail.com", 1500.0);
        stub.thousandFive();
        stub.incrementEmployeeSalaryEx(5012);
        stub.end_boxcar();
        assertEquals(stub.employeesByNumber(5012)[0].getSalary(), 3000.0, "Employee 5012's salary should equal to" +
                "3000.0");
    }

    @Test(groups = {"wso2.dss"})
    public void testIncrementEmployeeSalaryBoxcarring() throws DataServiceFault, RemoteException {
        log.info("Running RDBMSSampleTestCase#testIncrementEmployeeSalaryBoxcarring");
        RDBMSSampleStub stub = new RDBMSSampleStub(SERVICE_EPR);
        stub._getServiceClient().getOptions().setManageSession(true);
        stub.begin_boxcar();
        stub.addEmployee(5013, "Smith", "Will", "will@gmail.com", 1500.0);
        stub.incrementEmployeeSalary(2500.0, 5013);
        stub.incrementEmployeeSalary(200.0, 5013);
        stub.incrementEmployeeSalary(1200.0, 5013);
        stub.end_boxcar();
        assertEquals(stub.employeesByNumber(5013)[0].getSalary(), 5400.0, "Employee 5013's salary should equal to" +
                "5400.0");
    }

    @Test(groups = {"wso2.dss"})
    public void testEmployeeByNumberEndBoxcarringResult() throws DataServiceFault, RemoteException {
        log.info("Running RDBMSSampleTestCase#testEmployeeByNumberEndBoxcarringResult");
        RDBMSSampleStub stub = new RDBMSSampleStub(SERVICE_EPR);
        stub._getServiceClient().getOptions().setManageSession(true);
        stub.begin_boxcar();
        stub.addEmployee(5014, "Smith", "Will", "will@gmail.com", 1500.0);
        stub.employeesByNumber(5014);
        OMElement returnEl = stub.end_boxcar();
        assertTrue(returnEl.toString().contains("Smith"), "Employee name should contain Smith");
    }

    @Test(groups = {"wso2.dss"})
    public void testConnectionLeaksWithValidInOutRequests() throws DataServiceFault, RemoteException {
        log.info("Running RDBMSSampleTestCase#testConnectionLeaksWithValidInOutRequests");
        RDBMSSampleStub stub = new RDBMSSampleStub(SERVICE_EPR);
        for (int i = 0; i < 100; i++) {
            assertTrue(stub.employeesByNumber(1002).length > 0, "No of Employees for 1002 should be greater than" +
                    "zero");
        }
    }

    @Test(groups = {"wso2.dss"})
    public void testConnectionLeaksWithInvalidInOutRequests() throws Exception {

        log.info("Running RDBMSSampleTestCase#testConnectionLeaksWithInvalidInOutRequests");
        String payload = "<p:employeesByNumber xmlns:p=\"http://ws.wso2.org/dataservice/samples/rdbms_sample\">" +
                "<employeeNumber xmlns=\"http://ws.wso2.org/dataservice/samples/rdbms_sample\">" +
                "abc</employeeNumber></p:employeesByNumber>";
        for (int i = 0; i < 100; i++) {
            try {
                this.sendRecieveMessage(payload, SERVICE_EPR, "urn:employeesByNumber");
            } catch (Exception e) {
                if (e.getMessage().contains("java.lang.NumberFormatException")) {
                    continue;
                } else {
                    throw e;
                }
            }
        }
        RDBMSSampleStub stub = new RDBMSSampleStub(SERVICE_EPR);
        assertTrue(stub.employeesByNumber(1002).length > 0, "Employees for 1002 should be greater than zero");

    }

    @Test(groups = {"wso2.dss"})
    public void testConnectionLeaksWithOutRequests() throws DataServiceFault, RemoteException {
        log.info("Running RDBMSSampleTestCase#testConnectionLeaksWithOutRequests");
        RDBMSSampleStub stub = new RDBMSSampleStub(SERVICE_EPR);
        for (int i = 0; i < 100; i++) {
            assertTrue(stub.customersInBoston().length > 0, "Customers in Boston should be greater than zero");
        }
    }

    @Test(groups = {"wso2.dss"})
    public void testConnectionLeaksWithInRequests() throws DataServiceFault, RemoteException {

        log.info("Running RDBMSSampleTestCase#testConnectionLeaksWithInRequests");
        RDBMSSample stub = new RDBMSSampleStub(SERVICE_EPR);
        Employee employee;
        for (int i = 6000; i < 6100; i++) {
            stub.addEmployee(i, "Cena", "John", "john@gmail.com", 100000.0);
            employee = stub.employeesByNumber(i)[0];
            assertTrue("John".equals(employee.getFirstName()), "First name should be John");
            assertTrue("Cena".equals(employee.getLastName()), "Last name should be Cena");
            assertTrue("john@gmail.com".equals(employee.getEmail()), "Email should be john@gmail.com");
            assertTrue(employee.getSalary() == 100000.0, "Salary should be equal to 10000.0");
        }
    }

    private OMElement sendRecieveMessage(String payload, String epr, String soapAction)
            throws Exception {
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference(epr));
        options.setAction(soapAction);
        serviceClient.setOptions(options);
        OMElement omEl = new StAXOMBuilder(new ByteArrayInputStream(
                payload.getBytes())).getDocumentElement();
        return serviceClient.sendReceive(omEl);
    }

}
