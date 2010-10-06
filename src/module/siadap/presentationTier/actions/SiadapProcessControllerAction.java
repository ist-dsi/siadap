package module.siadap.presentationTier.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.siadap.activities.CreateObjectiveEvaluationActivityInformation;
import module.workflow.domain.WorkflowProcess;
import module.workflow.presentationTier.WorkflowLayoutContext;
import module.workflow.presentationTier.actions.ProcessManagement;
import myorg.presentationTier.Context;
import myorg.presentationTier.actions.ContextBaseAction;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;

@Mapping(path = "/siadapProcessController")
public class SiadapProcessControllerAction extends ContextBaseAction {

    public ActionForward addNewIndicator(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	CreateObjectiveEvaluationActivityInformation information = getRenderedObject("activityBean");
	information.addNewIndicator();
	RenderUtils.invalidateViewState();
	return ProcessManagement.performActivityPostback(information, request);
    }

    public ActionForward removeIndicator(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {
	CreateObjectiveEvaluationActivityInformation information = getRenderedObject("activityBean");
	Integer removeIndex = Integer.valueOf(request.getParameter("removeIndex"));
	RenderUtils.invalidateViewState();

	information.removeIndicator(removeIndex);
	return ProcessManagement.performActivityPostback(information, request);
    }

    @Override
    public Context createContext(String contextPathString, HttpServletRequest request) {
	WorkflowProcess process = getProcess(request);
	WorkflowLayoutContext layout = process.getLayout();
	layout.setElements(contextPathString);
	return layout;
    }

    protected <T extends WorkflowProcess> T getProcess(HttpServletRequest request) {
	return (T) getDomainObject(request, "processId");
    }
}
