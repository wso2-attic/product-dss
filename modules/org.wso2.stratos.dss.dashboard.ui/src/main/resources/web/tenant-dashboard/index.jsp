<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page contentType="text/html;charset=UTF-8" language="java"
	pageEncoding="UTF-8"%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<jsp:include page="../dialog/display_messages.jsp" />

<link href="../tenant-dashboard/css/dashboard-common.css"
	rel="stylesheet" type="text/css" media="all" />
<%
	Object param = session.getAttribute("authenticated");
	String passwordExpires = (String) session
			.getAttribute(ServerConstants.PASSWORD_EXPIRATION);
	boolean hasPermission = CarbonUIUtil.isUserAuthorized(request,
			"/permission/admin/configure/dataservices");
	boolean loggedIn = false;
	if (param != null) {
		loggedIn = (Boolean) param;
	}
%>


<div id="passwordExpire">
	<%
		if (loggedIn && passwordExpires != null) {
	%>
	<div class="info-box">
		<p>
			Your password expires at
			<%=passwordExpires%>. Please change by visiting <a
				href="../user/change-passwd.jsp?isUserChange=true&returnPath=../admin/index.jsp">here</a>
		</p>
	</div>
	<%
		}
	%>
</div>
<div id="middle">
	<div id="workArea">

		<style type="text/css">
.tip-table td.service-creation {
	background-image:
		url(../../carbon/tenant-dashboard/images/service-creation.png);
}

.tip-table td.service-hosting {
	background-image:
		url(../../carbon/tenant-dashboard/images/service-hosting.png);
}

.tip-table td.service-testing {
	background-image:
		url(../../carbon/tenant-dashboard/images/service-testing.png);
}

.tip-table td.message-tracing {
	background-image:
		url(../../carbon/tenant-dashboard/images/message-tracing.png);
}

.tip-table td.data-service-generate {
	background-image:
		url(../../carbon/tenant-dashboard/images/data-service-generate.png);
}

.tip-table td.wsdl2java {
	background-image:
		url(../../carbon/tenant-dashboard/images/wsdl2java.png);
}

.tip-table td.java2wsdl {
	background-image:
		url(../../carbon/tenant-dashboard/images/java2wsdl.png);
}
</style>
		<h2 class="dashboard-title">WSO2 Data Services quick start
			dashboard</h2>
		<table class="tip-table">
			<tr>
				<td class="tip-top service-creation"></td>
				<td class="tip-empty"></td>
				<td class="tip-top service-hosting"></td>
				<td class="tip-empty "></td>
				<td class="tip-top service-testing"></td>
				<td class="tip-empty "></td>
				<td class="tip-top message-tracing"></td>
			</tr>
			<tr>
				<td class="tip-content">
					<div class="tip-content-lifter">
						<%
							if (hasPermission) {
						%>
						<a class="tip-title"
							href="../ds/serviceDetails.jsp?region=region1&item=ds_create_menu">Service
							Creation Wizard</a> <br />
						<%
							} else {
						%>
						<h3>Service Creation Wizard </h3><br />

						<%
							}
						%>
						<p>Wizard based data service creation.</p>

					</div></td>
				<td class="tip-empty"></td>
				<td class="tip-content">
					<div class="tip-content-lifter">
						<%
							if (hasPermission) {
						%>
						<a class="tip-title"
							href="../service-mgt/index.jsp?region=region1&item=services_list_menu">Service
							Hosting</a> <br />
						<%
							} else {
						%>

						<h3>Service Hosting</h3> <br />
						<%
							}
						%>
						<p>Service Hosting features in Data Service Server enables
							deployment of different types of Web Services.</p>
					</div></td>
				<td class="tip-empty"></td>
				<td class="tip-content">
					<div class="tip-content-lifter">
						<%
							if (hasPermission) {
						%>
						<a class="tip-title"
							href="../tryit/index.jsp?region=region5&item=tryit">Service
							Testing</a> <br />
						<%
							} else {
						%>
						<h3>Service Testing</h3><br />
						<%
							}
						%>
						<p>Tryit tool can be used as a simple Web Service client which
							can be used to try your services within Data Service Server
							itself.</p>


					</div></td>
				<td class="tip-empty"></td>
				<td class="tip-content">
					<div class="tip-content-lifter">
					    	<%
							if (hasPermission) {
						%>
						<a class="tip-title"
							href="../tracer/index.jsp?region=region4&item=tracer_menu">Message
							Tracing</a> <br />
					   <%
							} else {
						%>
					  	<h3>	 Message Tracing </h3><br />
						<%
							}
						%>
						<p>Trace the request and responses to your service. Message
							Tracing is a vital debugging tool when you have clients from
							heterogeneous platforms.</p>

					</div></td>
			</tr>
			<tr>
				<td class="tip-bottom"></td>
				<td class="tip-empty"></td>
				<td class="tip-bottom"></td>
				<td class="tip-empty"></td>
				<td class="tip-bottom"></td>
				<td class="tip-empty"></td>
				<td class="tip-bottom"></td>
			</tr>
		</table>
		<div class="tip-table-div"></div>
		<table class="tip-table">
			<tr>
				<td class="tip-top data-service-generate"></td>
				<td class="tip-empty "></td>
				<td class="tip-top wsdl2java"></td>
				<td class="tip-empty "></td>
				<td class="tip-top java2wsdl"></td>
				<td class="tip-empty "></td>
				<td class="tip-empty"></td>
			</tr>
			<tr>
				<td class="tip-content">
					<div class="tip-content-lifter">
						<%
							if (hasPermission) {
						%>
						<a class="tip-title"
							href="../ds/scriptAddSource.jsp?region=region1&item=ds_generate_menu">Data
							Service Generate</a><br />
							 <%
							} else {
						%>
					 	    <h3>Data Service Generate</h3><br />
							<%
							}
						%>
						<p>Data Services Server provides the feature to create data
							services automatically using a given database structure.</p>


					</div></td>
				<td class="tip-empty"></td>
				<td class="tip-content">
					<div class="tip-content-lifter">
						<%
							if (hasPermission) {
						%>	
						<a class="tip-title"
							href="../wsdl2code/index.jsp?region=region5&item=wsdl2java_menu">WSDL2Java
							Tool</a> <br />
 						<%
							} else {
						%>
						<h3>WSDL2Java
							Tool</h3><br />
								<%
							}
						%>
						<p>Use WSDL2Java tool in Web Application Server to convert Web
							Service WSDL to a set of Java objects.</p>

					</div></td>
				<td class="tip-empty"></td>
				<td class="tip-content">
					<div class="tip-content-lifter">
						<%
							if (hasPermission) {
						%>	
						<a class="tip-title"
							href="../java2wsdl/index.jsp?region=region5&item=java2wsdl_menu">Java2WSDL
							Tool</a><br />
						<%
							} else {
						%>
						<h3>Java2WSDL
							Tool</h3><br />
						<%
							}
						%>
						<p>Use Java2WSDL tool in Web Application Server make it easy
							to develop a new web service.</p>

					</div></td>
			</tr>
			<tr>
				<td class="tip-bottom"></td>
				<td class="tip-empty"></td>
				<td class="tip-bottom"></td>
				<td class="tip-empty"></td>
				<td class="tip-bottom"></td>
				<td class="tip-empty"></td>
				<td class="tip-bottom"></td>
			</tr>
		</table>
		<p>
			<br />
		</p>
	</div>
</div>
