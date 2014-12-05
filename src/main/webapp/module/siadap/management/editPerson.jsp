<%@page import="org.fenixedu.bennu.core.groups.DynamicGroup"%>
<%@page import="org.fenixedu.bennu.core.security.Authenticate"%>
<%@page import="org.fenixedu.bennu.core.domain.User"%>
<%@page import="module.siadap.domain.SiadapRootModule"%>
<%@page import="module.organization.domain.AccountabilityType"%>
<%@page import="module.organization.domain.Accountability"%>
<%@page import="java.util.ArrayList"%>
<%@page import="module.siadap.domain.wrappers.PersonSiadapWrapper"%>
<%@page import="module.organization.domain.Person"%>
<%@page import="org.fenixedu.bennu.core.domain.groups.PersistentGroup"%>
<%@page import="module.siadap.domain.SiadapYearConfiguration"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/workflow" prefix="wf"%>

<script src="<%= request.getContextPath() + "/javaScript/jquery.alerts.js"%>" type="text/javascript"></script>
<script src="<%= request.getContextPath() + "/javaScript/alertHandlers.js"%>" type="text/javascript"></script>

<bean:define id="year" name="person" property="year"/>
<bean:define id="yearLabel" name="person" property="configuration.label" type="java.lang.String"/>
<bean:define id="personWrapper" name="person"  type="module.siadap.domain.wrappers.PersonSiadapWrapper"/>

<h2><bean:message key="link.siadap.structureManagement.forAGivenYear" arg0="<%=yearLabel%>" bundle="SIADAP_RESOURCES"/></h2>
<br/>

<bean:define id="personId" name="person" property="person.externalId"/>

<html:messages id="msg" property="message" bundle="SIADAP_RESOURCES" message="true" header="label.errors" >
<%-- <h3><bean:message bundle="SIADAP_RESOURCES" property="ERROR" key="label.errors"/>:</h3> --%>
  <div class="highlightBox">
		<p style="color: darkRed"><b><bean:write name="msg" /></b></p>
  </div> 
	</html:messages>
<%-- <h3><bean:message bundle="SIADAP_RESOURCES" key="label.warnings"/>:</h3> --%>
<html:messages id="message" bundle="SIADAP_RESOURCES" property="messageWarning" message="true" header="label.warnings">
	<div class="highlightBox"> 
		<p><b><bean:write name="message" /></b></p>
	</div>
</html:messages>

<logic:notEmpty name="person" property="siadap">
	<logic:equal value="false" name="person" property="personWorkingInValidSIADAPUnit">
		<div class="highlightBox"> 
			<p style="color: darkRed"><b><bean:message bundle="SIADAP_RESOURCES" key="warning.working.unit.is.not.in.siadap.structure" /></b></p>
		</div>
	</logic:equal>
</logic:notEmpty>

<fr:view name="person">
	<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
		<fr:slot name="name" key="label.name" />
		<fr:slot name="person.user.username" key="label.username" />
		<fr:slot name="workingUnit" layout="null-as-label" key="label.workingUnit">
			<fr:property name="subLayout" value="values"/>
			<fr:property name="subSchema" value="view.UnitSiadapWrapper.name"/>
		</fr:slot>
		<fr:slot name="defaultSiadapUniverse"/>
		<fr:slot name="careerName"/>
		<fr:slot name="quotaAware" key="label.evaluationForQuotas"/> 
		<fr:slot name="evaluator" layout="null-as-label" key="label.evaluator">
			<fr:property name="subLayout" value="values"/>
			<fr:property name="subSchema" value="view.PersonSiadapWrapper.name"/>
		</fr:slot>
		<fr:slot name="evaluator.person.user.username" layout="null-as-label" key="label.username"/>
		<fr:slot name="harmonizationUnitForSIADAP2" layout="null-as-label" key="label.harmonizationUnitForSiadap2">
			<fr:property name="subLayout" value="values"/>
			<fr:property name="subSchema" value="view.UnitSiadapWrapper.name"/>
		</fr:slot>
		<fr:slot name="harmonizationUnitForSIADAP3" layout="null-as-label" key="label.harmonizationUnitForSiadap3">
			<fr:property name="subLayout" value="values"/>
			<fr:property name="subSchema" value="view.UnitSiadapWrapper.name"/>
		</fr:slot>
	</fr:schema>
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
		<fr:property name="columnClasses" value="aright,aleft,"/>
	</fr:layout>
