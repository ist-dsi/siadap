<%@page import="module.siadap.domain.SiadapUniverse"%>
<%@page import="module.siadap.domain.wrappers.SiadapUniverseWrapper"%>
<%@page import="java.util.Set"%>
<%@page import="module.siadap.domain.wrappers.PersonSiadapWrapper"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>

<bean:define id="unitName" name="currentUnit" property="unit.partyName"/>
<bean:define id="unitId" name="currentUnit" property="unit.externalId"/>

<bean:define id="unitId" name="currentUnit" property="unit.externalId"/>

<bean:define id="year" name="currentUnit" property="year"/>

<h1>SIADAP <%=year.toString()%></h1>

<h2>
	<fr:view name="currentUnit" property="unit.partyName"/>
</h2>


<%-- Error messages: --%>
<logic:messagesPresent property="message" message="true">
	<div class="error1">
		<html:messages id="errorMessage" property="message" message="true"> 
			<span><fr:view name="errorMessage"/></span>
		</html:messages>
	</div>
</logic:messagesPresent>
<%-- *END* Error messages *END* --%>



<logic:present name="currentUnit" property="superiorUnit">
	<bean:define id="superiorUnit" name="currentUnit" property="superiorUnit" type="module.organization.domain.Unit"/>
	
	<p><strong>
		<bean:message key="label.superiorUnit" bundle="SIADAP_RESOURCES"/>:
	</strong>
	<%-- We only want to view the superior unit as a link if we have harm. responsabilities in it (or we are members of the CCA) --%>
	<logic:equal value="true" name="currentUnit" property="superiorHarmonizationUnitWrapper.responsibleForHarmonization">
		<html:link page="<%="/siadapManagement.do?method=viewUnitHarmonizationData&year=" + year.toString()%>" paramId="unitId" paramName="superiorUnit" paramProperty="externalId">		
			<fr:view name="superiorUnit" property="partyName"/>
		</html:link>
	</logic:equal>
	<%-- otherwise we will just presen the name of it--%>
	<logic:equal value="false" name="currentUnit" property="superiorHarmonizationUnitWrapper.responsibleForHarmonization" >
		<fr:view name="superiorUnit" property="partyName"/>
	</logic:equal>
	</p>
</logic:present>
<h3><i><bean:message bundle="SIADAP_RESOURCES" key="note.on.grades.not.being.the.latest"/></i></h3>

<%-- The global Info box is not needed ATM
<div class="infobox">
	<fr:view name="currentUnit">
		<fr:schema type="module.siadap.domain.wrappers.UnitSiadapWrapper" bundle="SIADAP_RESOURCES">
			<fr:slot name="unit.partyName" key="label.unit" bundle="ORGANIZATION_RESOURCES"/>
			<fr:slot name="harmonizationUnit" key="label.harmonizationUnit" layout="null-as-label">
				<fr:property name="subLayout" value="values"/>
				<fr:property name="subSchema" value="module.organization.domain.Party.view.short.name"/>
			</fr:slot>
			<fr:slot name="evaluationResponsibles" key="label.unitEvaluationResponsibles" layout="flowLayout">
				<fr:property name="eachLayout" value="values"/>
				<fr:property name="eachSchema" value="organization.domain.Person.view.short"/>
				<fr:property name="htmlSeparator" value=", "/>
			</fr:slot>
			<fr:slot name="harmonizationResponsibles" key="label.unitHarmonizationResponsibles" layout="flowLayout">
				<fr:property name="eachLayout" value="values"/>
				<fr:property name="eachSchema" value="organization.domain.Person.view.short"/>
				<fr:property name="htmlSeparator" value=", "/>
			</fr:slot>
			<fr:slot name="totalPeopleWorkingInUnitDescriptionString" key="label.totalEvaluatedCurrentUnit"/>
			<fr:slot name="highGradeQuota" />
			<fr:slot name="currentUsedHighGradeQuota"/>
			<fr:slot name="relevantEvaluationPercentage"/>
			<fr:slot name="excellencyGradeQuota"/>
			<fr:slot name="currentUsedExcellencyGradeQuota"/>
			<fr:slot name="excellencyEvaluationPercentage"/>
		</fr:schema>
		<fr:layout name="tabular">
			<fr:property name="columnClasses" value="aright,,"/>
		</fr:layout>
	</fr:view>
</div>
 --%>
<logic:equal name="currentUnit" property="responsibleForHarmonization" value="true">
	<%-- <logic:equal name="currentUnit" property="aboveQuotasHarmonization" value="true">
	
		<bean:define id="currentHighQuota" name="currentUnit" property="currentUsedHighGradeQuota"/>
		<bean:define id="highQuota" name="currentUnit" property="highGradeQuota"/>
		<bean:define id="currentExcellencyQuota" name="currentUnit" property="currentUsedExcellencyGradeQuota"/>
		<bean:define id="excellencyGrade" name="currentUnit" property="excellencyGradeQuota"/>
		
		<div class="highlightBox">
			<bean:message key="warning.harmonizationUnitAboveQuotas" bundle="SIADAP_RESOURCES" arg0="<%= currentHighQuota.toString() %>" arg1="<%= highQuota.toString() %>" arg2="<%= currentExcellencyQuota.toString() %>" arg3="<%= excellencyGrade.toString() %>"/>
		</div>
	</logic:equal>
	--%>
	<p>
	<%-- <html:link page="<%="/siadapManagement.do?method=listHighGlobalEvaluations&year=" + year.toString()%>" paramName="currentUnit" paramProperty="unit.externalId" paramId="unitId"> <bean:message key="label.viewGlobalEvaluations.relevant" bundle="SIADAP_RESOURCES"/> </html:link> | <html:link page="<%= "/siadapManagement.do?method=listExcellencyGlobalEvaluations&year=" + year.toString()%>" paramName="currentUnit" paramProperty="unit.externalId" paramId="unitId"> <bean:message key="label.viewGlobalEvaluations.excellency" bundle="SIADAP_RESOURCES"/> </html:link>--%>
	<logic:equal name="currentUnit" property="harmonizationActive" value="true">
	    <logic:equal name="currentUnit" property="harmonizationUnit" value="true">
	    	<logic:equal name="currentUnit" property="harmonizationFinished" value="false">
				 
				<%-- <html:link styleId="terminateHarmonization" page="<%="/siadapManagement.do?method=viewUnitHarmonizationData&year=" + year + "&unitId=" + unitId%>">
					<bean:message key="label.terminateHarmonization" bundle="SIADAP_RESOURCES"/> (<bean:message key="functionality.disabled.temporarily.short" bundle="SIADAP_RESOURCES"/>)
				</html:link> --%>
				  <html:link styleId="terminateHarmonization"  page="<%="/siadapManagement.do?method=terminateHarmonization&year="+year.toString()%>" paramName="currentUnit" paramProperty="unit.externalId" paramId="unitId">
					<bean:message key="label.terminateHarmonization" bundle="SIADAP_RESOURCES"/>
				</html:link> 
				| <html:link  page="<%="/siadapManagement.do?method=prepareAddExceedingQuotaSuggestion&year=" + year.toString()%>" paramName="currentUnit" paramProperty="unit.externalId" paramId="unitId">
						<bean:message key="label.ExcedingQuotaSuggestion" bundle="SIADAP_RESOURCES"/>
				  </html:link> 
				  <%-- <html:link  page="#">
						<bean:message key="label.addExcedingQuotaSuggestion" bundle="SIADAP_RESOURCES"/> (<bean:message key="functionality.disabled.temporarily.short" bundle="SIADAP_RESOURCES"/>)
				  </html:link> --%>  
				 
				 
		</logic:equal>
		 <logic:equal name="currentUnit" property="harmonizationFinished" value="true">
		    <html:link styleId="reOpenHarmonization"  page="<%="/siadapManagement.do?method=reOpenHarmonization&year="+year.toString()%>" paramName="currentUnit" paramProperty="unit.externalId" paramId="unitId">
					<bean:message key="label.reOpenHarmonization" bundle="SIADAP_RESOURCES"/>
			   </html:link>
				| <html:link  page="<%="/siadapManagement.do?method=prepareAddExceedingQuotaSuggestion&year=" + year.toString()%>" paramName="currentUnit" paramProperty="unit.externalId" paramId="unitId">
						<bean:message key="label.ExcedingQuotaSuggestion" bundle="SIADAP_RESOURCES"/>
				  </html:link> 
			<%--| <html:link  page="<%="/siadapManagement.do?method=prepareAddExceedingQuotaSuggestion&year=" + year.toString()%>" paramName="currentUnit" paramProperty="unit.externalId" paramId="unitId">
					<bean:message key="label.addExcedingQuotaSuggestion" bundle="SIADAP_RESOURCES"/>
			  </html:link> 
			    <html:link  page="#">
						<bean:message key="label.addExcedingQuotaSuggestion" bundle="SIADAP_RESOURCES"/> (<bean:message key="functionality.disabled.temporarily.short" bundle="SIADAP_RESOURCES"/>)
			  </html:link> --%>
			
			</logic:equal>
		</logic:equal>
	</logic:equal>
	</p>	
	
 <script src="<%= request.getContextPath() + "/javaScript/jquery.alerts.js"%>" type="text/javascript"></script> 
 <script src="<%= request.getContextPath() + "/javaScript/alertHandlers.js"%>" type="text/javascript"></script> 
 <script type="text/javascript"> 
   //linkConfirmationHook('terminateHarmonization', '<bean:message key="label.terminateHarmonization.confirmationMessage" bundle="SIADAP_RESOURCES" arg0="<%= unitName.toString() %>"/>','<bean:message key="label.terminateHarmonization" bundle="SIADAP_RESOURCES"/>'); 
 </script> 
	
	
