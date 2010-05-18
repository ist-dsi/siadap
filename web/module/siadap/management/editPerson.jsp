<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<bean:define id="personId" name="person" property="person.externalId"/>

<p>

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
			<th></th>
			</tr>
			<logic:iterate id="evaluated" name="person" property="peopleToEvaluate" indexId="index">
					<bean:define id="evalutedId" name="evaluated" property="person.externalId"/>
					<tr>
						<td><fr:view name="evaluated" property="name"/>
						<td><fr:view name="evaluated" property="workingUnit.name"/>
						<td><html:link page="<%= "/siadapPersonnelManagement.do?method=viewPerson&personId=" + evalutedId %>"> Ver avaliado </html:link></td>
					</tr>
			</logic:iterate>
		</table>		
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
				<fr:slot name="totalPeopleWorkingInUnit"/>
				<fr:slot name="totalPeopleWithSiadapWorkingInUnit"/>
			</fr:schema>
			<fr:layout name="tabular">
				<fr:property name="classes" value="tstyle2"/>
				<fr:property name="link(terminate)" value="<%=  "/siadapPersonnelManagement.do?method=terminateUnitHarmonization&personId=" + personId %>"/>
				<fr:property name="bundle(terminate)" value="MYORG_RESOURCES"/>
				<fr:property name="key(terminate)" value="link.remove"/>
				<fr:property name="param(terminate)" value="unit.externalId/unitId"/>
				<fr:property name="order(terminate)" value="1"/>
			</fr:layout>	
		</fr:view>
	</p>
</logic:notEmpty>


<fr:edit id="addHarmonizationUnit" name="bean" action="<%= "/siadapPersonnelManagement.do?method=addHarmonizationUnit&personId=" + personId %>">
	<fr:schema type="myorg.util.VariantBean" bundle="SIADAP_RESOURCES">
		<fr:slot name="domainObject" layout="autoComplete" key="label.party" bundle="ORGANIZATION_RESOURCES">
        <fr:property name="labelField" value="partyName.content"/>
		<fr:property name="format" value="${presentationName}"/>
		<fr:property name="minChars" value="3"/>		
		<fr:property name="args" value="provider=module.organization.presentationTier.renderers.providers.UnitAutoCompleteProvider"/>
		<fr:property name="size" value="60"/>
		<fr:validator name="pt.ist.fenixWebFramework.rendererExtensions.validators.RequiredAutoCompleteSelectionValidator">
			<fr:property name="message" value="label.pleaseSelectOne.unit"/>
			<fr:property name="bundle" value="EXPENDITURE_RESOURCES"/>
			<fr:property name="key" value="true"/>
		</fr:validator>
	</fr:slot>
	</fr:schema>	
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
	</fr:layout>
</fr:edit>
