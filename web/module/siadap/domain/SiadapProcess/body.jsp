<%@page import="module.siadap.domain.Siadap"%>
<%@page import="module.siadap.domain.SiadapProcessSchedulesEnum"%>
<%@page import="module.siadap.domain.SiadapProcess"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>
<%@ taglib uri="/WEB-INF/workflow.tld" prefix="wf"%>

<%-- Showing only the objectives and competences if we are the evaluator or they have been sealed --%>
<%-- A simple definition to be able to use the process on the Java code --%>
<bean:define id="processJava" name="process"/>
<bean:define id="evaluatorPersonWrapper" name="process" property="siadap.evaluator"/>
<bean:define id="evaluatedPersonWrapper" name="process" property="siadap.evaluatedWrapper"/>
<bean:define id="user" name="USER_SESSION_ATTRIBUTE" property="user"/>
<bean:define id="siadap" name="process" property="siadap"/>
<% SiadapProcess siadapProcess = (SiadapProcess) request.getAttribute("process");
boolean showObjectivesAndCompetences = siadapProcess.getSiadap().getObjectivesAndCompetencesSealedDate() != null || siadapProcess.getSiadap().getEvaluator().getPerson().getUser().equals(user);
boolean objectivesVisibileToEvaluated = siadapProcess.getSiadap().getObjectivesAndCompetencesSealedDate() != null;
request.setAttribute("showObjectivesAndCompetences", showObjectivesAndCompetences);
request.setAttribute("objectivesVisibleToEvaluated", objectivesVisibileToEvaluated);
%>
<style type="text/css">
pre {
	white-space: pre-wrap;
	border: 1pt solid #AEBDCC;
	background-color: #F3F5F7;
}
</style>
<%--	Warning messages --%>
<logic:notEmpty name="process" property="warningMessages">
	<div class="highlightBox">
			<logic:iterate id="warningMessage" name="process" property="andClearWarningMessages" indexId="warningMessage_id">
				<b><p style="color: darkRed" id="<%="warningMessage_id"+warningMessage_id%>"><bean:write name="warningMessage"/></p></b>
			</logic:iterate>
	</div>
</logic:notEmpty>

<logic:equal name="evaluatorPersonWrapper" property="emailDefined" value="false">
<div class="highlightBox"> 
	<b><p style="color:darkRed">Avaliador sem e-mail definido. Este avaliador não poderá receber notificações automáticas via e-mail. Para corrigir isto, por favor insira um contacto de-mail na secção informação pessoal no Fénix</p> </b>
</div>
</logic:equal>
<logic:equal name="evaluatedPersonWrapper" property="emailDefined" value="false">
<div class="highlightBox"> 
	<b><p style="color:darkRed">Avaliado sem e-mail definido. Este avaliado não poderá receber notificações automáticas via e-mail. Para corrigir isto, por favor insira um contacto de-mail na secção informação pessoal no Fénix</p> </b>
