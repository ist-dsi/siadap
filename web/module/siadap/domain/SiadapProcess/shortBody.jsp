<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr" %>

<bean:define id="evaluatedName" name="process" property="siadap.evaluated.name" type="java.lang.String"/>
<bean:define id="year" name="process" property="siadap.year"/>
  
<div>
	<bean:message key="label.userEvaluationProcess" bundle="SIADAP_RESOURCES" arg0="<%= evaluatedName %>" arg1="<%= year.toString() %>"/>
</div>