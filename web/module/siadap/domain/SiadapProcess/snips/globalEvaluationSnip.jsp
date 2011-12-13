<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<logic:equal name="process" property="siadap.evaluationDone" value="false">
	<em>
		<bean:message key="label.notEvaluatedYet" bundle="SIADAP_RESOURCES"/>
	</em>			
</logic:equal>

<logic:equal name="process" property="siadap.evaluationDone" value="true">			

		<%-- R1 refactoring evaluationData --%>
	<logic:equal name="process" property="siadap.evaluationData2.excellencyAward" value="true">
		<div class="highlightBox">
			<bean:message key="label.suggestedForExcellencyAward" bundle="SIADAP_RESOURCES"/>
		</div>
	</logic:equal>

	<table class="tstyle2" style="width: 100%">
		<tr>
			<th>
				<bean:message key="label.evaluationParameters" bundle="SIADAP_RESOURCES"/>
			</th>
			<th>
				<bean:message key="label.scoring" bundle="SIADAP_RESOURCES"/>
			</th>
			<th>
				<bean:message key="label.ponderation" bundle="SIADAP_RESOURCES"/>
			</th>
			<th>	
				<bean:message key="label.scoringPonderated" bundle="SIADAP_RESOURCES"/>
			</th>
		</tr>
		<tr>
			<td>
				<bean:message key="label.results" bundle="SIADAP_RESOURCES"/>
			</td>
			<td>
				<fr:view name="siadap" property="objectivesScoring"/>
			</td>
			<td>
				<fr:view name="siadap" property="objectivesPonderation"/>%
			</td>
			<td>
				<fr:view name="siadap" property="ponderatedObjectivesScoring"/> 
			</td>
		</tr>
		<tr>
			<td>
				<bean:message key="label.competences" bundle="SIADAP_RESOURCES"/>
			</td>
			<td>
				<fr:view name="siadap" property="competencesScoring"/>
			</td>
			<td>
				<fr:view name="siadap" property="competencesPonderation"/>%
			</td>
			<td>
				<fr:view name="siadap" property="ponderatedCompetencesScoring"/> 
			</td>
		</tr>
		<tr>
			<td colspan="3">
			</td>
			<td>
				<strong><fr:view name="siadap" property="totalEvaluationScoring"/> </strong>
			</td>
		</tr>	
	</table>
		
</logic:equal>
