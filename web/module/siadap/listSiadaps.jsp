<%@page import="module.siadap.domain.wrappers.SiadapYearWrapper"%>
<%@page import="module.siadap.domain.wrappers.PersonSiadapWrapper"%>
<%@page import="module.organization.domain.Person"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<h2> SIADAP </h2>

<%-- The year chooser: --%>
<fr:form action="/siadapManagement.do?method=manageSiadap">
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
<div class="infobox">
	<bean:message key="siadap.more.info" bundle="SIADAP_RESOURCES"/> <html:link target="_blank"  href="http://drh.ist.utl.pt/html/avaliacao/naodocente/"> <bean:message key="siadap.more.info.link" bundle="SIADAP_RESOURCES"/></html:link>
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
	<bean:message key="label.data.owners.warning" bundle="SIADAP_RESOURCES"/> <a href="mailto:ci@ist.utl.pt?subject=SIADAP%20dados%20hierarquicos%20incorrectos">drh@drh.ist.utl.pt</a>
</div>

 <logic:notEmpty name="person" property="allSiadaps">
 <p>
 	<h3> <bean:message key="label.myEvaluations" bundle="SIADAP_RESOURCES"/> </h3>
 	
 	<fr:view name="person" property="allSiadaps">
 		<fr:schema type="module.siadap.domain.Siadap" bundle="SIADAP_RESOURCES">
 			<fr:slot name="process.processNumber" key="label.processNumber" bundle="WORKFLOW_RESOURCES"/>
 			<fr:slot name="year"/>
 			<%-- TODO joantune: SIADAP-145 --%>
 			<fr:slot name="defaultTotalEvaluationScoring"/>
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

<%
SiadapYearWrapper siadapYearWrapper = (SiadapYearWrapper) request.getAttribute("siadapYearWrapper");%>
	<bean:define id="peopleToEvaluate" name="person" property="peopleToEvaluate" toScope="request"/>
	<jsp:include page="prepareCreateSiadap.jsp">
		<jsp:param value="<%=siadapYearWrapper.getChosenYear()%>" name="year"/>
	</jsp:include>
</logic:notEmpty>

<%-- TODO: Until all of the features of the interfaces contained within are checked, this harmonization part should stay commented  --%>
<logic:notEmpty name="person" property="harmozationUnits">
<%-- Defining the year here so that it can be more easily passed on the links below that use it --%> 
<bean:define id="year" name="siadapYearWrapper" property="chosenYear"/>
		<h3> <bean:message key="label.responsifleForHarmonizationOf" bundle="SIADAP_RESOURCES"/>: </h3>
	<p>
		<fr:view name="person" property="harmozationUnits">
			<fr:schema type="module.siadap.domain.wrappers.UnitSiadapWrapper" bundle="SIADAP_RESOURCES">
				<fr:slot name="unit.partyName"  key="label.unit" bundle="ORGANIZATION_RESOURCES" />
				<fr:slot name="unit.acronym" key="label.acronym" bundle="ORGANIZATION_RESOURCES" />
				<%-- <fr:slot name="relevantEvaluationPercentage"/>
				<fr:slot name="excellencyEvaluationPercentage"/>
				--%>
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
	</p>
</logic:notEmpty>
</logic:present>
<logic:notPresent name="person">
<strong><bean:message bundle="SIADAP_RESOURCES" key="label.noconfiguration"/> <a href="mailto:suporte@ist.utl.pt" ><bean:message bundle="SIADAP_RESOURCES" key="label.here" /></a></strong>
</logic:notPresent>
<!-- 
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/13" />	
</jsp:include>
 -->