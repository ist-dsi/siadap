<%@page import="java.util.List"%>
<%@page import="module.siadap.domain.SiadapUniverse"%>
<%@page import="module.siadap.domain.wrappers.SiadapUniverseWrapper"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<bean:define id="unitName" name="unit" property="unit.presentationName" />
<bean:define id="unitId" name="unit" property="unit.externalId" />
<bean:define id="year" name="unit" property="year" />
<bean:define id="siadapUniverseWrappers" name="siadapUniverseWrappers" />


<h2>
	<bean:message key="label.addSugestionToUnit" bundle="SIADAP_RESOURCES" arg0="<%=unitName.toString()%>" />
</h2>
<br/>

<%-- Error messages: --%>
<logic:messagesPresent property="message" message="true">
	<div class="error1">
		<html:messages id="errorMessage" property="message" message="true"> 
			<span><fr:view name="errorMessage"/></span>
		</html:messages>
	</div>
</logic:messagesPresent>
<%-- *END* Error messages *END* --%>

<p><html:link page="<%="/siadapManagement.do?method=viewUnitHarmonizationData&year=" + year.toString() + "&unitId=" + unitId.toString() %>">
	<bean:message key="label.harmonization.QuotaSuggestionInterface.backToHarmonization" bundle="SIADAP_RESOURCES"/> 
</html:link></p>
<br/>

 <logic:equal value="true" name="unit" property="harmonizationActive">
 	<%
 		boolean allEmpty = true;
 		for (SiadapUniverseWrapper siadapUniverseWrapper : ((List<SiadapUniverseWrapper>)siadapUniverseWrappers))
 		{
 		    if (!siadapUniverseWrapper.getSiadapUniverseForSuggestions().isEmpty())
 				{allEmpty = false; break;}
 		}
 		request.setAttribute("allUniversesEmpty", allEmpty);
 	%>
 	<logic:equal name="allUniversesEmpty" value="true">
	<p><i><bean:message key="label.exceedingQuota.no.one.to.add.exceedingQuotaTo" bundle="SIADAP_RESOURCES"/></i></p>
 	</logic:equal>
 	<logic:equal name="allUniversesEmpty" value="false">
 	<strong><bean:message bundle="SIADAP_RESOURCES" key="label.harmonization.QuotaSuggestionInterface.fillingInstructions"/></strong>
	<fr:form action="<%="/siadapManagement.do?method=addExceedingQuotaSuggestion&year=" + year.toString() + "&unitId=" + unitId.toString() %>">
		<fr:edit id="siadapUniverseWrappersList" name="siadapUniverseWrappers" visible="false"/>
    	<logic:iterate id="siadapUniverseWrapper" name="siadapUniverseWrappers">

