<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<html:link
	page="/siadapManagement.do?method=createNewSiadapYearConfiguration">Criar configuração para o ano corrente</html:link>
|

<html:link 
	page="/siadapPersonnelManagement.do?method=start">
	Gerir Estrutura
</html:link>
|

<html:link 
	page="/siadapManagement.do?method=manageHarmonizationUnitsForMode&mode=processValidation">
	CCA splash
</html:link>

| 

<html:link 
	page="/siadapManagement.do?method=manageHarmonizationUnitsForMode&mode=homologationDone">
	Homologation splash
</html:link>

<logic:present name="configuration">
<fr:edit name="configuration"
	action="/siadapManagement.do?method=manageSiadap">
	<fr:schema type="module.siadap.domain.SiadapYearConfiguration"
		bundle="SIADAP_RESOURCES">
		<fr:slot name="unitRelations" layout="menu-select">
			<fr:property name="providerClass"
				value="module.organization.presentationTier.renderers.providers.AccountabilityTypesProvider" />
			<fr:property name="choiceType"
				value="module.organization.domain.AccountabilityType" />
			<fr:property name="format" value="${name}" />
			<fr:property name="sortBy" value="name" />
			<fr:property name="saveOptions" value="true" />
		</fr:slot>
		<fr:slot name="harmonizationResponsibleRelation" layout="menu-select">
			<fr:property name="providerClass"
				value="module.organization.presentationTier.renderers.providers.AccountabilityTypesProvider" />
			<fr:property name="choiceType"
				value="module.organization.domain.AccountabilityType" />
			<fr:property name="format" value="${name}" />
			<fr:property name="sortBy" value="name" />
			<fr:property name="saveOptions" value="true" />
		</fr:slot>

		<fr:slot name="workingRelation" layout="menu-select">
			<fr:property name="providerClass"
				value="module.organization.presentationTier.renderers.providers.AccountabilityTypesProvider" />
			<fr:property name="choiceType"
				value="module.organization.domain.AccountabilityType" />
			<fr:property name="format" value="${name}" />
			<fr:property name="sortBy" value="name" />
			<fr:property name="saveOptions" value="true" />
		</fr:slot>
		
		<fr:slot name="workingRelationWithNoQuota" layout="menu-select">
			<fr:property name="providerClass"
				value="module.organization.presentationTier.renderers.providers.AccountabilityTypesProvider" />
			<fr:property name="choiceType"
				value="module.organization.domain.AccountabilityType" />
			<fr:property name="format" value="${name}" />
			<fr:property name="sortBy" value="name" />
			<fr:property name="saveOptions" value="true" />
		</fr:slot>
		
		<fr:slot name="evaluationRelation" layout="menu-select">
			<fr:property name="providerClass"
				value="module.organization.presentationTier.renderers.providers.AccountabilityTypesProvider" />
			<fr:property name="choiceType"
				value="module.organization.domain.AccountabilityType" />
			<fr:property name="format" value="${name}" />
			<fr:property name="sortBy" value="name" />
			<fr:property name="saveOptions" value="true" />
		</fr:slot>
		<fr:slot name="siadapStructureTopUnit" layout="autoComplete">
			<fr:property name="labelField" value="partyName.content" />
			<fr:property name="format" value="${presentationName}" />
			<fr:property name="minChars" value="3" />
			<fr:property name="args"
				value="provider=module.organization.presentationTier.renderers.providers.UnitAutoCompleteProvider" />
			<fr:property name="size" value="60" />
			<fr:validator
				name="pt.ist.fenixWebFramework.rendererExtensions.validators.RequiredAutoCompleteSelectionValidator">
				<fr:property name="message" value="label.pleaseSelectOne.unit" />
				<fr:property name="bundle" value="EXPENDITURE_RESOURCES" />
				<fr:property name="key" value="true" />
			</fr:validator>
		</fr:slot>

		<fr:slot name="autoEvaluationBegin" layout="picker"/>
		<fr:slot name="autoEvaluationEnd" layout="picker"/>
		<fr:slot name="evaluationBegin" layout="picker"/>
		<fr:slot name="evaluationEnd" layout="picker"/>
	</fr:schema>

	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2" />
	</fr:layout>
</fr:edit>
</logic:present>

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
