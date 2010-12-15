package module.siadap.presentationTier.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.siadap.domain.SiadapYearConfiguration;
import myorg.domain.RoleType;
import myorg.domain.VirtualHost;
import myorg.domain.contents.ActionNode;
import myorg.domain.contents.Node;
import myorg.domain.groups.AnyoneGroup;
import myorg.domain.groups.PersistentGroup;
import myorg.domain.groups.Role;
import myorg.domain.groups.UserGroup;
import myorg.presentationTier.actions.BaseAction;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.joda.time.DateTime;

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
		"link.siadap.structureManagement", SiadapYearConfiguration.getCcaMembersGroup());

	//use the static group TODO alter this	
	ActionNode.createActionNode(virtualHost, homeNode, "/siadapManagement",
		"manageHarmonizationUnitsForMode&mode=processValidation", "resources.SiadapResources",
		"link.siadap.validationProcedure", SiadapYearConfiguration.getCcaMembersGroup());

	//use the static group TODO alter this	
	ActionNode.createActionNode(virtualHost, homeNode, "/siadapManagement",
		"manageHarmonizationUnitsForMode&mode=homologationDone", "resources.SiadapResources",
		"link.siadap.homologationProcedure", SiadapYearConfiguration.getHomologationMembersGroup());

	return forwardToMuneConfiguration(request, virtualHost, node);

    }

}
