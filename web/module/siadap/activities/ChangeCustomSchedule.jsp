<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<bean:define id="processId" name="process" property="externalId"
	type="java.lang.String" />
<bean:define id="name" name="information" property="activityName" />

<%--<div class="dinline forminline"> --%>
<fr:form id="form" action='<%="/workflowProcessManagement.do?method=process&processId=" + processId + "&activity=" + name%>'>
	<html:hidden property="removeIndex" value=""/>
	
	<fr:edit id="activityBean" name="information" visible="false" />
	
	 <logic:iterate id="representation" name="information" property="customScheduleRepresentations" indexId="counter">
		<table>
		<tr><td><strong><bean:message bundle="SIADAP_RESOURCES" key="label.customScheduleRepresentation"/> <%=counter+1%></strong> <br/>
			<logic:greaterThan name="counter" value="0">
				<p style="text-align:right;"><a id='<%= "remove-" + counter %>' href="#"><bean:message  bundle="SIADAP_RESOURCES" key="activity.ChangeCustomSchedule.representation.remove"/></a></p>
	 			<input id='<%= "remove-" + counter %>' type="hidden" value="-"/>
	 		</logic:greaterThan>
	 		</td></tr>
		<tr>
		<td>
	 	<fr:edit id='<%= "representation" + counter %>' name="representation" >
	 		<fr:schema type="module.siadap.activities.ChangeCustomScheduleActivityInformation$CustomScheduleRepresentation" bundle="SIADAP_RESOURCES">
				<fr:slot name="typeOfSchedule" 	required="true" key="label.typeOfSchedule"/>
				<fr:slot name="newDeadlineDate" key="label.newDeadlineDate" required="true" layout="picker"/>
				<fr:slot name="justification" layout="longText"
					required="true" key="label.ChangeCustomSchedule.justification">
					<fr:property name="rows" value="1" />
					<fr:property name="columns" value="50" />
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
	 
	 <div style="text-align: right;">
			<a id='addNewIndicator' href="#"><bean:message bundle="SIADAP_RESOURCES" key="activity.ChangeCustomSchedule.schedule.representation.add"/></a>
     </div>
	 	<input id="addNewIndicator" type="hidden" value="+"/>
<%--</div> --%>
<div class="dinline forminline">
	<html:submit styleClass="inputbutton"><bean:message key="button.save" bundle="SIADAP_RESOURCES"/></html:submit>
	</fr:form>
	<fr:form id="form" action='<%="/workflowProcessManagement.do?method=viewProcess&processId=" + processId %>'>
		<html:submit styleClass="inputbutton"><bean:message key="button.back" bundle="SIADAP_RESOURCES"/></html:submit>
	</fr:form>
</div>

<a id="addIndicator" style="display: none;" href="<%= request.getContextPath() + "/siadapProcessController.do?skipValidation=true&method=addNewScheduleRepresentation&processId=" + processId + "#form" %>"></a>
<a id="removeIndicator" style="display: none;" href="<%= request.getContextPath() + "/siadapProcessController.do?skipValidation=true&method=removeScheduleRepresentation&processId=" + processId + "#form" %>"></a>

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
	 