</div>
</logic:equal>
<h3><bean:message bundle="SIADAP_RESOURCES" key="label.siadap.schedule" arg0="<%= ((SiadapProcess) processJava).getSiadap().getYear().toString()%>" />:</h3>
<%-- START: The table with the deadlines and custom deadlines if they are defined --%>
<table class="tstyle3 thleft mvert10px">
	<tbody>
		<tr>
			<th><bean:message  key="label.config.objectiveSpecificationBegin" bundle="SIADAP_RESOURCES"/></th>
			<logic:empty name="siadap" property="customObjectiveSpecificationBegin">
				<logic:notEmpty name="siadap" property="siadapYearConfiguration.objectiveSpecificationBegin">
					<td><fr:view name="siadap" property="siadapYearConfiguration.objectiveSpecificationBegin" /></td>
				</logic:notEmpty>
			</logic:empty>
			<logic:notEmpty name="siadap" property="customObjectiveSpecificationBegin">
				<logic:notEmpty name="siadap" property="siadapYearConfiguration.objectiveSpecificationBegin">
					<td style="text-decoration: line-through"><fr:view name="siadap" property="siadapYearConfiguration.objectiveSpecificationBegin" /></td>
				</logic:notEmpty>
				<td style="background: pink"><fr:view name="siadap" property="customObjectiveSpecificationBegin"/></td>
				<td><wf:activityLink processName="process" activityName="RemoveCustomSchedule" scope="request" paramName0="siadapProcessSchedulesEnumToRemove" paramValue0="<%=SiadapProcessSchedulesEnum.OBJECTIVES_SPECIFICATION_BEGIN_DATE.name()%>"><bean:message bundle="SIADAP_RESOURCES" key="activity.RemoveCustomSchedule" /></wf:activityLink></td>
			</logic:notEmpty>
		</tr>
		
		<tr>
			<th><bean:message  key="label.config.objectiveSpecificationEnd" bundle="SIADAP_RESOURCES"/></th>
			<logic:empty name="siadap" property="customObjectiveSpecificationEnd">
				<logic:notEmpty name="siadap" property="siadapYearConfiguration.objectiveSpecificationEnd" >
					<td><fr:view name="siadap" property="siadapYearConfiguration.objectiveSpecificationEnd" /></td>
				</logic:notEmpty>
			</logic:empty>
			<logic:notEmpty name="siadap" property="customObjectiveSpecificationEnd">
				<logic:notEmpty name="siadap" property="siadapYearConfiguration.objectiveSpecificationEnd" >
					<td style="text-decoration: line-through"><fr:view name="siadap" property="siadapYearConfiguration.objectiveSpecificationEnd" /></td>
				</logic:notEmpty>
				<td style="background: pink"><fr:view name="siadap" property="customObjectiveSpecificationEnd"/></td>
				<td><wf:activityLink processName="process" activityName="RemoveCustomSchedule" scope="request" paramName0="siadapProcessSchedulesEnumToRemove" paramValue0="<%=SiadapProcessSchedulesEnum.OBJECTIVES_SPECIFICATION_END_DATE.name()%>"><bean:message bundle="SIADAP_RESOURCES" key="activity.RemoveCustomSchedule" /></wf:activityLink></td>
			</logic:notEmpty>
		</tr>
		
		<tr>
			<th><bean:message  key="label.autoEvaluationBegin" bundle="SIADAP_RESOURCES"/></th>
			<logic:empty name="siadap" property="customAutoEvaluationBegin">
				<logic:notEmpty name="siadap" property="siadapYearConfiguration.autoEvaluationBegin">
					<td><fr:view name="siadap" property="siadapYearConfiguration.autoEvaluationBegin"/></td>
				</logic:notEmpty>
			</logic:empty>
			<logic:notEmpty name="siadap" property="customAutoEvaluationBegin">
				<logic:notEmpty name="siadap" property="siadapYearConfiguration.autoEvaluationBegin">
					<td style="text-decoration: line-through"><fr:view name="siadap" property="siadapYearConfiguration.autoEvaluationBegin" /></td>
				</logic:notEmpty>
				<td style="background: pink"><fr:view name="siadap" property="customAutoEvaluationBegin"/></td>
				<td><wf:activityLink processName="process" activityName="RemoveCustomSchedule" scope="request" paramName0="siadapProcessSchedulesEnumToRemove" paramValue0="<%=SiadapProcessSchedulesEnum.AUTOEVALUATION_BEGIN_DATE.name()%>"><bean:message bundle="SIADAP_RESOURCES" key="activity.RemoveCustomSchedule" /></wf:activityLink></td>
			</logic:notEmpty>
		</tr>
		
		<tr>
			<th><bean:message  key="label.autoEvaluationEnd" bundle="SIADAP_RESOURCES"/></th>
			<logic:empty name="siadap" property="customAutoEvaluationEnd">
				<logic:notEmpty name="siadap" property="siadapYearConfiguration.autoEvaluationEnd">
					<td><fr:view name="siadap" property="siadapYearConfiguration.autoEvaluationEnd" /></td>
				</logic:notEmpty>
			</logic:empty>
			<logic:notEmpty name="siadap" property="customAutoEvaluationEnd">
				<logic:notEmpty name="siadap" property="siadapYearConfiguration.autoEvaluationEnd">
					<td style="text-decoration: line-through"><fr:view name="siadap" property="siadapYearConfiguration.autoEvaluationEnd" /></td>
				</logic:notEmpty>
				<td style="background: pink"><fr:view name="siadap" property="customAutoEvaluationEnd"/></td>
				<td><wf:activityLink processName="process" activityName="RemoveCustomSchedule" scope="request" paramName0="siadapProcessSchedulesEnumToRemove" paramValue0="<%=SiadapProcessSchedulesEnum.AUTOEVALUATION_END_DATE.name()%>"><bean:message bundle="SIADAP_RESOURCES" key="activity.RemoveCustomSchedule" /></wf:activityLink></td>
			</logic:notEmpty>
		</tr>
		
		<tr>
			<th><bean:message  key="label.evaluationBegin" bundle="SIADAP_RESOURCES"/></th>
			<logic:empty name="siadap" property="customEvaluationBegin">
				<logic:notEmpty name="siadap" property="siadapYearConfiguration.evaluationBegin">
					<td><fr:view name="siadap" property="siadapYearConfiguration.evaluationBegin"/></td>
				</logic:notEmpty>
			</logic:empty>
			<logic:notEmpty name="siadap" property="customEvaluationBegin">
				<logic:notEmpty name="siadap" property="siadapYearConfiguration.evaluationBegin">
					<td style="text-decoration: line-through"><fr:view name="siadap" property="siadapYearConfiguration.evaluationBegin" /></td>
				</logic:notEmpty>
				<td style="background: pink"><fr:view name="siadap" property="customEvaluationBegin"/></td>
				<td><wf:activityLink processName="process" activityName="RemoveCustomSchedule" scope="request" paramName0="siadapProcessSchedulesEnumToRemove" paramValue0="<%=SiadapProcessSchedulesEnum.EVALUATION_BEGIN_DATE.name()%>"><bean:message bundle="SIADAP_RESOURCES" key="activity.RemoveCustomSchedule" /></wf:activityLink></td>
			</logic:notEmpty>
		</tr>
		
		<tr>
			<th><bean:message  key="label.evaluationEnd" bundle="SIADAP_RESOURCES"/></th>
			<logic:empty name="siadap" property="customEvaluationEnd">
				<logic:notEmpty name="siadap" property="siadapYearConfiguration.evaluationEnd" >
					<td><fr:view name="siadap" property="siadapYearConfiguration.evaluationEnd"/></td>
				</logic:notEmpty>
			</logic:empty>
			<logic:notEmpty name="siadap" property="customEvaluationEnd">
				<logic:notEmpty name="siadap" property="siadapYearConfiguration.evaluationEnd" >
					<td style="text-decoration: line-through"><fr:view name="siadap" property="siadapYearConfiguration.evaluationEnd" /></td>
				</logic:notEmpty>
				<td style="background: pink"><fr:view name="siadap" property="customEvaluationEnd"/></td>
				<td><wf:activityLink processName="process" activityName="RemoveCustomSchedule" scope="request" paramName0="siadapProcessSchedulesEnumToRemove" paramValue0="<%=SiadapProcessSchedulesEnum.EVALUATION_END_DATE.name()%>"><bean:message bundle="SIADAP_RESOURCES" key="activity.RemoveCustomSchedule" /></wf:activityLink></td>
			</logic:notEmpty>
		</tr>
	
	</tbody>
