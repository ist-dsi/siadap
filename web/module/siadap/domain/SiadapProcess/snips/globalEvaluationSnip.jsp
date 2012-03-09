<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<logic:equal name="process" property="siadap.defaultEvaluationDone" value="false">
	<em>
		<bean:message key="label.notEvaluatedYet" bundle="SIADAP_RESOURCES"/>
	</em>			
</logic:equal>

<logic:iterate id="siadapEvaluationUniverse" name="process" property="siadap.siadapEvaluationUniverses">
	<logic:equal name="siadapEvaluationUniverse" property="evaluationDone" value="true">			
	<%--Showing regular evaluations --%>
		<logic:equal name="siadapEvaluationUniverse" property="curriculumPonderation" value="false">
					<%-- R1 refactoring evaluationData --%>
				<logic:equal name="process" property="siadap.evaluationData2.excellencyAward" value="true">
					<div class="highlightBox">
						<bean:message key="label.suggestedForExcellencyAward" bundle="SIADAP_RESOURCES"/>
					</div>
				</logic:equal>
			
			<logic:equal value="true" name="siadapEvaluationUniverse" property="defaultEvaluationUniverse">
 				<p><b><i>Avaliação principal:</i></b></p>
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
							<fr:view name="siadapEvaluationUniverse" property="objectivesScoring"/>
						</td>
						<td>
							<fr:view name="siadapEvaluationUniverse" property="objectivesPonderation"/>%
						</td>
						<td>
							<fr:view name="siadapEvaluationUniverse" property="ponderatedObjectivesScoring"/> 
						</td>
					</tr>
					<tr>
						<td>
							<bean:message key="label.competences" bundle="SIADAP_RESOURCES"/>
						</td>
						<td>
							<fr:view name="siadapEvaluationUniverse" property="competencesScoring"/>
						</td>
						<td>
							<fr:view name="siadapEvaluationUniverse" property="competencesPonderation"/>%
						</td>
						<td>
							<fr:view name="siadapEvaluationUniverse" property="ponderatedCompetencesScoring"/> 
						</td>
					</tr>
					<tr>
						<td colspan="3">
						</td>
						<td>
							<strong><fr:view name="siadapEvaluationUniverse" property="totalEvaluationScoring"/> </strong>
						</td>
					</tr>	
				</table>
				<p><b>Nota final (após validação)</b>
					<logic:present name="siadapEvaluationUniverse" property="ccaClassification">
						<fr:view name="siadapEvaluationUniverse" property="ccaClassification"/>
					</logic:present>
					<logic:notPresent name="siadapEvaluationUniverse" property="ccaClassification">
						<fr:view name="siadapEvaluationUniverse" property="totalEvaluationScoring"/>
					</logic:notPresent>
					(<fr:view name="siadapEvaluationUniverse" property="siadapGlobalEvaluationEnumAfterValidation"/>) </p> 
					
				</logic:equal>
				<%--Showing curricular ponderation evaluations --%>
				<logic:equal name="siadapEvaluationUniverse" property="curriculumPonderation" value="true">
					<p><i><b>Avaliação por ponderação curricular:</b></i></p>
					<p>Nota sugerida pelo avaliador: <fr:view name="siadapEvaluationUniverse" property="totalEvaluationScoring"/></p>
					<p><b>Nota final (após validação)</b>
					<logic:present name="siadapEvaluationUniverse" property="ccaClassification">
						<fr:view name="siadapEvaluationUniverse" property="ccaClassification"/>
					</logic:present>
					<logic:notPresent name="siadapEvaluationUniverse" property="ccaClassification">
						<fr:view name="siadapEvaluationUniverse" property="totalEvaluationScoring"/>
					</logic:notPresent>
					(<fr:view name="siadapEvaluationUniverse" property="siadapGlobalEvaluationEnumAfterValidation"/>) </p> 
				</logic:equal>
			
	</logic:equal>

</logic:iterate>