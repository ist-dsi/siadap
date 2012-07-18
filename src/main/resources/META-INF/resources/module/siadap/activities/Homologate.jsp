<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>

<bean:define id="processId" name="process" property="externalId" type="java.lang.String" />
<bean:define id="name" name="information" property="activityName" />

<jsp:include page="ChangeGradeAnytimeAfterValidationByCCA.jsp" flush="true">
	<jsp:param value="homologation" name="mode"/>
</jsp:include>
