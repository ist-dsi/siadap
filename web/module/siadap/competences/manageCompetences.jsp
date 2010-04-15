<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr" %>

<html:link page="/competencesManagement.do?method=prepareCompetenceTypeCreation">
Criar novo tipo de competências
</html:link>


<fr:edit name="siadapRoot">
	<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.domain.SiadapRootModule">
		<fr:slot name="objectivesPonderation"/>
		<fr:slot name="competencesPonderation"/>
	</fr:schema>
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
	</fr:layout>
</fr:edit>

<fr:view name="competenceTypes">
	<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.domain.CompetenceType">
		<fr:slot name="name"/>
	</fr:schema>
	<fr:layout name="tabular">
			<fr:property name="link(newCompetence)" value="/competencesManagement.do?method=prepareCompetenceCreation" />
			<fr:property name="param(newCompetence)" value="externalId/competenceTypeId" />
			<fr:property name="key(newCompetence)" value="link.createNewCompetence" />
			<fr:property name="bundle(newCompetence)" value="SIADAP_RESOURCES" />
	</fr:layout>
</fr:view>