package org.wso2.carbon.dataservices.extension;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.auth.AuthorizationRoleRetriever;

import java.util.Map;

/**
 * This extension will return hardcoded role arrays for testing.
 */
public class RoleRetriever extends AuthorizationRoleRetriever {
    private static final Log log = LogFactory.getLog(RoleRetriever.class);


    @Override
    public String[] getRolesForUser(MessageContext messageContext) throws DataServiceFault {
        log.info("External role retriever invoked returning roles");
        String[] roleArray = {"admin","sampleRole1","sampleRole2"};
        return roleArray;
    }

    @Override
    public String[] getAllRoles() throws DataServiceFault {
        log.info("External role retriever invoked for get all roles");
        String[] roleArray = {"sampleRole1","sampleRole2","sampleRole3"};
        return roleArray;
    }

    @Override
    public String getUsernameFromMessageContext(MessageContext msgContext) {
        log.info("External role retriever invoked for get username");
        return "admin";
    }

    @Override
    public void setProperties(Map<String, String> stringStringMap) {

    }

    @Override
    public void init() throws DataServiceFault {

    }


}

