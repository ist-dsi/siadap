<%@page import="module.siadap.domain.SiadapProcess"%>
<%@page import="module.siadap.activities.CreateObjectiveEvaluationActivityInformation"%>
<%@page import="module.siadap.activities.EditObjectiveEvaluationActivityInformation"%>
<%@page import="module.workflow.activities.ActivityInformation"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>

<bean:define id="processId" name="process" property="externalId"
	type="java.lang.String" />
<bean:define id="name" name="information" property="activityName" />

<div class="dinline forminline">
<% ActivityInformation ai = (ActivityInformation) request.getAttribute("information"); 
		SiadapProcess siadapProcess = (SiadapProcess) request.getAttribute("process");
		boolean shouldTypeBeEditable =  siadapProcess.isNotSubmittedForConfirmation() ; //(ai instanceof EditObjectiveEvaluationActivityInformation) ? false : true;
		request.setAttribute("shouldTypeBeEditable", shouldTypeBeEditable);
%>

<fr:form id="form" action='<%="/workflowProcessManagement.do?method=process&processId=" + processId + "&activity=" + name%>'>

	<html:hidden property="removeIndex" value=""/>
	
	<fr:edit id="activityBean" name="information" visible="false" />

	<fr:edit id="information" name="information">
		<fr:schema type="module.siadap.activities.CreateObjectiveEvaluationActivityInformation" bundle="SIADAP_RESOURCES">
			<fr:slot name="objective" layout="longText"
					required="true">
			<fr:property name="rows" value="3" />
			<fr:property name="columns" value="50" />
		</fr:slot>
		<logic:present name="information" property="employJustification" >
			<logic:equal name="information" property="employJustification" value="true">
				<fr:slot name="justification" layout="longText"
				required="true">
					<fr:property name="rows" value="3" />
					<fr:property name="columns" value="50" />
				</fr:slot>
			</logic:equal>
		</logic:present>
		
		<logic:equal name="shouldTypeBeEditable" value="true">
			<fr:slot name="type" required="true"/>	
		</logic:equal>
		</fr:schema>
		<fr:layout name="tabular">
			<fr:property name="classes" value="form"/>
			<fr:property name="columnClasses" value=",,tderror"/>
			<fr:property name="requiredMarkShown" value="true"/>
		</fr:layout>
		<fr:destination name="invalid" path='<%="/workflowProcessManagement.do?method=process&processId=" + processId + "&activity=" + name%>' />
	</fr:edit>
	
	<logic:notEqual name="shouldTypeBeEditable" value="true">
		<fr:view name="information">
			<fr:schema type="module.siadap.activities.CreateObjectiveEvaluationActivityInformation" bundle="SIADAP_RESOURCES">
				<fr:slot name="type"/>
			</fr:schema>
		</fr:view>
	</logic:notEqual>
	

	 <logic:iterate id="indicator" name="information" property="indicators" indexId="counter">
		<table>
		<tr><td><strong><bean:message bundle="SIADAP_RESOURCES" key="label.indicator"/> <%=counter+1%></strong> 
			<logic:greaterThan name="counter" value="0">
				<p style="text-align:right;"><a id='<%= "remove-" + counter %>' href="#"><bean:message  bundle="SIADAP_RESOURCES" key="activity.CreateOrEditObjectiveEvaluation.indicator.remove"/></a></p>
	 			<input id='<%= "remove-" + counter %>' type="hidden" value="-"/>
	 		</logic:greaterThan>
	 		</td></tr>
		<tr>
		<td>
	 	<fr:edit id='<%= "indicator" + counter %>' name="indicator" >
	 		<fr:schema type="module.siadap.activities.CreateObjectiveEvaluationActivityInformation$ObjectiveIndicator" bundle="SIADAP_RESOURCES">
				<fr:slot name="measurementIndicator" layout="longText"
					required="true">
					<fr:property name="rows" value="3" />
					<fr:property name="columns" value="50" />
				</fr:slot>
				<fr:slot name="superationCriteria" layout="longText"
					required="true">
					<fr:property name="rows" value="3" />
					<fr:property name="columns" value="50" />
				</fr:slot>
				<fr:slot name="ponderationFactor" key="label.ponderationFactor.inPercentage"
					required="true" help="activity.CreateOrEditObjectiveEvaluation.ponderationFactor.help">
					<fr:property name="maxLength" value="3"/>
					<fr:property name="size" value="3"/>
					<fr:validator name="pt.ist.fenixWebFramework.rendererExtensions.validators.NumberRangeValidator">
						<fr:property name="lowerBound" value="1"/>
					</fr:validator>
				</fr:slot>
			</fr:schema>
	 	<fr:layout name="tabular">
	 		<fr:property name="classes" value="form"/>
			<fr:property name="columnClasses" value=",,tderror"/>
			<fr:property name="requiredMarkShown" value="true"/>
	 	</fr:layout>
		<fr:destination name="invalid" path='<%="/workflowProcessManagement.do?method=process&processId=" + processId + "&activity=" + name%>' />
	 	</fr:edit>
	 	</td>
	 	</tr>
	 	</table>
	 </logic:iterate>
	 <%--	Style for the hovering tooltip:  --%>
	 <style>
		div.tooltip div.tooltipText {
		top: 20px;
		left: -219px;
		}
	</style>
	 <%--	hovering tooltip stuff:  --%>
	 <div style="text-align: right;">
	   <div onmouseover="document.getElementById('adicionarDiv').className='tooltip tooltipOpen';" onmouseout="document.getElementById('adicionarDiv').className='tooltip tooltipClosed';" id="adicionarDiv" class="tooltip tooltipClosed">
			<a id='addNewIndicator' href="#"><bean:message bundle="SIADAP_RESOURCES" key="activity.CreateOrEditObjectiveEvaluation.indicator.add"/></a>
            <div class="tooltipText">
            	<bean:message bundle="SIADAP_RESOURCES" key="activity.CreateOrEditObjectiveEvaluation.indicator.add.explanation"/>
            </div>
            <script type="text/javascript">document.getElementById('adicionarDiv').className='tooltip tooltipClosed';</script>
        </div>
     </div>
	<input id="addNewIndicator" type="hidden" value="+"/>

	<html:submit styleClass="inputbutton"><bean:message key="button.save" bundle="SIADAP_RESOURCES"/></html:submit>
