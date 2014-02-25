/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/


package org.wso2.dss.sample;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * DSS Test Utilities
 */
public final class DSSTestUtils {

    public static final String SERVICE_BASE_EPR = "http://localhost:9763/services/samples/";
    
    public static final String DATA_SERVICE_RESPONSE_WRAPPER_ELEMENT = "DATA_SERVICE_RESPONSE";

    public static String getStringFromInputStream(InputStream in) throws IOException {
        InputStreamReader reader = new InputStreamReader(in);
        char[] buff = new char[1024];
        int i;
        StringBuffer retValue = new StringBuffer();
        while ((i = reader.read(buff)) > 0) {
            retValue.append(new String(buff, 0, i));
        }
        return retValue.toString();
    }

}
