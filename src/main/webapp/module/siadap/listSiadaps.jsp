<%@page import="module.siadap.domain.SiadapProcessStateEnum"%>
<%@page import="module.siadap.domain.util.SiadapProcessCounter"%>
<%@page import="module.siadap.domain.SiadapYearConfiguration"%>
<%@page import="module.siadap.domain.wrappers.SiadapYearWrapper"%>
<%@page import="module.siadap.domain.wrappers.PersonSiadapWrapper"%>
<%@page import="module.organization.domain.Person"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>

<script src="<%= request.getContextPath() + "/javaScript/jquery.alerts.js"%>" type="text/javascript"></script>
<script src="<%= request.getContextPath() + "/javaScript/alertHandlers.js"%>" type="text/javascript"></script>
<script src="<%= request.getContextPath() + "/javaScript/spin.js"%>" type="text/javascript"></script>
<script src="<%= request.getContextPath() + "/javaScript/jquery.blockUI.js"%>" type="text/javascript"></script>

<h2> SIADAP <bean:write name="siadapYearWrapper" property="chosenYearLabel"/></h2>

<%
SiadapYearWrapper siadapYearWrapper = (SiadapYearWrapper) request.getAttribute("siadapYearWrapper");%>

<%-- The year chooser: --%>
<fr:form action="/siadapManagement.do?method=manageSiadap">
	<fr:edit id="siadapYearWrapper" name="siadapYearWrapper" nested="true">
		<fr:schema bundle="SIADAP" type="module.siadap.domain.wrappers.SiadapYearWrapper">
			<fr:slot name="chosenYearLabel" bundle="SIADAP_RESOURCES" layout="menu-select-postback" key="siadap.start.siadapYearChoice">
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
<div class="infobox">
	<bean:message key="siadap.more.info" bundle="SIADAP_RESOURCES"/> <html:link target="_blank"  href="http://drh.ist.utl.pt/nao-docentes/avaliacao-siadap/"> <bean:message key="siadap.more.info.link" bundle="SIADAP_RESOURCES"/></html:link>
</div>

<logic:present name="person">
<p>
<h3> <bean:message key="label.myData" bundle="SIADAP_RESOURCES"/> </h3>
</p>
<fr:view name="person">
	<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
		<fr:slot name="name" key="label.name " bundle="ORGANIZATION_RESOURCES"/>
		<fr:slot name="workingUnit" layout="null-as-label" key="label.workingUnit">
			<fr:property name="subLayout" value="values"/>
			<fr:property name="subSchema" value="view.UnitSiadapWrapper.name"/>
		</fr:slot>
		<fr:slot name="evaluator" layout="null-as-label" key="label.evaluator">
			<fr:property name="subLayout" value="values"/>
			<fr:property name="subSchema" value="view.PersonSiadapWrapper.name"/>
		</fr:slot>
	</fr:schema>
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
		<fr:property name="columnClasses" value="aright,aleft,"/>
	</fr:layout>
</fr:view>

<div class="infobox">
	<bean:message key="label.data.owners.warning" bundle="SIADAP_RESOURCES"/> <a href="mailto:siadap@drh.ist.utl.pt?subject=SIADAP%20dados%20hierarquicos%20incorrectos">siadap@drh.ist.utl.pt</a>
</div>

 <logic:notEmpty name="person" property="allSiadaps">
 <p>
 	<h3> <bean:message key="label.myEvaluations" bundle="SIADAP_RESOURCES"/> </h3>
 	
 	<fr:view name="person" property="allSiadaps">
 		<fr:schema type="module.siadap.domain.Siadap" bundle="SIADAP_RESOURCES">
 			<fr:slot name="process.processNumber" key="label.processNumber" bundle="WORKFLOW_RESOURCES"/>
 			<fr:slot name="siadapYearConfiguration.label" key="label.year"/>
 			<%-- TODO joantune: SIADAP-145 
 			<fr:slot name="defaultTotalEvaluationScoring"/>
 			--%>
 		</fr:schema>
 		<fr:layout name="tabular">
 			<fr:property name="classes" value="tstyle2"/>
 			<fr:property name="link(view)" value="/workflowProcessManagement.do?method=viewProcess"/>
			<fr:property name="bundle(view)" value="MYORG_RESOURCES"/>
			<fr:property name="key(view)" value="link.view"/>
			<fr:property name="param(view)" value="process.externalId/processId"/>
			<fr:property name="order(view)" value="1"/>
 		</fr:layout>
 	</fr:view>
 </p>
 </logic:notEmpty>
 
