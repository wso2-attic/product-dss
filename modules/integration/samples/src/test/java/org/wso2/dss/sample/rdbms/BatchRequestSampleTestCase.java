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
import org.wso2.carbon.dataservices.samples.batch_request_sample.BatchRequestSample;
import org.wso2.carbon.dataservices.samples.batch_request_sample.BatchRequestSampleStub;
import org.wso2.carbon.dataservices.samples.batch_request_sample.DataServiceFault;
import org.wso2.dss.sample.DSSTestUtils;
import org.wso2.ws.dataservice.samples.batch_request_sample.AddEmployee;
import org.wso2.ws.dataservice.samples.batch_request_sample.AddEmployee_batch_req;
import org.wso2.ws.dataservice.samples.batch_request_sample.AddEmployee_type0;
import org.wso2.ws.dataservice.samples.batch_request_sample.DeleteEmployee;
import org.wso2.ws.dataservice.samples.batch_request_sample.DeleteEmployee_batch_req;
import org.wso2.ws.dataservice.samples.batch_request_sample.DeleteEmployee_type0;
import org.wso2.ws.dataservice.samples.batch_request_sample.EmployeeExists;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test for Batch Requests
 */
public class BatchRequestSampleTestCase {

    private final String SERVICE_EPR = DSSTestUtils.SERVICE_BASE_EPR + "BatchRequestSample";

    private static final Log log = LogFactory.getLog(DSSTestUtils.class);

    @Test(groups = {"wso2.dss"})
    public void testAddEmployee() throws Exception {
        log.info("Running BatchRequestSampleTestCase#testAddEmployee");
        BatchRequestSample stub = new BatchRequestSampleStub(SERVICE_EPR);
        this.addEmployee(stub, 13001, "tom@gmail.com");
        assert this.employeeExists(stub, 13001) : "Employee Does not exist";

    }

    private boolean employeeExists(BatchRequestSample stub, int id) throws Exception {
        EmployeeExists employeeExists = new EmployeeExists();
        employeeExists.setEmployeeNumber(id);
        return "1".equals(stub.employeeExists(employeeExists).
                getEmployees().getEmployee()[0].getExists());
    }

    private void addEmployee(BatchRequestSample stub, int id, String email) throws Exception {
        AddEmployee employee = new AddEmployee();
        AddEmployee_type0 employeeObj = new AddEmployee_type0();
        employeeObj.setEmail(email);
        employeeObj.setEmployeeNumber(id);
        employee.setAddEmployee(employeeObj);
        stub.addEmployee(employee);
    }

    private void deleteEmployee(BatchRequestSample stub, int id) throws Exception {
        DeleteEmployee deleteEmployee = new DeleteEmployee();
        DeleteEmployee_type0 deleteEmployeeObj = new DeleteEmployee_type0();
        deleteEmployeeObj.setEmployeeNumber(id);
        deleteEmployee.setDeleteEmployee(deleteEmployeeObj);
        stub.deleteEmployee(deleteEmployee);
    }

    @Test(groups = {"wso2.dss"})
    public void testEmployeeExists() throws Exception {
        log.info("Running BatchRequestSampleTestCase#testEmployeeExists");
        BatchRequestSample stub = new BatchRequestSampleStub(SERVICE_EPR);
        this.addEmployee(stub, 13002, "abc@gmail.com");
        assertFalse(this.employeeExists(stub, 14001), "Employee cannot exist for No. 14001");
        assertTrue(this.employeeExists(stub, 13002), "Employee should exist for No. 13002");
    }

    @Test(groups = {"wso2.dss"})
    public void testDeleteEmployee() throws Exception {
        log.info("Running BatchRequestSampleTestCase#testDeleteEmployee");
        BatchRequestSample stub = new BatchRequestSampleStub(SERVICE_EPR);
        this.addEmployee(stub, 13003, "abc@gmail.com");
        assertTrue(this.employeeExists(stub, 13003), "Employee should exist for No. 13003");
        this.deleteEmployee(stub, 13003);
        assertFalse(this.employeeExists(stub, 13003), "Employee should not exist for deleted No. 13003");

    }

