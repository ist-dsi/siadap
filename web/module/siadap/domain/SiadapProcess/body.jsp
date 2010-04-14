<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<logic:iterate id="objective" name="process" property="siadap.objectiveEvaluations" indexId="i">
	<fr:view name="i" />: 
	
	<fr:view name="objective">
		<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.domain.ObjectiveEvaluation">
			<fr:slot name="objective" />
			<fr:slot name="measurementIndicator" />
			<fr:slot name="superationCriteria" />
			<fr:slot name="autoEvaluation" />
			<fr:slot name="evaluation" />
		</fr:schema>
		<fr:layout>
			<fr:property name="columnClasses" value="aleft,"/>
		</fr:layout>
	</fr:view>
</logic:iterate>

<logic:iterate id="competenceEvaluation" name="process" property="siadap.competenceEvaluations">
	<fr:view name="competenceEvaluation">
		<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.domain.CompetenceEvaluation">
			<fr:slot name="competence.number" />
			<fr:slot name="competence.name" />
			<fr:slot name="autoEvaluation" />
			<fr:slot name="evaluation" />
		</fr:schema>
		<fr:layout>
			<fr:property name="columnClasses" value="aleft,"/>
		</fr:layout>
	</fr:view>
</logic:iterate>