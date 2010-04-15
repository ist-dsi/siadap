<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<bean:define id="objectiveEvaluation" name="objectiveEvaluation"/>
<bean:define id="index" name="index"/>

<div>
<table class="tstyle2 width100pc">
	<tr>
		<th rowspan="3" valign="middle" class="width30px">
			<fr:view name="index"/>
		</th>
		<th style="width30px"> <bean:message key="label.objective" bundle="SIADAP_RESOURCES"/> </th>
		<td style="width: 450px" class="aleft"> <fr:view name="objectiveEvaluation" property="objective"/>	</td>
		<th>
			<strong><bean:message key="label.autoEvaluation" bundle="SIADAP_RESOURCES"/></strong>
		</th>
		<th> 
		<strong>	<bean:message key="label.evaluation" bundle="SIADAP_RESOURCES"/></strong>
		</th>
	</tr>
	<tr>
		<th class="width30px"> <bean:message key="label.measurementIndicator" bundle="SIADAP_RESOURCES"/> </th>
		<td style="width: 450px" class="aleft"> <fr:view name="objectiveEvaluation" property="measurementIndicator"/>	</td>
		<td rowspan="2" valign="middle">
			<fr:view name="objectiveEvaluation" property="autoEvaluation" type="module.siadap.domain.SiadapEvaluation"/>
		</td>
		<td rowspan="2" valign="middle">
			<fr:view name="objectiveEvaluation" property="evaluation" type="module.siadap.domain.SiadapEvaluation"/>
		</td> 
	</tr>
	<tr>
		<th class="width30px"> <bean:message key="label.superationCriteria" bundle="SIADAP_RESOURCES"/> </th>
		<td style="width: 450px" class="aleft"> <fr:view name="objectiveEvaluation" property="superationCriteria"/>	</td>
	</tr>
</table>
</div>