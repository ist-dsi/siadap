<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr" %>

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
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/12" />	
</jsp:include>
