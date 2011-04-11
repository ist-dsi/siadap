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
import myorg.domain.groups.UserGroup;
import myorg.presentationTier.actions.BaseAction;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pt.ist.bennu.vaadin.domain.contents.VaadinNode;
import pt.ist.fenixWebFramework.servlets.functionalities.CreateNodeAction;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;

@Mapping(path = "/siadapInterfaceCreation")
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
		"link.siadap.showConfiguration",  managerGroup);

	//use the static group TODO alter this	
	ActionNode.createActionNode(virtualHost, homeNode, "/siadapPersonnelManagement", "start", "resources.SiadapResources",
		"link.siadap.structureManagement", SiadapYearConfiguration.getStructureManagementGroup());

	//use the static group TODO alter this	
	ActionNode.createActionNode(virtualHost, homeNode, "/siadapManagement",
		"manageHarmonizationUnitsForMode&mode=processValidation", "resources.SiadapResources",
		"link.siadap.validationProcedure", SiadapYearConfiguration.getCcaMembersGroup());

	//use the static group TODO alter this	
	ActionNode.createActionNode(virtualHost, homeNode, "/siadapManagement",
		"manageHarmonizationUnitsForMode&mode=homologationDone", "resources.SiadapResources",
		"link.siadap.homologationProcedure", SiadapYearConfiguration.getHomologationMembersGroup());

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

	ActionNode.createActionNode(virtualHost, node, "/siadapProcessCount", "showUnit",
 "resources.SiadapResources",
		"link.siadapProcessCount", SiadapRootModule.getInstance().getStatisticsAccessUnionGroup());

	return forwardToMuneConfiguration(request, virtualHost, node);

    }

}
