package module.siadap.presentationTier.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.organization.domain.Person;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapYearConfiguration;
import module.workflow.domain.WorkflowProcess;
import module.workflow.presentationTier.actions.ProcessManagement;
import myorg.applicationTier.Authenticate.UserView;
import myorg.presentationTier.actions.ContextBaseAction;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.struts.annotations.Mapping;

@Mapping(path = "/siadapManagement")
public class SiadapManagement extends ContextBaseAction {

    public final ActionForward createNewSiadapProcess(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	Person person = UserView.getCurrentUser().getPerson();
	SiadapProcess siadapProcess = SiadapProcess.createNewProcess(person, person);

	return ProcessManagement.forwardToProcess(siadapProcess);
    }

    public final ActionForward manageSiadap(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) throws Exception {

	request.setAttribute("siadaps", WorkflowProcess.getAllProcesses(SiadapProcess.class));
	return forward(request, "/module/siadap/listSiadaps.jsp");
    }

    public final ActionForward createNewSiadapYearConfiguration(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	SiadapYearConfiguration.createNewSiadapYearConfiguration(new LocalDate().getYear());
	return manageSiadap(mapping, form, request, response);
    }
}
