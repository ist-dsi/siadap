<%@page import="org.fenixedu.bennu.core.domain.User"%>
<%@page import="module.siadap.domain.wrappers.UnitSiadapWrapper"%>
<%@page import="module.siadap.presentationTier.actions.UnitManagementInterfaceAction.Mode"%>
<%@page import="module.siadap.presentationTier.actions.UnitManagementInterfaceAction"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="module.siadap.presentationTier.renderers.providers.SiadapStateToShowInCount"%>
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

<%
	final Mode mode = (Mode) request.getAttribute("mode");
%>

<bean:define id="year" name="siadapYearWrapper" property="chosenYear" type="java.lang.Integer"/>
<%-- the mode chooser: --%>
<%-- 
<p>
	<%
	if (UnitManagementInterfaceAction.Mode.HARMONIZATION_UNIT_MODE == mode) {
	%>
		<html:link action="<%="/unitManagementInterface.do?method=showUnit&year=" + year + "&mode="+UnitManagementInterfaceAction.Mode.REGULAR_UNIT_MODE.name()%>" >
			<bean:message bundle="SIADAP_RESOURCES" key="link.unitManagementInterface.regularUnitMode"/>
		</html:link>
		| <bean:message bundle="SIADAP_RESOURCES" key="link.unitManagementInterface.harmonizationUnitMode"/>
	<% } %>
	<%
	if (UnitManagementInterfaceAction.Mode.REGULAR_UNIT_MODE == mode) {
	%>
			<bean:message bundle="SIADAP_RESOURCES" key="link.unitManagementInterface.regularUnitMode"/>
		| 
		<html:link action="<%="/unitManagementInterface.do?method=showUnit&year=" + year + "&mode="+UnitManagementInterfaceAction.Mode.HARMONIZATION_UNIT_MODE.name()%>" >
			<bean:message bundle="SIADAP_RESOURCES" key="link.unitManagementInterface.harmonizationUnitMode"/>
		</html:link>
	<% } %>
</p>
--%>

<%-- the temporary mode chooser, because the regular unit mode is disabled: --%>
<%--
<p>
			<bean:message bundle="SIADAP_RESOURCES" key="link.unitManagementInterface.regularUnitMode"/> (desactivado)
		| 
			<b><bean:message bundle="SIADAP_RESOURCES" key="link.unitManagementInterface.harmonizationUnitMode"/></b>
</p>
 --%>

<html:link action="<%="/unitManagementInterface.do?method=downloadUnitStructure&year=" + year + "&mode="+mode.name()%>" >
	Download da listagem da estructura
</html:link>

<span style="float: right;">
<html:link action="<%= "/siadapManagement.do?method=validate" %>">
	<bean:message bundle="SIADAP_RESOURCES" key="link.siadap.validationProcedure"/>
</html:link>
|
<html:link action="<%= "/siadapManagement.do?method=manageHarmonizationUnitsForMode&mode=homologationDone" %>">
	<bean:message bundle="SIADAP_RESOURCES" key="link.siadap.homologationProcedure"/>
</html:link>
</span>

<br/>
<br/>


<br/>

<%-- The year chooser: --%>
<fr:form action="<%="/unitManagementInterface.do?method=showUnit&mode=" + mode.name()%>" >
	<fr:edit id="siadapYearWrapper" name="siadapYearWrapper" nested="true">
		<fr:schema bundle="SIADAP" type="module.siadap.domain.wrappers.SiadapYearWrapper">
			<fr:slot name="chosenYearLabel" bundle="SIADAP_RESOURCES" layout="menu-select-postback" key="siadap.start.siadapYearChoice">
					<fr:property name="providerClass" value="module.siadap.presentationTier.renderers.providers.SiadapYearsFromExistingSiadapConfigurations"/>
					<%-- 
					<fr:property name="format" value="${year}" />
					--%>
					<fr:property name="nullOptionHidden" value="true"/>
					<%-- 
					<fr:property name="eachSchema" value="module.siadap.presentationTier.renderers.providers.SiadapYearConfigurationsFromExisting.year"/>
					--%>
			</fr:slot>
		</fr:schema>
	</fr:edit>
</fr:form>  

<%
	final SiadapYearConfiguration configuration = (SiadapYearConfiguration) request.getAttribute("configuration");