</fr:view>

<%-- Showing either the create/edit siadap process for this person: --%>
<p>Processo SIADAP: 
	<logic:present name="person" property="siadap" >
		<html:link page="<%= "/workflowProcessManagement.do?method=viewProcess&year="
			+ personWrapper.getYear()
			+ "&processId="
			+ personWrapper.getSiadap().getProcess().getExternalId() %>">
			<bean:message key="link.view" bundle="MYORG_RESOURCES"/>
		</html:link>
	</logic:present>
	<logic:notPresent name="person" property="siadap">
		<a class="toggableLink" toggleDiv="createSiadapDiv" href="#" id="createSiadapLink">
			<bean:message key="link.create" bundle="MYORG_RESOURCES"/>
		</a>
		<logic:equal value="true" name="personWrapper" property="configuration.onlyAllowedToCreateSIADAP3">
			| <a class="toggableLink" toggleDiv="createSiadap2ForCurricularPonderationDiv" href="#">
				<bean:message key="link.create.forCurricularPonderation" bundle="SIADAP_RESOURCES"/>
			</a>
		</logic:equal>
		
	</logic:notPresent>
</p>

<%-- ACL for the ability to change anything --%>
<%
User currentUser = Authenticate.getUser();
Person person = currentUser.getPerson();
SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration((Integer)year);

boolean isAbleToChangeAnything = false;
boolean isManager = false;
boolean isCCAMember = false;
if (configuration.isCurrentUserMemberOfCCA()) {
    isCCAMember = true;
}
//if (configuration.getCcaMembers().contains(person) || Role.getRole(RoleType.MANAGER).isMember(currentUser) )
if (DynamicGroup.get("managers").isMember(Authenticate.getUser()) || configuration.isUserMemberOfStructureManagementGroup(currentUser) )
{
    isAbleToChangeAnything = true;
}
if (DynamicGroup.get("managers").isMember(Authenticate.getUser()))
	{
    isManager = true;
	}

request.setAttribute("isAbleToChangeAnything", isAbleToChangeAnything);
request.setAttribute("isManager", isManager);
request.setAttribute("isCCAMember", isCCAMember);
%>


	<%-- Part responsible for the changes on the SIADAP proccess (and the SIADAP user) --%>