</logic:equal>


<%
SiadapUniverseWrapper peopleWithQuotasSIADAP2 = (SiadapUniverseWrapper) request.getAttribute("people-withQuotas-SIADAP2");
SiadapUniverseWrapper peopleWithQuotasSIADAP3 = (SiadapUniverseWrapper) request.getAttribute("people-withQuotas-SIADAP3");
SiadapUniverseWrapper peopleWithoutQuotasSIADAP2 = (SiadapUniverseWrapper) request.getAttribute("people-withoutQuotas-SIADAP2");
SiadapUniverseWrapper peopleWithoutQuotasSIADAP3 = (SiadapUniverseWrapper) request.getAttribute("people-withoutQuotas-SIADAP3");

//if we have no listings for some reason, there's no point in having a form
boolean hasPeopleToHarmonize = ((peopleWithoutQuotasSIADAP2.getSiadapUniverse() == null || peopleWithoutQuotasSIADAP2.getSiadapUniverse().size() == 0) && (peopleWithQuotasSIADAP3.getSiadapUniverse() == null || peopleWithQuotasSIADAP3.getSiadapUniverse().size() == 0) && (peopleWithQuotasSIADAP2.getSiadapUniverse() == null || peopleWithQuotasSIADAP2.getSiadapUniverse().size() == 0) && (peopleWithoutQuotasSIADAP3.getSiadapUniverse() == null || peopleWithoutQuotasSIADAP3.getSiadapUniverse().size() ==0)) ? false : true;

	request.setAttribute("hasPeopleToHarmonize", hasPeopleToHarmonize);
%>

<logic:equal value="true" name="hasPeopleToHarmonize" >
	<fr:form action="<%="/siadapManagement.do?method=setUnitHarmonizationAssessmentData&year=" + year.toString() + "&unitId=" + unitId.toString() %>">
<style>
	.inline-list ul, .inline-list li {
		display: inline;
		margin: 0;
		padding: 0;
		font-weight: bold;
	}