%>
<logic:present name="configuration">
<bean:define id="unit" type="module.organization.domain.Unit" name="chart" property="element"/>
<% UnitSiadapWrapper unitWrapper  = new UnitSiadapWrapper(unit,year);
   request.setAttribute("unitWrapper", unitWrapper);
%>

<h2>
	<fr:view name="unit" property="presentationName"/>
</h2>

<%
	final SiadapProcessCounter mainCounter = new SiadapProcessCounter(unit, false, configuration, mode);
	int siadapTotalCount = 0;
	int nrDuplicatedPersons = mainCounter.getDuplicatePersons().size();
	for (int i = 0 ; i < mainCounter.getCounts().length && i <= SiadapStateToShowInCount.getMaximumStateToShowInCount().ordinal() + 1; i++) {
	    final int count = mainCounter.getCounts()[i];
	    final SiadapProcessStateEnum state = SiadapProcessStateEnum.values()[i];
	    siadapTotalCount += count;
	}
%>
<div class="infobox">
	<table align="center" style="width: 100%; text-align: center;">
		<tr>
			<td align="center">
				<chart:orgChart id="party" name="chart" type="module.organization.domain.Party">
					
						<%
							if (party == unit) {
						%>
							<div class="orgTBox orgTBoxLight">
								<strong>
									<bean:write name="party" property="presentationName"/>
								</strong>
							</div>
						<%			    
							} else if (party.isUnit()) {
							    final SiadapProcessCounter counter = new SiadapProcessCounter((Unit) party, false, configuration, mode);
							    String styleSuffix = null;
						%>
							<bean:define id="toolTipTitle" type="java.lang.String"><bean:message key="label.process.state.counts" bundle="SIADAP_RESOURCES"/></bean:define>
							<bean:define id="toolTip" type="java.lang.String">
								<ul>
								<%
									for (int i = 0 ; i < counter.getCounts().length && i <= SiadapStateToShowInCount.getMaximumStateToShowInCount().ordinal() + 1; i++) {
										final int count = counter.getCounts()[i];
										final SiadapProcessStateEnum state = SiadapProcessStateEnum.values()[i];
								%>
										<li>
											<%= state.getLocalizedName() %>: <%= count %>
										</li>
								<%
									}
								%>
								</ul>
							</bean:define>
							<div class="orgTBox orgTBoxLight"  title="header=[ <%= toolTipTitle %> ] body=[<%= toolTip %>]">
								<html:link page="<%="/unitManagementInterface.do?method=showUnit&year=" + year +"&mode=" + mode.name() %>" paramId="unitId" paramName="party" paramProperty="externalId" styleClass="secondaryLink">
									<bean:write name="party" property="partyName.content"/>
								</html:link>
							</div>
						<%			    
							}
						%>
				</chart:orgChart>
			</td>
		</tr>
	</table>
</div>

<logic:present name="siadapsDefinitiveCount">
<%	int siadapsDefinitiveCount = (Integer) request.getAttribute("siadapsDefinitiveCount"); %>
	<logic:notEqual value="<%=String.valueOf(siadapTotalCount)%>" name="siadapsDefinitiveCount" >
		<div class="highlightBox">
			<p>Atenção, existem <%=siadapsDefinitiveCount - siadapTotalCount%> processos não listados</p>
			<html:link page="/unitManagementInterface.do?method=manageUnlistedUsers" paramId="year" paramName="year" >
				<p>Gerir pessoas não listadas</p>
			</html:link>
		</div>
	</logic:notEqual>
</logic:present>


<%
final int NR_STATES_PER_ROW = 9;
final int MAX_NR_STATES = (mainCounter.getCounts().length <= SiadapStateToShowInCount.getMaximumStateToShowInCount().ordinal() + 1) ? mainCounter.getCounts().length :  SiadapStateToShowInCount.getMaximumStateToShowInCount().ordinal() + 1;
int j =0;
%>
<table class="tstyle tcenter">