<logic:equal name="isAbleToChangeAnything" value="true">
	<logic:present name="siadapProcess">
		<wf:activityLink processName="siadapProcess" activityName="NullifyProcess" scope="request" target="_blank">
			<wf:activityName processName="siadapProcess" activityName="NullifyProcess" scope="request"/>
		</wf:activityLink>
		<wf:activityLink processName="siadapProcess" activityName="RectifyNullifiedProcess" scope="request" target="_blank">
			<wf:activityName processName="siadapProcess" activityName="RectifyNullifiedProcess" scope="request"/>
		</wf:activityLink>
	</logic:present>
	<logic:notPresent name="siadapProcess">
		<html:link styleId="removeFromSiadapStructure"  page="<%="/siadapPersonnelManagement.do?method=removeFromSiadapStructure&year="+year.toString()+"&preserveResponsabilityRelations=true"%>" paramName="person" paramProperty="person.externalId" paramId="personId">
			<bean:message key="label.management.removeFromSiadapStructure.preserveResponsabilityRelations" bundle="SIADAP_RESOURCES"/>
		</html:link>
		| <html:link styleId="removeFromSiadapStructure"  page="<%="/siadapPersonnelManagement.do?method=removeFromSiadapStructure&year="+year.toString()+"&preserveResponsabilityRelations=false"%>" paramName="person" paramProperty="person.externalId" paramId="personId">
			<bean:message key="label.management.removeFromSiadapStructure" bundle="SIADAP_RESOURCES"/>
		</html:link>
	</logic:notPresent>
	<logic:present name="personWrapper" property="siadap">
		<p> <a class="toggableLink" toggleDiv="changeUnitDiv" href="#" id="changeUnit"> <bean:message key="label.changeWorkingUnit" bundle="SIADAP_RESOURCES"/> </a> | <a class="toggableLink" toggleDiv="changeHarmonizationUnitDiv" href="#" id="changeHarmonizationUnit"> <bean:message key="label.changeHarmonizationUnit" bundle="SIADAP_RESOURCES"/> </a> | <a class="toggableLink" toggleDiv="changeEvaluatorDiv" href="#" id="changeEvaluator"> <bean:message key="label.changeEvaluator" bundle="SIADAP_RESOURCES"/> </a>
			<logic:equal name="person" property="customEvaluatorDefined" value="true">
			| <html:link page="<%="/siadapPersonnelManagement.do?method=removeCustomEvaluator&activity=ChangePersonnelSituation&year=" + year.toString()%>" paramId="personId"  paramName="person"  paramProperty="person.externalId">
				<bean:message key="label.removeCustomEvaluator" bundle="SIADAP_RESOURCES"/>
			  </html:link> 
			</logic:equal>
			| <a class="toggableLink" toggleDiv="changeSiadapUniverseDiv" href="#" id="changeSiadapUniverse"> <bean:message key="label.changeSiadapUniverse" bundle="SIADAP_RESOURCES"/> </a>
			| <a class="toggableLink" toggleDiv="changeCompetenceTypeDiv" href="#" id="changeCompetenceTypeLink"> <bean:message key="label.changeCompetenceType" bundle="SIADAP_RESOURCES"/> </a>
			| <a class="toggableLink" toggleDiv="forceChangeCompetenceTypeDiv" href="#" id="forceChangeCompetenceTypeLink"> <bean:message key="label.forceChangeCompetenceType" bundle="SIADAP_RESOURCES"/> </a>
		   
		    <logic:equal value="true" name="isCCAMember">
		    	| <a class="toggableLink" toggleDiv="forceChangeSiadapUniverseDiv" href="#" id="forceChangeSiadapUniverse"> <bean:message key="label.forceChangeSiadapUniverse" bundle="SIADAP_RESOURCES"/> </a>
		    </logic:equal>
		</p>
	</logic:present> 
	<logic:notPresent name="personWrapper" property="siadap">
