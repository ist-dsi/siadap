<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<h2><bean:message key="link.siadap.structureManagement" bundle="SIADAP_RESOURCES"/></h2>

<fr:edit id="searchPerson" name="bean" action="/siadapPersonnelManagement.do?method=viewPerson">
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

