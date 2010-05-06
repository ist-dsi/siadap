package module.siadap.presentationTier.actions;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
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

	Person person = getDomainObject(request, "personId");
	SiadapProcess siadapProcess = SiadapProcess.createNewProcess(person);

	return ProcessManagement.forwardToProcess(siadapProcess);
    }

    public final ActionForward manageSiadap(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) throws Exception {

	request.setAttribute("siadaps", WorkflowProcess.getAllProcesses(SiadapProcess.class));
	request.setAttribute("configuration", SiadapYearConfiguration.getSiadapYearConfiguration(new LocalDate().getYear()));
	return forward(request, "/module/siadap/listSiadaps.jsp");
    }

    public final ActionForward createNewSiadapYearConfiguration(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	SiadapYearConfiguration.createNewSiadapYearConfiguration(new LocalDate().getYear());
	return manageSiadap(mapping, form, request, response);
    }

    public final ActionForward evaluationHarmonization(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	Person loggedPerson = UserView.getCurrentUser().getPerson();
	int year = new LocalDate().getYear();
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);

	List<UnitSiadapWrapper> unitSiadapEvaluations = new ArrayList<UnitSiadapWrapper>();

	for (Unit unit : configuration.getHarmozationUnitsFor(loggedPerson)) {
	    unitSiadapEvaluations.add(new UnitSiadapWrapper(unit, year));
	}
	request.setAttribute("harmonizationUnits", unitSiadapEvaluations);

	return forward(request, "/module/siadap/harmonization/listUnits.jsp");
    }

    public final ActionForward viewUnitHarmonizationData(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	int year = new LocalDate().getYear();
	
	Unit unit = getDomainObject(request.getParameter("unitId"));
	request.setAttribute("currentUnit", new UnitSiadapWrapper(unit,year));
	
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);

	List<PersonSiadapWrapper> peopleSiadapEvaluation = new ArrayList<PersonSiadapWrapper>();
	for (Person person : unit.getChildPersons(configuration.getWorkingRelation())) {
	    peopleSiadapEvaluation.add(new PersonSiadapWrapper(person, year));
	}
	request.setAttribute("people", peopleSiadapEvaluation);

	List<UnitSiadapWrapper> unitSiadapEvaluations = new ArrayList<UnitSiadapWrapper>();

	for (Unit subUnit : unit.getChildUnits(configuration.getUnitRelations())) {
	    unitSiadapEvaluations.add(new UnitSiadapWrapper(subUnit, year));
	}
	request.setAttribute("subUnits", unitSiadapEvaluations);

	return forward(request, "/module/siadap/harmonization/viewUnit.jsp");
    }
}