<p>(funcionalidades desactivadas, crie o processo SIADAP antes)</p>
	<p>
	    <bean:message key="label.changeWorkingUnit" bundle="SIADAP_RESOURCES"/>
	    | <bean:message key="label.changeHarmonizationUnit" bundle="SIADAP_RESOURCES"/>
	    | <bean:message key="label.changeEvaluator" bundle="SIADAP_RESOURCES"/>			
		| <bean:message key="label.changeSiadapUniverse" bundle="SIADAP_RESOURCES"/> 
		| <bean:message key="label.changeCompetenceType" bundle="SIADAP_RESOURCES"/>
		<logic:equal value="true" name="isCCAMember">
		| <bean:message key="label.forceChangeSiadapUniverse" bundle="SIADAP_RESOURCES"/> 
		</logic:equal>
	</p>
	</logic:notPresent>
	
	<div class="toggableDiv" id="createSiadapDiv" style="display: none;">
		<div class="highlightBox">
			<p>Criar SIADAP:</p>
			<fr:form action="<%="/siadapPersonnelManagement.do?method=createNewSiadapProcess&personId=" + personId + "&year=" + year.toString()%>">
				<fr:edit id="createSiadapBean" name="createSiadapBean" visible="false"/>
				
				<fr:edit id="createSiadapBean1" name="createSiadapBean">
					<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.presentationTier.actions.SiadapPersonnelManagement$SiadapCreationBean">
						<fr:slot name="defaultSiadapUniverse">
							<logic:equal value="true" name="personWrapper" property="configuration.onlyAllowedToCreateSIADAP3">
								<fr:property name="excludedValues" value="SIADAP2"/>
							</logic:equal>
						</fr:slot>
						<fr:slot name="competenceType" layout="menu-select">
							<fr:property name="providerClass" value="module.siadap.presentationTier.renderers.providers.CompetenceTypeProvider" />
							<fr:property name="format" value="${name}" />
							<fr:property name="sortBy" value="name" />
						</fr:slot>
					</fr:schema>
				</fr:edit>
			<html:submit styleClass="inputbutton"><bean:message key="renderers.form.submit.name" bundle="RENDERER_RESOURCES"/></html:submit>
			</fr:form>
		</div>
	</div>
	
	<div class="toggableDiv" id="createSiadap2ForCurricularPonderationDiv" style="display: none;">
		<div class="highlightBox">
			<p>Criar SIADAP2 para ponderação curricular:</p>
			<fr:form action="<%="/siadapPersonnelManagement.do?method=createNewSiadap2ProcessForCurricularPonderation&personId=" + personId + "&year=" + year.toString()%>">
				<fr:edit id="createSiadapBean" name="createSiadapBean" visible="false"/>
				
				<fr:edit id="createSiadapBean1" name="createSiadapBean">
					<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.presentationTier.actions.SiadapPersonnelManagement$SiadapCreationBean">
						<fr:slot name="competenceType" layout="menu-select">
							<fr:property name="providerClass" value="module.siadap.presentationTier.renderers.providers.CompetenceTypeProvider" />
							<fr:property name="format" value="${name}" />
							<fr:property name="sortBy" value="name" />
						</fr:slot>
					</fr:schema>
				</fr:edit>
			<html:submit styleClass="inputbutton"><bean:message key="renderers.form.submit.name" bundle="RENDERER_RESOURCES"/></html:submit>
			</fr:form>
		</div>
	</div>
	
	<div class="toggableDiv" id="changeCompetenceTypeDiv" style="display: none;">
		<div class="highlightBox">
			<p>Mudar a carreira:</p>
			<fr:form action="<%="/siadapPersonnelManagement.do?method=changeCompetenceType&activity=ChangePersonnelSituation&personId=" + personId + "&year=" + year.toString()%>">
				<fr:edit id="changeCompetenceTypeBean" name="changeCompetenceTypeBean" visible="false"/>
				
				<fr:edit id="changeCompetenceTypeBean1" name="changeCompetenceTypeBean">
					<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.presentationTier.actions.SiadapPersonnelManagement$CompetenceTypeBean">
						<fr:slot name="competenceType" layout="menu-select">
							<fr:property name="providerClass" value="module.siadap.presentationTier.renderers.providers.CompetenceTypeProvider" />
							<fr:property name="format" value="${name}" />
							<fr:property name="sortBy" value="name" />
						</fr:slot>
					</fr:schema>
				</fr:edit>
			<html:submit styleClass="inputbutton"><bean:message key="renderers.form.submit.name" bundle="RENDERER_RESOURCES"/></html:submit>
			</fr:form>
			
		</div>
	</div>
	
	<div class="toggableDiv" id="forceChangeCompetenceTypeDiv" style="display: none;">
		<div class="highlightBox">
			<p>Forçar a mudança da carreira:</p>
			<fr:form action="<%="/siadapPersonnelManagement.do?method=forceChangeCompetenceType&activity=ChangePersonnelSituation&personId=" + personId + "&year=" + year.toString()%>">
				<fr:edit id="forceChangeCompetenceTypeBean" name="forceChangeCompetenceTypeBean" visible="false"/>
				
				<fr:edit id="forceChangeCompetenceTypeBean1" name="forceChangeCompetenceTypeBean">
					<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.presentationTier.actions.SiadapPersonnelManagement$ForceChangeCompetenceTypeBean">
						<fr:slot name="competenceType" layout="menu-select">
							<fr:property name="providerClass" value="module.siadap.presentationTier.renderers.providers.CompetenceTypeProvider" />
							<fr:property name="format" value="${name}" />
							<fr:property name="sortBy" value="name" />
						</fr:slot>
					</fr:schema>
				</fr:edit>
			<html:submit styleClass="inputbutton"><bean:message key="renderers.form.submit.name" bundle="RENDERER_RESOURCES"/></html:submit>
			</fr:form>
			
		</div>
	</div>
	
	<div class="toggableDiv" id="changeUnitDiv" style="display: none;">
		<div class="highlightBox">
		<fr:form action="<%= "/siadapPersonnelManagement.do?method=changeWorkingUnit&activity=ChangePersonnelSituation&personId=" + personId + "&year=" + year.toString() %>">
		<fr:edit id="changeWorkingUnit" name="changeWorkingUnit" visible="false"/>
	
		<fr:edit id="changeWorkingUnit1" name="changeWorkingUnit" slot="unit" >
		
		<fr:layout name="autoComplete">
			<fr:property name="classes" value="tstyle2"/>
			<fr:property name="labelField" value="partyName.content"/>
			<fr:property name="format" value="${presentationName}"/>
			<fr:property name="minChars" value="3"/>		
			<fr:property name="args" value="provider=module.organization.presentationTier.renderers.providers.UnitAutoCompleteProvider"/>
			<fr:property name="size" value="60"/>
			<fr:validator name="pt.ist.fenixWebFramework.rendererExtensions.validators.RequiredAutoCompleteSelectionValidator">
				<fr:property name="message" value="label.pleaseSelectOne.unit"/>
				<fr:property name="bundle" value="EXPENDITURE_RESOURCES"/>
				<fr:property name="key" value="true"/>
			</fr:validator>
		</fr:layout>
	</fr:edit>
	
		Contabiliza para as quotas: <fr:edit id="changeWorkingUnit2" name="changeWorkingUnit" slot="withQuotas" >
		<fr:layout name="autoComplete">
			<fr:property name="classes" value="tstyle2"/>
		</fr:layout>
		</fr:edit>
		
		Data da alteração: <fr:edit id="changeWorkingUnit3" name="changeWorkingUnit" slot="dateOfChange" >
		<fr:layout name="picker"/>
		</fr:edit>
		
		<div style="vertical-align: middle;">
			<label>Justificação/Razão:</label>
			 <fr:edit id="changeWorkingUnit4" name="changeWorkingUnit" slot="justification" >
				<fr:layout name="longText">
					<%-- <fr:property name=""/> --%>
				</fr:layout>
			</fr:edit>
		</div>
		
		<div>
			<html:submit styleClass="inputbutton"><bean:message key="renderers.form.submit.name" bundle="RENDERER_RESOURCES"/></html:submit>
		</div>
	</fr:form>
		</div>
	</div>
	
	<div class="toggableDiv" id="changeHarmonizationUnitDiv" style="display: none;">
		<div class="highlightBox">
		<fr:form action="<%= "/siadapPersonnelManagement.do?method=changeHarmonizationUnit&activity=ChangePersonnelSituation&personId=" + personId + "&year=" + year.toString() %>">
		<fr:edit id="changeHarmonizationUnit" name="changeHarmonizationUnit" visible="false"/>
	
		<fr:edit id="changeHarmonizationUnit1" name="changeHarmonizationUnit" slot="unit" >
		
		<fr:layout name="autoComplete">
			<fr:property name="classes" value="tstyle2"/>
			<fr:property name="labelField" value="partyName.content"/>
			<fr:property name="format" value="${presentationName}"/>
			<fr:property name="minChars" value="3"/>		
			<fr:property name="args" value="<%="provider=module.siadap.presentationTier.renderers.providers.HarmonizationUnitAutoCompleteProvider," + "year=" + year%>"/>
			<fr:property name="size" value="60"/>
			<fr:validator name="pt.ist.fenixWebFramework.rendererExtensions.validators.RequiredAutoCompleteSelectionValidator">
				<fr:property name="message" value="label.pleaseSelectOne.unit"/>
				<fr:property name="bundle" value="EXPENDITURE_RESOURCES"/>
				<fr:property name="key" value="true"/>
			</fr:validator>
		</fr:layout>
	</fr:edit>
	
		Data da alteração: <fr:edit id="changeHarmonizationUnit2" name="changeHarmonizationUnit" slot="dateOfChange" >
		<fr:layout name="picker"/>
		</fr:edit>
		
		<div style="vertical-align: middle;">
			<label style="display: inline-block;">Justificação/Razão:</label>
			 <fr:edit id="changeHarmonizationUnit3" name="changeHarmonizationUnit" slot="justification" >
				<fr:layout name="longText">
					<fr:property name="style" value="display: inline-block"/>
				</fr:layout>
			</fr:edit>
		</div>
		
		<div>
			<html:submit styleClass="inputbutton"><bean:message key="renderers.form.submit.name" bundle="RENDERER_RESOURCES"/></html:submit>
		</div>
	</fr:form>
		</div>
	</div>
	
	<div class="toggableDiv" id="changeSiadapUniverseDiv" style="display: none;">
		<div class="highlightBox">
			<fr:form action="<%= "/siadapPersonnelManagement.do?method=changeSiadapUniverse&activity=ChangePersonnelSituation&personId=" + personId + "&year=" + year.toString() %>">
				<fr:edit id="changeSiadapUniverse" name="changeSiadapUniverse">
					<fr:schema type="module.siadap.presentationTier.actions.SiadapPersonnelManagement$ChangeSiadapUniverseBean" bundle="SIADAP_RESOURCES">
						<fr:slot name="siadapUniverse" key="label.changeSiadapUniverse" bundle="SIADAP_RESOURCES" layout="radio">
							<fr:validator name="pt.ist.fenixWebFramework.renderers.validators.RequiredValidator"/>
						</fr:slot>
						<fr:slot name="dateOfChange" layout="picker"/>
					</fr:schema>
				</fr:edit>
				<html:submit styleClass="inputbutton"><bean:message key="renderers.form.submit.name" bundle="RENDERER_RESOURCES"/></html:submit>
			</fr:form>
		</div>
	</div>
	
	<logic:equal value="true" name="isCCAMember">
		<div class="toggableDiv" id="forceChangeSiadapUniverseDiv" style="display: none;">
			<div class="highlightBox">
				<fr:form action="<%= "/siadapPersonnelManagement.do?method=changeSiadapUniverse&activity=ChangePersonnelSituation&force=true&personId=" + personId + "&year=" + year.toString() %>">
					<fr:edit id="forceChangeSiadapUniverse" name="forceChangeSiadapUniverse">
						<fr:schema type="module.siadap.presentationTier.actions.SiadapPersonnelManagement$ChangeSiadapUniverseBean" bundle="SIADAP_RESOURCES">
							<fr:slot name="siadapUniverse" key="label.changeSiadapUniverse" bundle="SIADAP_RESOURCES" layout="radio">
								<fr:validator name="pt.ist.fenixWebFramework.renderers.validators.RequiredValidator"/>
							</fr:slot>
							<fr:slot name="justificationForForcingChange" key="label.changeSiadapUniverse.justification" bundle="SIADAP_RESOURCES">
								<fr:validator name="pt.ist.fenixWebFramework.renderers.validators.RequiredValidator"/>
							</fr:slot>
							<fr:slot name="dateOfChange" layout="picker"/>
						</fr:schema>
					</fr:edit>
					<html:submit styleClass="inputbutton"><bean:message key="renderers.form.submit.name" bundle="RENDERER_RESOURCES"/></html:submit>
				</fr:form>
			</div>
		</div>
	</logic:equal>
	<div class="toggableDiv" id="changeEvaluatorDiv" style="display: none;" >
		<div class="highlightBox">
		
		<fr:form action="<%= "/siadapPersonnelManagement.do?method=changeEvaluator&activity=ChangePersonnelSituation&personId=" + personId + "&year=" + year.toString() %>">
		
		<fr:edit id="changeEvaluator" name="changeEvaluator" visible="false"/>
		
		<fr:edit id="changeEvaluator1" name="changeEvaluator" slot="evaluator">
		<fr:layout name="autoComplete">
	        <fr:property name="labelField" value="name"/>
			<fr:property name="format" value="<%= "${name} (${user.username})" %>"/>
			<fr:property name="minChars" value="3"/>		
			<fr:property name="args" value="provider=module.organization.presentationTier.renderers.providers.PersonAutoCompleteProvider"/>
			<fr:property name="size" value="60"/>
			<fr:validator name="pt.ist.fenixWebFramework.rendererExtensions.validators.RequiredAutoCompleteSelectionValidator">
				<fr:property name="message" value="label.pleaseSelectOne.unit"/>
				<fr:property name="bundle" value="EXPENDITURE_RESOURCES"/>
				<fr:property name="key" value="true"/>
			</fr:validator>
			</fr:layout>
		</fr:edit>
		
		Data da alteração: <fr:edit id="changeEvaluator1" name="changeEvaluator" slot="dateOfChange" >
		<fr:layout name="picker"/>
		</fr:edit>
		
		<html:submit styleClass="inputbutton"><bean:message key="renderers.form.submit.name" bundle="RENDERER_RESOURCES"/></html:submit>
	</fr:form>
			
		</div>
	</div>
