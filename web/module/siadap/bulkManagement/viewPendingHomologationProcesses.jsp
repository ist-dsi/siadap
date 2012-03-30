<%@page import="myorg.util.BundleUtil"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<script type="text/javascript">
function homologationConfirmation() {
	var answer = confirm("<%= BundleUtil.getFormattedStringFromResourceBundle("resources/SiadapResources", "label.confirm.batch.homologation") %>")
	if (answer){
		document.getElementById("selectionForm").submit()
	}
}

function toggleAll() {
	if (document.getElementById("selectAll").checked) {
		checkAll();
	} else {
		uncheckAll();
	}
}

function checkAll() {
	$(':checkbox').each(function () {
		this.checked = true;
	});
}

function uncheckAll() {
	$(':checkbox').each(function () {
		this.checked = false;
	});
}
</script>

<bean:define id="unitId" name="unit" property="unit.externalId"/>
<bean:define id="employees" name="employees" type="java.util.List"/>
<bean:define id="year" name="unit" property="year"/>

<h2><fr:view name="unit" property="name" /></h2>

<h3><%= employees.size() %> <bean:message key="title.siadap.processes.pendingHomologation" bundle="SIADAP_RESOURCES" /><%= " (SIADAP - " + year + ")" %></h3>

<p>
<fr:form id="selectionForm" action="<%= "/siadapManagement.do?method=batchHomologation&unitID=" + unitId + "&year=" + year %>">
<table class="tstyle2">
	<fr:edit visible="false" id="employees" name="employees"/>
<tr>
	<th><input id="selectAll" type="checkbox" onclick="toggleAll()"/></th>
	<th><bean:message bundle="SIADAP_RESOURCES" key="label.evaluated"/></th>
	<th><bean:message bundle="SIADAP_RESOURCES" key="label.process.state"/></th>
	<th><bean:message bundle="SIADAP_RESOURCES" key="label.validation.classification.SIADAP2"/></th>
	<th><bean:message bundle="SIADAP_RESOURCES" key="label.validation.evaluation.SIADAP2"/></th>
	<th><bean:message bundle="SIADAP_RESOURCES" key="label.validation.classification.SIADAP3"/></th>
	<th><bean:message bundle="SIADAP_RESOURCES" key="label.validation.evaluation.SIADAP3"/></th>
	<th></th>
</tr>
<logic:iterate id="personWrapper" name="employees" type="module.siadap.domain.wrappers.PersonSiadapWrapper">
<tr>
	<td>
		<fr:edit id="<%= "person" + personWrapper.getPerson().getExternalId() %>" name="personWrapper" slot="selectedForHomologation" layout="option-select"/>
	</td>
	<td>
		<%= "<strong>" + personWrapper.getPerson().getPartyName().getContent() + "</strong><br>(" + personWrapper.getPerson().getUser().getUsername() + ")" %>
	</td>
	<td>
		<%= personWrapper.getSiadap().getState().getDescription() %>
	</td>
	<td>
		<%= (personWrapper.getFinalClassificationForSIADAP2() == null) ? "-" : personWrapper.getFinalClassificationForSIADAP2() %>
	</td>
	<td>
		<%= (personWrapper.getSiadap().getSiadap2EvaluationAfterValidation() == null) ? "-" : personWrapper.getSiadap().getSiadap2EvaluationAfterValidation().getLocalizedName() %>
	</td>
	<td>
		<%= (personWrapper.getFinalClassificationForSIADAP3() == null) ? "-" : personWrapper.getFinalClassificationForSIADAP3() %>
	</td>
	<td>
		<%= (personWrapper.getSiadap().getSiadap3EvaluationAfterValidation() == null) ? "-" : personWrapper.getSiadap().getSiadap3EvaluationAfterValidation().getLocalizedName() %>
	</td>
	<td>
		<%
		if (personWrapper.isAccessibleToCurrentUser()) { 
		%>
		<html:link action="<%= "/workflowProcessManagement.do?method=viewProcess&processId=" + personWrapper.getSiadap().getProcess().getExternalId() %>">
			<bean:message bundle="MYORG_RESOURCES" key="link.view"/>
		</html:link>
		<% } %>
	</td>
</tr>
</logic:iterate>
</table>

<html:submit onclick="homologationConfirmation()"><bean:message bundle="SIADAP_RESOURCES" key="button.homologate.selected"/></html:submit>

</fr:form>

<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/18" />	
</jsp:include>