<% 
	for (int i = 0; i < MAX_NR_STATES; i=j) {
%>
	<tr>
	<%-- Let's take care of the blank spaces here
	 --%>
	<% 
	int nrBlanksToFill = 0;
		if ((i + NR_STATES_PER_ROW) > MAX_NR_STATES )
		{
		     nrBlanksToFill = ((i + NR_STATES_PER_ROW) - MAX_NR_STATES) / 2;
		}
	    %>
	    <%
	    	for (int k=0; k< nrBlanksToFill; k++) {
	    %>
	    <td></td>
	    <% } %>
<%
	for (j=i ; ((j - i) < NR_STATES_PER_ROW) && (j < MAX_NR_STATES); j++) {
	    final int count = mainCounter.getCounts()[j];
	    final SiadapProcessStateEnum state = SiadapProcessStateEnum.values()[j];
	    siadapTotalCount += count;
%>
		<th>
			<strong>
				<%= state.getLocalizedName() %>
			</strong>
		</th>
	<% } %>
	</tr>
	<tr>
		<%-- Let's take care of the blank spaces here
	 --%>
	    <%
	    	for (int k=0; k< nrBlanksToFill; k++) {
	    %>
	    <td></td>
	    <% } %>
	
<%
	for (j=i ; ((j - i) < NR_STATES_PER_ROW) && (j  <MAX_NR_STATES); j++) {
	    final int count = mainCounter.getCounts()[j];
	    final SiadapProcessStateEnum state = SiadapProcessStateEnum.values()[j];
	    siadapTotalCount += count;
%>
		
		<td>
				<%= count %>
		</td>
<%
	}
%>
	</tr>
	<%
}
%>
</table>


	


<table align="center" style="width: 100%; text-align: center;">
	<tr>
		<td style="width: 50%; vertical-align: top; padding: 15px;">
			<div class="infobox" style="height: 100%;">
				<h4>
					<bean:message key="label.unit.harmonizer" bundle="SIADAP_RESOURCES"/>
				</h4>
				<logic:notEmpty name="unitHarmonizers">
					<logic:iterate id="unitHarmonizer" name="unitHarmonizers">
						<bean:define id="username" name="unitHarmonizer" property="user.username" type="java.lang.String"/>
						<% if (User.findByUsername(username).getProfile() != null) { %>
							<img src="<%= User.findByUsername(username).getProfile().getAvatarUrl() %>">
						<% } %>
					</logic:iterate>
				</logic:notEmpty>
					<logic:notEmpty name="unitHarmonizers">
						<logic:iterate id="unitHarmonizer" name="unitHarmonizers">
							<p>
							<html:link page="<%= "/siadapPersonnelManagement.do?method=viewPerson&year=" + configuration.getYear() %>" 
								paramId="personId" paramName="unitHarmonizer" paramProperty="externalId"
									styleClass="secondaryLink">
								<bean:write name="unitHarmonizer" property="presentationName"/>
							</html:link> 
							<%--let's only allow removal/edition of reponsibles if we are in an harmonization unit --%>
							<logic:equal value="true" name="unitWrapper" property="harmonizationUnit">
								| 
								<html:link page="<%= "/unitManagementInterface.do?method=terminateUnitHarmonization&year=" +
								configuration.getYear()+"&unitId="+ unit.getExternalId() +
								"&personId=" + ((Person)unitHarmonizer).getExternalId() + "&mode=" + mode.name()%>" 
									paramId="personId" paramName="unitHarmonizer" paramProperty="externalId">
									 (Remover)
								</html:link>
							</logic:equal>
							</p>
						</logic:iterate>
					</logic:notEmpty>
					
					<logic:empty name="unitHarmonizers">
						<p><bean:message key="label.not.defined" bundle="SIADAP_RESOURCES"/></p>
					</logic:empty>
					<%
					//let's only allow edition/removal if we are in a harmonization unit
					if (mode.equals(Mode.HARMONIZATION_UNIT_MODE) && unitWrapper.isHarmonizationUnit() ) {
					%>
					<div id="addHarmonizationUnitResponsibleDiv" >
						<%--  <div class="highlightBox"> --%>
		
						<fr:form action="<%= "/unitManagementInterface.do?method=addHarmonizationUnitResponsible&year=" + 
						year.toString() + "&mode=" + mode.name() + "&unitId=" + unit.getExternalId() %>">
		
							<fr:edit id="addHarmonizationUnitResponsible" name="bean" visible="false"/>
		
						<fr:edit id="addHarmonizationUnitResponsible1" name="bean" slot="domainObject">
						<fr:layout name="autoComplete">
					        <fr:property name="labelField" value="name"/>
							<fr:property name="format" value="\${name} (\${user.username})"/>
							<fr:property name="minChars" value="3"/>		
							<fr:property name="args" value="provider=module.organization.presentationTier.renderers.providers.PersonAutoCompleteProvider"/>
							<fr:property name="size" value="60"/>
							<fr:validator name="pt.ist.fenixWebFramework.rendererExtensions.validators.RequiredAutoCompleteSelectionValidator">
								<fr:property name="message" value="label.pleaseSelectOne.unit"/>
								<fr:property name="bundle" value="EXPENDITURE_RESOURCES"/>
								<fr:property name="key" value="true"/>
							</fr:validator>
							</fr:layout>
						</fr:edit>
						
						<%--
						Data da alteração: <fr:edit id="changeEvaluator1" name="changeEvaluator" slot="dateOfChange" >
						<fr:layout name="picker"/>
						</fr:edit>
						 --%>
						 
						<html:submit styleClass="inputbutton"><bean:message key="button.unitManagementInterface.add.HarmonizationResponsible" bundle="SIADAP_RESOURCES"/></html:submit>
					</fr:form>
							
						<%--</div>  --%>
					</div>
					<%
					}
					%>
			</div>
		</td>
		<td style="width: 50%; text-align: center; vertical-align: top; padding: 15px;">
			<div class="infobox" style="height: 100%;">
				<h4>
					<bean:message key="label.unit.responsible" bundle="SIADAP_RESOURCES"/>
				</h4>
				<logic:present name="unitResponsible">
					<bean:define id="username" name="unitResponsible" property="user.username" type="java.lang.String"/>
					<% if (User.findByUsername(username).getProfile() != null) { %>
						<img src="<%= User.findByUsername(username).getProfile().getAvatarUrl() %>">
					<% } %>
				</logic:present>
				<p>
					<logic:present name="unitResponsible">
						<html:link page="<%= "/siadapPersonnelManagement.do?method=viewPerson&year=" + configuration.getYear() %>"
								paramId="personId" paramName="unitResponsible" paramProperty="externalId"
								styleClass="secondaryLink">
							<bean:write name="unitResponsible" property="presentationName"/>
						</html:link>
					</logic:present>
					<logic:notPresent name="unitResponsible">
						<bean:message key="label.not.defined" bundle="SIADAP_RESOURCES"/>
					</logic:notPresent>
				</p>
			</div>
		</td>
	</tr>
