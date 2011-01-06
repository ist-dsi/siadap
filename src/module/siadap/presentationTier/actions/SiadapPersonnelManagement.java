package module.siadap.presentationTier.actions;

import java.io.Serializable;
import java.util.ArrayList;
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
import module.siadap.domain.wrappers.SiadapYearWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import module.siadap.presentationTier.renderers.providers.SiadapYearsFromExistingSiadapConfigurations;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.exceptions.DomainException;
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

	SiadapYearWrapper siadapYearWrapper = (SiadapYearWrapper) getRenderedObject("siadapYearWrapper");
	if (siadapYearWrapper == null) {
	    ArrayList<Integer> yearsWithConfigs = SiadapYearsFromExistingSiadapConfigurations.getYearsWithExistingConfigs();
	    if (yearsWithConfigs.contains(new Integer(new LocalDate().getYear()))) {
		int year = new LocalDate().getYear();
		siadapYearWrapper = new SiadapYearWrapper(year);
	    } else {
		siadapYearWrapper = new SiadapYearWrapper(yearsWithConfigs.get(yearsWithConfigs.size() - 1));
	    }
	}
	request.setAttribute("siadapYearWrapper", siadapYearWrapper);
	VariantBean bean = new VariantBean();
	request.setAttribute("bean", bean);

	request.setAttribute("person", new PersonSiadapWrapper(UserView.getCurrentUser().getPerson(), new LocalDate().getYear()));
	return forward(request, "/module/siadap/management/start.jsp");
    }

    public final ActionForward viewPerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) throws Exception {

	VariantBean bean = getRenderedObject("searchPerson");
	Person person = (Person) ((bean != null) ? bean.getDomainObject() : getDomainObject(request, "personId"));

	int year = Integer.parseInt(request.getParameter("year"));
	PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(person, year);

	request.setAttribute("person", personSiadapWrapper);
	request.setAttribute("bean", new VariantBean());
	request.setAttribute("changeWorkingUnit", new ChangeWorkingUnitBean(person, year));
	request.setAttribute("changeEvaluator", new ChangeEvaluatorBean());
	request.setAttribute("history", personSiadapWrapper.getAccountabilitiesHistory());
	return forward(request, "/module/siadap/management/editPerson.jsp");
    }

    public final ActionForward terminateUnitHarmonization(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	LocalDate now = new LocalDate();
	int year = Integer.parseInt(request.getParameter("year"));
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

	int year = Integer.parseInt(request.getParameter("year"));

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

	int year = Integer.parseInt(request.getParameter("year"));
	PersonSiadapWrapper personWrapper = new PersonSiadapWrapper(bean.getPerson(), year);
	try {
	personWrapper.changeWorkingUnitTo(bean.getUnit(), bean.getWithQuotas(), bean.getDateOfChange());
	} catch (DomainException e) {
	    addMessage(request, e.getKey(), e.getArgs());
	}

	return viewPerson(mapping, form, request, response);
    }

    public final ActionForward changeEvaluator(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	int year = Integer.parseInt(request.getParameter("year"));
	ChangeEvaluatorBean changeEvaluatorBean = getRenderedObject("changeEvaluator");
	PersonSiadapWrapper personWrapper = new PersonSiadapWrapper((Person) getDomainObject(request, "personId"), year);

	try {
	    personWrapper.changeEvaluatorTo(changeEvaluatorBean.getEvaluator(), changeEvaluatorBean.getDateOfChange());
	} catch (DomainException e) {
	    addMessage(request, e.getKey(), e.getArgs());
	}

	return viewPerson(mapping, form, request, response);
    }

    public final ActionForward removeCustomEvaluator(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	int year = Integer.parseInt(request.getParameter("year"));
	PersonSiadapWrapper personWrapper = new PersonSiadapWrapper((Person) getDomainObject(request, "personId"), year);

	personWrapper.removeCustomEvaluator();

	return viewPerson(mapping, form, request, response);
    }

    public static class ChangeEvaluatorBean implements Serializable {
	private Person evaluator;
	private LocalDate dateOfChange;

	public ChangeEvaluatorBean() {
	    this.dateOfChange = new LocalDate();
	}

	public void setEvaluator(Person person) {
	    this.evaluator = person;
	}

	public Person getEvaluator() {
	    return evaluator;
	}
	public void setDateOfChange(LocalDate dateOfChange) {
	    this.dateOfChange = dateOfChange;
	}
	public LocalDate getDateOfChange() {
	    return dateOfChange;
	}
    }

    public static class ChangeWorkingUnitBean implements Serializable {

	private Person person;
	private Boolean withQuotas;
	private int year;
	private Unit unit;
	private LocalDate dateOfChange;

	public ChangeWorkingUnitBean(Person person, int year) {
	    this.person = person;
	    this.year = year;
	    this.dateOfChange = new LocalDate();
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

	public void setDateOfChange(LocalDate dateOfChange) {
	    this.dateOfChange = dateOfChange;
	}

	public LocalDate getDateOfChange() {
	    return dateOfChange;
	}

    }
}
