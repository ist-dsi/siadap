<%@page import="module.siadap.domain.SiadapProcess"%>
<%@page import="java.util.Collection"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<h2>
	<bean:message key="label.harmonizationUnits" bundle="SIADAP_RESOURCES"/>
</h2>

<%-- Warning messages: --%>
<logic:messagesPresent property="messageWarning" message="true">
	<div class="highlightBox">
		<html:messages id="warningMessage" property="messageWarning" message="true"> 
			<p><b><bean:write name="warningMessage" /></b></p>
		</html:messages>
	</div>
</logic:messagesPresent>

<html:messages id="messageSuccess" property="messageSuccess" message="true" bundle="SIADAP_RESOURCES">
	<p class="mtop15">
		<span style="margin: 1em 0; padding: 0.2em 0.5em 0.2em 0.5em; background-color: #e2f5e2; color: #146e14;"><bean:write name="messageSuccess" /></span>
	<p />
	<br />
</html:messages>


<%-- The year chooser: --%>
<fr:form action="/siadapManagement.do?method=manageHarmonizationUnitsForMode&mode=homologationDone">
	<fr:edit id="siadapYearWrapper" name="siadapYearWrapper" nested="true">
		<fr:schema bundle="SIADAP" type="module.siadap.domain.wrappers.SiadapYearWrapper">
			<fr:slot name="chosenYear" bundle="SIADAP_RESOURCES" layout="menu-select-postback" key="siadap.start.siadapYearChoice">
					<fr:property name="providerClass" value="module.siadap.presentationTier.renderers.providers.SiadapYearsFromExistingSiadapConfigurations"/>
					<%-- 
					<fr:property name="format" value="${year}" />
					--%>
					<fr:property name="nullOptionHidden" value="true"/>
					<%-- 
					<fr:property name="eachSchema" value="module.siadap.presentationTier.renderers.providers.SiadapYearConfigurationsFromExisting.year"/>
					--%>
			</fr:slot>
		</fr:schema>
	</fr:edit>
</fr:form>  
<bean:define id="year" name="siadapYearWrapper" property="chosenYear"/>

<table class="tstyle2">
<tr>
	<th><bean:message bundle="ORGANIZATION_RESOURCES" key="label.unit"/></th>
	<th><bean:message bundle="SIADAP_RESOURCES" key="label.relevantEvaluationPercentage"/></th>
	<th><bean:message bundle="SIADAP_RESOURCES" key="label.excellencyEvaluationPercentage"/></th>
	<th><bean:message bundle="SIADAP_RESOURCES" key="label.ongoing"/></th>
	<th><bean:message bundle="SIADAP_RESOURCES" key="label.pending.homologation"/></th>
	<th><bean:message bundle="SIADAP_RESOURCES" key="label.in.reviewCommission"/></th>
	<th><bean:message bundle="SIADAP_RESOURCES" key="label.homologated"/></th>
</tr>

<%
	Collection<SiadapProcess> processes = null;
%>
<logic:iterate id="harmonizationUnit" name="harmonizationUnits" type="module.siadap.domain.wrappers.UnitSiadapWrapper">
<tr>
	<td>
		<%= "<strong>" + harmonizationUnit.getUnit().getPartyName().getContent() + "</strong><br>(" + harmonizationUnit.getAllSiadapProcesses().size() %>
		<bean:message bundle="SIADAP_RESOURCES" key="label.process.state.counts"/>)
	</td>
	<td><%= harmonizationUnit.getRelevantEvaluationPercentage() %> %</td>
	<td><%= harmonizationUnit.getExcellencyEvaluationPercentage() %> %</td>
	<td><%= harmonizationUnit.getSiadapProcessesOngoing().size() %></td>
	<fr:form id="<%= "formHomologation" + harmonizationUnit.getUnit().getExternalId() %>" action="<%= "/siadapManagement.do?method=viewPendingHomologationProcesses&year=" + year.toString() + "&unitId=" + harmonizationUnit.getUnit().getExternalId() %>" >
		<%
			processes = harmonizationUnit.getSiadapProcessesPendingHomologation();
		
			if (!processes.isEmpty()) {
		%>
				<td style="cursor: pointer;" onclick="<%="document.getElementById('formHomologation" + harmonizationUnit.getUnit().getExternalId() + "').submit()"%>" >
					<html:link action="<%= "/siadapManagement.do?method=viewPendingHomologationProcesses&year=" + year.toString() + "&unitId=" + harmonizationUnit.getUnit().getExternalId() %>" >
						<strong><%= processes.size() %></strong>
					</html:link>
				</td>
			
		<%
			} else {
		%>
				<td><%= processes.size() %></td>
		<%
			}
		%>
	</fr:form>
	
	<fr:form id="<%= "formReviewCommission" + harmonizationUnit.getUnit().getExternalId() %>" action="<%= "/siadapManagement.do?method=viewOnlyProcesses&mode=viewReviewCommission&year=" + year.toString() + "&unitId=" + harmonizationUnit.getUnit().getExternalId() %>" >
		<%
			processes = harmonizationUnit.getSiadapProcessesInReviewCommission();
		
			if (!processes.isEmpty()) {
		%>
				<td style="cursor: pointer;" onclick="<%="document.getElementById('formReviewCommission" + harmonizationUnit.getUnit().getExternalId() + "').submit()"%>" >
					<html:link action="<%= "/siadapManagement.do?method=viewOnlyProcesses&mode=viewReviewCommission&year=" + year.toString() + "&unitId=" + harmonizationUnit.getUnit().getExternalId() %>" >
						<strong><%= processes.size() %></strong>
					</html:link>
				</td>
		<%
			} else {
		%>
				<td><%= processes.size() %></td>
		<%
			}
		%>
	</fr:form>
	<fr:form id="<%= "viewHomologatedProcesses" + harmonizationUnit.getUnit().getExternalId() %>" action="<%= "/siadapManagement.do?method=viewOnlyProcesses&mode=viewHomologatedProcesses&year=" + year.toString() + "&unitId=" + harmonizationUnit.getUnit().getExternalId() %>" >
		<%
			processes = harmonizationUnit.getSiadapProcessesHomologated();
		
			if (!processes.isEmpty()) {
		%>
				<td style="cursor: pointer;" onclick="<%="document.getElementById('viewHomologatedProcesses" + harmonizationUnit.getUnit().getExternalId() + "').submit()"%>" >
					<html:link action="<%= "/siadapManagement.do?method=viewOnlyProcesses&mode=viewHomologatedProcesses&year=" + year.toString() + "&unitId=" + harmonizationUnit.getUnit().getExternalId() %>" >
						<strong><%= processes.size() %></strong>
					</html:link>
				</td>
		<%
			} else {
		%>
				<td><%= processes.size() %></td>
		<%
			}
		%>
	</fr:form>
</tr>
</logic:iterate>
</table>

<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/17" />	
</jsp:include>