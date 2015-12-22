<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>

<bean:define id="unitId" name="unit" property="unit.externalId"/> 
<bean:define id="employees" name="employees" type="java.util.List"/> 
<bean:define id="unitId" name="unit" property="unit.externalId" />

<bean:define id="mode" name="mode"/>

<bean:define id="year" name="unit" property="year"/>

<bean:define id="siadapLabel" name="unit" property="configuration.label" type="java.lang.String"/>

<h2><fr:view name="unit" property="name" /></h2>

<logic:equal name="mode" value="viewHomologatedProcesses">
	<h3><%= employees.size() %> <bean:message key="title.siadap.processes.homologated" bundle="SIADAP_RESOURCES" /><%= " (SIADAP - " + siadapLabel + ")" %></h3> 
</logic:equal>
<logic:equal name="mode" value="viewReviewCommission">
	<h3><%= employees.size() %> <bean:message key="title.siadap.processes.in.reviewCommission" bundle="SIADAP_RESOURCES" /><%= " (SIADAP - " + siadapLabel + ")" %></h3> 
</logic:equal>

<fr:view name="employees">
	<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
		<fr:slot name="person.partyName" layout="format" key="label.evaluated">
			<fr:property name="useParent" value="true" />
			<fr:property name="format" value="\${person.partyName.content} (\${person.user.username})"/>
		</fr:slot>
		<fr:slot name="siadap.state.description" key="label.process.state"/>
		<fr:slot name="latestClassificationForSIADAP2" layout="null-as-label" key="label.validation.classification.SIADAP2"/>
		<fr:slot name="latestSiadapGlobalEvaluationForSIADAP2" layout="null-as-label" key="label.validation.evaluation.SIADAP2"/>
		<fr:slot name="latestClassificationForSIADAP3" layout="null-as-label" key="label.validation.classification.SIADAP3"/>
		<fr:slot name="latestSiadapGlobalEvaluationForSIADAP3" layout="null-as-label" key="label.validation.evaluation.SIADAP3"/>
	</fr:schema>
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2" />
		<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess" />
		<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES" />
		<fr:property name="key(viewProcess)" value="link.view" />
		<fr:property name="param(viewProcess)" value="siadap.externalId/processId" />
		<fr:property name="order(viewProcess)" value="1" />
		<fr:property name="visibleIf(viewProcess)" value="accessibleToCurrentUser" />

		<fr:property name="sortParameter" value="sortBy" />
		<fr:property name="sortUrl"
			value="<%="/siadapManagement.do?method=viewOnlyProcesses&mode="+ mode.toString() + "&unitId=" + unitId + "&year=" + year.toString()%>" />
		<fr:property name="sortBy"
			value="<%=request.getParameter("sortBy") == null ? "person.partyName=asc" : request
			    .getParameter("sortBy")%>" />
	</fr:layout>
</fr:view>
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/18" />	
</jsp:include>
