<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>
<%@ taglib uri="/WEB-INF/workflow.tld" prefix="wf"%>

<bean:define id="objectiveEvaluation" name="objectiveEvaluation"/>
<bean:define id="index" name="index"/>

<bean:define id="objectiveOID" name="objectiveEvaluation" property="externalId" type="java.lang.String"/>

<bean:define id="classToApply" value=""/>

<logic:equal name="process" property="currentUserEvaluated" value="true">
	<logic:notPresent name="objectiveEvaluation" property="acknowledgeDate"> 
		<bean:define id="classToApply" value="highlightBox"/>
	</logic:notPresent>
</logic:equal>

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
				<fr:view name="indicator" property="autoEvaluation" type="module.siadap.domain.scoring.SiadapObjectivesEvaluation"/>
			</td>
			<td valign="middle">
				<fr:view name="indicator" property="evaluation" type="module.siadap.domain.scoring.SiadapObjectivesEvaluation"/>
			</td> 
			<td valign="middle">
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