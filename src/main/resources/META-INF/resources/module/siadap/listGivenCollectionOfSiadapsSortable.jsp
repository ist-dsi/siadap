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


<!-- NOTE, currently not working, do not use -->


<%
	SiadapYearConfiguration configuration = null;
	Collection<Siadap> siadaps = (Collection<Siadap>) request.getAttribute("siadaps");
%>
<logic:notEmpty name="siadaps">
	<fr:view name="siadaps"  >
		<fr:schema bundle="SIADAP_RESOURCES" type="module.siadap.domain.Siadap">
			<fr:slot name="evaluated.presentationName"  />
		</fr:schema>
		
		<fr:layout name="tabular-sortable">
			<fr:property name="link(view)" value="/workflowProcessManagement.do?method=viewProcess"/>
			<fr:property name="param(view)" value="process.externalId/processId"/>
		</fr:layout>
	</fr:view>

</logic:notEmpty>
<logic:empty name="siadaps">
	Sem processos para listar
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




