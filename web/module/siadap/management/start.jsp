<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>


<fr:view name="person">
	<fr:schema type="module.siadap.domain.wrappers.PersonSiadapWrapper" bundle="SIADAP_RESOURCES">
		<fr:slot name="name"/>
		<fr:slot name="workingUnit.name"/>
		<fr:slot name="evaluator.name"/>
	</fr:schema>
</fr:view>
