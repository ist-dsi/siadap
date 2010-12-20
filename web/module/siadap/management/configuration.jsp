<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<%-- The year chooser: --%>
<fr:form action="/siadapManagement.do?method=showConfiguration">
	<fr:edit id="siadapYearWrapper" name="siadapYearWrapper" nested="true">
		<fr:schema bundle="SIADAP" type="module.siadap.domain.wrappers.SiadapYearWrapper">
			<fr:slot name="chosenYear" bundle="SIADAP_RESOURCES" layout="menu-select-postback" key="siadap.start.siadapYearChoice">
					<fr:property name="providerClass" value="module.siadap.presentationTier.renderers.providers.SiadapYearsFromExistingSiadapCfgPlusOne"/>
					<%-- 
					<fr:property name="format" value="${year}" />
					--%>
					<fr:property name="nullOptionHidden" value="true"/>
					<%-- 
					<fr:property name="eachSchema" value="module.siadap.presentationTier.renderers.providers.SiadapYearConfigurationsFromExisting.year"/>
					--%>
			</fr:slot>
		</fr:schema>
		<fr:destination name="postBack" path="/siadapManagement.do?method=showConfiguration"/>
	</fr:edit>
</fr:form>  

<logic:notPresent name="configuration"> 
	<html:link page="/siadapManagement.do?method=createNewSiadapYearConfiguration">
		<bean:message key="label.create.currentYearConfiguration" bundle="SIADAP_RESOURCES"/>
	</html:link>
</logic:notPresent>


<logic:present name="configuration">
<bean:define id="configurationId" name="configuration" property="externalId"/>

<fr:edit name="configuration"
	action="/siadapManagement.do?method=manageSiadap">
	<fr:schema type="module.siadap.domain.SiadapYearConfiguration"
		bundle="SIADAP_RESOURCES">
		<fr:slot name="unitRelations" layout="menu-select" key="label.config.unitRelations">
			<fr:property name="providerClass"
				value="module.organization.presentationTier.renderers.providers.AccountabilityTypesProvider" />
			<fr:property name="choiceType"
				value="module.organization.domain.AccountabilityType" />
			<fr:property name="format" value="${name}" />
			<fr:property name="sortBy" value="name" />
			<fr:property name="saveOptions" value="true" />
		</fr:slot>
		<fr:slot name="harmonizationResponsibleRelation" layout="menu-select" key="label.config.harmonizationResponsibleRelation">
			<fr:property name="providerClass"
				value="module.organization.presentationTier.renderers.providers.AccountabilityTypesProvider" />
			<fr:property name="choiceType"
				value="module.organization.domain.AccountabilityType" />
			<fr:property name="format" value="${name}" />
			<fr:property name="sortBy" value="name" />
			<fr:property name="saveOptions" value="true" />
		</fr:slot>

		<fr:slot name="workingRelation" layout="menu-select" key="label.config.workingRelation">
			<fr:property name="providerClass"
				value="module.organization.presentationTier.renderers.providers.AccountabilityTypesProvider" />
			<fr:property name="choiceType"
				value="module.organization.domain.AccountabilityType" />
			<fr:property name="format" value="${name}" />
			<fr:property name="sortBy" value="name" />
			<fr:property name="saveOptions" value="true" />
		</fr:slot>
		
		<fr:slot name="workingRelationWithNoQuota" layout="menu-select" key="label.config.workingRelationWithNoQuota">
			<fr:property name="providerClass"
				value="module.organization.presentationTier.renderers.providers.AccountabilityTypesProvider" />
			<fr:property name="choiceType"
				value="module.organization.domain.AccountabilityType" />
			<fr:property name="format" value="${name}" />
			<fr:property name="sortBy" value="name" />
			<fr:property name="saveOptions" value="true" />
		</fr:slot>
		
		<fr:slot name="evaluationRelation" layout="menu-select" key="label.config.evaluationRelation">
			<fr:property name="providerClass"
				value="module.organization.presentationTier.renderers.providers.AccountabilityTypesProvider" />
			<fr:property name="choiceType"
				value="module.organization.domain.AccountabilityType" />
			<fr:property name="format" value="${name}" />
			<fr:property name="sortBy" value="name" />
			<fr:property name="saveOptions" value="true" />
		</fr:slot>
		<fr:slot name="siadapStructureTopUnit" layout="autoComplete" key="label.config.siadapStructureTopUnit">
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

		<fr:slot name="objectiveSpecificationBegin" layout="picker" key="label.config.objectiveSpecificationBegin"/>
		<fr:slot name="objectiveSpecificationEnd" layout="picker" key="label.config.objectiveSpecificationEnd"/>
		<fr:slot name="autoEvaluationBegin" layout="picker"/>
		<fr:slot name="autoEvaluationEnd" layout="picker"/>
		<fr:slot name="evaluationBegin" layout="picker"/>
		<fr:slot name="evaluationEnd" layout="picker"/>
		<fr:slot name="lockHarmonizationOnQuota" key="label.config.lockHarmonizationOnQuota"/>
	</fr:schema>

	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2" />
	</fr:layout>
