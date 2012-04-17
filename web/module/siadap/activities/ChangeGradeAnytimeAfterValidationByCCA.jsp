<%@page import="module.siadap.activities.ChangeGradeAnytimeActivityInformation"%>
<%@page import="module.siadap.activities.HomologationActivityInformation"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<%
String mode = request.getParameter("mode");
if (mode == null || !mode.equalsIgnoreCase("homologation"))
{
    
    ChangeGradeAnytimeActivityInformation changeGradeAI = (ChangeGradeAnytimeActivityInformation) request.getAttribute("information");
    request.setAttribute("informationToBeUsed", changeGradeAI);
%>
<%-- 
<bean:define id="processId" name="process" property="externalId" type="java.lang.String" />
<bean:define id="name" name="information" property="activityName" />
--%>

<%
} else {
    request.setAttribute("homologationMode", true);
    HomologationActivityInformation homologationActivityInformation = (HomologationActivityInformation) request.getAttribute("information");
    request.setAttribute("informationToBeUsed", homologationActivityInformation.getChangeGradeAnytimeActivityInformation());
%>
<script src="<%= request.getContextPath() + "/javaScript/alertHandlers.js"%>" type="text/javascript"></script>
<script src="<%= request.getContextPath() + "/javaScript/jquery.alerts.js"%>" type="text/javascript"></script>
<script>
$(document).ready(function (){
	$('input[type="submit"][value="Homologar"]').click(function (){
		requestConfirmationForJQueryForm($('form[action*="Homologate"]'),'<bean:message bundle="SIADAP_RESOURCES" key="activity.confirmation.module.siadap.activities.Homologate"/>','<bean:message bundle="SIADAP_RESOURCES" key="label.confirm"/>');
		return false;
	});
	
});
</script>
<%-- 
<bean:parameter id="processId" name="processId"/>
<bean:parameter id="name" name="activityName" />

<bean:define id="informationToBeUsed" name="information" property="changeGradeAnytimeActivityInformation"/>
--%>
<%
} 
%>
<bean:define id="processId" name="process" property="externalId" type="java.lang.String" />
<bean:define id="name" name="information" property="activityName" />

<logic:present name="homologationMode" >
	<h3><bean:message bundle="SIADAP_RESOURCES" key="activity.ChangeGradeAnytimeAfterValidationByCCA"/></h3>
	<h4><bean:message bundle="SIADAP_RESOURCES" key="activity.ChangeGradeAnytimeAfterValidationByCCA.homologation.explanation"/></h4>
</logic:present>

<div class="dinline forminline">
<fr:form id="form" action='<%="/workflowProcessManagement.do?method=process&processId=" + processId + "&activity=" + name%>'>
<fr:edit id="activityBean" name="information" visible="false" />
<logic:iterate id="siadapEvaluationUniverseBean" name="informationToBeUsed" property="siadapEvaluationUniversesBeans" >
	<fr:edit name="siadapEvaluationUniverseBean">
		<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.activities.ChangeGradeAnytimeActivityInformation$GradePerUniverseBean">
			<fr:slot name="siadapEvaluationUniverse.siadapUniverse" key="label.change.grade.siadapEvaluationUniverse.siadapUniverse"  readOnly="true"/>
			<fr:slot name="siadapEvaluationUniverse.currentGrade" readOnly="true"/>
			<fr:slot name="gradeToChangeTo">
				<fr:property name="size" value="5"/>
				<fr:property name="maxLength" value="5"/>
			</fr:slot>
			<fr:slot name="siadapEvaluationUniverse.currentExcellencyAward" readOnly="true"/>
			<fr:slot name="assignExcellency"/>
			<fr:slot name="justification" key="label.ChangeGradeAnytimeAfterValidationByCCA.justification" layout="longText">
				<fr:property name="rows" value="8" />
				<fr:property name="columns" value="80" />
			</fr:slot>
		</fr:schema>
	</fr:edit>
</logic:iterate>
<logic:present name="homologationMode" >
	<html:submit styleClass="inputbutton"><bean:message key="button.homologate" bundle="SIADAP_RESOURCES"/></html:submit>
</logic:present>
<logic:notPresent name="homologationMode" >
	<html:submit styleClass="inputbutton"><bean:message key="button.alter" bundle="SIADAP_RESOURCES"/></html:submit>
</logic:notPresent>
</fr:form>
<logic:present name="homologationMode" >
<fr:form id="form" action='<%="/workflowProcessManagement.do?method=viewProcess&processId=" + processId %>'>
	<html:submit styleClass="inputbutton"><bean:message key="button.cancel" bundle="SIADAP_RESOURCES"/></html:submit>
</fr:form>
</logic:present>
<logic:notPresent name="homologationMode" >
<fr:form id="form" action='<%="/workflowProcessManagement.do?method=viewProcess&processId=" + processId %>'>
	<html:submit styleClass="inputbutton"><bean:message key="button.back" bundle="SIADAP_RESOURCES"/></html:submit>
</fr:form>
</logic:notPresent>
</div>