    @Test(groups = {"wso2.dss"})
    public void testAddDeleteEmployeeBatchRequest() throws Exception {
        log.info("Running BatchRequestSampleTestCase#testAddEmployeeBatchRequest");
        BatchRequestSample stub = new BatchRequestSampleStub(SERVICE_EPR);

        AddEmployee_type0 employeeObj1 = new AddEmployee_type0();
        employeeObj1.setEmail("1@gmail.com");
        employeeObj1.setEmployeeNumber(13004);

        AddEmployee_type0 employeeObj2 = new AddEmployee_type0();
        employeeObj2.setEmail("2@gmail.com");
        employeeObj2.setEmployeeNumber(13005);

        AddEmployee_type0 employeeObj3 = new AddEmployee_type0();
        employeeObj3.setEmail("3@gmail.com");
        employeeObj3.setEmployeeNumber(13006);

        AddEmployee_batch_req batchReq = new AddEmployee_batch_req();
        batchReq.addAddEmployee(employeeObj1);
        batchReq.addAddEmployee(employeeObj2);
        batchReq.addAddEmployee(employeeObj3);

        stub.addEmployee_batch_req(batchReq);

        assertTrue(this.employeeExists(stub, 13004), "Employee should exist for No. 13004");
        assertTrue(this.employeeExists(stub, 13005), "Employee should exist for No. 13005");
        assertTrue(this.employeeExists(stub, 13006), "Employee should exist for No. 13006");

        DeleteEmployee_type0 delEmplObj1 = new DeleteEmployee_type0();
        delEmplObj1.setEmployeeNumber(13004);

        DeleteEmployee_type0 delEmplObj2 = new DeleteEmployee_type0();
        delEmplObj2.setEmployeeNumber(13005);

        DeleteEmployee_type0 delEmplObj3 = new DeleteEmployee_type0();
        delEmplObj3.setEmployeeNumber(13006);

        DeleteEmployee_batch_req delBatchReq = new DeleteEmployee_batch_req();
        delBatchReq.addDeleteEmployee(delEmplObj1);
        delBatchReq.addDeleteEmployee(delEmplObj2);
        delBatchReq.addDeleteEmployee(delEmplObj3);

        stub.deleteEmployee_batch_req(delBatchReq);

        assertFalse(this.employeeExists(stub, 13004), "Employee should not exist for deleted No. 13004");
        assertFalse(this.employeeExists(stub, 13005), "Employee should not exist for deleted No. 13005");
        assertFalse(this.employeeExists(stub, 13006), "Employee should not exist for deleted No. 13006");

    }

    @Test(groups = {"wso2.dss"}, expectedExceptions = DataServiceFault.class)
    public void testAddEmployeeBatchRequestTransactionFail() throws Exception {
    	BatchRequestSample stub;

        log.info("Running BatchRequestSampleTestCase#testAddEmployeeBatchRequestTransactionFail");
        stub = new BatchRequestSampleStub(SERVICE_EPR);

        AddEmployee_type0 employeeObj1 = new AddEmployee_type0();
        employeeObj1.setEmail("1@gmail.com");
        employeeObj1.setEmployeeNumber(13007);

        AddEmployee_type0 employeeObj2 = new AddEmployee_type0();
        employeeObj2.setEmail("2.gmail.com");
        employeeObj2.setEmployeeNumber(13008);

        AddEmployee_type0 employeeObj3 = new AddEmployee_type0();
        employeeObj3.setEmail("3@gmail.com");
        employeeObj3.setEmployeeNumber(13009);

        AddEmployee_batch_req batchReq = new AddEmployee_batch_req();
        batchReq.addAddEmployee(employeeObj1);
        batchReq.addAddEmployee(employeeObj2);
        batchReq.addAddEmployee(employeeObj3);

        stub.addEmployee_batch_req(batchReq);


        assertFalse(this.employeeExists(stub, 13007), "Employee should not exist for 13007 after failed transaction");
        assertFalse(this.employeeExists(stub, 13008), "Employee should not exist for 13008 after failed transaction");
        assertFalse(this.employeeExists(stub, 13009), "Employee should not exist for 13009 after failed transaction");

    }

}
