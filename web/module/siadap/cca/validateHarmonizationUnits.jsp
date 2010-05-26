<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<bean:define id="unitId" name="unit" property="unit.externalId"/>

<fr:view name="employees">
<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
	<fr:slot name="person.partyName" key="label.evaluated"/>
	<fr:slot name="person.user.username" key="label.login.username" bundle="MYORG_RESOURCES"/>
	<fr:slot name="evaluator.name" key="label.evaluator"/>
	<fr:slot name="evaluator.person.user.username" key="label.login.username" bundle="MYORG_RESOURCES"/>
	<fr:slot name="totalEvaluationScoring" layout="null-as-label" key="label.totalEvaluationScoring">
		<fr:property name="subLayout" value=""/>
	</fr:slot>
</fr:schema>
<fr:layout name="tabular-sortable">
	<fr:property name="classes" value="tstyle2"/>
	<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess"/>
	<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES"/>
	<fr:property name="key(viewProcess)" value="link.view"/>
	<fr:property name="param(viewProcess)" value="siadap.process.externalId/processId"/>
	<fr:property name="order(viewProcess)" value="1"/>
	<fr:property name="visibleIf(viewProcess)" value="accessibleToCurrentUser"/>
	
	<fr:property name="sortParameter" value="sortByQuotas"/>
    			<fr:property name="sortUrl" value="<%= "/siadapManagement.do?method=validateHarmonizationData&unitId=" + unitId %>"/>
    <fr:property name="sortBy" value="<%= request.getParameter("sortByQuotas") == null ? "person.partyName=asc" : request.getParameter("sortByQuotas") %>"/>
	
</fr:layout>
</fr:view>