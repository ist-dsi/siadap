<%@page import="myorg.domain.exceptions.DomainException"%>
<%@page import="module.siadap.domain.Siadap"%>
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
    %>
    
    
    <h3>Carreira/Competências antigas:</h3>
    <p><bean:write name="information" property="siadap.defaultCompetenceType.name"/></p>
    <%-- let's show the 'old' competences --%>
    <ul>
    	<logic:iterate id="competenceEvaluation" name="information" property="siadap.competenceEvaluations">
    		<li><bean:write name="competenceEvaluation" property="competence.name"/></li>    	
    	</logic:iterate>
    </ul>
<%-- The id of the form has to be activityBean so that the activityDefaultPostback invalidates the viewstate of it in order to have a consistent postback behaviour happening --%>
<div class="dinline forminline">
 <fr:form  action='<%="/workflowProcessManagement.do?method=process&activity=" + name + "&processId=" + processId%>'>
<fr:edit id="activityBean" name="information" visible="false" />
<fr:edit id="edit1" name="information" schema="siadap.module.siadap.activities.ForceEditCompetenceSlashCareerEvaluationByCCA.postback">
	<fr:destination name="postBack" path='<%= "/workflowProcessManagement.do?method=activityDefaultPostback&processId=" + processId + "&activity=" + name%>'/>
	<fr:destination name="postback" path='<%= "/workflowProcessManagement.do?method=activityDefaultPostback&processId=" + processId + "&activity=" + name%>'/>
</fr:edit>
<logic:notEmpty name="information" property="competenceType" >
	<logic:notEmpty name="information" property="evaluatedOnlyByCompetences" >
	<% 
    requestActivityInfo.setInputDisplayed(true); 
	%>
	
<h4><bean:message bundle="SIADAP_RESOURCES" key="ForceEditCompetenceSlashCareerEvaluationByCCA.instructions"/>:</h4>
<fr:edit id="edit2" name="information">
	<fr:schema bundle="SIADAP_RESOURCES"
		type="module.siadap.activities.CreateOrEditCompetenceEvaluationActivityInformation">
		<fr:slot name="competences" layout="option-select">
		<%-- 
			<fr:validator name="pt.ist.fenixWebFramework.renderers.validators.RequiredNrItemsValidator">
            	<fr:property name="nrRequiredItems" value='<%=Integer.toString(nrRequiredItems)%>'/>
			</fr:validator>
			--%>
			<fr:property name="providerClass"
				value="module.siadap.presentationTier.renderers.providers.CompetencesForCompetenceType" />
			<fr:property name="sortBy" value="number" />
			<fr:property name="eachLayout" value="values" />
			<fr:property name="eachSchema" value="view.competence.name" />
		</fr:slot>
	</fr:schema>
	<fr:destination name="invalid" path='<%= "/workflowProcessManagement.do?method=activityDefaultPostback&processId=" + processId + "&activity=" + name%>'/>
</fr:edit>

	</logic:notEmpty>
</logic:notEmpty>
	<html:submit styleClass="inputbutton"><bean:message key="button.save" bundle="SIADAP_RESOURCES"/></html:submit>
</fr:form>
<fr:form action='<%= "/workflowProcessManagement.do?method=viewProcess&processId=" + processId %>'>
<html:submit styleClass="inputbutton"><bean:message key="button.back" bundle="SIADAP_RESOURCES"/></html:submit>
</fr:form>
</div>
<%-- Resolving a bug on WebKit 
<script>
$('document').ready(function() {
	$("td").css("background","yellow");
});
$("td").mouseenter(function() {
	  $("td").css("background","blue");
});
</script>
--%>
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/15" />	
</jsp:include>