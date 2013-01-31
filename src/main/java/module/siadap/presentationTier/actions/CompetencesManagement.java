/*
 * @(#)CompetencesManagement.java
 *
 * Copyright 2010 Instituto Superior Tecnico
 * Founding Authors: Paulo Abrantes
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the SIADAP Module.
 *
 *   The SIADAP Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The SIADAP Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the SIADAP Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.siadap.presentationTier.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.siadap.domain.Competence;
import module.siadap.domain.CompetenceType;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.dto.CompetenceBean;
import module.siadap.domain.dto.CompetenceTypeBean;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pt.ist.bennu.core.presentationTier.actions.ContextBaseAction;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;

@Mapping(path = "/competencesManagement")
/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public class CompetencesManagement extends ContextBaseAction {

	public ActionForward manageCompetences(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
			final HttpServletResponse response) {
		SiadapRootModule rootModule = SiadapRootModule.getInstance();
		request.setAttribute("competenceTypes", rootModule.getCompetenceTypes());
		request.setAttribute("siadapRoot", rootModule);
		return forward(request, "/module/siadap/competences/manageCompetences.jsp");
	}

	public ActionForward prepareCompetenceTypeCreation(final ActionMapping mapping, final ActionForm form,
			final HttpServletRequest request, final HttpServletResponse response) {

		CompetenceTypeBean competenceTypeBean = new CompetenceTypeBean();
		request.setAttribute("bean", competenceTypeBean);

		return forward(request, "/module/siadap/competences/createCompetenceType.jsp");
	}

	public ActionForward createCompetenceType(final ActionMapping mapping, final ActionForm form,
			final HttpServletRequest request, final HttpServletResponse response) {

		CompetenceTypeBean competenceTypeBean = getRenderedObject("bean");
		CompetenceType.createNewCompetenceType(competenceTypeBean.getName());

		return manageCompetences(mapping, form, request, response);
	}

	public ActionForward prepareCompetenceCreation(final ActionMapping mapping, final ActionForm form,
			final HttpServletRequest request, final HttpServletResponse response) {

		CompetenceType competenceType = getDomainObject(request, "competenceTypeId");
		CompetenceBean competenceBean = new CompetenceBean(competenceType);

		request.setAttribute("bean", competenceBean);

		return forward(request, "/module/siadap/competences/createCompetence.jsp");
	}

	public ActionForward createCompetence(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
			final HttpServletResponse response) {

		CompetenceBean competenceBean = getRenderedObject("bean");
		Competence.createNewCompetence(competenceBean.getCompetenceType(), competenceBean.getName(),
				competenceBean.getDescription());

		return manageCompetences(mapping, form, request, response);
	}

	public ActionForward showCompetences(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
			final HttpServletResponse response) {

		CompetenceType competenceType = getDomainObject(request, "competenceTypeId");
		request.setAttribute("type", competenceType);

		return forward(request, "/module/siadap/competences/listCompetences.jsp");

	}

}
