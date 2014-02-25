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
import org.wso2.carbon.dataservices.samples.file_service.DataServiceFault;
import org.wso2.carbon.dataservices.samples.file_service.FileService;
import org.wso2.carbon.dataservices.samples.file_service.FileServiceStub;
import org.wso2.dss.sample.DSSTestUtils;
import org.wso2.ws.dataservice.samples.file_service.file_names.File6;
import org.wso2.ws.dataservice.samples.file_service.file_records.File3;

import javax.activation.DataHandler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 * Test for File Services
 */
public class FileServiceTestCase {

	private final String SERVICE_EPR = DSSTestUtils.SERVICE_BASE_EPR + "FileService";

    private static final Log log = LogFactory.getLog(FileServiceTestCase.class);

    @Test(groups = {"wso2.dss"})
	public void testAddFile() throws DataServiceFault, RemoteException {

			log.info("Running FileServiceTestCase#testAddFile");
			FileService stub = new FileServiceStub(SERVICE_EPR);
		    stub._getcreatenewfile("file1.txt", "txt");
		    assertEquals(stub._getcheckfileexists("file1.txt")[0].getFileExists().intValue(), 1);

	}



    @Test(groups = {"wso2.dss"})
	public void testDeleteFile() throws DataServiceFault, RemoteException {

			log.info("Running FileServiceTestCase#testDeleteFile");
		    FileService stub = new FileServiceStub(SERVICE_EPR);
		    stub._getcreatenewfile("file2.txt", "txt");
		    assertEquals(stub._getcheckfileexists("file2.txt")[0].getFileExists().intValue(), 1);
		    stub._getdeletefile("file2.txt");
		    assertEquals(stub._getcheckfileexists("file2.txt")[0].getFileExists().intValue(), 0);

	}

    @Test(groups = {"wso2.dss"})
	public void testGetFileNames() throws DataServiceFault, RemoteException {

			log.info("Running FileServiceTestCase#testGetFileNames");
			FileService stub = new FileServiceStub(SERVICE_EPR);
		    stub._getcreatenewfile("file3.txt", "txt");
		    stub._getcreatenewfile("file4.txt", "txt");
		    boolean check1 = false, check2 = false;
		    for (File6 file : stub._getgetfilenames()) {
		    	if ("file3.txt".equals(file.getFileName())) {
		    		check1 = true;
		    	} else if ("file4.txt".equals(file.getFileName())) {
		    		check2 = true;
		    	} 
		    }
		    assertTrue(check1);
		    assertTrue(check2);

	}

    @Test(groups = {"wso2.dss"})
	public void testGetFileType() throws DataServiceFault, RemoteException {

			log.info("Running FileServiceTestCase#testGetFileType");
			FileService stub = new FileServiceStub(SERVICE_EPR);
		    stub._getcreatenewfile("file5.avi", "avi");
		    String type = stub._getgetfiletype("file5.avi")[0].getType();
		    assertTrue("avi".equals(type), "File should be type avi");

	}

    @Test(groups = {"wso2.dss"})
	public void testGetFileSize() throws DataServiceFault, RemoteException {

			log.info("Running FileServiceTestCase#testGetFileSize");
			FileService stub = new FileServiceStub(SERVICE_EPR);
		    stub._getcreatenewfile("file6.txt", "txt");
		    BigInteger value = stub._getgetfilesize("file6.txt")[0].getFileSize();
		    assertTrue(value == null || value.intValue() == 0);
		    DataHandler dh = new StringDataHandler("record1");
		    stub._postappenddatatofile("file6.txt", dh);
		    dh = new StringDataHandler("record2");
		    stub._postappenddatatofile("file6.txt", dh);
		    assertEquals(((stub._getgetfilesize("file6.txt")[0]).getFileSize()).intValue(), 14);

	}

    @Test(groups = {"wso2.dss"})
	public void testAppendRecords() throws DataServiceFault, IOException {

			log.info("Running FileServiceTestCase#testAppendRecords");
			FileService stub = new FileServiceStub(SERVICE_EPR);
		    stub._getcreatenewfile("file7.txt", "txt");
		    String srcData1 = "ABCD1234!@#$%_+_()<>?|[]XXXXXXXXXXXXXXXXXYYYYYYYYYYYYAB";
		    String srcData2 = "IOFIJW)($$$9999999999";
		    String srcData3 = "00009895409583[][l}{L:@$#@#@IIIJOOOG";
		    DataHandler dh = new StringDataHandler(srcData1);
		    stub._postappenddatatofile("file7.txt", dh);
		    dh = new StringDataHandler(srcData2);
		    stub._postappenddatatofile("file7.txt", dh);
		    dh = new StringDataHandler(srcData3);
		    stub._postappenddatatofile("file7.txt", dh);
		    StringBuffer resultData = new StringBuffer();
		    for (File3 file : stub._getgetfilerecords("file7.txt")) {
		    	resultData.append(DSSTestUtils.getStringFromInputStream(file.getRecord().getInputStream()));
		    }
		    assertTrue((srcData1 + srcData2 + srcData3).equals(resultData.toString()));

	}

	private class StringDataHandler extends DataHandler {
		
		public StringDataHandler(String value) {
			super(value, "text/plain");
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(((String) this.getContent()).getBytes());
		}
		
	}
	
}
