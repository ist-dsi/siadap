<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<h2>
	<bean:message key="label.viewHighGlobalEvaluations" bundle="SIADAP_RESOURCES"/>	
</h2>

<fr:view name="employees">

	<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
		<fr:slot name="person.partyName"  key="label.evaluated"/>
		<fr:slot name="workingUnit.unit.partyName" key="label.unit" bundle="ORGANIZATION_RESOURCES" />
	</fr:schema>
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
		<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess"/>
		<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES"/>
		<fr:property name="key(viewProcess)" value="link.view"/>
		<fr:property name="param(viewProcess)" value="siadap.process.externalId/processId"/>
		<fr:property name="order(viewProcess)" value="1"/>
		<fr:property name="visibleIf(viewProcess)" value="accessibleToCurrentUser"/>
	</fr:layout>
</fr:view> 