</fr:form>
<fr:form id="form" action='<%="/workflowProcessManagement.do?method=viewProcess&processId=" + processId %>'>
	<html:submit styleClass="inputbutton"><bean:message key="button.back" bundle="SIADAP_RESOURCES"/></html:submit>
</fr:form>

</div>
	
<a id="addIndicator" style="display: none;" href="<%= request.getContextPath() + "/siadapProcessController.do?skipValidation=true&method=addNewIndicator&processId=" + processId + "#form" %>"></a>
<a id="removeIndicator" style="display: none;" href="<%= request.getContextPath() + "/siadapProcessController.do?skipValidation=true&method=removeIndicator&processId=" + processId + "#form" %>"></a>
	
	<script type="text/javascript">

		$("a[id^=remove-]").click(function() {
			var form = $("#form");
			var index = $(this).attr('id').substring(7);
			form.attr('action',$("#removeIndicator").attr('href'));
			$("#form > input[name=removeIndex]").attr('value',index);
			form.submit();
		});

		$("#addNewIndicator").click(function(){
			var form = $("#form");
			form.attr('action',$("#addIndicator").attr('href'));
			form.submit();
		});
	</script>
<jsp:include page="/module/siadap/tracFeedBackSnip.jsp">	
   <jsp:param name="href" value="https://fenix-ashes.ist.utl.pt/trac/siadap/report/11" />	
</jsp:include>
