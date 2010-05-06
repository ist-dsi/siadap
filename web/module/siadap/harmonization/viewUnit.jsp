<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<h2>
	<fr:view name="currentUnit" property="unit.partyName"/>
</h2>

<div class="infobox">
	<fr:view name="currentUnit">
		<fr:schema type="module.siadap.domain.wrappers.UnitSiadapWrapper" bundle="SIADAP_RESOURCES">
			<fr:slot name="unit.partyName" key="label.unit" bundle="ORGANIZATION_RESOURCES"/>
			<fr:slot name="evaluationResponsibles" key="label.unitResponsibles" layout="flowLayout">
				<fr:property name="eachLayout" value="values"/>
				<fr:property name="eachSchema" value="organization.domain.Person.view.short"/>
				<fr:property name="htmlSeparator" value=", "/>
			</fr:slot>
			<fr:slot name="totalPeople" />
			<fr:slot name="highGradeQuota" />
			<fr:slot name="currentUsedHighGradeQuota"/>
			<fr:slot name="excellencyGradeQuota"/>
			<fr:slot name="currentUsedExcellencyGradeQuota"/>
		</fr:schema>
		<fr:layout name="tabular">
			<fr:property name="columnClasses" value="aright,,"/>
		</fr:layout>
	</fr:view>
</div>

<logic:notEmpty name="people">
	<strong>
		<bean:message key="label.unitEmployees" bundle="SIADAP_RESOURCES"/>:
	</strong>
	
	<p>
		<fr:view name="people">
			<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
				<fr:slot name="person.partyName"/>
				<fr:slot name="year"/>
				<fr:slot name="evaluator.partyName"/>
				<fr:slot name="evaluationStarted"/>
			</fr:schema>
			<fr:layout name="tabular">
				<fr:property name="classes" value="tstyle2"/>
				<fr:property name="link(create)" value="/siadapManagement.do?method=createNewSiadapProcess"/>
				<fr:property name="bundle(create)" value="MYORG_RESOURCES"/>
				<fr:property name="key(create)" value="link.create"/>
				<fr:property name="param(create)" value="person.externalId/personId"/>
				<fr:property name="order(create)" value="1"/>
				<fr:property name="visibleIf(create)" value="currentUserAbleToCreateProcess"/>
				
				<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess"/>
				<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES"/>
				<fr:property name="key(viewProcess)" value="link.view"/>
				<fr:property name="param(viewProcess)" value="siadap.process.externalId/processId"/>
				<fr:property name="order(viewProcess)" value="1"/>
				<fr:property name="visibleIf(viewProcess)" value="accessibleToCurrentUser"/>
			</fr:layout>
		</fr:view>
	</p>
</logic:notEmpty>

<logic:notEmpty name="subUnits">
	<strong>
		<bean:message key="label.subUnits" bundle="SIADAP_RESOURCES"/>:
	</strong>
	
	<p>
		<fr:view name="subUnits">
		<fr:schema type="module.siadap.domain.wrappers.UnitSiadapWrapper" bundle="SIADAP_RESOURCES">
				<fr:slot name="unit.partyName" />
				<fr:slot name="unit.acronym"/>
				<fr:slot name="relevantEvaluationPercentage"/>
				<fr:slot name="totalPeople"/>
				<fr:slot name="totalPeopleWithSiadap"/>
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