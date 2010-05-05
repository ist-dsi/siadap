<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>


<fr:view name="harmonizationUnits">
	<fr:schema type="module.siadap.domain.dto.UnitSiadapEvaluation" bundle="SIADAP_RESOURCES">
		<fr:slot name="unit.partyName" />
		<fr:slot name="unit.acronym"/>
		<fr:slot name="relevantEvaluationPercentage"/>
		<fr:slot name="totalPeople"/>
		<fr:slot name="totalPeopleEvaluated"/>
	</fr:schema>
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
	</fr:layout>	
</fr:view>