<%@page import="module.siadap.domain.ExceedingQuotaSuggestionType"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Set"%>
<%@page import="module.siadap.domain.wrappers.UnitSiadapWrapper"%>
<%@page import="module.organization.domain.Unit"%>
<%@page import="pt.ist.fenixframework.pstm.AbstractDomainObject"%>
<%@page import="module.siadap.presentationTier.renderers.providers.ExceedingQuotaSuggestionProvider"%>
<%@page import="java.util.List"%>
<%@page import="module.siadap.domain.SiadapUniverse"%>
<%@page import="module.siadap.domain.wrappers.SiadapUniverseWrapper"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<logic:notEmpty name="unit" property="unit">
	<bean:define id="unitName" name="unit" property="unit.presentationName" />
	
	<%--Title in case we have a unit defined --%>
	<h2>
		<bean:message key="label.validation" bundle="SIADAP_RESOURCES" arg0="<%=unitName.toString()%>" />
	</h2>
	<br/>

</logic:notEmpty>

<logic:empty name="unit" property="unit">
	<%--Title in case we DON'T have a unit defined --%>
	<h2>
		<bean:message key="label.validation.noTopUnit.defined" bundle="SIADAP_RESOURCES"/>
	</h2>
	<br/>
	
	<bean:define id="unitId" value="" />
</logic:empty>

<%
Unit unitFromWrapper = ((module.siadap.domain.wrappers.UnitSiadapWrapper)request.getAttribute("unit")).getUnit();
UnitSiadapWrapper unitWrapper = (module.siadap.domain.wrappers.UnitSiadapWrapper)request.getAttribute("unit");
String unitIdJava = unitFromWrapper == null ? "" : unitFromWrapper.getExternalId(); 
%>

<%-- The year chooser: --%>
<fr:form action="<%="/siadapManagement.do?method=validate&unitId=" + unitIdJava %>">
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

<%-- Error messages: --%>
<logic:messagesPresent property="message" message="true">
	<div class="error1">
		<html:messages id="errorMessage" property="message" message="true"> 
			<span><fr:view name="errorMessage"/></span>
		</html:messages>
	</div>
</logic:messagesPresent>
<style>
	.inline-list ul, .inline-list li {
		display: inline;
		margin: 0;
		padding: 0;
		font-weight: bold;
	}
</style>
<%-- *END* Error messages *END* --%>
<%
Set<SiadapUniverseWrapper> siadapUniverseWrappers = (Set<SiadapUniverseWrapper>)request.getAttribute("siadapUniverseWrappers");
boolean noData=true;
for (SiadapUniverseWrapper siadapUniverseWrapper : siadapUniverseWrappers)
{
    if (!siadapUniverseWrapper.getSiadapUniverse().isEmpty())
    {
		noData=false;
		break;
    }
}
request.setAttribute("noData", noData);
%>

<logic:equal name="noData" value="false">
<%-- so, if we have data, let's show it --%>
	<bean:define id="unitName" name="unit" property="unit.presentationName"  />
	<bean:define id="unitId" name="unit" property="unit.externalId" />
	<bean:define id="year" name="unit" property="year" />
	
