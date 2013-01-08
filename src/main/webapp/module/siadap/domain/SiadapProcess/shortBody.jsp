<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr" %>

<bean:define id="evaluatedName" name="process" property="siadap.evaluated.name" type="java.lang.String"/>
<bean:define id="year" name="process" property="siadap.year"/>
  
<div>
	<bean:message key="label.userEvaluationProcess" bundle="SIADAP_RESOURCES" arg0="<%= evaluatedName %>" arg1="<%= year.toString() %>"/>
</div>