package module.siadap.presentationTier.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.siadap.domain.wrappers.PersonSiadapWrapper;
import myorg.applicationTier.Authenticate.UserView;
import myorg.presentationTier.actions.ContextBaseAction;
import myorg.util.VariantBean;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.struts.annotations.Mapping;

@Mapping(path = "/siadapPersonnelManagement")
public class SiadapPersonnelManagement extends ContextBaseAction {

    public final ActionForward start(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) throws Exception {

	VariantBean bean = new VariantBean();
	request.setAttribute("bean", bean);

	request.setAttribute("person", new PersonSiadapWrapper(UserView.getCurrentUser().getPerson(), new LocalDate().getYear()));
	return forward(request, "/module/siadap/management/start.jsp");
    }

}