</logic:equal>
<script type="text/javascript">

	$('.toggableLink').each(function(index, Element){
		  console.log('coisas' + $(this).attr('togglediv'));
	
		 $(this).click(function(){
		   var div = '#' + $(this).attr('togglediv');
		   $('.toggableDiv').hide();
		   $(div).slideToggle();
		
		  });
	});

</script>

<logic:notEmpty name="person" property="peopleToEvaluate"> 
		<p><strong> <bean:message key="label.responsibleForEvaluationOf" bundle="SIADAP_RESOURCES"/></strong></p>
		
		<table class="tstyle2">
			<tr>
			<th>
				<bean:message key="label.evaluated" bundle="SIADAP_RESOURCES"/>
			</th>
			<th>
				<bean:message key="label.unit" bundle="ORGANIZATION_RESOURCES" />
			</th>
			<th></th>
			</tr>
			<logic:iterate id="evaluated" name="person" property="peopleToEvaluate" indexId="index">
					<bean:define id="evalutedId" name="evaluated" property="person.externalId"/>
					<tr>
						<td><fr:view name="evaluated" property="name"/>
						<td><fr:view name="evaluated" property="workingUnit.name"/>
						<td>
							<html:link page="<%= "/siadapPersonnelManagement.do?method=viewPerson&personId=" + evalutedId + "&year=" + year.toString()%>"> Ver avaliado </html:link>
							|
							<logic:present name="evaluated" property="siadap">
								<html:link page="<%= "/workflowProcessManagement.do?method=viewProcess&year="
								+ configuration.getYear()
								+ "&processId="
								+ ((module.siadap.domain.wrappers.PersonSiadapWrapper)evaluated).getSiadap().getProcess().getExternalId() %>">
								<bean:message key="link.view" bundle="MYORG_RESOURCES"/>
							</html:link>
							</logic:present>
							<logic:notPresent name="evaluated" property="siadap">
								<html:link page="<%= "/siadapManagement.do?method=createNewSiadapProcess&year="
									+ configuration.getYear()
									+ "&personId="
									+ ((module.siadap.domain.wrappers.PersonSiadapWrapper)evaluated).getPerson().getExternalId() %>">
									<bean:message key="link.create" bundle="MYORG_RESOURCES"/>
								</html:link>
							
							</logic:notPresent>
						</td>
					</tr>
			</logic:iterate>
		</table>		