</style>
<script type="text/javascript">
	String.prototype.endsWith = function(str) 
	{return (this.match(str+"$")==str)}
	
	function decreaseOne(elementToChange)
	{
		$(elementToChange).text(parseInt($(elementToChange).text(), 10) - 1)
		$(elementToChange).css("font-weight","bold");
	}
	function increaseOne(elementToChange)
	{
		$(elementToChange).text(parseInt($(elementToChange).text(), 10) + 1)
		$(elementToChange).css("font-weight","bold");
	}
	function handleWithoutQuotasSIADAP3RadioClick(radioElement)
	{
		if ($(radioElement).next().text().trim() == "Não")
		{
		//let's check if they had the Yes selected
		if ($(radioElement).parents("li").prevAll().children().children("input")[0].wasSetToTrue)
			{
			//in case they had, we will decrease the corresponding number
			if ($(radioElement).parents("td").prev().text().trim() == "Desempenho excelente")
				{
				 decreaseOne($('.current-harmonized-excellents-siadap3WithoutQuotas'));
				}
			if ($(radioElement).parents("td").prev().text().trim() == "Desempenho relevante")
				{
				 decreaseOne($('.current-harmonized-relevants-siadap3WithoutQuotas'));
				}
			}
		}
		if ($(radioElement).next().text().trim() == "Sim")
		{
			if ($(radioElement).parents("td").prev().text().trim() == "Desempenho excelente")
			{
			 increaseOne($('.current-harmonized-excellents-siadap3WithoutQuotas'));
			}
			if ($(radioElement).parents("td").prev().text().trim() == "Desempenho relevante")
			{
			 increaseOne($('.current-harmonized-relevants-siadap3WithoutQuotas'));
			}

		}
	}
	
	function extractTrimmedQualitativeEvaluationText(radioElement) {
		if (radioElement.name.indexOf("ExcellencyAward") != -1)
			return $(radioElement).parents("td").prev().prev().text().trim();
		else
			return $(radioElement).parents("td").prev().text().trim();
	}
	
	function disableAndSetToFalseNextExcellencyRadios(radioOfOrigin)
	{
		$(radioOfOrigin).parents("td").next().children().children().children().children("input").removeAttr("checked");
		$(radioOfOrigin).parents("td").next().children().children().children().children("input[value='false']").attr("checked", "true");
		$(radioOfOrigin).parents("td").next().children().children().children().children("input").attr("disabled","true");
		$(radioOfOrigin).parents("td").next().children().children().children().children("input[value='false']").change();
		
	}
	
	function enableNextExcellencyRadios(radioOfOrigin)
	{
		$(radioOfOrigin).parents("td").next().children().children().children().children("input").removeAttr("disabled");
		
	}
	function handleRadioClick(radioElement, universeString)
	{
		if ($(radioElement).next().text().trim() == "Não")
		{
		//let's check if they had the Yes selected
		if ($(radioElement).parents("li").prevAll().children().children("input")[0].wasSetToTrue)
			{
			//in case they had, we will decrease the corresponding number
			if (extractTrimmedQualitativeEvaluationText(radioElement) == "Desempenho excelente" || extractTrimmedQualitativeEvaluationText(radioElement) == "Desempenho relevante")
				{
				//so we ought to do something
					if (radioElement.name.indexOf("ExcellencyAward") != -1)
						{
						 //this is an Excellency eval, decrease them
						 decreaseOne($('.current-harmonized-excellents-'+universeString));
						}
					else {
						 //this is a regular eval, decrease them
						 decreaseOne($('.current-harmonized-relevants-'+universeString));
					}
				}
		
			}
			//set the excellency radios to No and disabled, if they exist
		  disableAndSetToFalseNextExcellencyRadios(radioElement)
		}
		if ($(radioElement).next().text().trim() == "Sim")
		{
			if (extractTrimmedQualitativeEvaluationText(radioElement) == "Desempenho excelente" || extractTrimmedQualitativeEvaluationText(radioElement) == "Desempenho relevante")
			{
				if (radioElement.name.indexOf("ExcellencyAward") != -1)
					{
					// it is an excellency award
					 increaseOne($('.current-harmonized-excellents-'+universeString));
					}
				else {
					 increaseOne($('.current-harmonized-relevants-'+universeString));
					 //let's enable the disabled excellents, which are next to here, if they exist
					 enableNextExcellencyRadios(radioElement);
				}
			}
		}
	}
	
	function handleWithQuotasSIADAP3RadioClick(radioElement)
	{
		if ($(radioElement).next().text().trim() == "Não")
		{
		//let's check if they had the Yes selected
		if ($(radioElement).parents("li").prevAll().children().children("input")[0].wasSetToTrue)
			{
			//in case they had, we will decrease the corresponding number
			if (extractTrimmedQualitativeEvaluationText(radioElement) == "Desempenho excelente" || extractTrimmedQualitativeEvaluationText(radioElement) == "Desempenho relevante")
				{
				//so we ought to do something
					if (radioElement.name.indexOf("ExcellencyAward") != -1)
						{
						 //this is an Excellency eval, decrease them
						 decreaseOne($('.current-harmonized-excellents-siadap3WithQuotas'));
						}
					else {
						 //this is a regular eval, decrease them
						 decreaseOne($('.current-harmonized-relevants-siadap3WithQuotas'));
					}
				}
		
			}
		}
		if ($(radioElement).next().text().trim() == "Sim")
		{
			if (extractTrimmedQualitativeEvaluationText(radioElement) == "Desempenho excelente" || extractTrimmedQualitativeEvaluationText(radioElement) == "Desempenho relevante")
			{
				if (radioElement.name.indexOf("ExcellencyAward") != -1)
					{
					// it is an excellency award
					 increaseOne($('.current-harmonized-excellents-siadap3WithQuotas'));
					}
				else {
					 increaseOne($('.current-harmonized-relevants-siadap3WithQuotas'));
				}
			}
		}
	}
	function handleWithoutQuotasSIADAP2RadioClick(radioElement)
	{
		if ($(radioElement).next().text().trim() == "Não")
		{
		//let's check if they had the Yes selected
		if ($(radioElement).parents("li").prevAll().children().children("input")[0].wasSetToTrue)
			{
			//in case they had, we will decrease the corresponding number
			if ($(radioElement).parents("td").prev().text().trim() == "Desempenho excelente")
				{
				 decreaseOne($('.current-harmonized-excellents-siadap2WithoutQuotas'));
				}
			if ($(radioElement).parents("td").prev().text().trim() == "Desempenho relevante")
				{
				 decreaseOne($('.current-harmonized-relevants-siadap2WithoutQuotas'));
				}
			}
		}
		if ($(radioElement).next().text().trim() == "Sim")
		{
			if ($(radioElement).parents("td").prev().text().trim() == "Desempenho excelente")
			{
			 increaseOne($('.current-harmonized-excellents-siadap2WithoutQuotas'));
			}
			if ($(radioElement).parents("td").prev().text().trim() == "Desempenho relevante")
			{
			 increaseOne($('.current-harmonized-relevants-siadap2WithoutQuotas'));
			}

		}
	}
	function handleWithQuotasSIADAP2RadioClick(radioElement)
	{
		if ($(radioElement).next().text().trim() == "Não")
		{
		//let's check if they had the Yes selected
		if ($(radioElement).parents("li").prevAll().children().children("input")[0].wasSetToTrue)
			{
			//in case they had, we will decrease the corresponding number
			if ($(radioElement).parents("td").prev().text().trim() == "Desempenho excelente")
				{
				 decreaseOne($('.current-harmonized-excellents-siadap2WithQuotas'));
				}
			if ($(radioElement).parents("td").prev().text().trim() == "Desempenho relevante")
				{
				 decreaseOne($('.current-harmonized-relevants-siadap2WithQuotas'));
				}
			}
		}
		if ($(radioElement).next().text().trim() == "Sim")
		{
			if ($(radioElement).parents("td").prev().text().trim() == "Desempenho excelente")
			{
			 increaseOne($('.current-harmonized-excellents-siadap2WithQuotas'));
			}
			if ($(radioElement).parents("td").prev().text().trim() == "Desempenho relevante")
			{
			 increaseOne($('.current-harmonized-relevants-siadap2WithQuotas'));
			}

		}
	}
	
	$(document).ready(function() {
		$("input[type=radio]").each(function(indexInArray, radioInput) {
			if (radioInput.checked && $(radioInput).next().text().trim() == "Sim"){
				//it is checked, let's mark it that way
				radioInput.wasSetToTrue=true;
				
			}
		});
		
		//let's make sure that when we save things, nothing is disabled
		$("input[type='radio']").parents("form").submit(function() {
			$("input[type='radio']").removeAttr("disabled")
			});
		
		//give a visual clue to the ones with skipped eval
		$("td").each(function(indexInArray, td) { if ($(td).text().trim() == "não avaliado" || $(td).text().trim() == "Processo anulado") $(td).parent("tr").addClass("disabled"); }); 
		
		$("input[type=radio]").change(function () {
			/* if ($(this).next().text().trim() == "Sim")
				{
					this.wasSetToTrue=true;
				}
			if ($(this).parents("li").attr("class") == "withoutQuotasSIADAP3")
				{
					handleWithoutQuotasSIADAP3RadioClick(this);
				}
			if ($(this).parents("li").attr("class") == "withQuotasSIADAP3")
				{
					handleWithQuotasSIADAP3RadioClick(this);
				}
			if ($(this).parents("li").attr("class") == "withoutQuotasSIADAP2")
				{
					handleWithoutQuotasSIADAP2RadioClick(this);
				}
			if ($(this).parents("li").attr("class") == "withQuotasSIADAP2")
				{
					handleWithQuotasSIADAP2RadioClick(this);
				} */
				var stringToUse = null;
				if ($(this).next().text().trim() == "Sim")
				{
					this.wasSetToTrue=true;
				}
			if ($(this).parents("li").attr("class") == "withoutQuotasSIADAP3")
				{
					stringToUse="siadap3WithoutQuotas";
				}
			if ($(this).parents("li").attr("class") == "withQuotasSIADAP3")
				{
					stringToUse="siadap3WithQuotas";
				}
			if ($(this).parents("li").attr("class") == "withoutQuotasSIADAP2")
				{
					stringToUse="siadap2WithoutQuotas";
				}
			if ($(this).parents("li").attr("class") == "withQuotasSIADAP2")
				{
					stringToUse="siadap2WithQuotas";
				}
			handleRadioClick(this,stringToUse);
		});

		 });
	
	
</script>
	
