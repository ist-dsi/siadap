package module.siadap.presentationTier.actions;

import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import module.workflow.domain.WorkflowProcess;
import module.workflow.presentationTier.actions.ProcessManagement;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.exceptions.DomainException;
import myorg.presentationTier.actions.ContextBaseAction;

import org.apache.commons.collections.Predicate;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.struts.annotations.Mapping;

@Mapping(path = "/siadapManagement")
public class SiadapManagement extends ContextBaseAction {

    public final ActionForward prepareToCreateNewSiadapProcess(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	Person evaluator = UserView.getCurrentUser().getPerson();
	PersonSiadapWrapper wrapper = new PersonSiadapWrapper(evaluator, new LocalDate().getYear());

	request.setAttribute("peopleToEvaluate", wrapper.getPeopleToEvaluate());

	return forward(request, "/module/siadap/prepareCreateSiadap.jsp");
    }

    public final ActionForward createNewSiadapProcess(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	Person person = getDomainObject(request, "personId");
	SiadapProcess siadapProcess = SiadapProcess.createNewProcess(person);

	return ProcessManagement.forwardToProcess(siadapProcess);
    }

    public final ActionForward manageSiadap(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) throws Exception {

	int year = new LocalDate().getYear();
	request.setAttribute("person", new PersonSiadapWrapper(UserView.getCurrentUser().getPerson(), year));
	request.setAttribute("siadaps", WorkflowProcess.getAllProcesses(SiadapProcess.class));
	request.setAttribute("configuration", SiadapYearConfiguration.getSiadapYearConfiguration(year));
	return forward(request, "/module/siadap/listSiadaps.jsp");
    }

    public final ActionForward createNewSiadapYearConfiguration(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	SiadapYearConfiguration.createNewSiadapYearConfiguration(new LocalDate().getYear());
	return manageSiadap(mapping, form, request, response);
    }

    public final ActionForward viewUnitHarmonizationData(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	int year = new LocalDate().getYear();

	Unit unit = getDomainObject(request, "unitId");
	UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, year);

	request.setAttribute("currentUnit", wrapper);

	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);

	request.setAttribute("people-withQuotas", wrapper.getUnitEmployeesWithQuotas(false));
	request.setAttribute("people-withoutQuotas", wrapper.getUnitEmployeesWithoutQuotas(false));

	List<UnitSiadapWrapper> unitSiadapEvaluations = new ArrayList<UnitSiadapWrapper>();

	for (Unit subUnit : unit.getChildUnits(configuration.getUnitRelations())) {
	    unitSiadapEvaluations.add(new UnitSiadapWrapper(subUnit, year));
	}
	request.setAttribute("subUnits", unitSiadapEvaluations);

	return forward(request, "/module/siadap/harmonization/viewUnit.jsp");
    }

    private final ActionForward listGlobalEvaluations(final HttpServletRequest request, Predicate predicate) throws Exception {

	int year = new LocalDate().getYear();

	Unit unit = getDomainObject(request, "unitId");
	UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(unit, year);

	request.setAttribute("unit", unitSiadapWrapper);
	request.setAttribute("employees", unitSiadapWrapper.getUnitEmployees(predicate));

	return forward(request, "/module/siadap/harmonization/listEvaluations.jsp");
    }

    public final ActionForward listHighGlobalEvaluations(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	request.setAttribute("label", "relevant");
	return listGlobalEvaluations(request, new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		PersonSiadapWrapper person = (PersonSiadapWrapper) arg0;
		return person.getSiadap() != null && person.isQuotaAware() && person.getSiadap().hasRelevantEvaluation();
	    }

	});

    }

    public final ActionForward listExcellencyGlobalEvaluations(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	request.setAttribute("label", "excellency");
	return listGlobalEvaluations(request, new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		PersonSiadapWrapper person = (PersonSiadapWrapper) arg0;
		return person.getSiadap() != null && person.isQuotaAware() && person.getSiadap().hasExcellencyAward();
	    }

	});
    }

    public final ActionForward terminateHarmonization(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	Unit unit = getDomainObject(request, "unitId");
	LocalDate localDate = new LocalDate();

	UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, localDate.getYear());
	try {
	    wrapper.finishHarmonization();
	    for (PersonSiadapWrapper person : wrapper.getUnitEmployees(true)) {

		Siadap siadap = person.getSiadap();
		if (siadap != null) {
		    siadap.markAsHarmonized(localDate);
		}
	    }
	} catch (DomainException e) {
	    addLocalizedMessage(request, e.getLocalizedMessage());
	}

	return viewUnitHarmonizationData(mapping, form, request, response);
    }

    public final ActionForward reOpenHarmonization(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	Unit unit = getDomainObject(request, "unitId");
	LocalDate localDate = new LocalDate();
	UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, localDate.getYear());
	try {
	    wrapper.reOpenHarmonization();

	    for (PersonSiadapWrapper person : wrapper.getUnitEmployees(true)) {

		Siadap siadap = person.getSiadap();
		if (siadap != null) {
		    siadap.removeHarmonizationMark();
		}
	    }
	} catch (DomainException e) {
	    addLocalizedMessage(request, e.getLocalizedMessage());
	}
	return viewUnitHarmonizationData(mapping, form, request, response);
    }

    public final ActionForward ccaSplashScreen(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	int year = new LocalDate().getYear();
	request.setAttribute("harmonizationUnits", SiadapYearConfiguration.getAllHarmonizationUnitsFor(year));

	return forward(request, "/module/siadap/cca/listHarmonizationUnits.jsp");
    }

    public final ActionForward validateHarmonizationData(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	int year = new LocalDate().getYear();
	Unit unit = getDomainObject(request, "unitId");

	UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(unit, year);
	request.setAttribute("unit", unitSiadapWrapper);
	request.setAttribute("employees", unitSiadapWrapper.getUnitEmployees(true));
	return forward(request, "/module/siadap/cca/validateHarmonizationUnits.jsp");
    }

}