</table>
<%-- END: The table with the deadlines and custom deadlines if they are defined --%>
<div class="highlightBox"> 
	<p><strong><bean:message bundle="SIADAP_RESOURCES" key="<%= ((Siadap) siadap).getState().getLabelPrefix() %>" /> - <bean:write name="evaluatedPersonWrapper" property="nextStep"/></strong></p>
</div>

<p><b>Carreira, no âmbito do SIADAP (Universo SIADAP):</b>
		 <logic:notEmpty name="evaluatedPersonWrapper" property="careerName">
		  	<bean:write name="evaluatedPersonWrapper" property="careerName"/> 
		 </logic:notEmpty>
		 <logic:empty name="evaluatedPersonWrapper" property="careerName">
		 	<bean:message key="competenceType.notDefined" bundle="SIADAP_RESOURCES"/>
		 </logic:empty>
		 <logic:empty name="process" property="siadap.defaultSiadapUniverse">
		 	(<bean:message key="label.undefinedUniverse" bundle="SIADAP_RESOURCES"/>)
		 </logic:empty>
		 <logic:notEmpty name="process" property="siadap.defaultSiadapUniverse">
		 	(<bean:write name="process" property="siadap.defaultSiadapUniverse"/>)
		 </logic:notEmpty>
</p>

