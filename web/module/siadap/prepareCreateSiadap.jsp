<%@page import="org.jfree.data.time.Year"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<h2>
	<bean:message key="title.createSiadap" arg0="<%=String.valueOf(request.getAttribute("year"))%>" bundle="SIADAP_RESOURCES"/>
</h2>

<fr:view name="peopleToEvaluate">
		<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
			<fr:slot name="person.partyName" key="label.evaluated"/>
			<fr:slot name="person.user.username" key="label.login.username" bundle="MYORG_RESOURCES"/>
			<fr:slot name="workingUnit.unit.partyName" key="label.unit" bundle="ORGANIZATION_RESOURCES"/>
			<fr:slot name="quotaAware" key="label.quotaAware"/>
			<fr:slot name="currentProcessState" key="label.state" bundle="SIADAP_RESOURCES" />
		</fr:schema>
		<fr:layout name="tabular-sortable">
			<fr:property name="classes" value="tstyle2"/>
			<fr:property name="columnClasses" value="aleft,aleft,,"/>
			<fr:property name="link(create)" value="<%="/siadapManagement.do?method=createNewSiadapProcess&year=" + request.getParameter("year")%>"/>
			<fr:property name="bundle(create)" value="MYORG_RESOURCES"/>
			<fr:property name="key(create)" value="link.create"/>
			<fr:property name="param(create)" value="person.externalId/personId"/>
			<fr:property name="order(create)" value="1"/>
			<fr:property name="visibleIf(create)" value="currentUserAbleToCreateProcess"/>
			
			<fr:property name="link(viewProcess)" value="<%="/workflowProcessManagement.do?method=viewProcess&year=" + request.getParameter("year")%>"/>
			<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES"/>
			<fr:property name="key(viewProcess)" value="link.view"/>
			<fr:property name="param(viewProcess)" value="siadap.process.externalId/processId"/>
			<fr:property name="order(viewProcess)" value="1"/>
			<fr:property name="visibleIf(viewProcess)" value="accessibleToCurrentUser"/>
			
			<fr:property name="sortParameter" value="sortBy"/>
       		<fr:property name="sortUrl" value="<%="/siadapManagement.do?method=prepareToCreateNewSiadapProcess&year=" + String.valueOf(request.getAttribute("year"))%>"/>
		    <fr:property name="sortBy" value="<%= request.getParameter("sortBy") == null ? "person.partyName=asc" : request.getParameter("sortBy") %>"/>
		</fr:layout>
</fr:view>
<h3><bean:message bundle="SIADAP_RESOURCES" key="label.state.legend" /></h3>
<ul>
  <li><bean:message bundle="SIADAP_RESOURCES" key="siadap.state.evaluation.not.going.to.be.done.explanation"/></li>
  <li><bean:message bundle="SIADAP_RESOURCES" key="siadap.state.incomplete.objectives.or.competences.explanation"/></li>
  <li><bean:message bundle="SIADAP_RESOURCES" key="siadap.state.not.created.explanation"/></li>
  <li><bean:message bundle="SIADAP_RESOURCES" key="siadap.state.not.evaluted.yet.explanation"/></li>
  <li><bean:message bundle="SIADAP_RESOURCES" key="siadap.state.not.sealed.explanation"/></li>
  <li><bean:message bundle="SIADAP_RESOURCES" key="siadap.state.not.submitted.for.acknowledgement.explanation"/></li>
  <li><bean:message bundle="SIADAP_RESOURCES" key="siadap.state.unimplemented.explanation"/></li>
  <li><bean:message bundle="SIADAP_RESOURCES" key="siadap.state.waiting.evaluation.objectives.acknowledgement.explanation"/></li>
  <li><bean:message bundle="SIADAP_RESOURCES" key="siadap.state.waiting.self.evaluation.explanation"/></li>
</ul>

<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/21" />	
</jsp:include>