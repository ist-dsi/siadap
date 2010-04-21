<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>


<logic:equal name="process" property="siadap.evaluatedWithKnowledgeOfObjectives" value="false">
	<div class="highlightBox mtop05 mbottom15">
		<bean:message key="label.info.objectivesNotKownToEvaluated" bundle="SIADAP_RESOURCES"/>
	</div>
</logic:equal>

<div class="infobox">
	<fr:view name="process" property="siadap">
		<fr:schema type="module.siadap.domain.Siadap" bundle="SIADAP_RESOURCES">
			<fr:slot name="totalEvaluationScoring">
				<fr:property name="classes" value="bold"/>
			</fr:slot>
			<logic:equal name="process" property="siadap.evaluationDone" value="true">
				<fr:slot name="objectivesScoring"/>
				<fr:slot name="process.siadap" key="label.objectivesPonderation" layout="format">
					<fr:property name="format" value="${objectivesPonderation}%%"/> 
				</fr:slot>
				<fr:slot name="competencesScoring"/>
				<fr:slot name="this" key="label.competencesPonderation" layout="format">
					<fr:property name="format" value="${competencesPonderation}%%"/> 
				</fr:slot>
				<fr:slot name="qualitativeEvaluation" />
				<fr:slot name="evaluationJustification"/>
			</logic:equal>
		</fr:schema>
		<fr:layout name="tabular">
			<fr:property name="classes" value="style1"/>
			<fr:property name="columnClasses" value="width165px aleft,,"/>
		</fr:layout>
	</fr:view>
	
	<div id="extraInfo" style="display: none;">
	
	<fr:view name="process" property="siadap">
		<fr:schema type="module.siadap.domain.Siadap" bundle="SIADAP_RESOURCES">
			<fr:slot name="validationDate"/>
			<fr:slot name="acknowledgeValidationDate"/>
			<fr:slot name="homologationDate"/>
			<fr:slot name="acknowledgeHomologationDate"/>
		</fr:schema>
		<fr:layout name="tabular">
			<fr:property name="classes" value="tstyle1"/>
			<fr:property name="columnClasses" value="width165px aleft,,"/>
		</fr:layout>
	</fr:view>
	</div>
	
	<p class="mvert05"><span id="show1" class="link"><bean:message key="label.moreInfo" bundle="SIADAP_RESOURCES"/></span></p>
	<p class="mvert05"><span id="show2" style="display: none" class="link"><bean:message key="label.lessInfo" bundle="SIADAP_RESOURCES"/></span></p>
	
	<script type="text/javascript">
			$("#show1").click(
					function() {
						$('#extraInfo').slideToggle();
						$('#show1').hide();
						$('#show2').show();
					}
				);

			$("#show2").click(
					function() {
						$('#extraInfo').slideToggle();
						$('#show2').hide();
						$('#show1').show();
					}
				);
	</script>
	
</div>

<strong><bean:message key="label.results"
	bundle="SIADAP_RESOURCES" />:</strong>

<logic:iterate id="objective" name="process"
	property="siadap.objectiveEvaluations" indexId="i">
	<bean:define id="index" value="<%=String.valueOf(i + 1)%>"
		toScope="request" />
	<bean:define id="objectiveEvaluation" name="objective"
		toScope="request" />
	<p><jsp:include page="snips/objectiveSnip.jsp" flush="true" /></p>
</logic:iterate>

<strong><bean:message key="label.competences"
	bundle="SIADAP_RESOURCES" />:</strong>
<p>
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
</p>

<strong><bean:message key="label.overalEvaluation" bundle="SIADAP_RESOURCES"/>:</strong>
<p>
	<bean:define id="siadap" name="process" property="siadap" toScope="request"/>
	<jsp:include page="snips/globalEvaluationSnip.jsp" flush="true"/>
</p>