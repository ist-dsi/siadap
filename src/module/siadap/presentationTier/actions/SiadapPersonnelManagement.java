package module.siadap.presentationTier.actions;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import myorg.applicationTier.Authenticate.UserView;
import myorg.presentationTier.actions.ContextBaseAction;
import myorg.util.VariantBean;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
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

    public final ActionForward viewPerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) throws Exception {

	VariantBean bean = getRenderedObject("searchPerson");
	Person person = (Person) ((bean != null) ? bean.getDomainObject() : getDomainObject(request, "personId"));

	request.setAttribute("person", new PersonSiadapWrapper(person, new LocalDate().getYear()));
	request.setAttribute("bean", new VariantBean());
	return forward(request, "/module/siadap/management/editPerson.jsp");
    }

    public final ActionForward terminateUnitHarmonization(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	LocalDate now = new LocalDate();
	int year = now.getYear();
	Unit unit = getDomainObject(request, "unitId");
	Person person = getDomainObject(request, "personId");

	Set<AccountabilityType> accountabilityTypes = Collections.singleton(SiadapYearConfiguration.getSiadapYearConfiguration(
		year).getHarmonizationResponsibleRelation());
	Collection<Accountability> parentAccountabilities = person.getParentAccountabilities(accountabilityTypes);

	for (Accountability accountability : parentAccountabilities) {
	    if (accountability.getParent() == unit) {
		accountability.editDates(accountability.getBeginDate(), now);
	    }
	}

	request.setAttribute("person", new PersonSiadapWrapper(person, year));
	request.setAttribute("bean", new VariantBean());
	return forward(request, "/module/siadap/management/editPerson.jsp");
    }

    public final ActionForward addHarmonizationUnit(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	LocalDate now = new LocalDate();
	int year = now.getYear();

	VariantBean bean = getRenderedObject("addHarmonizationUnit");
	Person person = getDomainObject(request, "personId");

	PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(person, year);
	UnitSiadapWrapper unitWrapper = new UnitSiadapWrapper((Unit) bean.getDomainObject(), year);

	unitWrapper.addResponsibleForHarmonization(person);

	request.setAttribute("person", personSiadapWrapper);
	request.setAttribute("bean", bean);
	RenderUtils.invalidateViewState("addHarmonizationUnit");

	return forward(request, "/module/siadap/management/editPerson.jsp");
    }

}