</logic:notEmpty>

<logic:notEmpty name="person" property="harmozationUnits">
		<p><strong> <bean:message key="label.responsifleForHarmonizationOf" bundle="SIADAP_RESOURCES"/></strong></p>
		<fr:view name="person" property="harmozationUnits">
			<fr:schema type="module.siadap.domain.wrappers.UnitSiadapWrapper" bundle="SIADAP_RESOURCES">
				<fr:slot name="unit.partyName"  key="label.unit" bundle="ORGANIZATION_RESOURCES" />
				<fr:slot name="unit.acronym" key="label.acronym" bundle="ORGANIZATION_RESOURCES" />
			<%-- 	<fr:slot name="relevantEvaluationPercentage"/>
				<fr:slot name="excellencyEvaluationPercentage"/> --%>
				<fr:slot name="totalPeopleWorkingInUnit"/>
				<fr:slot name="totalPeopleWithSiadapWorkingInUnit"/>
				<fr:slot name="validHarmonizationUnit"/>
			</fr:schema>
			<fr:layout name="tabular">
				<fr:property name="classes" value="tstyle2"/>
			 	<logic:equal name="isManager" value="true">
					<fr:property name="link(terminate)" value="<%=  "/siadapPersonnelManagement.do?method=terminateUnitHarmonization&personId=" + personId + "&year=" + year.toString()%>"/>
					<fr:property name="bundle(terminate)" value="MYORG_RESOURCES"/>
					<fr:property name="key(terminate)" value="link.remove"/>
					<fr:property name="param(terminate)" value="unit.externalId/unitId"/>
					<fr:property name="order(terminate)" value="1"/>
				</logic:equal>
			</fr:layout>	
		</fr:view>
