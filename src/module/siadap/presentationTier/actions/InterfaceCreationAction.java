/*
 * @(#)InterfaceCreationAction.java
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

import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;
import myorg.domain.RoleType;
import myorg.domain.VirtualHost;
import myorg.domain.contents.ActionNode;
import myorg.domain.contents.Node;
import myorg.domain.groups.PersistentGroup;
import myorg.domain.groups.Role;
import myorg.domain.groups.UnionGroup;
import myorg.domain.groups.UserGroup;
import myorg.presentationTier.actions.BaseAction;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pt.ist.bennu.vaadin.domain.contents.VaadinNode;
import pt.ist.fenixWebFramework.servlets.functionalities.CreateNodeAction;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;

@Mapping(path = "/siadapInterfaceCreation")
/**
 * 
 * @author João Antunes
 * @author Luis Cruz
 * @author Paulo Abrantes
 * 
 */
public class InterfaceCreationAction extends BaseAction {

    @CreateNodeAction(bundle = "SIADAP_RESOURCES", key = "add.node.siadap.siadapInterface", groupKey = "label.module.siadap")
    public final ActionForward createSiadapNode(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final VirtualHost virtualHost = getDomainObject(request, "virtualHostToManageId");
	final Node node = getDomainObject(request, "parentOfNodesToManageId");

	final Node homeNode = ActionNode.createActionNode(virtualHost, node, "/siadapManagement", "manageSiadap",
		"resources.SiadapResources", "link.siadapManagement", UserGroup.getInstance());

	PersistentGroup managerGroup = Role.getRole(RoleType.MANAGER);

	ActionNode.createActionNode(virtualHost, homeNode, "/siadapManagement", "manageSiadap", "resources.SiadapResources",
		"link.siadap.start", UserGroup.getInstance());

	ActionNode.createActionNode(virtualHost, homeNode, "/competencesManagement", "manageCompetences",
		"resources.SiadapResources", "link.siadap.compentencesManagement", Role.getRole(RoleType.MANAGER));

	ActionNode.createActionNode(virtualHost, homeNode, "/siadapManagement", "showConfiguration", "resources.SiadapResources",
		"link.siadap.showConfiguration", managerGroup);

	//use the static group TODO alter this	
	ActionNode.createActionNode(virtualHost, homeNode, "/siadapPersonnelManagement", "start", "resources.SiadapResources",
		"link.siadap.structureManagement", SiadapYearConfiguration.getStructureManagementGroup());

	//use the static group TODO alter this	
	ActionNode.createActionNode(virtualHost, homeNode, "/siadapManagement", "validate", "resources.SiadapResources",
		"link.siadap.validationProcedure", SiadapYearConfiguration.getCcaMembersGroup());

	ActionNode.createActionNode(virtualHost, homeNode, "/siadapManagement",
		"manageHarmonizationUnitsForMode&mode=homologationDone", "resources.SiadapResources",
		"link.siadap.homologationProcedure",
		UnionGroup.getOrCreateUnionGroup(SiadapYearConfiguration.getHomologationMembersGroup(),
			SiadapYearConfiguration.getCcaMembersGroup()));

	//the help link
	//	ActionNode.createActionNode(virtualHost, homeNode, "/vaadinContext", "forwardToVaadin#PageView-322122548202",
	//		"resources.SiadapResources", "label.link.help", UserGroup.getInstance());
	VaadinNode.createVaadinNode(virtualHost, homeNode, "resources.SiadapResources", "label.link.help",
		"PageView-322122548202", UserGroup.getInstance());

	return forwardToMuneConfiguration(request, virtualHost, node);

    }

    @CreateNodeAction(bundle = "SIADAP_RESOURCES", key = "add.node.siadap.siadapProcessCountInterface", groupKey = "label.module.siadap")
    public final ActionForward createSiadapProcessCountInterfaceNode(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	final VirtualHost virtualHost = getDomainObject(request, "virtualHostToManageId");
	final Node node = getDomainObject(request, "parentOfNodesToManageId");

	ActionNode.createActionNode(virtualHost, node, "/siadapProcessCount", "showUnit", "resources.SiadapResources",
		"link.siadapProcessCount", SiadapRootModule.getInstance().getStatisticsAccessUnionGroup());

	return forwardToMuneConfiguration(request, virtualHost, node);

    }

}
