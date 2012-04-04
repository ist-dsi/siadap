<%@page import="myorg.util.BundleUtil"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<script src="<%= request.getContextPath() + "/javaScript/jquery.alerts.js"%>" type="text/javascript"></script>
<script src="<%= request.getContextPath() + "/javaScript/alertHandlers.js"%>" type="text/javascript"></script>
<script src="<%= request.getContextPath() + "/javaScript/spin.js"%>" type="text/javascript"></script>
<script src="<%= request.getContextPath() + "/javaScript/jquery.blockUI.js"%>" type="text/javascript"></script>


<script type="text/javascript">
$(document).ready(function() {
	//linkConfirmationHookForm('selectionForm', '<bean:message key="label.confirm.batch.homologation" bundle="SIADAP_RESOURCES"/>','<bean:message key="label.batch.homologation.label" bundle="SIADAP_RESOURCES"/>');
	
	 $('#selectionForm :submit').click(function() { 
         $.blockUI({ message: $('#confirmationQuestion'), css: { width: '350px' } }); 
         return false;
     }); 
	 
	 $('#yes').click(function() { 
         // update the block message 
         displayLoadingScreen();
         $('#selectionForm').submit();
     	});

     $('#no').click(function() { 
         $.unblockUI(); 
         return false; 
     }); 

});

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

function requestConfirmation(){
	
}

function displayLoadingScreen() {
	var opts = {
			  lines: 13, // The number of lines to draw
			  length: 7, // The length of each line
			  width: 4, // The line thickness
			  radius: 10, // The radius of the inner circle
			  rotate: 0, // The rotation offset
			  color: '#000', // #rgb or #rrggbb
			  speed: 1, // Rounds per second
			  trail: 60, // Afterglow percentage
			  shadow: false, // Whether to render a shadow
			  hwaccel: false, // Whether to use hardware acceleration
			  className: 'spinner', // The CSS class to assign to the spinner
			  zIndex: 2e9, // The z-index (defaults to 2000000000)
			  //top: 'auto', // Top position relative to parent in px
			  //left: 'auto' // Left position relative to parent in px
			};
	$.blockUI({ message: $('#pleaseWait'),
				//TODO comment the timeout
				//timeout: 2000
				fadeIn: 1000,
				css: { top: '20%' }
				}); 
	var target = document.getElementById('pleaseWait');
	var spinner = new Spinner(opts).spin(target);
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

<div id="confirmationQuestion" style="display:none; cursor: default"> 
        <h3><bean:message key="label.confirm.batch.homologation" bundle="SIADAP_RESOURCES"/></h3> 
        <input type="button" id="yes" value="Sim" /> 
        <input type="button" id="no" value="Não" /> 
</div> 

<a href="#" id="testLink">Testar</a>
<div id="pleaseWait" style="display:none; height: 100px;"> 
    <p><b>A processar... por favor aguarde</b></p> 
</div> 

<h2><fr:view name="unit" property="name" /></h2>

<h3><%= employees.size() %> <bean:message key="title.siadap.processes.pendingHomologation" bundle="SIADAP_RESOURCES" /><%= " (SIADAP - " + year + ")" %></h3>

<%-- Warning messages:
<logic:messagesPresent property="messageWarning" message="true">
	<div class="highlightBox">
		<html:messages id="warningMessage" property="messageWarning" message="true"> 
			<p><b><bean:write name="warningMessage" /></b></p>
		</html:messages>
	</div>
</logic:messagesPresent>
 --%>
<fr:form id="selectionForm" action="<%= "/siadapManagement.do?method=batchHomologation&unitID=" + unitId + "&year=" + year %>" >
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

<html:submit styleClass="inputbutton"><bean:message bundle="SIADAP_RESOURCES" key="button.homologate.selected"/></html:submit>

</fr:form>

<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/18" />	
</jsp:include>
