<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<bean:define id="unitName" name="unit" property="unit.presentationName" />
<bean:define id="unitId" name="unit" property="unit.externalId" />
<bean:define id="year" name="unit" property="year" />

<h2>
	<bean:message key="label.addSugestionToUnit" bundle="SIADAP_RESOURCES" arg0="<%=unitName.toString()%>" />
</h2>

<logic:iterate id="siadapUniverseWrapper" name="siadapUniverseWrappers">
	<fr:edit name="siadapUniverseWrapper" property="siadapUniverse" nested="true">
		<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
			<fr:slot name="person.partyName" key="label.evaluated" readOnly="true" />
			<fr:slot name="person.user.username" key="label.login.username" bundle="MYORG_RESOURCES" readOnly="true" />
			<fr:slot name="totalEvaluationScoringSiadap2" layout="null-as-label" key="label.totalEvaluationScoring" readOnly="true">
				<fr:property name="subLayout" value="" />
			</fr:slot>
			<fr:slot name="totalQualitativeEvaluationScoringSiadap2" layout="null-as-label" key="label.totalQualitativeEvaluationScoring" readOnly="true">
				<fr:property name="subLayout" value="" />
			</fr:slot>
			<%--<fr:slot name="harmonizationCurrentAssessmentForSIADAP2" layout="radio" readOnly="true" key="label.harmonization.assessment">
				<fr:property name="classes" value="inline-list" />
				<fr:property name="eachClasses" value="withQuotasSIADAP2" />
			</fr:slot>
			--%>
			<fr:slot name="exceedingQuotaSuggestionBean.exceedingQuotaPriorityNumber" />
			<fr:slot name="exceedingQuotaSuggestionBean.type" />
		</fr:schema>
		<fr:layout name="tabular-row">
			<fr:property name="classes" value="tstyle2" />
			<fr:property name="columnClasses" value="aleft,aleft,," />
			<fr:property name="link(create)" value="/siadapManagement.do?method=createNewSiadapProcess" />
			<fr:property name="bundle(create)" value="MYORG_RESOURCES" />
			<fr:property name="key(create)" value="link.create" />
			<fr:property name="param(create)" value="person.externalId/personId" />
			<fr:property name="order(create)" value="1" />
			<fr:property name="visibleIf(create)" value="currentUserAbleToCreateProcess" />

			<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess" />
			<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES" />
			<fr:property name="key(viewProcess)" value="link.view" />
			<fr:property name="param(viewProcess)" value="siadap.process.externalId/processId" />
			<fr:property name="order(viewProcess)" value="1" />
			<fr:property name="visibleIf(viewProcess)" value="accessibleToCurrentUser" />

			<fr:property name="sortParameter" value="sortByQuotas" />
			<fr:property name="sortUrl" value="<%= "/siadapManagement.do?method=viewUnitHarmonizationData&unitId=" + unitId + "&year=" + year.toString()%>" />
			<fr:property name="sortBy" value="<%= request.getParameter("sortByQuotas") == null ? "person.partyName=asc" : request.getParameter("sortByQuotas") %>" />

		</fr:layout>
	</fr:edit>

</logic:iterate>


<%--
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
 --%>
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">
	<jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/12" />
</jsp:include>