</fr:edit>

<strong><bean:message key="label.ccaMembers" bundle="SIADAP_RESOURCES"/>:</strong>
<fr:view name="configuration" property="ccaMembers">
	<fr:schema type="module.organization.domain.Person" bundle="SIADAP_RESOURCES">
		<fr:slot name="user.username"/>
		<fr:slot name="partyName"/>
	</fr:schema>
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
		<fr:property name="link(remove)" value="<%=  "/siadapManagement.do?method=removeCCAMember&configurationId=" + configurationId %>"/>
		<fr:property name="bundle(remove)" value="MYORG_RESOURCES"/>
		<fr:property name="key(remove)" value="link.remove"/>
		<fr:property name="param(remove)" value="externalId/personId"/>
		<fr:property name="order(remove)" value="1"/>
	</fr:layout>	
</fr:view>

<fr:edit id="ccaMember" name="addCCAMember" action="<%=  "/siadapManagement.do?method=addCCAMember&configurationId=" + configurationId %>">
	<fr:schema type="module.organization.domain.Person" bundle="SIADAP_RESOURCES">
		<fr:slot name="user.username"/>
		<fr:slot name="partyName"/>
	</fr:schema>
<fr:schema type="myorg.util.VariantBean" bundle="SIADAP_RESOURCES">
		<fr:slot name="domainObject" layout="autoComplete" key="label.person" bundle="ORGANIZATION_RESOURCES">
        <fr:property name="labelField" value="partyName.content"/>
		<fr:property name="format" value="${presentationName}"/>
		<fr:property name="minChars" value="3"/>		
		<fr:property name="args" value="provider=module.organization.presentationTier.renderers.providers.PersonAutoCompleteProvider"/>
		<fr:property name="size" value="60"/>
		<fr:validator name="pt.ist.fenixWebFramework.rendererExtensions.validators.RequiredAutoCompleteSelectionValidator">
			<fr:property name="message" value="label.pleaseSelectOne.person"/>
			<fr:property name="bundle" value="EXPENDITURE_RESOURCES"/>
			<fr:property name="key" value="true"/>
		</fr:validator>
	</fr:slot>
	</fr:schema>	
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
	</fr:layout>
</fr:edit>

<strong><bean:message key="label.homologationMembers" bundle="SIADAP_RESOURCES"/>:</strong>
<fr:view name="configuration" property="homologationMembers">
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
		<fr:property name="link(remove)" value="<%=  "/siadapManagement.do?method=removeHomologationMember&configurationId=" + configurationId %>"/>
		<fr:property name="bundle(remove)" value="MYORG_RESOURCES"/>
		<fr:property name="key(remove)" value="link.remove"/>
		<fr:property name="param(remove)" value="externalId/personId"/>
		<fr:property name="order(remove)" value="1"/>
	</fr:layout>	
</fr:view>
<fr:edit id="homologationMember" name="addHomologationMember" action="<%=  "/siadapManagement.do?method=addHomologationMember&configurationId=" + configurationId %>">
<fr:schema type="myorg.util.VariantBean" bundle="SIADAP_RESOURCES">
		<fr:slot name="domainObject" layout="autoComplete" key="label.person" bundle="ORGANIZATION_RESOURCES">
        <fr:property name="labelField" value="partyName.content"/>
		<fr:property name="format" value="${presentationName}"/>
		<fr:property name="minChars" value="3"/>		
		<fr:property name="args" value="provider=module.organization.presentationTier.renderers.providers.PersonAutoCompleteProvider"/>
		<fr:property name="size" value="60"/>
		<fr:validator name="pt.ist.fenixWebFramework.rendererExtensions.validators.RequiredAutoCompleteSelectionValidator">
			<fr:property name="message" value="label.pleaseSelectOne.person"/>
			<fr:property name="bundle" value="EXPENDITURE_RESOURCES"/>
			<fr:property name="key" value="true"/>
		</fr:validator>
	</fr:slot>
	</fr:schema>	
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
	</fr:layout>
</fr:edit>

</logic:present>
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/12" />	
</jsp:include>