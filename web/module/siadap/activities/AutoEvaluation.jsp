<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr" %>

<bean:define id="processId" name="process" property="externalId" type="java.lang.String"/>
<bean:define id="name" name="information" property="activityName"/>

<fr:form action='<%= "/workflowProcessManagement.do?method=process&processId=" + processId + "&activity=" + name %>'>
	
		<fr:edit id="activityBean" name="information" visible="false"/>
		
		Objectivos:
		<table>
		<logic:iterate id="evaluation" name="information" property="process.siadap.objectiveEvaluations">
			<tr>
				<td><fr:view name="evaluation" property="objective"/></td>
				<td>
					<fr:edit name="evaluation" slot="autoEvaluation"/>					
				</td>
			</tr>
		</logic:iterate>		
		</table>
	    
	    Competências:
	    <table>
	    <logic:iterate id="competence" name="information" property="process.siadap.competenceEvaluations">
			<tr>
				<td><fr:view name="competence" property="competence.name"/></td>
				<td>
					<fr:edit name="competence" slot="autoEvaluation"/>					
				</td>
			</tr>
		</logic:iterate>	
		</table>	
	
	<html:submit>Submeter</html:submit>
</fr:form>
