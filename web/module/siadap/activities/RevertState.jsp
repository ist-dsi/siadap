<%@page import="module.siadap.domain.SiadapUniverse"%>
<%@page import="module.siadap.domain.Siadap"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<jsp:include page="../processStateLegend.jsp"/>
<br/>

<bean:define id="name" name="information" property="activityName" />
<bean:define id="processId" name="process" property="externalId" type="java.lang.String" />
<div class="dinline forminline">
	<fr:form action="<%="/workflowProcessManagement.do?method=process&activity="+name+"&processId=" + processId%>" >
		<fr:edit id="activityBean" name="information" schema="activityInformation.RevertState"/>
		<html:submit styleClass="inputbutton">
			<bean:message key="label.save"
				bundle="SIADAP_RESOURCES" />
		</html:submit>
	</fr:form>
	<fr:form action='<%="/workflowProcessManagement.do?method=viewProcess&processId=" + processId%>'>
		<html:submit styleClass="inputbutton">
			<bean:message key="renderers.form.cancel.name"
				bundle="RENDERER_RESOURCES" />
		</html:submit>
	</fr:form>
</div>