<style> 
tr.noHarmonization {
color: darkRed;
}
</style> 
	<%-- Control links --%>
	<logic:equal name="unit" property="siadapStructureTopUnit" value="true">
		<%-- <html:link page="<%="/siadapManagement.do?method=terminateValidation&year="+year.toString()%>" >
			<bean:message key="label.terminateValidation" bundle="SIADAP_RESOURCES"/>
		</html:link>  --%>
			<bean:message key="label.terminateValidation" bundle="SIADAP_RESOURCES"/> (temporariamente desactivado)
	</logic:equal>
	<logic:notEmpty name="unit" property="unitAboveViaHarmRelation" >
		<div class="infobox">
			<table class="tstyle4">
				<tr>
					<th>Unidade Superior: <html:link action="<%="/siadapManagement.do?method=validate&year=" + year.toString() + "&unitId=" + unitWrapper.getUnitAboveViaHarmRelation().getExternalId() %>">
											<bean:write name="unit" property="unitAboveViaHarmRelation.presentationName"/>
						 				</html:link>
					</th>
				</tr>
				<tr>
					<th>
						Harmonização encerrada: <fr:view name="unit" property="harmonizationFinished"/>
					</th>
				</tr>
				<tr>
					<th>Responsáveis de harmonização:</th>
						<th>
														<logic:iterate id="harmResponsible" name="unit" property="harmonizationResponsibles">
															<p><fr:view name="harmResponsible" property="presentationName"/></p>
														</logic:iterate>
					</th>
				</tr>
			</table>
		</div>
		<%-- Unidade superior: <html:link action="<%="/siadapManagement.do?method=validate&year=" + year.toString() + "&unitId=" + unitWrapper.getUnitAboveViaHarmRelation().getExternalId() %>">
							<bean:write name="unit" property="unitAboveViaHarmRelation.presentationName"/>
						 </html:link> --%>
	</logic:notEmpty>
	
	<script type="text/javascript">
	 $(document).ready(function() {
		//identifying rows of non harmonized people, and marking them in red
		$("tr").each(function(index, tr) { 
			var trChildren = $(tr).children();
			 if (trChildren.size() === 9) 
				{
				 if ($(trChildren[6]).text().trim() === "" && $(trChildren[7]).text().trim() === "" && $(trChildren[8]).text().trim() === "" )
					{
					//if we have no text in these three fields, we are in a tr of an unharmonized eval
					$(tr).addClass("noHarmonization");
					//let's show the generic warning, if it is not shown yet
					$(tr).closest('table').prev("div.highlightBox").show(500);
					} 
				}
		});
		
		
	}); 
	</script>
	
	
	<fr:form action="<%="/siadapManagement.do?method=applyValidationData&year=" + year.toString() + "&unitId=" + unitId.toString() %>"> 
		<fr:edit id="siadapUniverseWrappersList" name="siadapUniverseWrappers" visible="false"/>
		
    		<logic:iterate id="siadapUniverseWrapper" name="siadapUniverseWrappers">

				<%-- Title --%>
				<logic:notEmpty name="siadapUniverseWrapper" property="siadapUniverse">
					<strong><bean:message key="<%=((module.siadap.domain.wrappers.SiadapUniverseWrapper)siadapUniverseWrapper).getUniverseTitleQuotaSuggestionKey() %>" bundle="SIADAP_RESOURCES"/></strong>
				<%-- END Title --%>
				
				<%-- Summary table --%>
				<div class="infobox">
					<table class="tstyle4">
						<logic:equal value="false" name="unit" property="siadapStructureTopUnit">
							<tr>
								<th colspan="6">Unidade:</th>
								<th colspan="6">Global:</th>
							</tr>
						</logic:equal>
						<tr>
							<th><bean:message key="label.validation.totalEvaluations" bundle="SIADAP_RESOURCES" /></th>
							<td <%=unitWrapper.isSiadapStructureTopUnit() ?  "" : "colspan='2'"%>><fr:view name="siadapUniverseWrapper" property="numberRelevantPeopleInUniverse"/></td>
							<th>de</th>
							<td <%=unitWrapper.isSiadapStructureTopUnit() ?  "" : "colspan='2'"%>><fr:view name="siadapUniverseWrapper" property="numberTotalRelevantForQuotaPeopleInUniverse"/></td>
							<%-- exclusively global part --%>
							<logic:equal value="false" name="unit" property="siadapStructureTopUnit">
								<td colspan="2"><fr:view name="siadapUniverseWrapper" property="globalNumberRelevantPeopleInUniverse"/></td>
								<th>de</th>
								<td colspan="3"><fr:view name="siadapUniverseWrapper" property="globalNumberTotalRelevantForQuotaPeopleInUniverse"/></td>
							</logic:equal>
						</tr>
						<tr>
							<th><bean:message key="label.harmonization.quota.excellents" bundle="SIADAP_RESOURCES" /></th>
							<td <%=unitWrapper.isSiadapStructureTopUnit() ?  "" : "colspan='5'"%>><fr:view name="siadapUniverseWrapper" property="excellencyQuota"/></td>
							<%-- exclusively global part --%>
							<logic:equal value="false" name="unit" property="siadapStructureTopUnit">
								<td colspan="5"><fr:view name="siadapUniverseWrapper" property="globalExcellencyQuota"/></td>
							</logic:equal>
						</tr>
						<tr>
							<th><bean:message key="label.harmonization.current.excellents.used.quota" bundle="SIADAP_RESOURCES" /></th>
							<td><fr:view name="siadapUniverseWrapper" property="currentEvaluationExcellents"/></td>
							<th><bean:message key="label.harmonized" bundle="SIADAP_RESOURCES" /></th>
							<td class="<%=((SiadapUniverseWrapper) siadapUniverseWrapper).getCurrentHarmonizedExcellentsHTMLClass()%>"><fr:view name="siadapUniverseWrapper" property="currentHarmonizedExcellents"/></td>
							<th><bean:message key="label.validated.summary.board" bundle="SIADAP_RESOURCES" /></th>
							<td class="<%=((SiadapUniverseWrapper) siadapUniverseWrapper).getCurrentValidatedExcellentsHTMLClass()%>"><fr:view name="siadapUniverseWrapper" property="currentValidatedExcellents"/></td>
							
							<%-- exclusively global part --%>
							<logic:equal value="false" name="unit" property="siadapStructureTopUnit">
								<td><fr:view name="siadapUniverseWrapper" property="globalCurrentEvaluationExcellents"/></td>
								<th><bean:message key="label.harmonized" bundle="SIADAP_RESOURCES" /></th>
								<td class="<%=((SiadapUniverseWrapper) siadapUniverseWrapper).getCurrentHarmonizedExcellentsHTMLClass()+"_global"%>"><fr:view name="siadapUniverseWrapper" property="globalCurrentHarmonizedExcellents"/></td>
								<th><bean:message key="label.validated.summary.board" bundle="SIADAP_RESOURCES" /></th>
								<td class="<%=((SiadapUniverseWrapper) siadapUniverseWrapper).getCurrentValidatedExcellentsHTMLClass()+"_global"%>"><fr:view name="siadapUniverseWrapper" property="globalCurrentValidatedExcellents"/></td>
							</logic:equal>
						</tr>
						<tr>
							<th><bean:message key="label.harmonization.quota.relevant" bundle="SIADAP_RESOURCES" /></th>
							<td <%=unitWrapper.isSiadapStructureTopUnit() ? "" : "colspan='5'" %>><fr:view name="siadapUniverseWrapper" property="relevantQuota"/></td>
							<%-- exclusively global part --%>
							<logic:equal value="false" name="unit" property="siadapStructureTopUnit">
								<td colspan="5"><fr:view name="siadapUniverseWrapper" property="globalRelevantQuota"/></td>
							</logic:equal>
						</tr>
						<tr>
							<th><bean:message key="label.harmonization.current.relevant.used.quota" bundle="SIADAP_RESOURCES" /></th>
							<td><fr:view name="siadapUniverseWrapper" property="currentEvaluationRelevants"/></td>
							<th><bean:message key="label.harmonized" bundle="SIADAP_RESOURCES" /></th>
							<td class="<%=((SiadapUniverseWrapper) siadapUniverseWrapper).getCurrentHarmonizedRelevantsHTMLClass()%>"><fr:view name="siadapUniverseWrapper" property="currentHarmonizedRelevants"/></td>
							<th><bean:message key="label.validated.summary.board" bundle="SIADAP_RESOURCES" /></th>
							<td class="<%=((SiadapUniverseWrapper) siadapUniverseWrapper).getCurrentValidatedRelevantsHTMLClass()%>"><fr:view name="siadapUniverseWrapper" property="currentValidatedRelevants"/></td>
							
							<%-- exclusively global part --%>
							<logic:equal value="false" name="unit" property="siadapStructureTopUnit">
								<td><fr:view name="siadapUniverseWrapper" property="globalCurrentEvaluationRelevants"/></td>
								<th><bean:message key="label.harmonized" bundle="SIADAP_RESOURCES" /></th>
								<td class="<%=((SiadapUniverseWrapper) siadapUniverseWrapper).getCurrentHarmonizedRelevantsHTMLClass()+"_global"%>"><fr:view name="siadapUniverseWrapper" property="globalCurrentHarmonizedRelevants"/></td>
								<th><bean:message key="label.validated.summary.board" bundle="SIADAP_RESOURCES" /></th>
								<td class="<%=((SiadapUniverseWrapper) siadapUniverseWrapper).getCurrentValidatedRelevantsHTMLClass()+"_global"%>"><fr:view name="siadapUniverseWrapper" property="globalCurrentValidatedRelevants"/></td>
							</logic:equal>
						</tr>
					</table>
				</div>	
				<%-- END Summary table --%>
				<div class="highlightBox" style="display: none;">
					Atenção, este universo contem avaliações cuja harmonização ainda não foi encerrada. Isto pode fazer com que haja pareceres de harmonização que se venham a alterar.
				</div>
			
				</logic:notEmpty>
				
			 <%-- Suggestions part --%>			 
				<logic:notEmpty name="siadapUniverseWrapper" property="siadapExceedingQuotaSuggestionsByTypeForUniverse">
					<logic:iterate id="suggestionsByType" name="siadapUniverseWrapper" property="siadapExceedingQuotaSuggestionsByTypeForUniverse">
						<logic:notEmpty name="suggestionsByType" property="value">
						
							<%-- Title --%>
							<strong><bean:message key="<%="ExceedingQuotaSuggestionType."+((java.util.Map.Entry<module.siadap.domain.ExceedingQuotaSuggestionType, List>)suggestionsByType).getKey().name()%>" bundle="SIADAP_RESOURCES"/></strong>
							<fr:edit name="suggestionsByType" property="value" nested="true">
								<fr:schema type="module.siadap.domain.wrappers.SiadapSuggestionBean" bundle="SIADAP_RESOURCES">
									<fr:slot name="personWrapper.person.name" key="label.evaluated" bundle="SIADAP_RESOURCES" readOnly="true" />
									<fr:slot name="personWrapper.person.user.username" key="label.login.username" bundle="MYORG_RESOURCES" readOnly="true" />
									<% 
									SiadapUniverseWrapper siadapUWrapper = (SiadapUniverseWrapper) siadapUniverseWrapper;
									if (siadapUWrapper.getSiadapUniverseEnum().equals(SiadapUniverse.SIADAP2))
									{
									    %>
									<fr:slot name="personWrapper.totalEvaluationScoringSiadap2" layout="null-as-label" key="label.totalEvaluationScoring" readOnly="true">
										<fr:property name="subLayout" value="" />
									</fr:slot>
									<fr:slot name="personWrapper.totalQualitativeEvaluationScoringSiadap2" layout="null-as-label" key="label.totalQualitativeEvaluationScoring" readOnly="true">
										<fr:property name="subLayout" value="" />
									</fr:slot>
									<%
									} else {
									%>
									<fr:slot name="personWrapper.totalEvaluationScoringSiadap3" layout="null-as-label" key="label.totalEvaluationScoring" readOnly="true"> 
										<fr:property name="subLayout" value="" />
									</fr:slot>
									<fr:slot name="personWrapper.totalQualitativeEvaluationScoringSiadap3" layout="null-as-label" key="label.totalQualitativeEvaluationScoring" readOnly="true">
										<fr:property name="subLayout" value="" />
									</fr:slot>
									<% } %>
									
									<fr:slot name="exceedingQuotaPriorityNumber" key="label.harmonization.exceedingQuotaPriorityNumber" bundle="SIADAP_RESOURCES" readOnly="true"/>
									<fr:slot name="type" readOnly="true"/>
								</fr:schema>
								<fr:layout name="tabular-row">
									<fr:property name="classes" value="tstyle2" />
									<fr:property name="columnClasses" value="aleft,aleft,," />
						
									<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess" />
									<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES" />
									<fr:property name="key(viewProcess)" value="link.view" />
									<fr:property name="param(viewProcess)" value="personWrapper.siadap.process.externalId/processId" />
									<fr:property name="order(viewProcess)" value="1" />
						 			<fr:property name="visibleIf(viewProcess)" value="personWrapper.accessibleToCurrentUser" />
						 			
						<%--
									<fr:property name="sortParameter" value="sortByQuotas" />
									<fr:property name="sortUrl" value="<%= "/siadapManagement.do?method=viewUnitHarmonizationData&unitId=" + unitId + "&year=" + year.toString()%>" />
									<fr:property name="sortBy" value="<%= request.getParameter("sortByQuotas") == null ? "person.partyName=asc" : request.getParameter("sortByQuotas") %>" />
									--%>
				 				</fr:layout>
							</fr:edit>
						</logic:notEmpty>
					</logic:iterate>
					
										 <%-- Title 
					<strong><bean:message key="<%=((module.siadap.domain.wrappers.SiadapUniverseWrapper)siadapUniverseWrapper).getUniverseTitleQuotaSuggestionKey() %>" bundle="SIADAP_RESOURCES"/></strong>
			
				 	<fr:edit name="siadapUniverseWrapper" property="siadapUniverseForSuggestions" nested="true">
						<fr:schema type="module.siadap.domain.wrappers.SiadapSuggestionBean" bundle="SIADAP_RESOURCES">
							<fr:slot name="personWrapper.person.name" key="label.evaluated" bundle="SIADAP_RESOURCES" readOnly="true" />
							<fr:slot name="personWrapper.person.user.username" key="label.login.username" bundle="MYORG_RESOURCES" readOnly="true" />
							<% 
							SiadapUniverseWrapper siadapUWrapper = (SiadapUniverseWrapper) siadapUniverseWrapper;
							if (siadapUWrapper.getSiadapUniverseEnum().equals(SiadapUniverse.SIADAP2))
							{
							    %>
							<fr:slot name="personWrapper.totalEvaluationScoringSiadap2" layout="null-as-label" key="label.totalEvaluationScoring" readOnly="true">
								<fr:property name="subLayout" value="" />
							</fr:slot>
							<fr:slot name="personWrapper.totalQualitativeEvaluationScoringSiadap2" layout="null-as-label" key="label.totalQualitativeEvaluationScoring" readOnly="true">
								<fr:property name="subLayout" value="" />
							</fr:slot>
							<%
							} else {
							%>
							<fr:slot name="personWrapper.totalEvaluationScoringSiadap3" layout="null-as-label" key="label.totalEvaluationScoring" readOnly="true"> 
								<fr:property name="subLayout" value="" />
							</fr:slot>
							<fr:slot name="personWrapper.totalQualitativeEvaluationScoringSiadap3" layout="null-as-label" key="label.totalQualitativeEvaluationScoring" readOnly="true">
								<fr:property name="subLayout" value="" />
							</fr:slot>
							<% } %>
							
							<fr:slot name="exceedingQuotaPriorityNumber" key="label.harmonization.exceedingQuotaPriorityNumber" bundle="SIADAP_RESOURCES" readOnly="true"/>
							<fr:slot name="type" readOnly="true"/>
						</fr:schema>
						<fr:layout name="tabular-row">
							<fr:property name="classes" value="tstyle2" />
							<fr:property name="columnClasses" value="aleft,aleft,," />
						<%-- 	<fr:property name="link(create)" value="/siadapManagement.do?method=createNewSiadapProcess" />
							<fr:property name="bundle(create)" value="MYORG_RESOURCES" />
							<fr:property name="key(create)" value="link.create" />
							<fr:property name="param(create)" value="personWrapper.person.externalId/personId" />
							<fr:property name="order(create)" value="1" />
							<fr:property name="visibleIf(create)" value="personWrapper.currentUserAbleToCreateProcess" /> 
				
							<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess" />
							<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES" />
							<fr:property name="key(viewProcess)" value="link.view" />
							<fr:property name="param(viewProcess)" value="personWrapper.siadap.process.externalId/processId" />
							<fr:property name="order(viewProcess)" value="1" />
				 			<fr:property name="visibleIf(viewProcess)" value="personWrapper.accessibleToCurrentUser" />
				 			
				 			<logic:equal value="false" name="unit" property="harmonizationFinished">
								<fr:property name="link(removeSuggestion)" value="<%="/siadapManagement.do?method=removeExceedingQuotaProposal&year="+year+"&unitId="+unitId%>" />
								<fr:property name="bundle(removeSuggestion)" value="SIADAP_RESOURCES" />
								<fr:property name="key(removeSuggestion)" value="link.remove.ExceedingQuotaProposal" />
								<fr:property name="param(removeSuggestion)" value="proposal.externalId/proposalId" />
								<fr:property name="order(removeSuggestion)" value="2" />
							</logic:equal>
				
				<%--
							<fr:property name="sortParameter" value="sortByQuotas" />
							<fr:property name="sortUrl" value="<%= "/siadapManagement.do?method=viewUnitHarmonizationData&unitId=" + unitId + "&year=" + year.toString()%>" />
							<fr:property name="sortBy" value="<%= request.getParameter("sortByQuotas") == null ? "person.partyName=asc" : request.getParameter("sortByQuotas") %>" />
				 
						</fr:layout>
					</fr:edit> --%>
				</logic:notEmpty>
			 	<fr:edit name="siadapUniverseWrapper" property="siadapUniverse" nested="true">
					<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
						<fr:slot name="person.name" key="label.evaluated" bundle="SIADAP_RESOURCES" readOnly="true" />
						<fr:slot name="person.user.username" key="label.login.username" bundle="MYORG_RESOURCES" readOnly="true" />
						<% 
						SiadapUniverseWrapper siadapUWrapper = (SiadapUniverseWrapper) siadapUniverseWrapper;
						if (siadapUWrapper.getSiadapUniverseEnum().equals(SiadapUniverse.SIADAP2))
						{
						    %>
						<fr:slot name="totalEvaluationScoringSiadap2" layout="null-as-label" key="label.totalEvaluationScoring" readOnly="true">
							<fr:property name="subLayout" value="" />
						</fr:slot>
						<fr:slot name="totalQualitativeEvaluationScoringSiadap2" layout="null-as-label" key="label.totalQualitativeEvaluationScoring" readOnly="true">
							<fr:property name="subLayout" value="" />
						</fr:slot>
						<fr:slot name="harmonizationCurrentAssessmentForSIADAP2" layout="radio" key="label.harmonization.assessment" readOnly="true"/> 
						<fr:slot name="harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2" layout="radio" key="label.harmonization.assessment.forExcellencyAward" readOnly="true"/> 
						
						<fr:slot name="validationCurrentAssessmentForSIADAP2" layout="radio" key="label.validation.validationCurrentAssessment">
							<fr:property name="readOnlyIfNot" value="siadap2AbleToBeValidated" />
							<fr:property name="classes" value="inline-list"/>
						</fr:slot>
						<fr:slot name="validationCurrentAssessmentForExcellencyAwardForSIADAP2" layout="radio" key="label.validation.validationCurrentAssessmentForExcellencyAward">
							<fr:property name="readOnlyIfNot" value="siadap2AbleToBeValidated" />
							<fr:property name="classes" value="inline-list"/>
						</fr:slot>
						<fr:slot name="validationClassificationForSIADAP2" key="label.validation.classification">
							<fr:property name="size" value="5"/>
							<fr:property name="maxLength" value="5"/>
						</fr:slot>
						<%
						} else {
						%>
						<fr:slot name="totalEvaluationScoringSiadap3" layout="null-as-label" key="label.totalEvaluationScoring" readOnly="true"> 
							<fr:property name="subLayout" value="" />
						</fr:slot>
						<fr:slot name="totalQualitativeEvaluationScoringSiadap3" layout="null-as-label" key="label.totalQualitativeEvaluationScoring" readOnly="true">
							<fr:property name="subLayout" value="" />
						</fr:slot>
						<fr:slot name="harmonizationCurrentAssessmentForSIADAP3" layout="radio" key="label.harmonization.assessment" readOnly="true"/> 
						<fr:slot name="harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3" layout="radio" key="label.harmonization.assessment.forExcellencyAward" readOnly="true"/> 
						<fr:slot name="validationCurrentAssessmentForSIADAP3" layout="radio" key="label.validation.validationCurrentAssessment">
							<fr:property name="readOnlyIfNot" value="siadap3AbleToBeValidated" />
							<fr:property name="classes" value="inline-list"/>
						</fr:slot>
						<fr:slot name="validationCurrentAssessmentForExcellencyAwardForSIADAP3" layout="radio" key="label.validation.validationCurrentAssessmentForExcellencyAward">
							<fr:property name="readOnlyIfNot" value="siadap3AbleToBeValidated" />
							<fr:property name="classes" value="inline-list"/>
						</fr:slot>
						
						<fr:slot name="validationClassificationForSIADAP3" key="label.validation.classification">
							<fr:property name="size" value="5"/>
							<fr:property name="maxLength" value="5"/>
						</fr:slot>
						<% } %>
						
					</fr:schema>
					<fr:layout name="tabular-row">
						<fr:property name="classes" value="tstyle2" />
						<fr:property name="columnClasses" value="aleft,aleft,," />
					<%-- 	<fr:property name="link(create)" value="/siadapManagement.do?method=createNewSiadapProcess" />
						<fr:property name="bundle(create)" value="MYORG_RESOURCES" />
						<fr:property name="key(create)" value="link.create" />
						<fr:property name="param(create)" value="personWrapper.person.externalId/personId" />
						<fr:property name="order(create)" value="1" />
						<fr:property name="visibleIf(create)" value="personWrapper.currentUserAbleToCreateProcess" /> 
			
						<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess" />
						<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES" />
						<fr:property name="key(viewProcess)" value="link.view" />
						<fr:property name="param(viewProcess)" value="personWrapper.siadap.process.externalId/processId" />
						<fr:property name="order(viewProcess)" value="1" />
			 			<fr:property name="visibleIf(viewProcess)" value="personWrapper.accessibleToCurrentUser" />
			 			
			 			<logic:equal value="false" name="unit" property="harmonizationFinished">
							<fr:property name="link(removeSuggestion)" value="<%="/siadapManagement.do?method=removeExceedingQuotaProposal&year="+year+"&unitId="+unitId%>" />
							<fr:property name="bundle(removeSuggestion)" value="SIADAP_RESOURCES" />
							<fr:property name="key(removeSuggestion)" value="link.remove.ExceedingQuotaProposal" />
							<fr:property name="param(removeSuggestion)" value="proposal.externalId/proposalId" />
							<fr:property name="order(removeSuggestion)" value="2" />
						</logic:equal>
			
			<%--
						<fr:property name="sortParameter" value="sortByQuotas" />
						<fr:property name="sortUrl" value="<%= "/siadapManagement.do?method=viewUnitHarmonizationData&unitId=" + unitId + "&year=" + year.toString()%>" />
						<fr:property name="sortBy" value="<%= request.getParameter("sortByQuotas") == null ? "person.partyName=asc" : request.getParameter("sortByQuotas") %>" />
			 --%>
					</fr:layout>
				</fr:edit>
				
				
	</logic:iterate>
	<html:submit styleClass="inputbutton">
		<bean:message key="label.save" bundle="SIADAP_RESOURCES" />
	</html:submit>
	</fr:form> 
