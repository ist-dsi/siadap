<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<strong><bean:message key="label.objectives"
	bundle="SIADAP_RESOURCES" />:</strong>

<logic:equal name="process" property="siadap.evaluationDone" value="true">
<div>	
<bean:message key="label.evaluation" bundle="SIADAP_RESOURCES"/>:	<fr:view name="process" property="siadap.objectivesScoring" /> x <fr:view
		name="process"
		property="siadap.siadapRootModule.objectivesPonderation" />% = <strong><fr:view
		name="process" property="siadap.ponderatedObjectivesScoring" /></strong>
	</div>
</logic:equal>

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

<logic:equal name="process" property="siadap.evaluationDone" value="true">
	<div>	
<bean:message key="label.evaluation" bundle="SIADAP_RESOURCES"/>:<fr:view name="process" property="siadap.competencesScoring" /> x <fr:view
		name="process"
		property="siadap.siadapRootModule.competencesPonderation" />% = <strong><fr:view
		name="process" property="siadap.ponderatedCompetencesScoring" /></strong>
		</div>
</logic:equal>
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
		</fr:layout>
	</fr:view>
</p>

<strong><bean:message key="label.overalEvaluation" bundle="SIADAP_RESOURCES"/></strong>:

<fr:view name="process" property="siadap.totalEvaluationScoring"/>
<fr:view name="process" property="siadap.qualitativeEvaluation" type="module.siadap.domain.SiadapEvaluation"/>
<fr:view name="process" property="siadap.evaluationJustification" type="java.lang.String"/>
