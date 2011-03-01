<%@page import="module.organization.domain.Unit"%>
<%@page import="module.siadap.domain.Siadap"%>
<%@page import="module.organization.domain.Person"%>
<%@page import="module.siadap.domain.SiadapYearConfiguration"%>
<%@page import="module.siadap.domain.wrappers.SiadapProcessStateEnum"%>
<%@page import="module.siadap.domain.util.SiadapProcessCounter"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>
<%@ taglib uri="/WEB-INF/chart.tld" prefix="chart" %>

<bean:define id="unit" type="module.organization.domain.Unit" name="chart" property="element"/>

<h2>
	<fr:view name="unit" property="presentationName"/>
</h2>

<div class="infobox">
	<table align="center" style="width: 100%; text-align: center;">
		<tr>
			<td align="center">
				<chart:orgChart id="party" name="chart" type="module.organization.domain.Party">
					
						<%
							if (party == request.getAttribute("unit")) {
						%>
							<div class="orgTBox orgTBoxLight">
								<strong>
									<bean:write name="party" property="partyName"/>
								</strong>
							</div>
						<%			    
							} else if (party.isUnit()) {
						%>

								<%
									final SiadapProcessCounter counter = new SiadapProcessCounter((Unit) party);
									final StringBuilder builder = new StringBuilder();
									for (int i = 0 ; i < counter.getCounts().length; i++) {
										final int count = counter.getCounts()[i];
										final SiadapProcessStateEnum state = SiadapProcessStateEnum.values()[i];
										if (i > 0) {
										    builder.append(" / ");
										}
										builder.append(count);
									}
								%>
							<div class="orgTBox orgTBoxLight" title="<%= builder.toString() %>">
								<html:link page="/siadapProcessCount.do?method=showUnit" paramId="unitId" paramName="party" paramProperty="externalId" styleClass="secondaryLink">
									<bean:write name="party" property="partyName"/>
								</html:link>
							</div>
						<%			    
							} else {
							    final SiadapYearConfiguration configuration = (SiadapYearConfiguration) request.getAttribute("configuration");
							    final Siadap siadap = configuration.getSiadapFor((Person) party);
							    final SiadapProcessStateEnum state = SiadapProcessStateEnum.getState(siadap);
						%>
							<div class="orgTBox orgTBoxLight" title="<%= state.getLocalizedName() %>">
								<bean:write name="party" property="partyName"/>
							</div>
						<%			    
							}
						%>
				</chart:orgChart>
			</td>
		</tr>
	</table>
</div>

<%
	final SiadapProcessCounter counter = new SiadapProcessCounter(unit);
	for (int i = 0 ; i < counter.getCounts().length; i++) {
	    final int count = counter.getCounts()[i];
	    final SiadapProcessStateEnum state = SiadapProcessStateEnum.values()[i];
%>
		<%= state.getLocalizedName() %>
		<%= count %>
		<br/>
		<br/>
<%
	}
%>