<%@page import="module.siadap.domain.SiadapUniverse"%>
<%@page import="module.siadap.domain.Siadap"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>

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
