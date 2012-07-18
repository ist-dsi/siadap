<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/workflow" prefix="wf"%>

<bean:define id="objectiveEvaluation" name="objectiveEvaluation"/>
<bean:define id="index" name="index"/>

<bean:define id="objectiveOID" name="objectiveEvaluation" property="externalId" type="java.lang.String"/>

<bean:define id="classToApply" value=""/>
<bean:define id="applyXpto" value=""/>

<logic:equal name="process" property="currentUserEvaluated" value="true">
	<logic:notPresent name="objectiveEvaluation" property="acknowledgeDate"> 
		<bean:define id="classToApply" value="highlightBox"/>
	</logic:notPresent>
</logic:equal>
<logic:equal name="process" property="currentUserEvaluated" value="false">
	<logic:equal name="objectiveEvaluation" property="valid" value="false">
		<bean:define id="applyXpto" value="xpto"/>
	</logic:equal>
</logic:equal>

<%-- Let's assert if we should display the autoEvaluation or not - for that, we'll use the wrapper --%>
<bean:define id="evaluatedPersonWrapper" name="process" property="siadap.evaluatedWrapper"/>

<style>
.xpto span {
	background: #fdeaa5; 
	padding: 1px 2px;
}

</style>

<div class="<%= classToApply %>"/>
	<logic:notPresent name="objectiveEvaluation" property="acknowledgeDate"> 
		<logic:equal name="process" property="currentUserEvaluated" value="true">
			<logic:notEmpty name="objectiveEvaluation" property="justificationForModification">
				<p>
					<strong>
						<bean:message key="label.justificationForModification" bundle="SIADAP_RESOURCES"/>:
					</strong> 
					<fr:view name="objectiveEvaluation" property="justificationForModification"/>
				</p>
			</logic:notEmpty>
		</logic:equal>
	</logic:notPresent>
<table class="tstyle2 width100pc">

	<tr>
		<td colspan="6" class="aleft">
				<strong><bean:message key="label.type" bundle="SIADAP_RESOURCES"/></strong>: <fr:view name="objectiveEvaluation" property="type" type="module.siadap.domain.SiadapEvaluationObjectivesType"/>
				<wf:isActive processName="process" activityName="EditObjectiveEvaluation" scope="request">		
					<span>
					<wf:activityLink id="<%= "edit-" + objectiveOID %>" processName="process" activityName="EditObjectiveEvaluation" scope="request" paramName0="evaluation" paramValue0="<%= objectiveOID %>">
							<bean:message key="link.edit" bundle="MYORG_RESOURCES"/>
					</wf:activityLink>	
					</span>
				</wf:isActive>			
				<wf:isActive processName="process" activityName="RemoveObjectiveEvaluation" scope="request">		
					| <span>
					<wf:activityLink id="<%= "remove-" + objectiveOID %>" processName="process" activityName="RemoveObjectiveEvaluation" scope="request" paramName0="evaluation" paramValue0="<%= objectiveOID %>">
							<bean:message key="link.remove" bundle="MYORG_RESOURCES"/>
					</wf:activityLink>	
					</span>
				</wf:isActive>			
		</td>
	</tr>
	
	<bean:size id="indicatorsCount" name="objectiveEvaluation" property="indicators"/>
	
	<tr>
	<th rowspan="<%= (indicatorsCount * 2) + 1 %>" valign="middle" style="width: 3%">
		<fr:view name="index"/>
	</th>
	<th> <bean:message key="label.objective" bundle="SIADAP_RESOURCES"/> </th>
	<td class="aleft" colspan="4"> <fr:view name="objectiveEvaluation" property="objective"/></td>
	</tr>
	<logic:iterate id="indicator" name="objectiveEvaluation" property="indicators">
		<tr>
		<th> <bean:message key="label.measurementIndicator" bundle="SIADAP_RESOURCES"/> </th>
		<th> <bean:message key="label.superationCriteria" bundle="SIADAP_RESOURCES"/> </th>
		<th style="width: 15%">
			<strong><bean:message key="label.autoEvaluation" bundle="SIADAP_RESOURCES"/></strong>
		</th>
		<th  style="width: 15%"> 
		<strong>	<bean:message key="label.evaluation" bundle="SIADAP_RESOURCES"/></strong>
		</th>
		<th  style="width: 15%"> 
		<strong>	<bean:message key="label.ponderationFactor" bundle="SIADAP_RESOURCES"/></strong>
		</th>
		</tr>
		<tr>
			<td class="aleft"> <fr:view name="indicator" property="measurementIndicator"/>	</td>
			<td class="aleft"> <fr:view name="indicator" property="superationCriteria"/>	</td>
			<td valign="middle">
			<%-- let's make sure that we can show these details --%>
			<logic:equal name="evaluatedPersonWrapper" property="currentUserAbleToSeeAutoEvaluationDetails" value="true">
				<fr:view name="indicator" property="autoEvaluation" type="module.siadap.domain.scoring.SiadapObjectivesEvaluation"/>
			</logic:equal>
			</td>
			<td valign="middle">
				<logic:equal name="evaluatedPersonWrapper" property="currentUserAbleToSeeEvaluationDetails" value="true">
					<fr:view name="indicator" property="evaluation" type="module.siadap.domain.scoring.SiadapObjectivesEvaluation"/>
				</logic:equal>
			</td> 
			<td valign="middle" class="<%=applyXpto%>">
			<fr:view name="indicator" type="module.siadap.domain.ObjectiveEvaluationIndicator" layout="values">
					<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.domain.ObjectiveEvaluation">
						<fr:slot name="ponderationFactor" layout="decimal-format">
				 			<fr:property name="format" value="###.##%"/>
						</fr:slot>
					</fr:schema>
				</fr:view>
			</td> 
		</tr>
	</logic:iterate>
	<%-- 
	<tr>
		<th style="width: 10%"> <bean:message key="label.objective" bundle="SIADAP_RESOURCES"/> </th>
		<td style="width: 57%" class="aleft"> <fr:view name="objectiveEvaluation" property="objective"/>	</td>
		<th style="width: 15%">
			<strong><bean:message key="label.autoEvaluation" bundle="SIADAP_RESOURCES"/></strong>
		</th>
		<th  style="width: 15%"> 
		<strong>	<bean:message key="label.evaluation" bundle="SIADAP_RESOURCES"/></strong>
		</th>
	</tr>
	<tr>
		<th> <bean:message key="label.measurementIndicator" bundle="SIADAP_RESOURCES"/> </th>
		<td class="aleft"> <fr:view name="objectiveEvaluation" property="measurementIndicator"/>	</td>
		<td rowspan="3" valign="middle">
			<fr:view name="objectiveEvaluation" property="autoEvaluation" type="module.siadap.domain.scoring.SiadapObjectivesEvaluation"/>
		</td>
		<td rowspan="3" valign="middle">
			<fr:view name="objectiveEvaluation" property="evaluation" type="module.siadap.domain.scoring.SiadapObjectivesEvaluation"/>
		</td> 
	</tr>
	<tr>
		<th> <bean:message key="label.superationCriteria" bundle="SIADAP_RESOURCES"/> </th>
		<td class="aleft"> <fr:view name="objectiveEvaluation" property="superationCriteria"/>	</td>
	</tr>
	<tr>
		<th> <bean:message key="label.type" bundle="SIADAP_RESOURCES"/> </th>
		<td class="aleft"> <fr:view name="objectiveEvaluation" property="type" type="module.siadap.domain.SiadapEvaluationObjectivesType"/>	</td>
	</tr>
	--%>
	
	
</table>
</div>
