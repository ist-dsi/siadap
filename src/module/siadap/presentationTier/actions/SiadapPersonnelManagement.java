package module.siadap.presentationTier.actions;

import java.io.Serializable;
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

	int year = new LocalDate().getYear();
	PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(person, year);

	request.setAttribute("person", personSiadapWrapper);
	request.setAttribute("bean", new VariantBean());
	request.setAttribute("changeWorkingUnit", new ChangeWorkingUnitBean(person, year));
	request.setAttribute("changeEvaluator", new VariantBean());
	request.setAttribute("history", personSiadapWrapper.getAccountabilitiesHistory());
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

	return viewPerson(mapping, form, request, response);
    }

    public final ActionForward addHarmonizationUnit(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	LocalDate now = new LocalDate();
	int year = now.getYear();

	VariantBean bean = getRenderedObject("addHarmonizationUnit");
	Person person = getDomainObject(request, "personId");

	UnitSiadapWrapper unitWrapper = new UnitSiadapWrapper((Unit) bean.getDomainObject(), year);

	unitWrapper.addResponsibleForHarmonization(person);

	RenderUtils.invalidateViewState("addHarmonizationUnit");
	return viewPerson(mapping, form, request, response);
    }

    public final ActionForward changeWorkingUnit(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	ChangeWorkingUnitBean bean = getRenderedObject("changeWorkingUnit");

	int year = bean.getYear();
	PersonSiadapWrapper personWrapper = new PersonSiadapWrapper(bean.getPerson(), year);
	personWrapper.changeWorkingUnitTo(bean.getUnit(), bean.getWithQuotas());

	return viewPerson(mapping, form, request, response);
    }

    public final ActionForward changeEvaluator(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	int year = new LocalDate().getYear();
	Person newEvaluator = getRenderedObject("changeEvaluator");
	PersonSiadapWrapper personWrapper = new PersonSiadapWrapper((Person) getDomainObject(request, "personId"), year);

	personWrapper.changeEvaluatorTo(newEvaluator);

	return viewPerson(mapping, form, request, response);
    }

    public final ActionForward removeCustomEvaluator(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	int year = new LocalDate().getYear();
	PersonSiadapWrapper personWrapper = new PersonSiadapWrapper((Person) getDomainObject(request, "personId"), year);

	personWrapper.removeCustomEvaluator();

	return viewPerson(mapping, form, request, response);
    }

    public static class ChangeWorkingUnitBean implements Serializable {

	private Person person;
	private Boolean withQuotas;
	private int year;
	private Unit unit;

	public ChangeWorkingUnitBean(Person person, int year) {
	    this.person = person;
	    this.year = year;
	}

	public Person getPerson() {
	    return person;
	}

	public void setPerson(Person person) {
	    this.person = person;
	}

	public Unit getUnit() {
	    return unit;
	}

	public void setUnit(Unit unit) {
	    this.unit = unit;
	}

	public int getYear() {
	    return year;
	}

	public void setYear(int year) {
	    this.year = year;
	}

	public void setWithQuotas(Boolean withQuotas) {
	    this.withQuotas = withQuotas;
	}

	public Boolean getWithQuotas() {
	    return withQuotas;
	}

    }
}
