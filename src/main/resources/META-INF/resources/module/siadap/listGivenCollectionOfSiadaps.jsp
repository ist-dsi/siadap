<%@page import="java.util.Collection"%>
<%@page import="com.google.common.collect.Multiset"%>
<%@page import="java.util.Set"%>
<%@page import="module.organization.domain.Unit"%>
<%@page import="module.siadap.domain.SiadapYearConfiguration"%>
<%@page import="module.siadap.domain.util.SiadapProcessCounter"%>
<%@page import="module.siadap.domain.Siadap"%>
<%@page import="module.siadap.domain.wrappers.PersonSiadapWrapper"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/chart" prefix="chart" %>


<%
	SiadapYearConfiguration configuration = null;
	Collection<Siadap> siadaps = (Collection<Siadap>) request.getAttribute("siadaps");
%>
<logic:notEmpty name="siadaps">
<p>A listar <%=siadaps.size()%> processos</p>
<table>
	<logic:iterate id="siadap" name="siadaps">
		<tr>
			<th><html:link page="<%= "/workflowProcessManagement.do?method=viewProcess&processId=" + ((module.siadap.domain.Siadap)siadap).getProcess().getExternalId() %>">
					<bean:write name="siadap" property="evaluated.presentationName"/>
				</html:link>
			</th>
			<td>
				<%
				Siadap siadapObtained = (Siadap)siadap;
				if (configuration == null)
				{
				    configuration = SiadapYearConfiguration.getSiadapYearConfiguration(siadapObtained.getYear());
				}
				PersonSiadapWrapper siadapWrapper = new PersonSiadapWrapper(siadapObtained.getEvaluated(),siadapObtained.getYear());
				Siadap defaultSiadap = siadapWrapper.getSiadap();
				%>
			<logic:present role="pt.ist.bennu.core.domain.RoleType.MANAGER">
					<html:link  page="<%= "/workflowProcessManagement.do?method=viewProcess&processId=" + defaultSiadap.getProcess().getExternalId()%>" >
						Default siadap
					</html:link>
				</td>
				<td>
					<%=siadapObtained.getExternalId().equals(defaultSiadap.getExternalId()) %>
				</td>
				<td>
					<%=siadapObtained.getState().getLocalizedName()%>
				</td>
				<logic:present name="renderRemoveLink">
					<logic:equal value="true" name="renderRemoveLink">
						<td>
							<html:link page="<%="/siadapProcessCount.do?method=removeSiadap&siadapId=" + siadapObtained.getExternalId() %>">
								Remover SIADAP
							</html:link>
						</td>
					</logic:equal>
				</logic:present>
			</logic:present>
			<logic:present name="renderManageSiadapPersonLink">
				<logic:equal value="true" name="renderManageSiadapPersonLink">
					<td>
						<html:link page="<%="/siadapPersonnelManagement.do?method=viewPersonLinkAction&year=" + siadapObtained.getYear().toString() + "&personId=" + siadapObtained.getEvaluated().getExternalId()%>">
							Gerir estrutura SIADAP
						</html:link>
					</td>
					<logic:present role="pt.ist.bennu.core.domain.RoleType.MANAGER">
						<td>
							<html:link page="<%="/organization.do?method=viewParty&partyOid=" + siadapObtained.getEvaluated().getExternalId()%>">
								Gerir accs
							</html:link>
						</td>
					</logic:present>
					
				</logic:equal>
			</logic:present>
		</tr>
	
	</logic:iterate>
</table>
</logic:notEmpty>
<logic:empty name="siadaps">
	Siadaps vazio
</logic:empty>
<%

if (configuration == null)
{
  int year = Integer.valueOf(request.getParameter("year"));
  configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
  
}
	SiadapProcessCounter processCounter = new SiadapProcessCounter(configuration.getSiadapStructureTopUnit(), false, configuration);
	Set<Unit> duplicateUnits = processCounter.getDuplicateUnits();
	Multiset<Unit> originalDuplicateUnits = processCounter.getOriginalDuplicateUnits();
    request.setAttribute("duplicateUnits", duplicateUnits);
%>

<logic:present role="pt.ist.bennu.core.domain.RoleType.MANAGER">
	<p>Unidades duplicadas: <%=duplicateUnits.size() %></p>
	<logic:notEmpty name="duplicateUnits">
		<logic:iterate id="duplicateUnit" name="duplicateUnits">
			<p>
				<html:link page="<%="/organization.do?method=editUnit&unitOid=" + ((module.organization.domain.Unit)duplicateUnit).getExternalId()%>">
					<bean:write name="duplicateUnit" property="presentationName"/>
				</html:link>
				<%=" (" + originalDuplicateUnits.count(duplicateUnit) + ")" %>
			</p>
			
		</logic:iterate>
	
	</logic:notEmpty>
</logic:present>