<bean:define id="personJava" name="person"/>

<logic:notEmpty name="person" property="peopleToEvaluate">
	<br/>
	<h3> <bean:message key="label.responsibleForEvaluationOf" bundle="SIADAP_RESOURCES"/>: </h3>
<%-- 
	<p>
	</p>
		<table class="tstyle2">
			<tr>
			<th>
				<bean:message key="label.evaluated" bundle="SIADAP_RESOURCES"/>
			</th>
			<th>
				<bean:message key="label.unit" bundle="ORGANIZATION_RESOURCES" />
			</th>
			</tr>
			<logic:iterate id="evaluated" name="person" property="peopleToEvaluate" indexId="index">
				<logic:lessThan name="index" value="5">
					<tr>
						<td><fr:view name="evaluated" property="name"/></td>
						<logic:notPresent name="evaluated" property="workingUnit">
							<td>-</td>
						</logic:notPresent>
						<logic:present name="evaluated" property="workingUnit">
							<td><fr:view name="evaluated" property="workingUnit.name"/></td>
						</logic:present>
					</tr>
				</logic:lessThan>
			</logic:iterate>
		</table>
		
		<logic:greaterThan name="nrOfPersonsToEvaluate" value="5">
				<i><bean:message bundle="SIADAP_RESOURCES" key="label.shown5ofX" arg0="<%=String.valueOf(((PersonSiadapWrapper) personJava).getPeopleToEvaluate().size())%>" /></i>
				<br/><br/>
		</logic:greaterThan>

		<html:link page="/siadapManagement.do?method=prepareToCreateNewSiadapProcess" paramId="year" paramName="person" paramProperty="year"> <bean:message key="label.viewAllEvaluated" bundle="SIADAP_RESOURCES"/> </html:link>
 --%>
		<bean:size id="nrOfPersonsToEvaluate" name="person" property="peopleToEvaluate"/>
		<bean:message bundle="SIADAP_RESOURCES" key="siadap.nr.processes.with.pending.actions.label"/>: <bean:write name="person" property="nrPendingProcessActions"/>
		<br/>
		<bean:message bundle="SIADAP_RESOURCES" key="siadap.nr.processes.with.unread.comments.label"/>: <bean:write name="person" property="nrPersonsWithUnreadComments"/>
		<br/>


	<bean:define id="peopleToEvaluate" name="person" property="peopleToEvaluate" toScope="request"/>
	<jsp:include page="prepareCreateSiadap.jsp">
		<jsp:param value="<%=siadapYearWrapper.getChosenYear()%>" name="year"/>
	</jsp:include>
</logic:notEmpty>

<logic:notEmpty name="person" property="activeHarmonizationUnits">
	<%-- <logic:equal value="true" name="person" property="harmonizationPeriodOpen"> --%>
		<%-- Defining the year here so that it can be more easily passed on the links below that use it --%>
		<bean:define id="year" name="siadapYearWrapper" property="chosenYear"/>
				<h3> <bean:message key="label.responsifleForHarmonizationOf" bundle="SIADAP_RESOURCES"/>: </h3>
			<div>
				<fr:view name="person" property="activeHarmonizationUnits">
					<fr:schema type="module.siadap.domain.wrappers.UnitSiadapWrapper" bundle="SIADAP_RESOURCES">
						<fr:slot name="unit.partyName"  key="label.unit" bundle="ORGANIZATION_RESOURCES" />
						<fr:slot name="unit.acronym" key="label.acronym" bundle="ORGANIZATION_RESOURCES" />
					<%-- 	<fr:slot name="relevantEvaluationPercentage"/>
						<fr:slot name="excellencyEvaluationPercentage"/> --%> 
						
						<fr:slot name="totalPeopleHarmonizedInUnit" key="label.totalEvaluated"/>
						<fr:slot name="totalPeopleHarmonizedInUnitWithSiadapStarted" key="label.totalPeopleWithSiadapHarmonizedInUnit"/>
					</fr:schema>
					<fr:layout name="tabular">
						<fr:property name="classes" value="tstyle2"/>
							<fr:property name="link(view)" value="<%="/siadapManagement.do?method=viewUnitHarmonizationData&year=" + year.toString() %>"/>
							<fr:property name="bundle(view)" value="MYORG_RESOURCES"/>
							<fr:property name="key(view)" value="link.view"/>
							<fr:property name="param(view)" value="unit.externalId/unitId"/>
							<fr:property name="order(view)" value="1"/>
					</fr:layout>	
				</fr:view>
			</div>
