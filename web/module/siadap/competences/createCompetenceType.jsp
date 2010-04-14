<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr" %>

<fr:edit id="bean" name="bean" action="/competencesManagement.do?method=createCompetenceType">
	<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.domain.dto.CompetenceTypeBean">
		<fr:slot name="name"/>
	</fr:schema>
	
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
	</fr:layout>
</fr:edit>