<%@page import="module.siadap.domain.scoring.SiadapGlobalEvaluation"%>
<%@page import="module.siadap.domain.util.SiadapProcessCounter.NumberAndGradeCounter"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.joda.time.LocalDate"%>
<%@page import="module.organization.domain.Accountability"%>
<%@page import="module.siadap.domain.wrappers.PersonSiadapWrapper"%>
<%@page import="module.organization.domain.Unit"%>
<%@page import="module.siadap.domain.Siadap"%>
<%@page import="module.organization.domain.Person"%>
<%@page import="module.siadap.domain.SiadapYearConfiguration"%>
<%@page import="module.siadap.domain.SiadapProcessStateEnum"%>
<%@page import="module.siadap.domain.util.SiadapProcessCounter"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/chart" prefix="chart" %>

<bean:define id="unit" name="unit" type="module.organization.domain.Unit" />

<h2>
	<fr:view name="unit" property="presentationName"/> (<fr:view name="configuration" property="year"/>)
</h2>

<%
	final SiadapYearConfiguration configuration = (SiadapYearConfiguration) request.getAttribute("configuration");
	final SiadapProcessCounter counter = new SiadapProcessCounter(unit, true, configuration);
	HashMap<String, NumberAndGradeCounter> counterHashMap;
	request.setAttribute("counter", counter);
%>

<%
for (Boolean booleanKey : counter.getCountsByQuotaAndCategories().keySet())
{
  if (booleanKey == true)
  {
      %>
<h3 class="mtop2 mbottom05">Universo que <i>contabiliza</i> para as quotas</h3>
      <% 
  } else {
      %>
<h3 class="mtop1 mbottom05">Universo que <i>não contabiliza</i> para as quotas</h3>
      <% 
  }
  	counterHashMap = counter.getCountsByQuotaAndCategories().get(booleanKey);
	 for (String categoryString : counterHashMap.keySet())
	 {
%>
<p class="mtop2"><%=categoryString%>:</p>
<table class="tstyle2 thlight tdleft thleft"  style="width: 100%; ">
	<tr>
<%
	NumberAndGradeCounter numberAndGradeCounter = counterHashMap.get(categoryString);
if (numberAndGradeCounter.hasSubCategories()) {
	for (int i = 0 ; i < numberAndGradeCounter.getNumberSubCategories(); i++) {
	    final SiadapProcessStateEnum state = SiadapProcessStateEnum.values()[i];
%>
		<th>
				<%= state.getLocalizedName() %>
		</th>
<%
	}
}
else {
    %>
    <%-- <th>
				Total
		</th>
		--%>
<% 
}
%>
	</tr>
	<tr>
<%
if (numberAndGradeCounter.hasSubCategories()) {
	for (int i = 0 ; i < numberAndGradeCounter.getNumberSubCategories(); i++) {
	    final int count = numberAndGradeCounter.getNumberOfPeopleForSubcategory(i);
	    final SiadapProcessStateEnum state = SiadapProcessStateEnum.values()[i];
%>
		<td>
					<%= count %>
		</td>
<%
	}
}
else {
    %>
    <td><%= numberAndGradeCounter.getTotalNumberOfCategoryPeople() %> </td>
    <%
}
%>
	</tr>
</table>

<!-- Grade section -->
<h4>Notas:</h4>
<ul>
<%
 	for (SiadapGlobalEvaluation grade : SiadapGlobalEvaluation.values()) {
 	    if (grade.equals(SiadapGlobalEvaluation.HIGH)) {
 		
 	    %>
 	    <li><%=grade.getLocalizedName()%>: <%=numberAndGradeCounter.getGradeCounter().count(grade)%> + <%= numberAndGradeCounter.getGradeCounter().count(SiadapGlobalEvaluation.EXCELLENCY)%> </li>
 	    <%
 	    }
 	    else {
 		%>
 	    <li><%=grade.getLocalizedName()%>: <%=numberAndGradeCounter.getGradeCounter().count(grade)%></li>
 		<%
 	    }
 	    
 	}
%>
</ul>

<%
  }
}
	 if (!counter.getCurricularPonderationSiadaps().isEmpty()) {
	 %>
	 <h3>Ponderações curriculares</h3>
	 <fr:view name="counter" property="curricularPonderationSiadaps">
	 	<fr:schema type="module.siadap.domain.Siadap" bundle="SIADAP_RESOURCES">
	 		<fr:slot name="evaluated.presentationName" key="label.name"/>
	 	</fr:schema>
	 	<fr:layout name="tabular-row">
	 		<fr:property name="classes" value="tstyle2"/>
			<fr:property name="columnClasses" value="aleft,acenter,aleft"/>
			<fr:property name="link(viewProcess)" value="/workflowProcessManagement.do?method=viewProcess"/>
			<fr:property name="bundle(viewProcess)" value="MYORG_RESOURCES"/>
			<fr:property name="key(viewProcess)" value="link.view"/>
			<fr:property name="param(viewProcess)" value="process.externalId/processId"/>
			<fr:property name="order(viewProcess)" value="1"/>
	 	</fr:layout>
	 		
	 </fr:view>
	 <%
	 }
%>