</logic:notEmpty>


<p><strong><bean:message key="label.history" bundle="SIADAP_RESOURCES"/></strong></p>
<jsp:include page="/organization/viewAccountabilityHistory.jsp" flush="true" >
 <jsp:param value="<%=personId%>" name="parties"/>
 <jsp:param value="<%=configuration.getEvaluationRelation().getExternalId()%>" name="accountabilities"/>
 <jsp:param value="<%=configuration.getWorkingRelation().getExternalId()%>" name="accountabilities"/>
 <jsp:param value="<%=configuration.getWorkingRelationWithNoQuota().getExternalId()%>" name="accountabilities"/>
 <jsp:param value="<%=configuration.getHarmonizationResponsibleRelation().getExternalId()%>" name="accountabilities"/>
 <jsp:param value="<%=configuration.getSiadap2HarmonizationRelation().getExternalId()%>" name="accountabilities"/>
 <jsp:param value="<%=configuration.getSiadap3HarmonizationRelation().getExternalId()%>" name="accountabilities"/>
 <jsp:param value="<%=configuration.getLastDay().getYear()%>" name="endDateYear"/>
 <jsp:param value="<%=configuration.getLastDay().getMonthOfYear()%>" name="endDateMonth"/>
 <jsp:param value="<%=configuration.getLastDay().getDayOfMonth()%>" name="endDateDay"/>
 <jsp:param value="true" name="showDeletedAccountabilities"/>
