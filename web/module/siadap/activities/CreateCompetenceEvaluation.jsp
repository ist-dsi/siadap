<%@page import="module.siadap.activities.CreateOrEditCompetenceEvaluationActivityInformation"%>
<%@page import="myorg.util.BundleUtil"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<bean:define id="processId" name="process" property="externalId"  type="java.lang.String"/>
<bean:define id="name" name="information" property="activityName"/>
<%-- TODO Assert why do everybody has this hidden bean here?!  
<fr:edit id="activityBean" name="information" visible="false" />--%>

<% CreateOrEditCompetenceEvaluationActivityInformation requestActivityInfo = (CreateOrEditCompetenceEvaluationActivityInformation) request.getAttribute("information");
    requestActivityInfo.setInputDisplayed(true); 
    %>
<%-- The id of the form has to be activityBean so that the activityDefaultPostback invalidates the viewstate of it in order to have a consistent postback behaviour happening --%>
<fr:edit id="activityBean" name="information" schema="activityInformation.CreateCompetenceEvaluation" action='<%="/workflowProcessManagement.do?method=process&activity=" + name + "&processId=" + processId%>'>
	<fr:destination name="postback" path='<%= "/workflowProcessManagement.do?method=activityDefaultPostback&processId=" + processId%>'/>
	
<%-- 
	<fr:destination name="postback" path='non-existant'/>
	--%>
</fr:edit>