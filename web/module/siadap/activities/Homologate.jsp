<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<bean:define id="processId" name="process" property="externalId" type="java.lang.String" />
<bean:define id="name" name="information" property="activityName" />

<jsp:include page="ChangeGradeAnytimeAfterValidationByCCA.jsp" flush="true">
	<jsp:param value="homologation" name="mode"/>
</jsp:include>