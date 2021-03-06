<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr" %>

<h2><bean:message key="label.newCompetenceType" bundle="SIADAP_RESOURCES"/></h2>

<fr:edit id="bean" name="bean" action="/competencesManagement.do?method=createCompetenceType">
	<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.domain.dto.CompetenceTypeBean">
		<fr:slot name="name"/>
	</fr:schema>
	
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
		<fr:property name="columnClasses" value=",aleft,tderror"/>
	</fr:layout>
		
	<fr:destination name="cancel" path="/competencesManagement.do?method=manageCompetences"/>
		
</fr:edit>
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/12" />	
</jsp:include>