<style> 
tr.disabled {
color: #999;
}
</style> 
		<logic:notEmpty name="people-withQuotas-SIADAP2" property="siadapUniverse">
 			<strong>
				<bean:message key="label.unitEmployees.withQuotasSIADAP2" bundle="SIADAP_RESOURCES"/>:
			</strong>
		<div class="infobox">
			<table class="tstyle2">
				<tr>
					<th><bean:message key="label.harmonization.totalHarmonizedInUniverse" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withQuotas-SIADAP2" property="numberRelevantPeopleInUniverse"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.quota.excellents" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withQuotas-SIADAP2" property="excellencyQuota"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.current.excellents.used.quota" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withQuotas-SIADAP2" property="currentEvaluationExcellents"/></td>
					<th><bean:message key="label.harmonized" bundle="SIADAP_RESOURCES" /></th>
					<td class="<%=peopleWithQuotasSIADAP2.getCurrentHarmonizedExcellentsHTMLClass()%>"><fr:view name="people-withQuotas-SIADAP2" property="currentHarmonizedExcellents"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.quota.relevant" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withQuotas-SIADAP2" property="relevantQuota"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.current.relevant.used.quota" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withQuotas-SIADAP2" property="currentEvaluationRelevants"/></td>
					<th><bean:message key="label.harmonized" bundle="SIADAP_RESOURCES" /></th>
					<td class="<%=peopleWithQuotasSIADAP2.getCurrentHarmonizedRelevantsHTMLClass()%>"><fr:view name="people-withQuotas-SIADAP2" property="currentHarmonizedRelevants"/></td>
				</tr>
			</table>
		</div>
	 		<logic:equal name="people-withQuotas-SIADAP2" property="siadapUniverseWithQuotasAboveQuotaHarmonization" value="true">
			
				<bean:define id="relevantQuota" name="people-withQuotas-SIADAP2" property="relevantQuota"/>
				
				<bean:define id="currentHarmonizedRelevants" name="people-withQuotas-SIADAP2" property="currentHarmonizedRelevants"/>
				<bean:define id="currentHarmonizedExcellents" name="people-withQuotas-SIADAP2" property="currentHarmonizedExcellents"/>
				<bean:define id="excellencyQuota" name="people-withQuotas-SIADAP2" property="excellencyQuota"/>
				
				<div class="highlightBox">
					<bean:message key="warning.harmonizationUnitAboveQuotas" bundle="SIADAP_RESOURCES" arg0="<%= currentHarmonizedRelevants.toString() %>" arg1="<%= relevantQuota.toString() %>" arg2="<%= currentHarmonizedExcellents.toString() %>" arg3="<%= excellencyQuota.toString() %>"/>
				</div>
			</logic:equal>
				<fr:edit id="people-withQuotas-SIADAP2" name="people-withQuotas-SIADAP2" visible="false" nested="true" />
				<fr:edit id="people-withQuotas-SIADAP2id" name="people-withQuotas-SIADAP2" property="siadapUniverse" nested="true">
					<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
						<fr:slot name="person.partyName" key="label.evaluated" readOnly="true"/>
						<fr:slot name="person.user.username" key="label.login.username" bundle="MYORG_RESOURCES" readOnly="true"/>
						<fr:slot name="totalEvaluationScoringSiadap2" layout="null-as-label" key="label.totalEvaluationScoring" readOnly="true">
							<fr:property name="subLayout" value=""/>
						</fr:slot>
						<fr:slot name="totalQualitativeEvaluationScoringSiadap2" layout="null-as-label" key="label.totalQualitativeEvaluationScoring" readOnly="true">
							<fr:property name="subLayout" value=""/>
						</fr:slot>
						<fr:slot name="currentProcessState" layout="null-as-label" key="label.state" readOnly="true" >
							<fr:property name="subLayout" value=""/>
						</fr:slot>
						<%-- Harmonization assessments --%>
						<logic:equal name="currentUnit" property="harmonizationFinished" value="false">
							<fr:slot name="harmonizationCurrentAssessmentForSIADAP2" layout="radio" key="label.harmonization.assessment"> 
 								<fr:property name="readOnlyIf" value="withSkippedEvalForSiadap2" />
								<fr:property name="classes" value="inline-list"/>
								<fr:property name="eachClasses" value="withQuotasSIADAP2"/>
							</fr:slot>
							<fr:slot name="harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2" layout="radio" key="label.harmonization.assessment.forExcellencyAward"> 
 								<fr:property name="readOnlyIf" value="withoutExcellencyAwardForSiadap2" />
								<fr:property name="classes" value="inline-list"/>
								<fr:property name="eachClasses" value="withQuotasSIADAP2"/>
							</fr:slot>
						</logic:equal>
						<logic:equal name="currentUnit" property="harmonizationFinished" value="true">
							<fr:slot name="harmonizationCurrentAssessmentForSIADAP2" layout="radio" readOnly="true" key="label.harmonization.assessment">
								<fr:property name="classes" value="inline-list"/>
								<fr:property name="eachClasses" value="withQuotasSIADAP2"/>
							</fr:slot>
							<fr:slot name="harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2" layout="radio" readOnly="true" key="label.harmonization.assessment.forExcellencyAward"> 
								<fr:property name="classes" value="inline-list"/>
								<fr:property name="eachClasses" value="withQuotasSIADAP2"/>
							</fr:slot>
						</logic:equal>
						<fr:slot name="careerName" readOnly="true"/>
						<%-- END Harmonization assessments --%>
					</fr:schema>
					<fr:layout name="tabular-row">
						<fr:property name="classes" value="tstyle2"/>
						<fr:property name="columnClasses" value="aleft,acenter,aleft"/>
						<fr:property name="link(create)" value="/siadapManagement.do?method=createNewSiadapProcess"/>
						<fr:property name="bundle(create)" value="MYORG_RESOURCES"/>
						<fr:property name="key(create)" value="link.create"/>
						<fr:property name="param(create)" value="person.externalId/personId"/>
						<fr:property name="order(create)" value="1"/>
						<fr:property name="visibleIf(create)" value="currentUserAbleToCreateProcess"/>
						
						<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess"/>
						<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES"/>
						<fr:property name="key(viewProcess)" value="link.view"/>
						<fr:property name="param(viewProcess)" value="siadap.process.externalId/processId"/>
						<fr:property name="order(viewProcess)" value="1"/>
						<fr:property name="visibleIf(viewProcess)" value="accessibleToCurrentUser"/>
						
						<fr:property name="link(removeAssessments)" value="<%="/siadapManagement.do?method=removeHarmonizationAssessments&unitId="+unitId + "&siadapUniverse="+module.siadap.domain.SiadapUniverse.SIADAP2%>"/>
						<fr:property name="bundle(removeAssessments)" value="SIADAP_RESOURCES"/>
						<fr:property name="key(removeAssessments)" value="link.removeHarmonizationAssessments"/>
						<fr:property name="param(removeAssessments)" value="person.externalId/personId,year/year"/>
						<fr:property name="order(removeAssessments)" value="1"/>
						<fr:property name="visibleIf(removeAssessments)" value="ableToRemoveAssessmentsForSIADAP2"/>
						
						<fr:property name="sortParameter" value="sortByQuotas"/>
		       			<fr:property name="sortUrl" value="<%= "/siadapManagement.do?method=viewUnitHarmonizationData&unitId=" + unitId + "&year=" + year.toString()%>"/>
					    <fr:property name="sortBy" value="<%= request.getParameter("sortByQuotas") == null ? "person.partyName=asc" : request.getParameter("sortByQuotas") %>"/>
						
					</fr:layout>
				</fr:edit>
		 </logic:notEmpty>
		
		<logic:notEmpty name="people-withQuotas-SIADAP3" property="siadapUniverse">
			<strong>
				<bean:message key="label.unitEmployees.withQuotasSIADAP3" bundle="SIADAP_RESOURCES"/>:
			</strong>
			
		<div class="infobox">
			<table class="tstyle2">
				<tr>
					<th><bean:message key="label.harmonization.totalHarmonizedInUniverse" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withQuotas-SIADAP3" property="numberRelevantPeopleInUniverse"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.quota.excellents" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withQuotas-SIADAP3" property="excellencyQuota"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.current.excellents.used.quota" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withQuotas-SIADAP3" property="currentEvaluationExcellents"/></td>
					<th><bean:message key="label.harmonized" bundle="SIADAP_RESOURCES" /></th>
					<td class="<%=peopleWithQuotasSIADAP3.getCurrentHarmonizedExcellentsHTMLClass()%>"><fr:view name="people-withQuotas-SIADAP3" property="currentHarmonizedExcellents"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.quota.relevant" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withQuotas-SIADAP3" property="relevantQuota"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.current.relevant.used.quota" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withQuotas-SIADAP3" property="currentEvaluationRelevants"/></td>
					<th><bean:message key="label.harmonized" bundle="SIADAP_RESOURCES" /></th>
					<td class="<%=peopleWithQuotasSIADAP3.getCurrentHarmonizedRelevantsHTMLClass()%>"><fr:view name="people-withQuotas-SIADAP3" property="currentHarmonizedRelevants"/></td>
				</tr>
			</table>
		</div>
			<p>
			<logic:equal name="people-withQuotas-SIADAP3" property="siadapUniverseWithQuotasAboveQuotaHarmonization" value="true">
			
				<bean:define id="relevantQuota" name="people-withQuotas-SIADAP3" property="relevantQuota"/>
				
				<bean:define id="currentHarmonizedRelevants" name="people-withQuotas-SIADAP3" property="currentHarmonizedRelevants"/>
				<bean:define id="currentHarmonizedExcellents" name="people-withQuotas-SIADAP3" property="currentHarmonizedExcellents"/>
				<bean:define id="excellencyQuota" name="people-withQuotas-SIADAP3" property="excellencyQuota"/>
				
				<div class="highlightBox">
					<bean:message key="warning.harmonizationUnitAboveQuotas" bundle="SIADAP_RESOURCES" arg0="<%= currentHarmonizedRelevants.toString() %>" arg1="<%= relevantQuota.toString() %>" arg2="<%= currentHarmonizedExcellents.toString() %>" arg3="<%= excellencyQuota.toString() %>"/>
				</div>
			</logic:equal>
				<fr:edit visible="false" nested="true" id="people-withQuotas-SIADAP3" name="people-withQuotas-SIADAP3"/>
				<fr:edit id="people-withQuotas-SIADAP3id" name="people-withQuotas-SIADAP3" property="siadapUniverse" nested="true">
					<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
						<fr:slot name="person.partyName" key="label.evaluated" readOnly="true" />
						<fr:slot name="person.user.username" key="label.login.username" bundle="MYORG_RESOURCES" readOnly="true" />
						<%--<fr:slot name="evaluator.name" key="label.evaluator"/>
						<fr:slot name="evaluator.person.user.username" key="label.login.username" bundle="MYORG_RESOURCES"/> --%>
						<fr:slot name="totalEvaluationScoringSiadap3" layout="null-as-label" key="label.totalEvaluationScoring" readOnly="true">
							<fr:property name="subLayout" value=""/>
						</fr:slot>
						<fr:slot name="totalQualitativeEvaluationScoringSiadap3" layout="null-as-label" key="label.totalQualitativeEvaluationScoring" readOnly="true">
							<fr:property name="subLayout" value=""/>
						</fr:slot>
						<fr:slot name="currentProcessState" layout="null-as-label" key="label.state" readOnly="true" >
							<fr:property name="subLayout" value=""/>
						</fr:slot>
						<%-- Harmonization assessments --%>
						<logic:equal name="currentUnit" property="harmonizationFinished" value="false">
 								<fr:slot name="harmonizationCurrentAssessmentForSIADAP3" layout="radio" key="label.harmonization.assessment">
 									<fr:property name="readOnlyIf" value="withSkippedEvalForSiadap3" />
									<fr:property name="classes" value="inline-list"/>
									<fr:property name="eachClasses" value="withQuotasSIADAP3"/>
								</fr:slot>
								<fr:slot name="harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3" layout="radio" key="label.harmonization.assessment.forExcellencyAward"> 
	 								<fr:property name="readOnlyIf" value="withoutExcellencyAwardForSiadap3" />
									<fr:property name="classes" value="inline-list"/>
									<fr:property name="eachClasses" value="withQuotasSIADAP3"/>
								</fr:slot>
 						</logic:equal>
						<logic:equal name="currentUnit" property="harmonizationFinished" value="true">
								<fr:slot name="harmonizationCurrentAssessmentForSIADAP3" layout="radio" key="label.harmonization.assessment" readOnly="true">
									<fr:property name="classes" value="inline-list"/>
									<fr:property name="eachClasses" value="withQuotasSIADAP3"/>
								</fr:slot>
								<fr:slot name="harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3" layout="radio" key="label.harmonization.assessment.forExcellencyAward" readOnly="true"> 
									<fr:property name="classes" value="inline-list"/>
									<fr:property name="eachClasses" value="withQuotasSIADAP3"/>
								</fr:slot>
						</logic:equal> 
						<fr:slot name="careerName" readOnly="true"/>
						<%-- END Harmonization assessments --%>
					</fr:schema>
					<fr:layout name="tabular-row">
						<fr:property name="classes" value="tstyle2"/>
						<fr:property name="columnClasses" value="aleft,acenter,aleft"/>
						<fr:property name="link(create)" value="/siadapManagement.do?method=createNewSiadapProcess"/>
						<fr:property name="bundle(create)" value="MYORG_RESOURCES"/>
						<fr:property name="key(create)" value="link.create"/>
						<fr:property name="param(create)" value="person.externalId/personId"/>
						<fr:property name="order(create)" value="1"/>
						<fr:property name="visibleIf(create)" value="currentUserAbleToCreateProcess"/>
						
						<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess"/>
						<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES"/>
						<fr:property name="key(viewProcess)" value="link.view"/>
						<fr:property name="param(viewProcess)" value="siadap.process.externalId/processId"/>
						<fr:property name="order(viewProcess)" value="1"/>
						<fr:property name="visibleIf(viewProcess)" value="accessibleToCurrentUser"/>
						
						<fr:property name="link(removeAssessment)" value="<%="/siadapManagement.do?method=removeHarmonizationAssessments&unitId="+unitId + "&siadapUniverse="+module.siadap.domain.SiadapUniverse.SIADAP3%>"/>
						<fr:property name="bundle(removeAssessment)" value="SIADAP_RESOURCES"/>
						<fr:property name="key(removeAssessment)" value="link.removeHarmonizationAssessments"/>
						<fr:property name="param(removeAssessment)" value="person.externalId/personId,year/year"/>
						<fr:property name="order(removeAssessment)" value="1"/>
						<fr:property name="visibleIf(removeAssessment)" value="ableToRemoveAssessmentsForSIADAP3"/>
						
						<fr:property name="sortParameter" value="sortByQuotas"/>
		       			<fr:property name="sortUrl" value="<%= "/siadapManagement.do?method=viewUnitHarmonizationData&unitId=" + unitId + "&year=" + year.toString()%>"/>
					    <fr:property name="sortBy" value="<%= request.getParameter("sortByQuotas") == null ? "person.partyName=asc" : request.getParameter("sortByQuotas") %>"/>
						
					</fr:layout>
				</fr:edit>
			</p>
		</logic:notEmpty>
		
		<logic:notEmpty name="people-withoutQuotas-SIADAP2" property="siadapUniverse">
			<strong>
				<bean:message key="label.unitEmployees.withoutQuotasSIADAP2" bundle="SIADAP_RESOURCES"/>:
			</strong>
		<div class="infobox">
			<table class="tstyle2">
				<tr>
					<th><bean:message key="label.harmonization.totalHarmonizedInUniverse" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withoutQuotas-SIADAP2" property="numberRelevantPeopleInUniverse"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.quota.excellents" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withoutQuotas-SIADAP2" property="excellencyQuota"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.current.excellents.used.quota" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withoutQuotas-SIADAP2" property="currentEvaluationExcellents"/></td>
					<th><bean:message key="label.harmonized" bundle="SIADAP_RESOURCES" /></th>
					<td class="<%=peopleWithoutQuotasSIADAP2.getCurrentHarmonizedExcellentsHTMLClass()%>"><fr:view name="people-withoutQuotas-SIADAP2" property="currentHarmonizedExcellents"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.quota.relevant" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withoutQuotas-SIADAP2" property="relevantQuota"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.current.relevant.used.quota" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withoutQuotas-SIADAP2" property="currentEvaluationRelevants"/></td>
					<th><bean:message key="label.harmonized" bundle="SIADAP_RESOURCES" /></th>
					<td class="<%=peopleWithoutQuotasSIADAP2.getCurrentHarmonizedRelevantsHTMLClass()%>"><fr:view name="people-withoutQuotas-SIADAP2" property="currentHarmonizedRelevants"/></td>
				</tr>
			</table>
		</div>
			<p>
			<logic:equal name="people-withoutQuotas-SIADAP2" property="siadapUniverseWithQuotasAboveQuotaHarmonization" value="true">
			
				<bean:define id="relevantQuota" name="people-withoutQuotas-SIADAP2" property="relevantQuota"/>
				
				<bean:define id="currentHarmonizedRelevants" name="people-withoutQuotas-SIADAP2" property="currentHarmonizedRelevants"/>
				<bean:define id="currentHarmonizedExcellents" name="people-withoutQuotas-SIADAP2" property="currentHarmonizedExcellents"/>
				<bean:define id="excellencyQuota" name="people-withoutQuotas-SIADAP2" property="excellencyQuota"/>
				
				<div class="highlightBox">
					<bean:message key="warning.harmonizationUnitAboveQuotas" bundle="SIADAP_RESOURCES" arg0="<%= currentHarmonizedRelevants.toString() %>" arg1="<%= relevantQuota.toString() %>" arg2="<%= currentHarmonizedExcellents.toString() %>" arg3="<%= excellencyQuota.toString() %>"/>
				</div>
			</logic:equal>
			
				<fr:edit visible="false" nested="true" id="people-withoutQuotas-SIADAP2" name="people-withoutQuotas-SIADAP2" />
				<fr:edit id="people-withoutQuotas-SIADAP2id" name="people-withoutQuotas-SIADAP2" property="siadapUniverse" nested="true">
					<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
						<fr:slot name="person.partyName" key="label.evaluated" readOnly="true" />
						<fr:slot name="person.user.username" key="label.login.username" bundle="MYORG_RESOURCES" readOnly="true"/>
						<%--<fr:slot name="evaluator.name" key="label.evaluator"/>
						<fr:slot name="evaluator.person.user.username" key="label.login.username" bundle="MYORG_RESOURCES"/>  --%>
						<fr:slot name="totalEvaluationScoringSiadap2" layout="null-as-label" key="label.totalEvaluationScoring" readOnly="true">
							<fr:property name="subLayout" value=""/>
						</fr:slot>
						<fr:slot name="totalQualitativeEvaluationScoringSiadap2" layout="null-as-label" key="label.totalQualitativeEvaluationScoring" readOnly="true">
							<fr:property name="subLayout" value=""/>
						</fr:slot>
						<fr:slot name="currentProcessState" layout="null-as-label" key="label.state" readOnly="true" >
							<fr:property name="subLayout" value=""/>
						</fr:slot>
						<%-- Harmonization assessments --%>
						<logic:equal name="currentUnit" property="harmonizationFinished" value="false">
							<fr:slot name="harmonizationCurrentAssessmentForSIADAP2" layout="radio" key="label.harmonization.assessment">
 								<fr:property name="readOnlyIf" value="withSkippedEvalForSiadap2" />
								<fr:property name="classes" value="inline-list"/>
								<fr:property name="eachClasses" value="withoutQuotasSIADAP2"/>
							</fr:slot>
							<fr:slot name="harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2" layout="radio" key="label.harmonization.assessment.forExcellencyAward"> 
 								<fr:property name="readOnlyIf" value="withoutExcellencyAwardForSiadap2" />
								<fr:property name="classes" value="inline-list"/>
								<fr:property name="eachClasses" value="withQuotasSIADAP2"/>
							</fr:slot>
						</logic:equal>
						<logic:equal name="currentUnit" property="harmonizationFinished" value="true">
							<fr:slot name="harmonizationCurrentAssessmentForSIADAP2" layout="radio" key="label.harmonization.assessment" readOnly="true">
								<fr:property name="classes" value="inline-list"/>
								<fr:property name="eachClasses" value="withoutQuotasSIADAP2"/>
							</fr:slot>
							<fr:slot name="harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2" layout="radio" readOnly="true" key="label.harmonization.assessment.forExcellencyAward"> 
								<fr:property name="classes" value="inline-list"/>
								<fr:property name="eachClasses" value="withQuotasSIADAP2"/>
							</fr:slot>
						</logic:equal>
						<fr:slot name="careerName" readOnly="true"/>
						<%-- END Harmonization assessments --%>
					</fr:schema>
					<fr:layout name="tabular-row">
						<fr:property name="classes" value="tstyle2"/>
						<fr:property name="columnClasses" value="aleft,acenter,aleft"/>
						<fr:property name="link(create)" value="/siadapManagement.do?method=createNewSiadapProcess"/>
						<fr:property name="bundle(create)" value="MYORG_RESOURCES"/>
						<fr:property name="key(create)" value="link.create"/>
						<fr:property name="param(create)" value="person.externalId/personId"/>
						<fr:property name="order(create)" value="1"/>
						<fr:property name="visibleIf(create)" value="currentUserAbleToCreateProcess"/>
						
						<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess"/>
						<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES"/>
						<fr:property name="key(viewProcess)" value="link.view"/>
						<fr:property name="param(viewProcess)" value="siadap.process.externalId/processId"/>
						<fr:property name="order(viewProcess)" value="1"/>
						<fr:property name="visibleIf(viewProcess)" value="accessibleToCurrentUser"/>
						
						<fr:property name="link(removeAssessment)" value="<%="/siadapManagement.do?method=removeHarmonizationAssessments&unitId="+unitId + "&siadapUniverse="+module.siadap.domain.SiadapUniverse.SIADAP2%>"/>
						<fr:property name="bundle(removeAssessment)" value="SIADAP_RESOURCES"/>
						<fr:property name="key(removeAssessment)" value="link.removeHarmonizationAssessments"/>
						<fr:property name="param(removeAssessment)" value="person.externalId/personId,year/year"/>
						<fr:property name="order(removeAssessment)" value="1"/>
						<fr:property name="visibleIf(removeAssessment)" value="ableToRemoveAssessmentsForSIADAP2"/>
						
						<fr:property name="sortParameter" value="sortByQuotas"/>
		       			<fr:property name="sortUrl" value="<%= "/siadapManagement.do?method=viewUnitHarmonizationData&unitId=" + unitId + "&year=" + year.toString()%>"/>
					    <fr:property name="sortBy" value="<%= request.getParameter("sortByQuotas") == null ? "person.partyName=asc" : request.getParameter("sortByQuotas") %>"/>
						
					</fr:layout>
				</fr:edit>
			</p>
		</logic:notEmpty>
		
		<logic:notEmpty name="people-withoutQuotas-SIADAP3" property="siadapUniverse">
			<strong>
				<bean:message key="label.unitEmployees.withoutQuotasSIADAP3" bundle="SIADAP_RESOURCES"/>:
			</strong>
		<div class="infobox">
			<table class="tstyle2">
				<tr>
					<th><bean:message key="label.harmonization.totalHarmonizedInUniverse" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withoutQuotas-SIADAP3" property="numberRelevantPeopleInUniverse"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.quota.excellents" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withoutQuotas-SIADAP3" property="excellencyQuota"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.current.excellents.used.quota" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withoutQuotas-SIADAP3" property="currentEvaluationExcellents"/></td>
					<th><bean:message key="label.harmonized" bundle="SIADAP_RESOURCES" /></th>
					<td class="<%=peopleWithoutQuotasSIADAP3.getCurrentHarmonizedExcellentsHTMLClass()%>"><fr:view name="people-withoutQuotas-SIADAP3" property="currentHarmonizedExcellents"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.quota.relevant" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withoutQuotas-SIADAP3" property="relevantQuota"/></td>
				</tr>
				<tr>
					<th><bean:message key="label.harmonization.current.relevant.used.quota" bundle="SIADAP_RESOURCES" />
					<td><fr:view name="people-withoutQuotas-SIADAP3" property="currentEvaluationRelevants"/></td>
					<th><bean:message key="label.harmonized" bundle="SIADAP_RESOURCES" /></th>
					<td class="<%=peopleWithoutQuotasSIADAP3.getCurrentHarmonizedRelevantsHTMLClass()%>"><fr:view name="people-withoutQuotas-SIADAP3" property="currentHarmonizedRelevants"/></td>
				</tr>
			</table>
		</div>	
			<div>
			<logic:equal name="people-withoutQuotas-SIADAP3" property="siadapUniverseWithQuotasAboveQuotaHarmonization" value="true">
				
				<bean:define id="relevantQuota" name="people-withoutQuotas-SIADAP3" property="relevantQuota"/>
				<bean:define id="currentHarmonizedRelevants" name="people-withoutQuotas-SIADAP3" property="currentHarmonizedRelevants"/>
				<bean:define id="currentHarmonizedExcellents" name="people-withoutQuotas-SIADAP3" property="currentHarmonizedExcellents"/>
				<bean:define id="excellencyQuota" name="people-withoutQuotas-SIADAP3" property="excellencyQuota"/>
				
				<div class="highlightBox">
					<bean:message key="warning.harmonizationUnitAboveQuotas" bundle="SIADAP_RESOURCES" arg0="<%= currentHarmonizedRelevants.toString() %>" arg1="<%= relevantQuota.toString() %>" arg2="<%= currentHarmonizedExcellents.toString() %>" arg3="<%= excellencyQuota.toString() %>"/>
				</div>
			</logic:equal>
				<fr:edit visible="false" nested="true" id="people-withoutQuotas-SIADAP3" name="people-withoutQuotas-SIADAP3"/>
				<fr:edit id="people-withoutQuotas-SIADAP3id" name="people-withoutQuotas-SIADAP3" property="siadapUniverse" nested="true">
					<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
						<fr:slot name="person.partyName" key="label.evaluated" readOnly="true" />
						<fr:slot name="person.user.username" key="label.login.username" bundle="MYORG_RESOURCES" readOnly="true"/>
						<%-- <fr:slot name="evaluator.name" key="label.evaluator"/>
						<fr:slot name="evaluator.person.user.username" key="label.login.username" bundle="MYORG_RESOURCES"/> --%>
						<fr:slot name="totalEvaluationScoringSiadap3" layout="null-as-label" key="label.totalEvaluationScoring" readOnly="true" >
							<fr:property name="subLayout" value=""/>
						</fr:slot>
						<fr:slot name="totalQualitativeEvaluationScoringSiadap3" layout="null-as-label" key="label.totalQualitativeEvaluationScoring" readOnly="true" >
							<fr:property name="subLayout" value=""/>
						</fr:slot>
						<fr:slot name="currentProcessState" layout="null-as-label" key="label.state" readOnly="true" >
							<fr:property name="subLayout" value=""/>
						</fr:slot>
						<%-- Harmonization assessments --%>
						<logic:equal name="currentUnit" property="harmonizationFinished" value="false">
							<fr:slot name="harmonizationCurrentAssessmentForSIADAP3" layout="radio" key="label.harmonization.assessment">
 								<fr:property name="readOnlyIf" value="withSkippedEvalForSiadap3" />
								<fr:property name="classes" value="inline-list"/>
								<fr:property name="eachClasses" value="withoutQuotasSIADAP3"/>
							</fr:slot>
							<fr:slot name="harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3" layout="radio" key="label.harmonization.assessment.forExcellencyAward"> 
 								<fr:property name="readOnlyIf" value="withoutExcellencyAwardForSiadap3" />
								<fr:property name="classes" value="inline-list"/>
								<fr:property name="eachClasses" value="withQuotasSIADAP3"/>
							</fr:slot>
						</logic:equal>
						<logic:equal name="currentUnit" property="harmonizationFinished" value="true">
								<fr:slot name="harmonizationCurrentAssessmentForSIADAP3" layout="radio" key="label.harmonization.assessment" readOnly="true">
									<fr:property name="classes" value="inline-list"/>
									<fr:property name="eachClasses" value="withoutQuotasSIADAP3"/>
								</fr:slot>
								<fr:slot name="harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3" layout="radio" readOnly="true" key="label.harmonization.assessment.forExcellencyAward"> 
									<fr:property name="classes" value="inline-list"/>
									<fr:property name="eachClasses" value="withQuotasSIADAP3"/>
							</fr:slot>
						</logic:equal>
						<fr:slot name="careerName" readOnly="true"/>
						<%-- END Harmonization assessments --%>
					</fr:schema>
					<fr:layout name="tabular-row">
						<fr:property name="classes" value="tstyle2"/>
						<fr:property name="columnClasses" value="aleft,acenter,aleft"/>
						<fr:property name="link(create)" value="/siadapManagement.do?method=createNewSiadapProcess"/>
						<fr:property name="bundle(create)" value="MYORG_RESOURCES"/>
						<fr:property name="key(create)" value="link.create"/>
						<fr:property name="param(create)" value="person.externalId/personId"/>
						<fr:property name="order(create)" value="1"/>
						<fr:property name="visibleIf(create)" value="currentUserAbleToCreateProcess"/>
						
						<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess"/>
						<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES"/>
						<fr:property name="key(viewProcess)" value="link.view"/>
						<fr:property name="param(viewProcess)" value="siadap.process.externalId/processId"/>
						<fr:property name="order(viewProcess)" value="1"/>
						<fr:property name="visibleIf(viewProcess)" value="accessibleToCurrentUser"/>
						
						<fr:property name="link(removeAssessment)" value="<%="/siadapManagement.do?method=removeHarmonizationAssessments&unitId="+unitId + "&siadapUniverse="+module.siadap.domain.SiadapUniverse.SIADAP3%>"/>
						<fr:property name="bundle(removeAssessment)" value="SIADAP_RESOURCES"/>
						<fr:property name="key(removeAssessment)" value="link.removeHarmonizationAssessments"/>
						<fr:property name="param(removeAssessment)" value="person.externalId/personId,year/year"/>
						<fr:property name="order(removeAssessment)" value="1"/>
						<fr:property name="visibleIf(removeAssessment)" value="ableToRemoveAssessmentsForSIADAP3"/>
						
						<fr:property name="sortParameter" value="sortByQuotas"/>
		       			<fr:property name="sortUrl" value="<%= "/siadapManagement.do?method=viewUnitHarmonizationData&unitId=" + unitId + "&year=" + year.toString()%>"/>
					    <fr:property name="sortBy" value="<%= request.getParameter("sortByQuotas") == null ? "person.partyName=asc" : request.getParameter("sortByQuotas") %>"/>
						
					</fr:layout>
				</fr:edit>
			</div>
		</logic:notEmpty>
		
		
		<logic:equal name="currentUnit" property="harmonizationActive" value="true">
			<html:submit styleClass="inputbutton">
				<bean:message key="label.save" bundle="SIADAP_RESOURCES" />
			</html:submit>
		</logic:equal>
	</fr:form>
