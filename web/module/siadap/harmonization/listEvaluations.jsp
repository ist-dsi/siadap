<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<bean:define id="label" name="label"/>
<h2>
	<bean:message key="<%=  "label.viewGlobalEvaluations." + label %>" bundle="SIADAP_RESOURCES"/>	
</h2>

<ul>
	<li>
		<html:link page="/siadapManagement.do?method=viewUnitHarmonizationData" paramId="unitId" paramName="unit" paramProperty="unit.externalId">
		<bean:message key="link.back" bundle="MYORG_RESOURCES"/>
		</html:link>
	</li>
</ul>
<fr:view name="employees">

	<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
		<fr:slot name="name"  key="label.evaluated"/>
		<fr:slot name="workingUnit.name" key="label.unit" bundle="ORGANIZATION_RESOURCES" />
		<fr:slot name="siadap.totalEvaluationScoring" key="label.totalEvaluationScoring"/>
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
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/12" />	
</jsp:include>