<logic:equal name="evaluatedPersonWrapper" property="currentUserAbleToSeeDetails" value="true">
	<logic:equal name="showObjectivesAndCompetences" value="true">
		<logic:equal name="process" property="siadap.evaluatedWithKnowledgeOfObjectives" value="false">
			<logic:equal name="objectivesVisibleToEvaluated" value="true">
			<div class="highlightBox mtop05 mbottom15">
				<bean:message key="label.info.objectivesNotKownToEvaluated" bundle="SIADAP_RESOURCES"/>
			</div>
			</logic:equal>
		</logic:equal>
		
		<%-- Messages only shown to the evaluator: --%>
		<logic:equal name="evaluatorPersonWrapper" property="person.user.username" value="<%=user.toString()%>">
			<logic:equal value="false" name="process" property="siadap.allEvaluationItemsValid" >
				<div class="highlightBox mtop05 mbottom15" style="background: lightPink">
					<bean:message key="warning.invalid.objectives.ponderationFactors" bundle="SIADAP_RESOURCES"/>
				</div>
			</logic:equal>
			
			<logic:equal value="false" name="process" property="siadap.coherentOnTypeOfEvaluation">
				<div class="highlightBox mtop05 mbottom15" style="background: lightPink">
					<bean:message key="warning.invalid.incoherent.evaluation.items" bundle="SIADAP_RESOURCES"/>
				</div>
			</logic:equal>
		</logic:equal>
		
		<logic:equal name="process" property="siadap.autoEvaluationIntervalFinished" value="true">
			<logic:equal name="process" property="siadap.autoEvaliationDone" value="false">
				<div class="highlightBox mtop05 mbottom15">
					<bean:message key="label.info.evaluatedFailedToDoAutoEvaluation" bundle="SIADAP_RESOURCES"/>
				</div>
			</logic:equal>
		</logic:equal>
		
		<h3><bean:message key="label.results"
			bundle="SIADAP_RESOURCES" />:</h3>
		
		<h4><bean:message key="label.objectives" bundle="SIADAP_RESOURCES" />:</h4>
		
			<logic:iterate id="objective" name="process"
				property="siadap.objectiveEvaluations" indexId="i">
				<bean:define id="index" value="<%=String.valueOf(i + 1)%>"
					toScope="request" />
				<bean:define id="objectiveEvaluation" name="objective"
					toScope="request" />
				<p><jsp:include page="snips/objectiveSnip.jsp" flush="true" /></p>
			</logic:iterate>
		
		<%--  The self evaluation with the justification for the objectives --%>
		<logic:equal name="evaluatedPersonWrapper" property="currentUserAbleToSeeAutoEvaluationDetails" value="true">
		<%-- R1 refactoring autoEvaluationData --%>
			<logic:notEmpty name="process" property="siadap.autoEvaluationData2" >
				<logic:notEmpty name="process" property="siadap.autoEvaluationData2.objectivesJustification">
					<p><strong>(<bean:message key="label.autoEvaluation" bundle="SIADAP_RESOURCES" />) <bean:message key="label.autoEvaluation.objectivesJustification" bundle="SIADAP_RESOURCES"/>:</strong></p>
					<pre><bean:write name="process" property="siadap.autoEvaluationData2.objectivesJustification" /></pre>
				</logic:notEmpty>
			</logic:notEmpty>
		</logic:equal>
		
		<logic:equal value="true" name="process" property="siadap.evaluatedOnlyByCompetences" >
			<p><strong><bean:message bundle="SIADAP_RESOURCES" key="label.evaluatedOnlyByCompetences" /></p></strong>
		</logic:equal>
				<h4><bean:message key="label.competences" bundle="SIADAP_RESOURCES" />:</h4>
			<%-- link to allow to edit the competences--%>
			<wf:isActive processName="process" activityName="EditCompetenceEvaluation" scope="request">		
							<span>
							<wf:activityLink id="editCompetences" processName="process" activityName="EditCompetenceEvaluation" scope="request" >
									<bean:message key="link.edit" bundle="MYORG_RESOURCES"/>
							</wf:activityLink>	
							</span>
						</wf:isActive>	
						
			<fr:view name="process" property="siadap.competenceEvaluations">
				<fr:schema bundle="SIADAP_RESOURCES"
					type="module.siadap.domain.CompetenceEvaluation">
					<fr:slot name="competence.number" />
					<fr:slot name="competence.name" />
					<logic:equal name="evaluatedPersonWrapper" property="currentUserAbleToSeeAutoEvaluationDetails" value="true"> 
						<fr:slot name="autoEvaluation" />
					</logic:equal>
					<logic:equal name="evaluatedPersonWrapper" property="currentUserAbleToSeeEvaluationDetails" value="true">
						<fr:slot name="evaluation" />
					</logic:equal>
				</fr:schema>
				<fr:layout name="tabular">
					<fr:property name="classes" value="tstyle2 width100pc" />
					<fr:property name="sortBy" value="competence.number"/>
				</fr:layout>
			</fr:view>
		
		<%--  The self evaluation with the justification for the competences --%>
		<logic:equal name="evaluatedPersonWrapper" property="currentUserAbleToSeeAutoEvaluationDetails" value="true">
		<%-- R1 refactoring autoEvaluationData --%>
			<logic:notEmpty name="process" property="siadap.autoEvaluationData2" >
				<logic:notEmpty name="process" property="siadap.autoEvaluationData2.competencesJustification">
					<p><strong>(<bean:message key="label.autoEvaluation" bundle="SIADAP_RESOURCES" />) <bean:message key="label.autoEvaluation.competencesJustification" bundle="SIADAP_RESOURCES"/>:</strong></p>
					<pre><bean:write name="process" property="siadap.autoEvaluationData2.competencesJustification" /></pre>
				</logic:notEmpty>
			</logic:notEmpty>
		 </logic:equal>
			
		<%--  The factors list--%>
		<%-- R1 refactoring autoEvaluationData --%>
		<logic:equal name="evaluatedPersonWrapper" property="currentUserAbleToSeeAutoEvaluationDetails" value="true">
			<logic:notEmpty name="process" property="siadap.autoEvaluationData2" >
				<logic:notEmpty name="process" property="siadap.autoEvaluationData2.factorOneClassification">
					<p><strong>(<bean:message key="label.autoEvaluation" bundle="SIADAP_RESOURCES" />) <bean:message bundle="SIADAP_RESOURCES" key="label.autoEvaluation.performanceInfluencingFactors" />:</strong></p>
					<p><bean:message bundle="SIADAP_RESOURCES" key="label.autoEvaluation.performanceInfluencingFactors.show.explanation"/></p>
					<fr:view name="process" property="siadap.autoEvaluationData2" >
		<%-- R1 refactoring autoEvaluationData --%>
						<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.domain.SiadapAutoEvaluation">
							<fr:slot name="factorOneClassification" readOnly="true"
							key="label.autoEvaluation.factorOneClassification"/>
							<fr:slot name="factorTwoClassification" readOnly="true" key="label.autoEvaluation.factorTwoClassification"/>
							<fr:slot name="factorThreeClassification" readOnly="true" key="label.autoEvaluation.factorThreeClassification"/>
							<fr:slot name="factorFourClassification" readOnly="true" key="label.autoEvaluation.factorFourClassification"/>
							<fr:slot name="factorFiveClassification" readOnly="true" key="label.autoEvaluation.factorFiveClassification"/>
							<fr:slot name="factorSixClassification" readOnly="true" key="label.autoEvaluation.factorSixClassification"/>
						</fr:schema>
						<fr:layout name="tabular">
							<fr:property name="classes" value="tstyle3 thleft" />
						</fr:layout>
					</fr:view>
				</logic:notEmpty>	
				
				<%-- Other factors --%>
		<%-- R1 refactoring autoEvaluationData --%>
				<logic:notEmpty name="process" property="siadap.autoEvaluationData2.otherFactorsJustification">
					<p><strong>(<bean:message key="label.autoEvaluation" bundle="SIADAP_RESOURCES" />) <bean:message bundle="SIADAP_RESOURCES" key="label.autoEvaluation.show.otherFactorsJustification" /></strong></p>
					<pre><bean:write name="process" property="siadap.autoEvaluationData2.otherFactorsJustification"/></pre>
				</logic:notEmpty>
				
				<%-- Extreme values of the factors justification --%>
		<%-- R1 refactoring autoEvaluationData --%>
				<logic:notEmpty name="process" property="siadap.autoEvaluationData2.extremesJustification">
					<p><strong>(<bean:message key="label.autoEvaluation" bundle="SIADAP_RESOURCES" />) <bean:message bundle="SIADAP_RESOURCES" key="label.autoEvaluation.show.extremesJustification" /></strong></p>
					<pre><bean:write name="process" property="siadap.autoEvaluationData2.extremesJustification"/></pre>
				</logic:notEmpty>
				
				<%-- Suggestions and proposals given by the evaluated person--%>
		<%-- R1 refactoring autoEvaluationData --%>
				<logic:notEmpty name="process" property="siadap.autoEvaluationData2.commentsAndProposals">
					<p><strong>(<bean:message key="label.autoEvaluation" bundle="SIADAP_RESOURCES" />) <bean:message bundle="SIADAP_RESOURCES" key="label.autoEvaluation.show.commentsAndProposals" /></strong></p>
					<pre><bean:write name="process" property="siadap.autoEvaluationData2.commentsAndProposals"/></pre>
				</logic:notEmpty>
			</logic:notEmpty>
		</logic:equal>
		
		<logic:equal name="process" property="siadap.withSkippedEvaluation" value="false">
			<logic:equal name="evaluatedPersonWrapper" property="currentUserAbleToSeeEvaluationDetails" value="true">
				<p><strong><bean:message key="label.overalEvaluation" bundle="SIADAP_RESOURCES"/>:</strong></p>
				<p>
					<bean:define id="siadap" name="process" property="siadap" toScope="request"/>
					<jsp:include page="snips/globalEvaluationSnip.jsp" flush="true"/>
				</p>
			 </logic:equal>
		</logic:equal>
		
		
		<%-- Evaluation justification --%>
		<%-- R1 refactoring evaluationData --%>
		<logic:notEmpty name="process" property="siadap.evaluationData2">
			<logic:equal name="process" property="siadap.withSkippedEvaluation" value="false">
				<logic:equal name="evaluatedPersonWrapper" property="currentUserAbleToSeeEvaluationDetails" value="true">
					<p>
						<strong>
							(<bean:message key="label.evaluation" bundle="SIADAP_RESOURCES" />) <bean:message key="label.show.evaluationJustification" bundle="SIADAP_RESOURCES"/>:
						</strong>
					</p>
						
		<%-- R1 refactoring evaluationData --%>
					<logic:notEmpty name="process" property="siadap.evaluationData2.evaluationJustification">
						<pre><fr:view name="process" property="siadap.evaluationData2.evaluationJustification"/></pre>
					</logic:notEmpty>
					<logic:empty name="process" property="siadap.evaluationData2.evaluationJustification">
						<p><em><bean:message key="label.noJustification" bundle="SIADAP_RESOURCES"/></em></p>
					</logic:empty>
						
		<%-- R1 refactoring evaluationData --%>
					<logic:notEmpty name="process" property="siadap.evaluationData2.excellencyAwardJustification">
						<p>
							<strong>
								(<bean:message key="label.evaluation" bundle="SIADAP_RESOURCES" />) <bean:message key="label.show.excellencyAwardJustification" bundle="SIADAP_RESOURCES"/>:
							</strong>
						</p>
						<pre><fr:view name="process" property="siadap.evaluationData2.excellencyAwardJustification"/></pre>
					</logic:notEmpty>
				
		<%-- R1 refactoring evaluationData --%>
					<logic:notEmpty name="process" property="siadap.evaluationData2.personalDevelopment">
						<p>
							<strong>
								(<bean:message key="label.evaluation" bundle="SIADAP_RESOURCES" />) <bean:message key="label.show.personalDevelopment" bundle="SIADAP_RESOURCES"/>:
							</strong>
						</p>
		<%-- R1 refactoring evaluationData --%>
							<pre><fr:view name="process" property="siadap.evaluationData2.personalDevelopment"/></pre>
					</logic:notEmpty>
					
					
		<%-- R1 refactoring evaluationData --%>
					<logic:notEmpty name="process" property="siadap.evaluationData2.trainningNeeds">
						<p><strong>
							(<bean:message key="label.evaluation" bundle="SIADAP_RESOURCES" />) <bean:message key="label.show.trainingNeeds" bundle="SIADAP_RESOURCES"/>:
						</strong></p>
		<%-- R1 refactoring evaluationData --%>
							<pre><fr:view name="process" property="siadap.evaluationData2.trainningNeeds"/></pre>
					</logic:notEmpty>
				</logic:equal>
			</logic:equal>
		</logic:notEmpty>
		
		<logic:equal name="process" property="siadap.withSkippedEvaluation" value="true">
			<p><strong>(<bean:message key="label.evaluation" bundle="SIADAP_RESOURCES" />) <bean:message key="label.noEvaluationJustification" bundle="SIADAP_RESOURCES"/>:</strong></p>
		<%-- R1 refactoring evaluationData --%>
			<pre><fr:view name="process" property="siadap.evaluationData2.noEvaluationJustification"/></pre>
		</logic:equal>
	
	</logic:equal>
	<%-- End of condition if the objectives and competences should show or not --%>
</logic:equal>
<logic:equal name="evaluatedPersonWrapper" property="currentUserAbleToSeeDetails" value="false">
 - Detalhes do processo confidenciais, se acha que deveria ter acesso, por favor contacte o suporte -
</logic:equal>
<logic:notEqual name="showObjectivesAndCompetences" value="true">
<p><bean:message bundle="SIADAP_RESOURCES" key="label.objectivesAndCompetencesNotSealedYet"/></p>
</logic:notEqual>
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/20" />	
</jsp:include>