</logic:notEmpty>

<%-- Get the View of the top unit for harmonization, if we are part of the CCA group--%>
<logic:equal value="true" name="person" property="CCAMember">
<%-- TODO: localize --%>
<%--Vista das unidades de harmonização --%>
<h3><bean:message bundle="SIADAP_RESOURCES" key="label.listSiadaps.jsp.harmonizationUnitsView" />:</h3>
<html:link page="<%="/siadapManagement.do?method=viewUnitHarmonizationData&year=" + String.valueOf(siadapYearWrapper.getChosenYear()) + "&unitId=" + module.siadap.domain.SiadapYearConfiguration.getSiadapYearConfiguration(siadapYearWrapper.getChosenYear()).getSiadapStructureTopUnit().getExternalId()%>" >
	<%-- Ir para unidade topo --%>
	<bean:message bundle="SIADAP_RESOURCES" key="label.listSiadaps.jsp.goto.top.unit.link"/>
</html:link>

<%--<h3>Acções em massa do CCA</h3> --%>
<h3><bean:message bundle="SIADAP_RESOURCES" key="label.listSiadaps.jsp.CCA.bulk.actions" /></h3>
<p>Processos passíveis de serem forçosamente disponibilizados já para homologação <%= String.valueOf( SiadapProcessCounter.getSiadapsInState(siadapYearWrapper.getChosenYear(), new SiadapProcessStateEnum[] { SiadapProcessStateEnum.WAITING_SUBMITTAL_BY_EVALUATOR_AFTER_VALIDATION, SiadapProcessStateEnum.WAITING_VALIDATION_ACKNOWLEDGMENT_BY_EVALUATED, SiadapProcessStateEnum.VALIDATION_ACKNOWLEDGED}).size()) %> </p>



<script type="text/javascript">
$(document).ready(function() {
	//linkConfirmationHookForm('selectionForm', '<bean:message key="label.confirm.batch.homologation" bundle="SIADAP_RESOURCES"/>','<bean:message key="label.batch.homologation.label" bundle="SIADAP_RESOURCES"/>');
	
	 $('#forceHomologationReadinessForm :submit').click(function() { 
         $.blockUI({ message: $('#confirmationQuestion'), css: { width: '350px' } }); 
         return false;
     }); 
	 
	 $('#yes').click(function() { 
         // update the block message 
         displayLoadingScreen();
         $('#forceHomologationReadinessForm').submit();
     	});

     $('#no').click(function() { 
         $.unblockUI(); 
         return false; 
     }); 

});

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

</script>

<div id="pleaseWait" style="display:none; height: 100px;"> 
    <p><b>A processar... por favor aguarde</b></p> 
</div> 

<form id="forceHomologationReadinessForm" action="<%=request.getContextPath()%>/siadapManagement.do?method=batchForceReadinessForEvaluation&year=<%=siadapYearWrapper.getChosenYear().toString()%>" method="post">
<input class="inputbutton" type="submit" value="Forçar prontidão para avaliação">
</form>

<form id="forceHomologationReadinessForm" action="<%=request.getContextPath()%>/siadapManagement.do?method=batchForceReadinessToHomologation&year=<%=siadapYearWrapper.getChosenYear().toString()%>" method="post">
<input class="inputbutton" type="submit" value="Forçar prontidão para homologação">
</form>

<div id="confirmationQuestion" style="display:none; cursor: default"> 
        <h3><bean:message key="activity.confirmation.module.siadap.activities.ForceReadinessToHomologate" bundle="SIADAP_RESOURCES"/></h3> 
        <input type="button" id="yes" value="Sim" /> 
        <input type="button" id="no" value="Não" /> 
</div> 

</logic:equal>
</logic:present>
<logic:notPresent name="person">
<strong><bean:message bundle="SIADAP_RESOURCES" key="label.noconfiguration"/> <a href="mailto:suporte@ist.utl.pt" ><bean:message bundle="SIADAP_RESOURCES" key="label.here" /></a></strong>
</logic:notPresent>
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/21" />	
</jsp:include>
