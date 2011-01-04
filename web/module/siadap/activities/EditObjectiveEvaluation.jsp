<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<jsp:include page="CreateObjectiveEvaluation.jsp"/>
<%-- 
<bean:define id="processId" name="process" property="externalId"
	type="java.lang.String" />
<bean:define id="name" name="information" property="activityName" />

<div class="dinline forminline">

<fr:form id="form" action='<%="/workflowProcessManagement.do?method=process&processId=" + processId + "&activity=" + name%>'>

	<html:hidden property="removeIndex" value=""/>
	
	<fr:edit id="activityBean" name="information" visible="false" />

	<fr:edit name="information">
		<fr:schema type="module.siadap.activities.CreateObjectiveEvaluationActivityInformation" bundle="SIADAP_RESOURCES">
			<fr:slot name="objective" layout="longText"
					validator="pt.ist.fenixWebFramework.renderers.validators.RequiredValidator">
			<fr:property name="rows" value="3" />
			<fr:property name="columns" value="50" />
		</fr:slot>
		<logic:equal name="employJustification" value="true">
			<fr:slot name="justification" layout="longText"
			validator="pt.ist.fenixWebFramework.renderers.validators.RequiredValidator">
				<fr:property name="rows" value="3" />
				<fr:property name="columns" value="50" />
			</fr:slot>
		</logic:equal>
		</fr:schema>
		<fr:layout name="tabular">
			<fr:property name="classes" value="form"/>
			<fr:property name="columnClasses" value=",,tderror"/>
			<fr:property name="requiredMarkShown" value="true"/>
		</fr:layout>
	</fr:edit>

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
					validator="pt.ist.fenixWebFramework.renderers.validators.RequiredValidator">
					<fr:property name="rows" value="3" />
					<fr:property name="columns" value="50" />
				</fr:slot>
				<fr:slot name="superationCriteria" layout="longText"
					validator="pt.ist.fenixWebFramework.renderers.validators.RequiredValidator">
					<fr:property name="rows" value="3" />
					<fr:property name="columns" value="50" />
				</fr:slot>
				<fr:slot name="ponderationFactor" key="label.ponderationFactor.inPercentage"
					validator="pt.ist.fenixWebFramework.renderers.validators.RequiredValidator" help="activity.CreateOrEditObjectiveEvaluation.ponderationFactor.help">
					<fr:property name="size" value="3"/>
					<fr:property name="maxLength" value="3"/>
					<fr:validator name="pt.ist.fenixWebFramework.renderers.validators.NumberValidator"/>
				</fr:slot>
			</fr:schema>
	 	<fr:layout name="tabular">
	 		<fr:property name="classes" value="form"/>
			<fr:property name="columnClasses" value=",,tderror"/>
			<fr:property name="requiredMarkShown" value="true"/>
	 	</fr:layout>
	 	</fr:edit>
	 	</td>
	 	</tr>
	 	</table>
	 </logic:iterate>
	 <%--	Style for the hovering tooltip:  
	 <style>
		div.tooltip div.tooltipText {
		top: 20px;
		left: -219px;
		}
	</style>
	 <%--	hovering tooltip stuff:  
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
		
	<html:submit styleClass="inputbutton"><bean:message key="renderers.form.submit.name" bundle="RENDERER_RESOURCES"/></html:submit>
</fr:form>
<fr:form id="form" action='<%="/workflowProcessManagement.do?method=viewProcess&processId=" + processId %>'>
	<html:submit styleClass="inputbutton"><bean:message key="renderers.form.cancel.name" bundle="RENDERER_RESOURCES"/></html:submit>
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
--%>
