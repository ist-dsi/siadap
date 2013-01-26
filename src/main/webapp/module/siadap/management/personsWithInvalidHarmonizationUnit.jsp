<%@page import="java.util.Collection"%>
<%@page import="com.google.common.collect.Multiset"%>
<%@page import="java.util.Set"%>
<%@page import="module.organization.domain.Unit"%>
<%@page import="module.siadap.domain.SiadapYearConfiguration"%>
<%@page import="module.siadap.domain.util.SiadapProcessCounter"%>
<%@page import="module.siadap.domain.Siadap"%>
<%@page import="module.siadap.domain.wrappers.PersonSiadapWrapper"%>
<%@page import="module.siadap.domain.wrappers.UnitSiadapWrapper"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/chart" prefix="chart" %>


<%
	SiadapYearConfiguration configuration = null;
	Collection<Siadap> siadaps = (Collection<Siadap>) request.getAttribute("siadaps");
	Integer year = (Integer) request.getAttribute("year");
	
%>
<h1>SIADAP <bean:write name="year"/></h1>
<h2>Processos sem unidade de harmonização válida</h2>

<html:link page="<%="/siadapPersonnelManagement.do?method=start&year=" + year   %>"> Voltar a gestão de pessoas </html:link>

<logic:notEmpty name="siadaps">
<p>A listar <%=siadaps.size()%> processos</p>
<table>
	<logic:iterate id="siadap" name="siadaps">
		<tr>
			<th><html:link page="<%= "/workflowProcessManagement.do?method=viewProcess&processId=" + ((module.siadap.domain.Siadap)siadap).getProcess().getExternalId() %>">
					<bean:write name="siadap" property="evaluated.presentationName"/>
				</html:link>
			</th>
				<%
				Siadap siadapObtained = (Siadap)siadap;
				if (configuration == null)
				{
				    configuration = SiadapYearConfiguration.getSiadapYearConfiguration(siadapObtained.getYear());
				}
				PersonSiadapWrapper siadapWrapper = new PersonSiadapWrapper(siadapObtained.getEvaluated(),siadapObtained.getYear());
				Siadap defaultSiadap = siadapWrapper.getSiadap();
				%>
				<td>
					<%=siadapObtained.getState().getLocalizedName()%>
				</td>
				<td>
					<%=siadapWrapper.getUnitWhereIsHarmonized() == null ? "-" : siadapWrapper.getUnitWhereIsHarmonized() %>
				</td>
				<td>
					<% //let ascertain the probable reason
					String reason = "";
					if (siadapWrapper.getWorkingUnit() == null)
					{
						reason="o utilizador não tem unidade de trabalho definida";
					}
					else if (siadapWrapper.getUnitWhereIsHarmonized() == null) {
						reason = "o utilizador não tem nenhuma unidade de harmonização definida";
					} else if (!new UnitSiadapWrapper(siadapWrapper.getUnitWhereIsHarmonized(), year).isValidHarmonizationUnit()) {
						reason = "o utilizador está numa U.H. desactivada";
					}
					%>
					<%=reason%>
				</td>
					<td>
						<html:link page="<%="/siadapPersonnelManagement.do?method=viewPersonLinkAction&year=" + siadapObtained.getYear().toString() + "&personId=" + siadapObtained.getEvaluated().getExternalId()%>">
							Gerir pessoa na estructura SIADAP
						</html:link>
					</td>
					
		</tr>
	
	</logic:iterate>
</table>
</logic:notEmpty>
<logic:empty name="siadaps">
	Sem processos sem unidade de harmonização válida
</logic:empty>