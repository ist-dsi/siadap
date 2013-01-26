<%@page import="pt.ist.bennu.core.domain.User"%>
<%@page import="pt.ist.fenixWebFramework.security.UserView"%>
<%@page import="module.siadap.domain.SiadapYearConfiguration"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>

<bean:define id="user" name="USER_SESSION_ATTRIBUTE" property="user"/>

<h2><bean:message key="link.siadap.structureManagement" bundle="SIADAP_RESOURCES"/></h2>


<%-- The year chooser: --%>
<fr:form action="/siadapPersonnelManagement.do?method=start">
	<fr:edit id="siadapYearWrapper" name="siadapYearWrapper" nested="true">
		<fr:schema bundle="SIADAP" type="module.siadap.domain.wrappers.SiadapYearWrapper">
			<fr:slot name="chosenYear" bundle="SIADAP_RESOURCES" layout="menu-select-postback" key="siadap.start.siadapYearChoice">
					<fr:property name="providerClass" value="module.siadap.presentationTier.renderers.providers.SiadapYearsFromExistingSiadapConfigurations"/>
					<%-- 
					<fr:property name="format" value="${year}" />
					--%>
					<fr:property name="nullOptionHidden" value="true"/>
					<%-- 
					<fr:property name="eachSchema" value="module.siadap.presentationTier.renderers.providers.SiadapYearConfigurationsFromExisting.year"/>
					--%>
			</fr:slot>
		</fr:schema>
	</fr:edit>
</fr:form>  

<html:messages id="message" bundle="SIADAP_RESOURCES" property="messageWarning" message="true" header="label.warnings">
	<div class="highlightBox"> 
		<p><b><bean:write name="message" /></b></p>
	</div>
</html:messages>


	
	
<bean:define id="year" name="siadapYearWrapper" property="chosenYear"/>

<html:link page="/siadapPersonnelManagement.do?method=manageUsersWithoutValidHarmonizationUnit" paramId="year" paramName="year" >
	<p>Gerir processos sem unidade de harmonização válida</p>
</html:link>

<html:link page="/siadapProcessCount.do?method=manageUnlistedUsers" paramId="year" paramName="year" >
	<p>Gerir pessoas não listadas</p>
</html:link>
<%-- Link for the hierarchy excel file --%>
<html:link action="/siadapPersonnelManagement.do?method=downloadNormalSIADAPStructure" paramId="year" paramName="siadapYearWrapper" paramProperty="chosenYear">
<p>Download da listagem de hierarquias SIADAP</p>
</html:link>

<%-- Link for the hierarchy excel file with SIADAP universe--%>
<html:link action="/siadapPersonnelManagement.do?method=downloadSIADAPStructureWithUniverse" paramId="year" paramName="siadapYearWrapper" paramProperty="chosenYear">
<p>Download da listagem de hierarquias SIADAP (com universos)</p>
</html:link>

<%-- Link for the hierarchy excel file with SIADAP universe--%>
<html:link action="/siadapPersonnelManagement.do?method=downloadSIADAPRawData" paramId="year" paramName="siadapYearWrapper" paramProperty="chosenYear">
<p>Download da listagem com todos os dados SIADAP</p>
</html:link>

<%-- Link for the hierarchy excel file with SIADAP universe and confidential data--%>
<% if (SiadapYearConfiguration.getSiadapYearConfiguration((Integer)year).getCcaMembers().contains(((User)user).getPerson())) { %>
<html:link action="/siadapPersonnelManagement.do?method=downloadSIADAPRawDataWithConfidentialData" paramId="year" paramName="siadapYearWrapper" paramProperty="chosenYear">
<p>Download da listagem com todos os dados SIADAP e detalhes de avaliações (apenas disponível ao CCA)</p>
</html:link>
<% } %>

<fr:edit id="searchPerson" name="bean" action="<%="/siadapPersonnelManagement.do?method=viewPerson&year=" + year.toString()%>" >
<fr:schema type="pt.ist.bennu.core.util.VariantBean" bundle="SIADAP_RESOURCES">
		<fr:slot name="domainObject" layout="autoComplete" key="label.person" bundle="ORGANIZATION_RESOURCES">
        <fr:property name="labelField" value="name"/>
		<fr:property name="format" value="${name} (${user.username})"/>
		<fr:property name="minChars" value="3"/>		
		<fr:property name="args" value="provider=module.organization.presentationTier.renderers.providers.PersonAutoCompleteProvider"/>
		<fr:property name="size" value="60"/>
		<fr:validator name="pt.ist.fenixWebFramework.rendererExtensions.validators.RequiredAutoCompleteSelectionValidator">
			<fr:property name="message" value="label.pleaseSelectOne.person"/>
			<fr:property name="bundle" value="ORGANIZATION_RESOURCES"/>
			<fr:property name="key" value="true"/>
		</fr:validator>
	</fr:slot>
	</fr:schema>	
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
	</fr:layout>
	<fr:destination name="cancel" path="/siadapPersonnelManagement.do?method=start"/>
</fr:edit>


<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/16" />	
</jsp:include>