<%-- Title --%>
	<logic:notEmpty name="siadapUniverseWrapper" property="siadapUniverseForSuggestions">
		<strong><bean:message key="<%=((module.siadap.domain.wrappers.SiadapUniverseWrapper)siadapUniverseWrapper).getUniverseTitleQuotaSuggestionKey() %>" bundle="SIADAP_RESOURCES"/></strong>
	</logic:notEmpty>

 	<fr:edit name="siadapUniverseWrapper" property="siadapUniverseForSuggestions" nested="true">
		<fr:schema type="module.siadap.domain.wrappers.SiadapSuggestionBean" bundle="SIADAP_RESOURCES">
			<fr:slot name="personWrapper.person.name" key="label.evaluated" bundle="SIADAP_RESOURCES" readOnly="true" />
			<fr:slot name="personWrapper.person.user.username" key="label.login.username" bundle="MYORG_RESOURCES" readOnly="true" />
			<% 
			SiadapUniverseWrapper siadapUWrapper = (SiadapUniverseWrapper) siadapUniverseWrapper;
			if (siadapUWrapper.getSiadapUniverseEnum().equals(SiadapUniverse.SIADAP2))
			{
			    %>
			<fr:slot name="personWrapper.totalEvaluationScoringSiadap2" layout="null-as-label" key="label.totalEvaluationScoring" readOnly="true">
				<fr:property name="subLayout" value="" />
			</fr:slot>
			<fr:slot name="personWrapper.totalQualitativeEvaluationScoringSiadap2" layout="null-as-label" key="label.totalQualitativeEvaluationScoring" readOnly="true">
				<fr:property name="subLayout" value="" />
			</fr:slot>
			<%
			} else {
			%>
			<fr:slot name="personWrapper.totalEvaluationScoringSiadap3" layout="null-as-label" key="label.totalEvaluationScoring" readOnly="true">
				<fr:property name="subLayout" value="" />
			</fr:slot>
			<fr:slot name="personWrapper.totalQualitativeEvaluationScoringSiadap3" layout="null-as-label" key="label.totalQualitativeEvaluationScoring" readOnly="true">
				<fr:property name="subLayout" value="" />
			</fr:slot>
			<% } %>
			<%--<fr:slot name="harmonizationCurrentAssessmentForSIADAP2" layout="radio" readOnly="true" key="label.harmonization.assessment">
				<fr:property name="classes" value="inline-list" />
				<fr:property name="eachClasses" value="withQuotasSIADAP2" />
			</fr:slot>
			--%>
			<fr:slot name="exceedingQuotaPriorityNumber" key="label.harmonization.exceedingQuotaPriorityNumber" bundle="SIADAP_RESOURCES" />
			<fr:slot name="type" />
		</fr:schema>
		<fr:layout name="tabular-row">
			<fr:property name="classes" value="tstyle2" />
			<fr:property name="columnClasses" value="aleft,aleft,," />
		<%-- 	<fr:property name="link(create)" value="/siadapManagement.do?method=createNewSiadapProcess" />
			<fr:property name="bundle(create)" value="MYORG_RESOURCES" />
			<fr:property name="key(create)" value="link.create" />
			<fr:property name="param(create)" value="personWrapper.person.externalId/personId" />
			<fr:property name="order(create)" value="1" />
			<fr:property name="visibleIf(create)" value="personWrapper.currentUserAbleToCreateProcess" /> --%>

			<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess" />
			<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES" />
			<fr:property name="key(viewProcess)" value="link.view" />
			<fr:property name="param(viewProcess)" value="personWrapper.siadap.process.externalId/processId" />
			<fr:property name="order(viewProcess)" value="1" />
			<fr:property name="visibleIf(viewProcess)" value="personWrapper.accessibleToCurrentUser" />

<%--
			<fr:property name="sortParameter" value="sortByQuotas" />
			<fr:property name="sortUrl" value="<%= "/siadapManagement.do?method=viewUnitHarmonizationData&unitId=" + unitId + "&year=" + year.toString()%>" />
			<fr:property name="sortBy" value="<%= request.getParameter("sortByQuotas") == null ? "person.partyName=asc" : request.getParameter("sortByQuotas") %>" />
 --%>
		</fr:layout>
	</fr:edit>

</logic:iterate>
			<html:submit styleClass="inputbutton">
				<bean:message key="label.save" bundle="SIADAP_RESOURCES" />
			</html:submit>
	</fr:form>
</logic:equal>
<logic:equal value="false" name="unit" property="harmonizationActive">
	<p><i><bean:message key="label.exceedingQuota.harmonization.not.active" bundle="SIADAP_RESOURCES"/></i></p>
</logic:equal>
</logic:equal>
	


<%--
<fr:edit name="bean" id="bean"
	action='<%="/siadapManagement.do?unitId=" + unitId + "&method=addExcedingQuotaSuggestion&year=" + year.toString()%>'>
	<fr:schema
		type="module.siadap.presentationTier.actions.SiadapSuggestionBean"
		bundle="SIADAP_RESOURCES">
		<fr:slot name="person" layout="autoComplete" key="label.person"
			bundle="ORGANIZATION_RESOURCES">
			<fr:property name="labelField" value="partyName.content" />
			<fr:property name="format" value="${presentationName}" />
			<fr:property name="minChars" value="3" />
			<fr:property name="args"
				value="provider=module.siadap.presentationTier.renderers.providers.ExcedingQuotaSuggestionProvider,unitId=${unit.externalId}" />
			<fr:property name="size" value="60" />
			<fr:validator
				name="pt.ist.fenixWebFramework.rendererExtensions.validators.RequiredAutoCompleteSelectionValidator">
				<fr:property name="message" value="label.pleaseSelectOne.person" />
				<fr:property name="bundle" value="ORGANIZATION_RESOURCES" />
				<fr:property name="key" value="true" />
			</fr:validator>
		</fr:slot>
		<fr:slot name="type" required="true" />
	</fr:schema>
	<fr:destination name="cancel"
		path='<%="/siadapManagement.do?unitId=" + unitId + "&method=viewUnitHarmonizationData&year=" + year.toString()%>' />
</fr:edit>
 --%>
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">
	<jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/12" />
</jsp:include>