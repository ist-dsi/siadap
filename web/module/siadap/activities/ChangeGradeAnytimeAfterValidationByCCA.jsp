<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<bean:define id="processId" name="process" property="externalId" type="java.lang.String" />
<bean:define id="name" name="information" property="activityName" />


<div class="dinline forminline">
<fr:form id="form" action='<%="/workflowProcessManagement.do?method=process&processId=" + processId + "&activity=" + name%>'>
<fr:edit id="activityBean" name="information" visible="false" />
<logic:iterate id="siadapEvaluationUniverseBean" name="information" property="siadapEvaluationUniversesBeans" >
	<fr:edit name="siadapEvaluationUniverseBean">
		<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.activities.ChangeGradeAnytimeActivityInformation$GradePerUniverseBean">
			<fr:slot name="siadapEvaluationUniverse.siadapUniverse" key="label.change.grade.siadapEvaluationUniverse.siadapUniverse"  readOnly="true"/>
			<fr:slot name="siadapEvaluationUniverse.currentGrade" readOnly="true"/>
			<fr:slot name="gradeToChangeTo">
				<fr:property name="size" value="5"/>
				<fr:property name="maxLength" value="5"/>
			</fr:slot>
			<fr:slot name="siadapEvaluationUniverse.currentExcellencyAward" readOnly="true"/>
			<fr:slot name="assignExcellency"/>
			<fr:slot name="justification" key="label.ChangeGradeAnytimeAfterValidationByCCA.justification" layout="longText">
				<fr:property name="rows" value="8" />
				<fr:property name="columns" value="80" />
			</fr:slot>
		</fr:schema>
	</fr:edit>
</logic:iterate>
<html:submit styleClass="inputbutton"><bean:message key="button.alter" bundle="SIADAP_RESOURCES"/></html:submit>
</fr:form>
<fr:form id="form" action='<%="/workflowProcessManagement.do?method=viewProcess&processId=" + processId %>'>
	<html:submit styleClass="inputbutton"><bean:message key="button.back" bundle="SIADAP_RESOURCES"/></html:submit>
</fr:form>
</div>