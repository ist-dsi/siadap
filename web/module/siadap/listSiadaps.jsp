<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<h2> SIADAP </h2>

<p>

<strong> <bean:message key="label.myData" bundle="SIADAP_RESOURCES"/> </strong>
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
</p>
 
<div class="infobox">
	<bean:message key="label.data.owners.warning" bundle="SIADAP_RESOURCES"/>
</div>

 <logic:notEmpty name="person" property="allSiadaps">
 <p>
 	<strong> <bean:message key="label.myEvaluations" bundle="SIADAP_RESOURCES"/> </strong>
 	
 	<fr:view name="person" property="allSiadaps">
 		<fr:schema type="module.siadap.domain.Siadap" bundle="SIADAP_RESOURCES">
 			<fr:slot name="process.processNumber" key="label.processNumber" bundle="WORKFLOW_RESOURCES"/>
 			<fr:slot name="year"/>
 			<fr:slot name="totalEvaluationScoring"/>
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
 
<logic:notEmpty name="person" property="peopleToEvaluate"> 
	<p>
		<strong> <bean:message key="label.responsibleForEvaluationOf" bundle="SIADAP_RESOURCES"/>: </strong>
		
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
						<td><fr:view name="evaluated" property="name"/>
						<td><fr:view name="evaluated" property="workingUnit.name"/>
					</tr>
				</logic:lessThan>
			</logic:iterate>
		</table>
		
		<html:link page="/siadapManagement.do?method=prepareToCreateNewSiadapProcess"> <bean:message key="label.viewAllEvaluated" bundle="SIADAP_RESOURCES"/> </html:link>
	</p>
</logic:notEmpty>
<logic:notEmpty name="person" property="harmozationUnits">
	<p>
		<strong> <bean:message key="label.responsifleForHarmonizationOf" bundle="SIADAP_RESOURCES"/>: </strong>
		<fr:view name="person" property="harmozationUnits">
			<fr:schema type="module.siadap.domain.wrappers.UnitSiadapWrapper" bundle="SIADAP_RESOURCES">
				<fr:slot name="unit.partyName"  key="label.unit" bundle="ORGANIZATION_RESOURCES" />
				<fr:slot name="unit.acronym" key="label.acronym" bundle="ORGANIZATION_RESOURCES" />
				<fr:slot name="relevantEvaluationPercentage"/>
				<fr:slot name="excellencyEvaluationPercentage"/>
				<fr:slot name="totalPeopleWorkingInUnitIncludingNoQuotaPeople" key="label.totalEvaluated"/>
				<fr:slot name="totalPeopleWithSiadapWorkingInUnit"/>
			</fr:schema>
			<fr:layout name="tabular">
				<fr:property name="classes" value="tstyle2"/>
				<fr:property name="link(view)" value="/siadapManagement.do?method=viewUnitHarmonizationData"/>
				<fr:property name="bundle(view)" value="MYORG_RESOURCES"/>
				<fr:property name="key(view)" value="link.view"/>
				<fr:property name="param(view)" value="unit.externalId/unitId"/>
				<fr:property name="order(view)" value="1"/>
			</fr:layout>	
		</fr:view>
	</p>
</logic:notEmpty>
