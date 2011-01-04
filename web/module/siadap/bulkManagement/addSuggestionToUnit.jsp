<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<bean:define id="unitName" name="bean" property="unit.presentationName" />
<bean:define id="unitId" name="bean" property="unit.externalId" />
<bean:define id="year" name="bean" property="year"/>

<h2><bean:message key="label.addSugestionToUnit"
	bundle="SIADAP_RESOURCES" arg0="<%=unitName.toString()%>" /></h2>


<fr:edit name="bean" id="bean"
	action='<%="/siadapManagement.do?unitId=" + unitId + "&method=addExcedingQuotaSuggestion&year=" + year.toString()%>'>
	<fr:schema
		type="module.siadap.presentationTier.actions.SiadapSuggestionBean"
		bundle="SIADAP_RESOURCES">
		<fr:slot name="person" layout="autoComplete" key="label.person"
			bundle="ORGANIZATION_RESOURCES">
			<fr:property name="labelField" value="partyName.content" />
			<fr:property name="format" value="${presentationName}" />
			<fr:property name="minChars" value="3" />
			<fr:property name="args"
				value="provider=module.siadap.presentationTier.renderers.providers.ExcedingQuotaSuggestionProvider,unitId=${unit.externalId}" />
			<fr:property name="size" value="60" />
			<fr:validator
				name="pt.ist.fenixWebFramework.rendererExtensions.validators.RequiredAutoCompleteSelectionValidator">
				<fr:property name="message" value="label.pleaseSelectOne.person" />
				<fr:property name="bundle" value="ORGANIZATION_RESOURCES" />
				<fr:property name="key" value="true" />
			</fr:validator>
		</fr:slot>
		<fr:slot name="type" required="true" />
	</fr:schema>
	<fr:destination name="cancel"
		path='<%="/siadapManagement.do?unitId=" + unitId + "&method=viewUnitHarmonizationData&year=" + year.toString()%>' />
</fr:edit>
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/12" />	
</jsp:include>