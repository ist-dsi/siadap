<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>


<html:link page="/siadapManagement.do?method=createNewSiadapProcess">Criar Novo processo</html:link>
|
<html:link
	page="/siadapManagement.do?method=createNewSiadapYearConfiguration">Criar configuração para o ano corrente</html:link>
|
<html:link
	page="/siadapManagement.do?method=evaluationHarmonization">Harmonizar valores</html:link>

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

	</fr:schema>

	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2" />
	</fr:layout>
</fr:edit>
</logic:present>

<ul>
	<logic:iterate id="process" name="siadaps">
		<li><html:link
			page="/workflowProcessManagement.do?method=viewProcess"
			paramName="process" paramProperty="externalId" paramId="processId">
			<fr:view name="process" property="processNumber" />
		</html:link></li>
	</logic:iterate>
</ul>