</table>



<logic:notEmpty name="activePersons">
	<br/>
	<br/>
	<h3><%=mode.getLabelActivePersons()%></h3>
	<table class="tstyle3" align="center" style="width: 100%; text-align: center;">
		<tr>
			<th>
			</th>
			<th>
				<bean:message key="label.person" bundle="ORGANIZATION_RESOURCES"/>
			</th>
			<th>
				<bean:message key="label.evaluator" bundle="SIADAP_RESOURCES"/>
			</th>
			<th>
				<bean:message key="label.quotaAware" bundle="SIADAP_RESOURCES"/>
			</th>
			<th>
				<bean:message key="label.state" bundle="SIADAP_RESOURCES"/>
			</th>
			<th>
			</th>
		</tr>
		<logic:iterate id="personSiadapWrapper" name="activePersons" type="module.siadap.domain.wrappers.PersonSiadapWrapper">
			<bean:define id="person" type="module.organization.domain.Person" name="personSiadapWrapper" property="person"/>
			<tr>
				<td>
					<bean:define id="username" name="person" property="user.username" type="java.lang.String"/>
					<% if (User.findByUsername(username).getProfile() != null) { %>
						<img src="<%= User.findByUsername(username).getProfile().getAvatarUrl() %>">
					<% } %>
				</td>
				<td>
					<html:link page="<%= "/siadapPersonnelManagement.do?method=viewPerson&year=" + configuration.getYear() %>"
							paramId="personId" paramName="person" paramProperty="externalId"
							styleClass="secondaryLink">
						<bean:write name="person" property="presentationName"/>
					</html:link>
				</td>
				<td>
					<logic:present name="personSiadapWrapper" property="evaluator">
					<html:link page="<%= "/siadapPersonnelManagement.do?method=viewPerson&year=" + configuration.getYear() 
							+ "&personId=" + personSiadapWrapper.getEvaluator().getPerson().getExternalId() %>"
							styleClass="secondaryLink">
							<%= personSiadapWrapper.getEvaluator().getPerson().getName() %>
					</html:link>
					</logic:present>
					<logic:notPresent name="personSiadapWrapper" property="evaluator">-</logic:notPresent>
					
				</td>
				<td>
					<%
						if (personSiadapWrapper.isQuotaAware()) {
					%>
						<bean:message bundle="SIADAP_RESOURCES" key="siadap.true.yes"/>
					<% } else { %>
						<bean:message bundle="SIADAP_RESOURCES" key="siadap.false.no"/>
					<% } %>
				</td>
				<td>
					<%
						final SiadapProcessStateEnum state = personSiadapWrapper.getCurrentProcessState();
					%>
						<%= state.getLocalizedName() %>
				</td>
				<td>
					<%
						if (personSiadapWrapper.getSiadap() == null) {
					%>
						<html:link page="<%= "/siadapPersonnelManagement.do?method=viewPerson&year=" + configuration.getYear() %>"
								paramId="personId" paramName="person" paramProperty="externalId">
								<bean:message key="link.create" bundle="MYORG_RESOURCES"/>
						</html:link>
					<%
						} else {
					%>
						<html:link page="<%= "/workflowProcessManagement.do?method=viewProcess&year="
							+ configuration.getYear()
							+ "&processId="
							+ personSiadapWrapper.getSiadap().getProcess().getExternalId() %>">
							<bean:message key="link.view" bundle="MYORG_RESOURCES"/>
						</html:link>
					<%
						}
					%>
				</td>
			</tr>
		</logic:iterate>
	</table>
