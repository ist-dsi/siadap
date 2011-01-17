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
<% SiadapProcess siadapProcess = (SiadapProcess) request.getAttribute("process");
boolean showObjectivesAndCompetences = siadapProcess.getSiadap().getObjectivesAndCompetencesSealedDate() != null || siadapProcess.getSiadap().getEvaluator().getPerson().getUser().equals(user);
boolean objectivesVisibileToEvaluated = siadapProcess.getSiadap().getObjectivesAndCompetencesSealedDate() != null;
request.setAttribute("showObjectivesAndCompetences", showObjectivesAndCompetences);
request.setAttribute("objectivesVisibleToEvaluated", objectivesVisibileToEvaluated);
%>

<p><strong><bean:write name="evaluatedPersonWrapper" property="nextStep"/></strong></p>

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
	
	
		<logic:iterate id="objective" name="process"
			property="siadap.objectiveEvaluations" indexId="i">
			<bean:define id="index" value="<%=String.valueOf(i + 1)%>"
				toScope="request" />
			<bean:define id="objectiveEvaluation" name="objective"
				toScope="request" />
			<p><jsp:include page="snips/objectiveSnip.jsp" flush="true" /></p>
		</logic:iterate>
	
	<%--  The self evaluation with the justification for the objectives --%>
	<logic:notEmpty name="process" property="siadap.autoEvaluationData" >
		<logic:notEmpty name="process" property="siadap.autoEvaluationData.objectivesJustification">
			<p><strong>(<bean:message key="label.autoEvaluation" bundle="SIADAP_RESOURCES" />) <bean:message key="label.autoEvaluation.objectivesJustification" bundle="SIADAP_RESOURCES"/>:</strong></p>
			<p><bean:write name="process" property="siadap.autoEvaluationData.objectivesJustification" /></p>
		</logic:notEmpty>
	</logic:notEmpty>
	
	
	<logic:equal value="true" name="process" property="siadap.evaluatedOnlyByCompetences" >
		<p><strong><bean:message bundle="SIADAP_RESOURCES" key="label.evaluatedOnlyByCompetences" /></p></strong>
	</logic:equal>
	<h4><bean:message key="label.competences"
		bundle="SIADAP_RESOURCES" />:</h4>
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
				<fr:slot name="autoEvaluation" />
				<fr:slot name="evaluation" />
			</fr:schema>
			<fr:layout name="tabular">
				<fr:property name="classes" value="tstyle2 width100pc" />
				<fr:property name="sortBy" value="competence.number"/>
			</fr:layout>
		</fr:view>
	
	<%--  The self evaluation with the justification for the competences --%>
	<logic:notEmpty name="process" property="siadap.autoEvaluationData" >
		<logic:notEmpty name="process" property="siadap.autoEvaluationData.competencesJustification">
			<p><strong>(<bean:message key="label.autoEvaluation" bundle="SIADAP_RESOURCES" />) <bean:message key="label.autoEvaluation.competencesJustification" bundle="SIADAP_RESOURCES"/>:</strong></p>
			<p><bean:write name="process" property="siadap.autoEvaluationData.objectivesJustification" /></p>
		</logic:notEmpty>
	
	<%--  The factors list--%>
		<logic:notEmpty name="process" property="siadap.autoEvaluationData.factorOneClassification">
			<p><strong>(<bean:message key="label.autoEvaluation" bundle="SIADAP_RESOURCES" />) <bean:message bundle="SIADAP_RESOURCES" key="label.autoEvaluation.performanceInfluencingFactors" />:</strong></p>
			<p><bean:message bundle="SIADAP_RESOURCES" key="label.autoEvaluation.performanceInfluencingFactors.show.explanation"/></p>
			<fr:view name="process" property="siadap.autoEvaluationData" >
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
		<logic:notEmpty name="process" property="siadap.autoEvaluationData.otherFactorsJustification">
			<p><strong>(<bean:message key="label.autoEvaluation" bundle="SIADAP_RESOURCES" />) <bean:message bundle="SIADAP_RESOURCES" key="label.autoEvaluation.show.otherFactorsJustification" /></strong></p>
			<p><bean:write name="process" property="siadap.autoEvaluationData.otherFactorsJustification"/></p>
		</logic:notEmpty>
		
		<%-- Extreme values of the factors justification --%>
		<logic:notEmpty name="process" property="siadap.autoEvaluationData.extremesJustification">
			<p><strong>(<bean:message key="label.autoEvaluation" bundle="SIADAP_RESOURCES" />) <bean:message bundle="SIADAP_RESOURCES" key="label.autoEvaluation.show.extremesJustification" /></strong></p>
			<p><bean:write name="process" property="siadap.autoEvaluationData.extremesJustification"/></p>
		</logic:notEmpty>
		
		<%-- Suggestions and proposals given by the evaluated person--%>
		<logic:notEmpty name="process" property="siadap.autoEvaluationData.commentsAndProposals">
			<p><strong>(<bean:message key="label.autoEvaluation" bundle="SIADAP_RESOURCES" />) <bean:message bundle="SIADAP_RESOURCES" key="label.autoEvaluation.show.commentsAndProposals" /></strong></p>
			<p><bean:write name="process" property="siadap.autoEvaluationData.commentsAndProposals"/></p>
		</logic:notEmpty>
	</logic:notEmpty>
	
	<logic:equal name="process" property="siadap.withSkippedEvaluation" value="false">
		<p><strong><bean:message key="label.overalEvaluation" bundle="SIADAP_RESOURCES"/>:</strong></p>
		<p>
			<bean:define id="siadap" name="process" property="siadap" toScope="request"/>
			<jsp:include page="snips/globalEvaluationSnip.jsp" flush="true"/>
		</p>
	</logic:equal>
	
	<logic:equal name="process" property="siadap.evaluationDone" value="true">
	
	<logic:equal name="process" property="siadap.withSkippedEvaluation" value="false">
	
		
		<p>
			<strong>
				<bean:message key="label.show.evaluationJustification" bundle="SIADAP_RESOURCES"/>:
			</strong>
		</p>
			<p>
				<logic:notEmpty name="process" property="siadap.evaluationData.evaluationJustification">
					<fr:view name="process" property="siadap.evaluationData.evaluationJustification"/>
				</logic:notEmpty>
				<logic:empty name="process" property="siadap.evaluationData.evaluationJustification">
					<em><bean:message key="label.noJustification" bundle="SIADAP_RESOURCES"/></em>
				</logic:empty>
			</p>
	
		<logic:notEmpty name="process" property="siadap.evaluationData.personalDevelopment">
			<p>
				<strong>
					<bean:message key="label.show.personalDevelopment" bundle="SIADAP_RESOURCES"/>:
				</strong>
			</p>
			<p>
				<fr:view name="process" property="siadap.evaluationData.personalDevelopment"/>
			</p>
		</logic:notEmpty>
		
		
		<logic:notEmpty name="process" property="siadap.evaluationData.trainningNeeds">
			<p><strong>
				<bean:message key="label.show.trainingNeeds" bundle="SIADAP_RESOURCES"/>:
			</strong></p>
			<p>
				<fr:view name="process" property="siadap.evaluationData.trainningNeeds"/>
			</p>
		</logic:notEmpty>
		
	</logic:equal>
	</logic:equal>
	
	<logic:equal name="process" property="siadap.withSkippedEvaluation" value="true">
		<p>
			<strong><bean:message key="label.noEvaluationJustification" bundle="SIADAP_RESOURCES"/>:</strong>
			<p>
				<fr:view name="process" property="siadap.evaluationData.noEvaluationJustification"/>
			</p>
		</p>
	</logic:equal>

<%-- End of condition if the objectives and competences should show or not --%>
</logic:equal>

<logic:notEqual name="showObjectivesAndCompetences" value="true">
<p><bean:message bundle="SIADAP_RESOURCES" key="label.objectivesAndCompetencesNotSealedYet"/></p>
</logic:notEqual>
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/20" />	
</jsp:include>

