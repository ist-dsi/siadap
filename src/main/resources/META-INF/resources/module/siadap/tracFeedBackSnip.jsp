<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
    <%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
    <%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
    
    <p style="text-align:right;"><html:link href='<%=request.getParameter("href")%>' target="_blank"><bean:message bundle="SIADAP_RESOURCES" key="siadap.feedback.trac.link"/></html:link></p>