</logic:notEmpty>

<br/>
<jsp:include page="../processStateLegend.jsp"/>

<script type="text/javascript">
if (typeof document.attachEvent!='undefined') {
	   window.attachEvent('onload',init);
	   document.attachEvent('onmousemove',moveMouse);
	   document.attachEvent('onclick',checkMove); }
	else {
	   window.addEventListener('load',init,false);
	   document.addEventListener('mousemove',moveMouse,false);
	   document.addEventListener('click',checkMove,false);
	}

	var oDv=document.createElement("div");
	var dvHdr=document.createElement("div");
	var dvBdy=document.createElement("div");
	var windowlock,boxMove,fixposx,fixposy,lockX,lockY,fixx,fixy,ox,oy,boxLeft,boxRight,boxTop,boxBottom,evt,mouseX,mouseY,boxOpen,totalScrollTop,totalScrollLeft;
	boxOpen=false;
	ox=10;
	oy=10;
	lockX=0;
	lockY=0;

	function init() {
		oDv.appendChild(dvHdr);
		oDv.appendChild(dvBdy);
		oDv.style.position="absolute";
		oDv.style.visibility='hidden';
		document.body.appendChild(oDv);	
	}

	function defHdrStyle() {
		dvHdr.innerHTML='<img  style="vertical-align:middle"  src="info.gif">&nbsp;&nbsp;'+dvHdr.innerHTML;
		dvHdr.style.fontWeight='bold';
		dvHdr.style.width='150px';
		dvHdr.style.fontFamily='arial';
		dvHdr.style.border='1px solid #A5CFE9';
		dvHdr.style.padding='3';
		dvHdr.style.fontSize='11';
		dvHdr.style.color='#4B7A98';
		dvHdr.style.background='#D5EBF9';
		dvHdr.style.filter='alpha(opacity=85)'; // IE
		dvHdr.style.opacity='0.85'; // FF
	}

	function defBdyStyle() {
		dvBdy.style.borderBottom='1px solid #A5CFE9';
		dvBdy.style.borderLeft='1px solid #A5CFE9';
		dvBdy.style.borderRight='1px solid #A5CFE9';
		dvBdy.style.width='150px';
		dvBdy.style.fontFamily='arial';
		dvBdy.style.fontSize='11';
		dvBdy.style.padding='3';
		dvBdy.style.color='#1B4966';
		dvBdy.style.background='#FFFFFF';
		dvBdy.style.filter='alpha(opacity=85)'; // IE
		dvBdy.style.opacity='0.85'; // FF
	}

	function checkElemBO(txt) {
	if (!txt || typeof(txt) != 'string') return false;
	if ((txt.indexOf('header')>-1)&&(txt.indexOf('body')>-1)&&(txt.indexOf('[')>-1)&&(txt.indexOf('[')>-1)) 
	   return true;
	else
	   return false;
	}

	function scanBO(curNode) {
		  if (checkElemBO(curNode.title)) {
	         curNode.boHDR=getParam('header',curNode.title);
	         curNode.boBDY=getParam('body',curNode.title);
				curNode.boCSSBDY=getParam('cssbody',curNode.title);			
				curNode.boCSSHDR=getParam('cssheader',curNode.title);
				curNode.IEbugfix=(getParam('hideselects',curNode.title)=='on')?true:false;
				curNode.fixX=parseInt(getParam('fixedrelx',curNode.title));
				curNode.fixY=parseInt(getParam('fixedrely',curNode.title));
				curNode.absX=parseInt(getParam('fixedabsx',curNode.title));
				curNode.absY=parseInt(getParam('fixedabsy',curNode.title));
				curNode.offY=(getParam('offsety',curNode.title)!='')?parseInt(getParam('offsety',curNode.title)):10;
				curNode.offX=(getParam('offsetx',curNode.title)!='')?parseInt(getParam('offsetx',curNode.title)):10;
				curNode.fade=(getParam('fade',curNode.title)=='on')?true:false;
				curNode.fadespeed=(getParam('fadespeed',curNode.title)!='')?getParam('fadespeed',curNode.title):0.04;
				curNode.delay=(getParam('delay',curNode.title)!='')?parseInt(getParam('delay',curNode.title)):0;
				if (getParam('requireclick',curNode.title)=='on') {
					curNode.requireclick=true;
					document.all?curNode.attachEvent('onclick',showHideBox):curNode.addEventListener('click',showHideBox,false);
					document.all?curNode.attachEvent('onmouseover',hideBox):curNode.addEventListener('mouseover',hideBox,false);
				}
				else {// Note : if requireclick is on the stop clicks are ignored   			
	   			if (getParam('doubleclickstop',curNode.title)!='off') {
	   				document.all?curNode.attachEvent('ondblclick',pauseBox):curNode.addEventListener('dblclick',pauseBox,false);
	   			}	
	   			if (getParam('singleclickstop',curNode.title)=='on') {
	   				document.all?curNode.attachEvent('onclick',pauseBox):curNode.addEventListener('click',pauseBox,false);
	   			}
	   		}
				curNode.windowLock=getParam('windowlock',curNode.title).toLowerCase()=='off'?false:true;
				curNode.title='';
				curNode.hasbox=1;
		   }
		   else
		      curNode.hasbox=2;   
	}


	function getParam(param,list) {
		var reg = new RegExp('([^a-zA-Z]' + param + '|^' + param + ')\\s*=\\s*\\[\\s*(((\\[\\[)|(\\]\\])|([^\\]\\[]))*)\\s*\\]');
		var res = reg.exec(list);
		var returnvar;
		if(res)
			return res[2].replace('[[','[').replace(']]',']');
		else
			return '';
	}

	function Left(elem){	
		var x=0;
		if (elem.calcLeft)
			return elem.calcLeft;
		var oElem=elem;
		while(elem){
			 if ((elem.currentStyle)&& (!isNaN(parseInt(elem.currentStyle.borderLeftWidth)))&&(x!=0))
			 	x+=parseInt(elem.currentStyle.borderLeftWidth);
			 x+=elem.offsetLeft;
			 elem=elem.offsetParent;
		  } 
		oElem.calcLeft=x;
		return x;
		}

	function Top(elem){
		 var x=0;
		 if (elem.calcTop)
		 	return elem.calcTop;
		 var oElem=elem;
		 while(elem){		
		 	 if ((elem.currentStyle)&& (!isNaN(parseInt(elem.currentStyle.borderTopWidth)))&&(x!=0))
			 	x+=parseInt(elem.currentStyle.borderTopWidth); 
			 x+=elem.offsetTop;
		         elem=elem.offsetParent;
	 	 } 
	 	 oElem.calcTop=x;
	 	 return x;
	 	 
	}

	var ah,ab;
	function applyStyles() {
		if(ab)
			oDv.removeChild(dvBdy);
		if (ah)
			oDv.removeChild(dvHdr);
		dvHdr=document.createElement("div");
		dvBdy=document.createElement("div");
		CBE.boCSSBDY?dvBdy.className=CBE.boCSSBDY:defBdyStyle();
		CBE.boCSSHDR?dvHdr.className=CBE.boCSSHDR:defHdrStyle();
		dvHdr.innerHTML=CBE.boHDR;
		dvBdy.innerHTML=CBE.boBDY;
		ah=false;
		ab=false;
		if (CBE.boHDR!='') {		
			oDv.appendChild(dvHdr);
			ah=true;
		}	
		if (CBE.boBDY!=''){
			oDv.appendChild(dvBdy);
			ab=true;
		}	
	}

	var CSE,iterElem,LSE,CBE,LBE, totalScrollLeft, totalScrollTop, width, height ;
	var ini=false;

	// Customised function for inner window dimension
	function SHW() {
	   if (document.body && (document.body.clientWidth !=0)) {
	      width=document.body.clientWidth;
	      height=document.body.clientHeight;
	   }
	   if (document.documentElement && (document.documentElement.clientWidth!=0) && (document.body.clientWidth + 20 >= document.documentElement.clientWidth)) {
	      width=document.documentElement.clientWidth;   
	      height=document.documentElement.clientHeight;   
	   }   
	   return [width,height];
	}


	var ID=null;
	function moveMouse(e) {
	   //boxMove=true;
		e?evt=e:evt=event;
		
		CSE=evt.target?evt.target:evt.srcElement;
		
		if (!CSE.hasbox) {
		   // Note we need to scan up DOM here, some elements like TR don't get triggered as srcElement
		   iElem=CSE;
		   while ((iElem.parentNode) && (!iElem.hasbox)) {
		      scanBO(iElem);
		      iElem=iElem.parentNode;
		   }	   
		}
		
		if ((CSE!=LSE)&&(!isChild(CSE,dvHdr))&&(!isChild(CSE,dvBdy))){		
		   if (!CSE.boxItem) {
				iterElem=CSE;
				while ((iterElem.hasbox==2)&&(iterElem.parentNode))
						iterElem=iterElem.parentNode; 
				CSE.boxItem=iterElem;
				}
			iterElem=CSE.boxItem;
			if (CSE.boxItem&&(CSE.boxItem.hasbox==1))  {
				LBE=CBE;
				CBE=iterElem;
				if (CBE!=LBE) {
					applyStyles();
					if (!CBE.requireclick)
						if (CBE.fade) {
							if (ID!=null)
								clearTimeout(ID);
							ID=setTimeout("fadeIn("+CBE.fadespeed+")",CBE.delay);
						}
						else {
							if (ID!=null)
								clearTimeout(ID);
							COL=1;
							ID=setTimeout("oDv.style.visibility='visible';ID=null;",CBE.delay);						
						}
					if (CBE.IEbugfix) {hideSelects();} 
					fixposx=!isNaN(CBE.fixX)?Left(CBE)+CBE.fixX:CBE.absX;
					fixposy=!isNaN(CBE.fixY)?Top(CBE)+CBE.fixY:CBE.absY;			
					lockX=0;
					lockY=0;
					boxMove=true;
					ox=CBE.offX?CBE.offX:10;
					oy=CBE.offY?CBE.offY:10;
				}
			}
			else if (!isChild(CSE,dvHdr) && !isChild(CSE,dvBdy) && (boxMove))	{
				// The conditional here fixes flickering between tables cells.
				if ((!isChild(CBE,CSE)) || (CSE.tagName!='TABLE')) {   			
	   			CBE=null;
	   			if (ID!=null)
	  					clearTimeout(ID);
	   			fadeOut();
	   			showSelects();
				}
			}
			LSE=CSE;
		}
		else if (((isChild(CSE,dvHdr) || isChild(CSE,dvBdy))&&(boxMove))) {
			totalScrollLeft=0;
			totalScrollTop=0;
			
			iterElem=CSE;
			while(iterElem) {
				if(!isNaN(parseInt(iterElem.scrollTop)))
					totalScrollTop+=parseInt(iterElem.scrollTop);
				if(!isNaN(parseInt(iterElem.scrollLeft)))
					totalScrollLeft+=parseInt(iterElem.scrollLeft);
				iterElem=iterElem.parentNode;			
			}
			if (CBE!=null) {
				boxLeft=Left(CBE)-totalScrollLeft;
				boxRight=parseInt(Left(CBE)+CBE.offsetWidth)-totalScrollLeft;
				boxTop=Top(CBE)-totalScrollTop;
				boxBottom=parseInt(Top(CBE)+CBE.offsetHeight)-totalScrollTop;
				doCheck();
			}
		}
		
		if (boxMove&&CBE) {
			// This added to alleviate bug in IE6 w.r.t DOCTYPE
			bodyScrollTop=document.documentElement&&document.documentElement.scrollTop?document.documentElement.scrollTop:document.body.scrollTop;
			bodyScrollLet=document.documentElement&&document.documentElement.scrollLeft?document.documentElement.scrollLeft:document.body.scrollLeft;
			mouseX=evt.pageX?evt.pageX-bodyScrollLet:evt.clientX-document.body.clientLeft;
			mouseY=evt.pageY?evt.pageY-bodyScrollTop:evt.clientY-document.body.clientTop;
			if ((CBE)&&(CBE.windowLock)) {
				mouseY < -oy?lockY=-mouseY-oy:lockY=0;
				mouseX < -ox?lockX=-mouseX-ox:lockX=0;
				mouseY > (SHW()[1]-oDv.offsetHeight-oy)?lockY=-mouseY+SHW()[1]-oDv.offsetHeight-oy:lockY=lockY;
				mouseX > (SHW()[0]-dvBdy.offsetWidth-ox)?lockX=-mouseX-ox+SHW()[0]-dvBdy.offsetWidth:lockX=lockX;			
			}
			oDv.style.left=((fixposx)||(fixposx==0))?fixposx:bodyScrollLet+mouseX+ox+lockX+"px";
			oDv.style.top=((fixposy)||(fixposy==0))?fixposy:bodyScrollTop+mouseY+oy+lockY+"px";		
			
		}
	}

	function doCheck() {	
		if (   (mouseX < boxLeft)    ||     (mouseX >boxRight)     || (mouseY < boxTop) || (mouseY > boxBottom)) {
			if (!CBE.requireclick)
				fadeOut();
			if (CBE.IEbugfix) {showSelects();}
			CBE=null;
		}
	}

	function pauseBox(e) {
	   e?evt=e:evt=event;
		boxMove=false;
		evt.cancelBubble=true;
	}

	function showHideBox(e) {
		oDv.style.visibility=(oDv.style.visibility!='visible')?'visible':'hidden';
	}

	function hideBox(e) {
		oDv.style.visibility='hidden';
	}

	var COL=0;
	var stopfade=false;
	function fadeIn(fs) {
			ID=null;
			COL=0;
			oDv.style.visibility='visible';
			fadeIn2(fs);
	}

	function fadeIn2(fs) {
			COL=COL+fs;
			COL=(COL>1)?1:COL;
			oDv.style.filter='alpha(opacity='+parseInt(100*COL)+')';
			oDv.style.opacity=COL;
			if (COL<1)
			 setTimeout("fadeIn2("+fs+")",20);		
	}


	function fadeOut() {
		oDv.style.visibility='hidden';
		
	}

	function isChild(s,d) {
		while(s) {
			if (s==d) 
				return true;
			s=s.parentNode;
		}
		return false;
	}

	var cSrc;
	function checkMove(e) {
		e?evt=e:evt=event;
		cSrc=evt.target?evt.target:evt.srcElement;
		if ((!boxMove)&&(!isChild(cSrc,oDv))) {
			fadeOut();
			if (CBE&&CBE.IEbugfix) {showSelects();}
			boxMove=true;
			CBE=null;
		}
	}

	function showSelects(){
	   var elements = document.getElementsByTagName("select");
	   for (i=0;i< elements.length;i++){
	      elements[i].style.visibility='visible';
	   }
	}

	function hideSelects(){
	   var elements = document.getElementsByTagName("select");
	   for (i=0;i< elements.length;i++){
	   elements[i].style.visibility='hidden';
	   }
	}
</script>
</logic:present>
