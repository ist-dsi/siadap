<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr" %>


<html:link page="/siadapManagement.do?method=createNewSiadapProcess">Criar Novo processo</html:link>

<html:link page="/siadapManagement.do?method=createNewSiadapYearConfiguration">Criar configuração para o ano corrente</html:link>
<ul>
<logic:iterate id="process" name="siadaps">
		<li><html:link page="/workflowProcessManagement.do?method=viewProcess" paramName="process" paramProperty="externalId" paramId="processId"><fr:view name="process" property="processNumber"/></html:link></li>
</logic:iterate>
</ul>