</jsp:include>


<%-- For now, let's also comment the ability to add people to do the harmonization of certain units, I should check the type of accountability
that is desirable (start and end dates) 
<logic:equal name="isManager" value="true">
	<p>
	<strong><bean:message key="label.addHarmonizationResponsability" bundle="SIADAP_RESOURCES"/></strong>
	<fr:edit id="addHarmonizationUnit" name="bean" action="<%= "/siadapPersonnelManagement.do?method=addHarmonizationUnit&personId=" + personId + "&year=" + year.toString() %>">
		<fr:schema type="pt.ist.bennu.core.util.VariantBean" bundle="SIADAP_RESOURCES">
			<fr:slot name="domainObject" layout="autoComplete" key="label.unit" bundle="ORGANIZATION_RESOURCES">
	        <fr:property name="labelField" value="partyName.content"/>
			<fr:property name="format" value="${presentationName}"/>
			<fr:property name="minChars" value="3"/>		
			<fr:property name="args" value="provider=module.organization.presentationTier.renderers.providers.UnitAutoCompleteProvider"/>
			<fr:property name="size" value="60"/>
			<fr:validator name="pt.ist.fenixWebFramework.rendererExtensions.validators.RequiredAutoCompleteSelectionValidator">
				<fr:property name="message" value="label.pleaseSelectOne.unit"/>
				<fr:property name="bundle" value="EXPENDITURE_RESOURCES"/>
				<fr:property name="key" value="true"/>
			</fr:validator>
		</fr:slot>
		</fr:schema>	
		<fr:layout name="tabular">
			<fr:property name="classes" value="tstyle2"/>
		</fr:layout>
		<fr:destination name="cancel" path="/siadapPersonnelManagement.do?method=start" />
	</fr:edit>
	</p>
</logic:equal>
--%>
<%-- End of isAbleToChangeAnything --%>

<%-- For now, let's comment out the history section
<strong> <bean:message key="label.history" bundle="SIADAP_RESOURCES"/>: </strong>
 
<fr:view name="history">
	<fr:schema type="module.organization.domain.Accountability" bundle="ORGANIZATION_RESOURCES">
		<fr:slot name="beginDate" key="label.begin" />
		<fr:slot name="endDate" key="label.end"/>
		<fr:slot name="parent.partyName" key="label.unit"/>
	</fr:schema>
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
	</fr:layout>
</fr:view>
--%>
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/16" />	
</jsp:include>