</logic:equal>
<logic:equal name="noData" value="true">
<i>Sem dados para este ano</i>
</logic:equal>

<%-- Sub Harmonization units --%>
<logic:notEmpty name="unit" property="unit">
	<bean:define id="year" name="unit" property="year" />
	<bean:define id="unitName" name="unit" property="unit.presentationName"  />
	<bean:define id="unitId" name="unit" property="unit.externalId" />
	
	<p><strong>
		<bean:message key="label.subUnits" bundle="SIADAP_RESOURCES"/>:
	</strong></p>
			
	<p>
		<fr:view name="unit" property="subHarmonizationUnits">
			<fr:schema type="module.siadap.domain.wrappers.UnitSiadapWrapper" bundle="SIADAP_RESOURCES">
				<fr:slot name="unit.partyName" key="label.unit" bundle="ORGANIZATION_RESOURCES" />
				<fr:slot name="unit.acronym" key="label.acronym" bundle="ORGANIZATION_RESOURCES"/>
			<%-- <fr:slot name="relevantEvaluationPercentage"/>
				<fr:slot name="excellencyEvaluationPercentage"/>
					--%>
				<fr:slot name="totalPeopleHarmonizedInUnit" key="label.totalEvaluated"/>
				<fr:slot name="totalPeopleHarmonizedInUnitWithSiadapStarted" key="label.totalPeopleWithSiadapHarmonizedInUnit"/>
				<fr:slot name="harmonizationFinished" key="label.validation.harmonization.closed"/>
			</fr:schema>
			<fr:layout name="tabular">
				<fr:property name="classes" value="tstyle2"/>
				<fr:property name="link(view)" value="<%="/siadapManagement.do?method=validate&year=" + year.toString() + "&unitId=" + unitId.toString()%>"/>
				<fr:property name="bundle(view)" value="MYORG_RESOURCES"/>
				<fr:property name="key(view)" value="link.view"/>
				<fr:property name="param(view)" value="unit.externalId/unitId"/>
				<fr:property name="order(view)" value="1"/>
			</fr:layout>
		</fr:view>
	</p>
</logic:notEmpty>
