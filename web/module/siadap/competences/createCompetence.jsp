<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr" %>

<h2>
	<fr:view name="bean" property="competenceType" />
</h2>

<fr:edit id="bean" name="bean" action="/competencesManagement.do?method=createCompetence">
	<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.domain.dto.CompetenceBean">
		<fr:slot name="name" key="label.competence.name">
			<fr:property name="size" value="50" />
		</fr:slot>
		<fr:slot name="description" layout="longText">
			<fr:property name="rows" value="3" />
			<fr:property name="columns" value="50" />
		</fr:slot>
	</fr:schema>
	
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
		<fr:property name="columnClasses" value=",aleft,tderror"/>
	</fr:layout>
	
	<fr:destination name="cancel" path="/competencesManagement.do?method=manageCompetences"/>
</fr:edit>