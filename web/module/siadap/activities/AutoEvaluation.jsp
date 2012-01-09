<%@page import="myorg.util.BundleUtil"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<script src="<%= request.getContextPath() + "/javaScript/alertHandlers.js"%>" type="text/javascript"></script>
<script src="<%= request.getContextPath() + "/javaScript/jquery.alerts.js"%>" type="text/javascript"></script>

<bean:define id="processId" name="process" property="externalId"
	type="java.lang.String" />
<bean:define id="name" name="information" property="activityName" />

<div class=highlightBox>
<h3>Note: A sessão expira em ~30min, lembre-se de gravar regularmente os dados no sistema ou noutro sitio para evitar perder o que escreveu</h3>
</div>

<div class="dinline forminline"><fr:form id="auto-evaluation-input"
	action='<%="/workflowProcessManagement.do?method=process&processId=" + processId + "&activity=" + name%>'>

	<fr:edit id="activityBean" name="information" visible="false" />

	<bean:message key="label.objectives" bundle="SIADAP_RESOURCES" />:
		<table class="tstyle2">
		<logic:iterate id="evaluation" name="information"
			property="process.siadap.objectiveEvaluations"
			type="module.siadap.domain.ObjectiveEvaluation">
			<tr>
				<th  style="white-space: normal !important;" colspan="3" class="aleft"><fr:view name="evaluation" property="objective" /></th>
			</tr>
			<logic:iterate id="indicator" name="evaluation" property="indicators">
			<tr>
				<td  style="white-space: normal !important;" >
					<i><bean:message bundle="SIADAP_RESOURCES" key="label.measurementIndicator.singularLabel"/>:</i> <fr:view name="indicator" property="measurementIndicator"/>
				</td>	
				<td style="white-space: normal !important;">
					<i><bean:message bundle="SIADAP_RESOURCES" key="label.superationCriteria"/>:</i> <fr:view name="indicator" property="superationCriteria"/>
				</td>
				<td><fr:edit name="indicator" slot="autoEvaluation" /></td>
			</tr>
			</logic:iterate>
			<tr><td> </td></tr>
			
		</logic:iterate>
	</table>

	<div><strong><bean:message
		key="label.autoEvaluation.objectivesJustification"
		bundle="SIADAP_RESOURCES" /> (<bean:message key="label.requiredField" bundle="SIADAP_RESOURCES" />) :</strong>
	<p><fr:edit name="information" slot="objectivesJustification"
		type="java.lang.String">
		<fr:layout name="longText">
			<fr:property name="rows" value="8" />
			<fr:property name="columns" value="80" />
		</fr:layout>
	</fr:edit></p>
	</div>

	<bean:message key="label.competences" bundle="SIADAP_RESOURCES" />:
	    <table class="tstyle2" style="white-space: normal !important;">
		<logic:iterate id="competence" name="information"
			property="process.siadap.competenceEvaluations">
			<tr>
				<th class="aleft"><fr:view name="competence" property="competence.name" /></th>
				<td><fr:edit name="competence" slot="autoEvaluation" /></td>
			</tr>
		</logic:iterate>
	</table>

	<div><strong><bean:message
		key="label.autoEvaluation.competencesJustification"
		bundle="SIADAP_RESOURCES" /> (<bean:message key="label.requiredField" bundle="SIADAP_RESOURCES" />) :</strong>

	<p><fr:edit name="information" slot="competencesJustification">
		<fr:layout name="longText">
			<fr:property name="rows" value="8" />
			<fr:property name="columns" value="80" />
		</fr:layout>
	</fr:edit></p>
	</div>


	<strong>
	<bean:message key="label.autoEvaluation.performanceInfluencingFactors" bundle="SIADAP_RESOURCES"/>:
	</strong>
	
	<bean:message key="label.autoEvaluation.performanceInfluencingFactors.explanation" bundle="SIADAP_RESOURCES"/>
	
	<fr:edit name="information" >
		<fr:schema
			type="module.siadap.activities.AutoEvaluationActivityInformation"
			bundle="SIADAP_RESOURCES">
			<fr:slot name="factorOneClassification"
				key="label.autoEvaluation.factorOneClassification"
				layout="radio-select">
				<fr:property name="classes" value="liinline" />
				<fr:property name="saveOptions" value="true" />
				<fr:property name="providerClass"
					value="module.siadap.presentationTier.renderers.providers.FactorScaleProvider" />
			</fr:slot>
			<fr:slot name="factorTwoClassification"
				key="label.autoEvaluation.factorTwoClassification"
				layout="radio-select">
				<fr:property name="classes" value="liinline" />
				<fr:property name="saveOptions" value="true" />
				<fr:property name="providerClass"
					value="module.siadap.presentationTier.renderers.providers.FactorScaleProvider" />
			</fr:slot>

			<fr:slot name="factorThreeClassification"
				key="label.autoEvaluation.factorThreeClassification"
				layout="radio-select">
				<fr:property name="classes" value="liinline" />
				<fr:property name="saveOptions" value="true" />
				<fr:property name="providerClass"
					value="module.siadap.presentationTier.renderers.providers.FactorScaleProvider" />
			</fr:slot>

			<fr:slot name="factorFourClassification"
				key="label.autoEvaluation.factorFourClassification"
				layout="radio-select">
				<fr:property name="classes" value="liinline" />
				<fr:property name="saveOptions" value="true" />
				<fr:property name="providerClass"
					value="module.siadap.presentationTier.renderers.providers.FactorScaleProvider" />
			</fr:slot>

			<fr:slot name="factorFiveClassification"
				key="label.autoEvaluation.factorFiveClassification"
				layout="radio-select">
				<fr:property name="classes" value="liinline" />
				<fr:property name="saveOptions" value="true" />
				<fr:property name="providerClass"
					value="module.siadap.presentationTier.renderers.providers.FactorScaleProvider" />
			</fr:slot>

			<fr:slot name="factorSixClassification"
				key="label.autoEvaluation.factorSixClassification"
				layout="radio-select">
				<fr:property name="classes" value="liinline" />
				<fr:property name="saveOptions" value="true" />
				<fr:property name="nullOptionBundle" value="SIADAP_RESOURCES"/>
				<fr:property name="nullOptionKey" value="label.nullRadioOption"/>
				<fr:property name="providerClass"
					value="module.siadap.presentationTier.renderers.providers.FactorScaleProvider" />
			</fr:slot>

		</fr:schema>
		<fr:layout name="tabular">
			<fr:property name="classes" value="tstyle2" />
			<fr:property name="columnClasses" value="aleft,,tderror"/>
		</fr:layout>
	</fr:edit>
	</table>
	<div><strong><bean:message
		key="label.autoEvaluation.otherFactorsJustification"
		bundle="SIADAP_RESOURCES" />:</strong>

	<p><fr:edit name="information" slot="otherFactorsJustification"
		type="java.lang.String">
		<fr:layout name="longText">
			<fr:property name="rows" value="8" />
			<fr:property name="columns" value="80" />
		</fr:layout>
	</fr:edit></p>
	</div>

	<div><strong><bean:message
		key="label.autoEvaluation.extremesJustification"
		bundle="SIADAP_RESOURCES" />:</strong>

	<p><fr:edit name="information" slot="extremesJustification"
		type="java.lang.String">
		<fr:layout name="longText">
			<fr:property name="rows" value="8" />
			<fr:property name="columns" value="80" />
		</fr:layout>
	</fr:edit></p>
	</div>

	<div><strong><bean:message
		key="label.autoEvaluation.commentsAndProposals"
		bundle="SIADAP_RESOURCES" />:</strong>

	<p><fr:edit name="information" slot="commentsAndProposals"
		type="java.lang.String">
		<fr:layout name="longText">
			<fr:property name="rows" value="8" />
			<fr:property name="columns" value="80" />
		</fr:layout>
	</fr:edit></p>
	</div>
	<html:submit styleClass="inputbutton">
		<bean:message key="label.save"
			bundle="SIADAP_RESOURCES" />
	</html:submit>
</fr:form> 
<script>
    $(document).ready(function() {
        $("<div class=\"dinline forminline\"><form id='" + 'auto-evaluation-input' + "form' action='" + 'teste' + "' method=\"post\"'></form></div>").insertBefore("#" + 'auto-evaluation-input'); 
    });

</script>

<fr:form
	action='<%="/workflowProcessManagement.do?method=viewProcess&processId=" + processId%>'>
	<html:submit styleClass="inputbutton">
		<bean:message key="renderers.form.cancel.name"
			bundle="RENDERER_RESOURCES" />
	</html:submit>
</fr:form></div>

<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/9" />	
</jsp:include>