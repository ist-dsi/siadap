<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr" %>


<h2>
	<fr:view name="type" property="name"/>
</h2>

<ul>
	<li>
		<html:link page="/competencesManagement.do?method=manageCompetences">
			<bean:message key="link.back" bundle="MYORG_RESOURCES"/>
		</html:link>
	</li>
</ul>


<fr:view name="type" property="competences">
	<fr:schema type="module.siadap.domain.Competence" bundle="SIADAP_RESOURCES">
		<fr:slot name="number"/>
		<fr:slot name="name" key="label.competence.name"/>
		<fr:slot name="description"/>
	</fr:schema>
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
		<fr:property name="sortBy" value="number"/>
	</fr:layout>
</fr:view>
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/12" />	
</jsp:include>
