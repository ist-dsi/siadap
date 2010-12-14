<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr" %>

<html:link page="/competencesManagement.do?method=prepareCompetenceTypeCreation">
	<bean:message key="label.createNewCompetenceType" bundle="SIADAP_RESOURCES"/>
</html:link>

<p>
	<fr:view name="competenceTypes">
		<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.domain.CompetenceType">
			<fr:slot name="name" key="label.name" bundle="SIADAP_RESOURCES"/>
			<fr:slot name="competencesCount" key="label.competencesNumbers" bundle="SIADAP_RESOURCES"/>
		</fr:schema>
		<fr:layout name="tabular">
				<fr:property name="classes" value="tstyle2"/>
				<fr:property name="link(view)" value="/competencesManagement.do?method=showCompetences" />
				<fr:property name="param(view)" value="externalId/competenceTypeId" />
				<fr:property name="key(view)" value="link.viewCompetences" />
				<fr:property name="bundle(view)" value="SIADAP_RESOURCES" />
			
				<fr:property name="link(newCompetence)" value="/competencesManagement.do?method=prepareCompetenceCreation" />
				<fr:property name="param(newCompetence)" value="externalId/competenceTypeId" />
				<fr:property name="key(newCompetence)" value="link.createNewCompetence" />
				<fr:property name="bundle(newCompetence)" value="SIADAP_RESOURCES" />
		</fr:layout>
	</fr:view>
</p>
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/12" />	
</jsp:include>