</logic:equal>
		<%-- Old code
		<logic:notEmpty name="people-withQuotas">
			<strong>
				<bean:message key="label.unitEmployees.withQuotas" bundle="SIADAP_RESOURCES"/>:
			</strong>
			
			<p>
				<fr:view name="people-withQuotas">
					<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
						<fr:slot name="person.partyName" key="label.evaluated"/>
						<fr:slot name="person.user.username" key="label.login.username" bundle="MYORG_RESOURCES"/>
						<fr:slot name="evaluator.name" key="label.evaluator"/>
						<fr:slot name="evaluator.person.user.username" key="label.login.username" bundle="MYORG_RESOURCES"/>
						<fr:slot name="totalEvaluationScoring" layout="null-as-label" key="label.totalEvaluationScoring">
							<fr:property name="subLayout" value=""/>
						</fr:slot>
					</fr:schema>
					<fr:layout name="tabular-sortable">
						<fr:property name="classes" value="tstyle2"/>
						<fr:property name="columnClasses" value="aleft,aleft,,"/>
						<fr:property name="link(create)" value="/siadapManagement.do?method=createNewSiadapProcess"/>
						<fr:property name="bundle(create)" value="MYORG_RESOURCES"/>
						<fr:property name="key(create)" value="link.create"/>
						<fr:property name="param(create)" value="person.externalId/personId"/>
						<fr:property name="order(create)" value="1"/>
						<fr:property name="visibleIf(create)" value="currentUserAbleToCreateProcess"/>
						
						<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess"/>
						<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES"/>
						<fr:property name="key(viewProcess)" value="link.view"/>
						<fr:property name="param(viewProcess)" value="siadap.process.externalId/processId"/>
						<fr:property name="order(viewProcess)" value="1"/>
						<fr:property name="visibleIf(viewProcess)" value="accessibleToCurrentUser"/>
						
						<fr:property name="sortParameter" value="sortByQuotas"/>
		       			<fr:property name="sortUrl" value="<%= "/siadapManagement.do?method=viewUnitHarmonizationData&unitId=" + unitId + "&year=" + year.toString()%>"/>
					    <fr:property name="sortBy" value="<%= request.getParameter("sortByQuotas") == null ? "person.partyName=asc" : request.getParameter("sortByQuotas") %>"/>
						
					</fr:layout>
				</fr:view>
			</p>
		</logic:notEmpty>
		
		<logic:notEmpty name="people-withoutQuotas">
			<strong>
				<bean:message key="label.unitEmployees.withoutQuotas" bundle="SIADAP_RESOURCES"/>:
			</strong>
			
			<p>
				<fr:view name="people-withoutQuotas">
					<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
						<fr:slot name="person.partyName" key="label.evaluated"/>
						<fr:slot name="person.user.username" key="label.login.username" bundle="MYORG_RESOURCES"/>
						<fr:slot name="evaluator.name" key="label.evaluator"/>
						<fr:slot name="evaluator.person.user.username" key="label.login.username" bundle="MYORG_RESOURCES"/>
						<fr:slot name="totalEvaluationScoring" layout="null-as-label" key="label.totalEvaluationScoring">
							<fr:property name="subLayout" value=""/>
						</fr:slot>
					</fr:schema>
					<fr:layout name="tabular-sortable">
						<fr:property name="classes" value="tstyle2"/>
						<fr:property name="columnClasses" value="aleft,aleft,,"/>
						<fr:property name="link(create)" value="/siadapManagement.do?method=createNewSiadapProcess"/>
						<fr:property name="bundle(create)" value="MYORG_RESOURCES"/>
						<fr:property name="key(create)" value="link.create"/>
						<fr:property name="param(create)" value="person.externalId/personId"/>
						<fr:property name="order(create)" value="1"/>
						<fr:property name="visibleIf(create)" value="currentUserAbleToCreateProcess"/>
						
						<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess"/>
						<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES"/>
						<fr:property name="key(viewProcess)" value="link.view"/>
						<fr:property name="param(viewProcess)" value="siadap.process.externalId/processId"/>
						<fr:property name="order(viewProcess)" value="1"/>
						<fr:property name="visibleIf(viewProcess)" value="accessibleToCurrentUser"/>
						
						<fr:property name="sortParameter" value="sortByNoQuotas"/>
		       			<fr:property name="sortUrl" value="<%= "/siadapManagement.do?method=viewUnitHarmonizationData&unitId=" + unitId + "&year=" + year.toString()%>"/>
					    <fr:property name="sortBy" value="<%= request.getParameter("sortByNoQuotas") == null ? "person.partyName=asc" : request.getParameter("sortByNoQuotas") %>"/>
					</fr:layout>
				</fr:view>
			</p>
		</logic:notEmpty>
		
		<bean:define id="highQuotaSuggestions" name="currentUnit" property="orderedExcedingQuotaProposalSuggestionsForHighEvaluation"/>
		
		<logic:notEmpty name="highQuotaSuggestions">
			<strong> <bean:message key="label.listSuggestionForExcedingQuota.high" bundle="SIADAP_RESOURCES"/>: </strong>
			
			<fr:view name="highQuotaSuggestions">
				<fr:schema type="module.siadap.domain.ExcedingQuotaProposal" bundle="SIADAP_RESOURCES">
					<fr:slot name="proposalOrder"/>
					<fr:slot name="suggestion.presentationName" key="label.suggestion"/>
				</fr:schema>
				<fr:layout name="tabular">
					<fr:property name="classes" value="tstyle2"/>
					<fr:property name="link(delete)" value='<%= "/siadapManagement.do?method=removeExcedingQuotaSuggestion&unitId=" + unitId.toString() %>' />
					<fr:property name="bundle(delete)" value="MYORG_RESOURCES"/>
					<fr:property name="key(delete)" value="link.delete"/>
					<fr:property name="param(delete)" value="externalId/proposalId"/>
					<fr:property name="order(delete)" value="1"/>
				</fr:layout>
			</fr:view>
		</logic:notEmpty>
		
		<bean:define id="excellencyQuotaSuggestions" name="currentUnit" property="orderedExcedingQuotaProposalSuggestionsForExcellencyAward"/>
		
		<logic:notEmpty name="excellencyQuotaSuggestions">
		
			<strong> <bean:message key="label.listSuggestionForExcedingQuota.excellency" bundle="SIADAP_RESOURCES"/>: </strong>
			
			<fr:view name="excellencyQuotaSuggestions">
				<fr:schema type="module.siadap.domain.ExcedingQuotaProposal" bundle="SIADAP_RESOURCES">
					<fr:slot name="proposalOrder"/>
					<fr:slot name="suggestion.presentationName" key="label.suggestion"/>
				</fr:schema>
				<fr:layout name="tabular">
					<fr:property name="classes" value="tstyle2"/>
					<fr:property name="link(delete)" value='<%= "/siadapManagement.do?method=removeExcedingQuotaSuggestion&unitId=" + unitId.toString() %>' />
					<fr:property name="bundle(delete)" value="MYORG_RESOURCES"/>
					<fr:property name="key(delete)" value="link.delete"/>
					<fr:property name="param(delete)" value="externalId/proposalId"/>
					<fr:property name="order(delete)" value="1"/>
				</fr:layout>
			</fr:view>
		
		</logic:notEmpty>
		
		--%>
		<logic:notEmpty name="subUnits">
			<bean:define id="currentUnitId" name="currentUnit" property="unit.externalId"/>
			
			<strong>
				<bean:message key="label.subUnits" bundle="SIADAP_RESOURCES"/>:
			</strong>
			
			<p>
				<fr:view name="subUnits">
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
						<fr:property name="link(view)" value="<%="/siadapManagement.do?method=viewUnitHarmonizationData&year=" + year.toString() %>"/>
						<fr:property name="bundle(view)" value="MYORG_RESOURCES"/>
						<fr:property name="key(view)" value="link.view"/>
						<fr:property name="param(view)" value="unit.externalId/unitId"/>
						<fr:property name="order(view)" value="1"/>
					</fr:layout>
				</fr:view>
			</p>
		</logic:notEmpty>

<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/19" />	
</jsp:include>
