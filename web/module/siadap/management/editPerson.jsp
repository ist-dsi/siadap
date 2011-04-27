<%@page import="module.organization.domain.AccountabilityType"%>
<%@page import="module.organization.domain.Accountability"%>
<%@page import="java.util.ArrayList"%>
<%@page import="module.siadap.domain.wrappers.PersonSiadapWrapper"%>
<%@page import="module.organization.domain.Person"%>
<%@page import="myorg.domain.RoleType"%>
<%@page import="myorg.domain.groups.Role"%>
<%@page import="myorg.domain.groups.PersistentGroup"%>
<%@page import="module.siadap.domain.SiadapYearConfiguration"%>
<%@page import="myorg.domain.User"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<bean:define id="user" name="USER_SESSION_ATTRIBUTE" property="user"/>
<bean:define id="year" name="person" property="year"/>

<h2><bean:message key="link.siadap.structureManagement.forAGivenYear" arg0="<%=year.toString()%>" bundle="SIADAP_RESOURCES"/></h2>
<br/>

<bean:define id="personId" name="person" property="person.externalId"/>

<%-- <h3><bean:message bundle="SIADAP_RESOURCES" property="ERROR" key="label.errors"/>:</h3> --%>
	<html:messages id="msg" property="message" bundle="SIADAP_RESOURCES" message="true" header="label.errors" >
<div class="highlightBox">
		<p style="color: darkRed"><b>.<bean:write name="msg" /></b></p>
</div>
	</html:messages>
<%-- <h3><bean:message bundle="SIADAP_RESOURCES" key="label.warnings"/>:</h3> --%>
	<html:messages id="message" bundle="SIADAP_RESOURCES" property="WARNING" message="true" header="label.warnings">
<div class="highlightBox"> 
	<p><b><bean:write name="message" /></b></p>
</div>
	</html:messages>

<fr:view name="person">
	<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
		<fr:slot name="name" key="label.name " bundle="ORGANIZATION_RESOURCES"/>
		<fr:slot name="person.user.username" key="label.username" />
		<fr:slot name="workingUnit" layout="null-as-label" key="label.workingUnit">
			<fr:property name="subLayout" value="values"/>
			<fr:property name="subSchema" value="view.UnitSiadapWrapper.name"/>
		</fr:slot>
		<fr:slot name="quotaAware" key="label.evaluationForQuotas"/> 
		<fr:slot name="evaluator" layout="null-as-label" key="label.evaluator">
			<fr:property name="subLayout" value="values"/>
			<fr:property name="subSchema" value="view.PersonSiadapWrapper.name"/>
		</fr:slot>
		<fr:slot name="evaluator.person.user.username" layout="null-as-label" key="label.username"/>
	</fr:schema>
	<fr:layout name="tabular">
		<fr:property name="classes" value="tstyle2"/>
		<fr:property name="columnClasses" value="aright,aleft,"/>
	</fr:layout>
</fr:view>

<%-- ACL for the ability to change anything --%>
<%
User currentUser = (User)user;
Person person = currentUser.getPerson();
SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration((Integer)year);

boolean isAbleToChangeAnything = false;
//if (configuration.getCcaMembers().contains(person) || Role.getRole(RoleType.MANAGER).isMember(currentUser) )
if (Role.getRole(RoleType.MANAGER).isMember(currentUser) || configuration.isUserMemberOfStructureManagementGroup(currentUser) )
{
    isAbleToChangeAnything = true;
}

request.setAttribute("isAbleToChangeAnything", isAbleToChangeAnything);
%>


<logic:equal name="isAbleToChangeAnything" value="true">
	<p><a href="#" id="changeUnit"> <bean:message key="label.changeWorkingUnit" bundle="SIADAP_RESOURCES"/> </a> | <a href="#" id="changeEvaluator"> <bean:message key="label.changeEvaluator" bundle="SIADAP_RESOURCES"/> </a>
	<logic:equal name="person" property="customEvaluatorDefined" value="true">
		| <html:link page="<%="/siadapPersonnelManagement.do?method=removeCustomEvaluator&year=" + year.toString()%>" paramId="personId"  paramName="person"  paramProperty="person.externalId"><bean:message key="label.removeCustomEvaluator" bundle="SIADAP_RESOURCES"/> </html:link>
	</logic:equal>
	</p>
	
	<div id="changeUnitDiv" style="display: none;">
		<div class="highlightBox">
		<fr:form action="<%= "/siadapPersonnelManagement.do?method=changeWorkingUnit&personId=" + personId + "&year=" + year.toString() %>">
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
		
		<html:submit styleClass="inputbutton"><bean:message key="renderers.form.submit.name" bundle="RENDERER_RESOURCES"/></html:submit>
	</fr:form>
		</div>
	</div>
	
	<div id="changeEvaluatorDiv" style="display: none;" >
		<div class="highlightBox">
		
		<fr:form action="<%= "/siadapPersonnelManagement.do?method=changeEvaluator&personId=" + personId + "&year=" + year.toString() %>">
		
		<fr:edit id="changeEvaluator" name="changeEvaluator" visible="false"/>
		
		<fr:edit id="changeEvaluator1" name="changeEvaluator" slot="evaluator">
		<fr:layout name="autoComplete">
	        <fr:property name="labelField" value="name"/>
			<fr:property name="format" value="${name} (${user.username})"/>
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
	$("#changeUnit").click(function() {
		$("#changeEvaluatorDiv").hide();
		$("#changeUnitDiv").slideToggle();
	});

	$("#changeEvaluator").click(function() {
		$("#changeEvaluatorDiv").slideToggle();
		$("#changeUnitDiv").hide();
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
				<fr:slot name="relevantEvaluationPercentage"/>
				<fr:slot name="excellencyEvaluationPercentage"/>
				<fr:slot name="totalPeopleWorkingInUnit"/>
				<fr:slot name="totalPeopleWithSiadapWorkingInUnit"/>
			</fr:schema>
			<fr:layout name="tabular">
				<fr:property name="classes" value="tstyle2"/>
				<logic:equal name="isAbleToChangeAnything" value="true">
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
 <jsp:param value="<%=year%>" name="startDateYear"/>
 <jsp:param value="1" name="startDateMonth"/>
 <jsp:param value="1" name="startDateDay"/>
 <jsp:param value="<%=year%>" name="endDateYear"/>
 <jsp:param value="12" name="endDateMonth"/>
 <jsp:param value="31" name="endDateDay"/>
</jsp:include>


<%-- For now, let's also comment the ability to add people to do the harmonization of certain units, I should check the type of accountability
that is desirable (start and end dates) --%>
<%-- 
<logic:equal name="isAbleToChangeAnything" value="true">
	<p>
	<strong><bean:message key="label.addHarmonizationResponsability" bundle="SIADAP_RESOURCES"/></strong>
	<fr:edit id="addHarmonizationUnit" name="bean" action="<%= "/siadapPersonnelManagement.do?method=addHarmonizationUnit&personId=" + personId + "&year=" + year.toString() %>">
		<fr:schema type="myorg.util.VariantBean" bundle="SIADAP_RESOURCES">
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

