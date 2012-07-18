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
	<fr:view name="unit" property="presentationName"/>
</h2>

<%
	final SiadapYearConfiguration configuration = (SiadapYearConfiguration) request.getAttribute("configuration");
	final SiadapProcessCounter counter = new SiadapProcessCounter(unit, true);
	HashMap<String,int[]> counterHashMap;
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
	int counts[] = counterHashMap.get(categoryString);
if (counts.length != 1) {
	for (int i = 0 ; i < counts.length; i++) {
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
	for (int i = 0 ; i < counts.length; i++) {
	    final int count = counts[i];
	    final SiadapProcessStateEnum state = SiadapProcessStateEnum.values()[i];
%>
		<td>
					<%= count %>
		</td>
<%
	}
%>
	</tr>
</table>
<%
  }
}
%>
