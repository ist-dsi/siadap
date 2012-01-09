package module.siadap.presentationTier.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.ExcedingQuotaProposal;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.SiadapUniverseWrapper;
import module.siadap.domain.wrappers.SiadapYearWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import module.siadap.presentationTier.renderers.providers.SiadapYearsFromExistingSiadapConfigurations;
import module.workflow.domain.WorkflowProcess;
import module.workflow.presentationTier.actions.ProcessManagement;
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

@Mapping(path = "/siadapManagement")
public class SiadapManagement extends ContextBaseAction {

    public final ActionForward prepareToCreateNewSiadapProcess(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	Person evaluator = UserView.getCurrentUser().getPerson();
	int year = 0;
	if (request.getParameter("year") == null)
	    year = new LocalDate().getYear();
	else {
	    year = Integer.parseInt(request.getParameter("year"));
	}
	request.setAttribute("year", year);

	if (SiadapYearConfiguration.getSiadapYearConfiguration(year) != null) {
	    PersonSiadapWrapper wrapper = new PersonSiadapWrapper(evaluator, year);
	    request.setAttribute("peopleToEvaluate", wrapper.getPeopleToEvaluate());
	}

	return forward(request, "/module/siadap/prepareCreateSiadap.jsp");
    }

    public final ActionForward createNewSiadapProcess(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	Person person = getDomainObject(request, "personId");
	Integer year = Integer.parseInt(request.getParameter("year"));
	//let's try to assert the universe by getting previous SIADAPs, if any exist,
	//otherwise, let's assign null here
	SiadapProcess siadapProcess = SiadapProcess.createNewProcess(person, year, Siadap.getLastSiadapUniverseUsedBy(person));

	return ProcessManagement.forwardToProcess(siadapProcess);
    }

    public final ActionForward manageSiadap(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) {

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
	SiadapYearConfiguration siadapYearConfiguration = siadapYearWrapper.getSiadapYearConfiguration();
	if (siadapYearConfiguration != null) {
	    request.setAttribute("person",
		    new PersonSiadapWrapper(UserView.getCurrentUser().getPerson(), siadapYearConfiguration.getYear()));
	    request.setAttribute("siadaps", WorkflowProcess.getAllProcesses(SiadapProcess.class));
	}
	return forward(request, "/module/siadap/listSiadaps.jsp");
    }

    public final ActionForward showConfiguration(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {
	SiadapYearWrapper siadapYearWrapper = (SiadapYearWrapper) getRenderedObject("siadapYearWrapper");
	if (siadapYearWrapper == null) {
	    int year = new LocalDate().getYear();
	    siadapYearWrapper = new SiadapYearWrapper(year);
	}
	RenderUtils.invalidateViewState();
	request.setAttribute("siadapYearWrapper", siadapYearWrapper);
	SiadapYearConfiguration siadapYearConfiguration = siadapYearWrapper.getSiadapYearConfiguration();
	request.setAttribute("configuration", siadapYearConfiguration);
	request.setAttribute("addCCAMember", new VariantBean());
	request.setAttribute("addHomologationMember", new VariantBean());
	request.setAttribute("addScheduleExtenderMember", new VariantBean());
	request.setAttribute("addStructureManagementGroupMember", new VariantBean());
	return forward(request, "/module/siadap/management/configuration.jsp");
    }

    public final ActionForward addCCAMember(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) {

	SiadapYearConfiguration configuration = getDomainObject(request, "configurationId");
	VariantBean bean = getRenderedObject("ccaMember");
	configuration.addCcaMembers(((Person) bean.getDomainObject()));
	// add them also to the unique (for now TODO) group
	SiadapYearConfiguration.addCCAMember(((Person) bean.getDomainObject()).getUser());
	// TODO make the nodes access list to be updated
	RenderUtils.invalidateViewState("ccaMember");
	return showConfiguration(mapping, form, request, response);
    }

    public final ActionForward addScheduleExtenderMember(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	SiadapYearConfiguration configuration = getDomainObject(request, "configurationId");
	VariantBean bean = getRenderedObject("scheduleExtenderMember");
	configuration.addScheduleEditors(((Person) bean.getDomainObject()));
	// TODO make the nodes access list to be updated
	RenderUtils.invalidateViewState("scheduleExtenderMember");
	return showConfiguration(mapping, form, request, response);
    }

    public final ActionForward addRevertStateMember(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	SiadapYearConfiguration configuration = getDomainObject(request, "configurationId");
	VariantBean bean = getRenderedObject("scheduleExtenderMember");
	configuration.addRevertStateGroupMember(((Person) bean.getDomainObject()));
	RenderUtils.invalidateViewState("scheduleExtenderMember");
	return showConfiguration(mapping, form, request, response);
    }

    public final ActionForward addHomologationMember(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	SiadapYearConfiguration configuration = getDomainObject(request, "configurationId");
	VariantBean bean = getRenderedObject("homologationMember");
	configuration.addHomologationMembers(((Person) bean.getDomainObject()));
	// add them also to the unique (for now TODO) group
	SiadapYearConfiguration.addHomologationMember(((Person) bean.getDomainObject()).getUser());
	// TODO make the nodes access list to be updated
	RenderUtils.invalidateViewState("homologationMember");
	return showConfiguration(mapping, form, request, response);
    }

    public final ActionForward addStructureManagementMember(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	SiadapYearConfiguration configuration = getDomainObject(request, "configurationId");
	VariantBean bean = getRenderedObject("structureManagementMember");
	configuration.addStructureManagementGroupMembers(((Person) bean.getDomainObject()));
	RenderUtils.invalidateViewState("homologationMember");
	return showConfiguration(mapping, form, request, response);
    }

    public final ActionForward removeStructureManagementMember(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	SiadapYearConfiguration configuration = getDomainObject(request, "configurationId");
	Person person = getDomainObject(request, "personId");
	configuration.removeStructureManagementGroupMembers(person);
	return showConfiguration(mapping, form, request, response);
    }

    public final ActionForward removeCCAMember(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	SiadapYearConfiguration configuration = getDomainObject(request, "configurationId");
	Person person = getDomainObject(request, "personId");
	configuration.removeCcaMembers(person);
	// remove them from the persistent group as well
	SiadapYearConfiguration.removeCCAMember(person.getUser());
	return showConfiguration(mapping, form, request, response);
    }

    public final ActionForward removeSchedulerExtendersMember(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	SiadapYearConfiguration configuration = getDomainObject(request, "configurationId");
	Person person = getDomainObject(request, "personId");
	configuration.removeScheduleEditors(person);
	// remove them from the persistent group as well
	return showConfiguration(mapping, form, request, response);
    }

    public final ActionForward removeSchedulerExtenderMember(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	SiadapYearConfiguration configuration = getDomainObject(request, "configurationId");
	Person person = getDomainObject(request, "personId");
	configuration.removeScheduleEditors(person);
	return showConfiguration(mapping, form, request, response);
    }

    public final ActionForward removeRevertStateMember(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	SiadapYearConfiguration configuration = getDomainObject(request, "configurationId");
	Person person = getDomainObject(request, "personId");
	configuration.removeRevertStateGroupMember(person);
	// remove them from the persistent group as well
	return showConfiguration(mapping, form, request, response);
    }

    public final ActionForward removeHomologationMember(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	SiadapYearConfiguration configuration = getDomainObject(request, "configurationId");
	Person person = getDomainObject(request, "personId");
	configuration.removeHomologationMembers(person);
	// remove them from the persistent group as well
	SiadapYearConfiguration.removeHomologationMember(person.getUser());
	return showConfiguration(mapping, form, request, response);
    }

    public final ActionForward createNewSiadapYearConfiguration(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	Integer year = new Integer(request.getParameter("year"));

	SiadapYearConfiguration.createNewSiadapYearConfiguration(year);
	return manageSiadap(mapping, form, request, response);
    }

    public final ActionForward setUnitHarmonizationAssessmentData(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	ArrayList<SiadapUniverseWrapper> siadapUniverseWrappers = new ArrayList<SiadapUniverseWrapper>();
	//	SiadapUniverseWrapper peopleWithQuotasSiadap2 = (SiadapUniverseWrapper) getRenderedObject("people-withQuotas-SIADAP2");
	//	SiadapUniverseWrapper peopleWithQuotasSiadap3 = (SiadapUniverseWrapper) getRenderedObject("people-withQuotas-SIADAP3");
	//	SiadapUniverseWrapper peopleWithoutQuotasSiadap2 = (SiadapUniverseWrapper) getRenderedObject("people-withoutQuotas-SIADAP2");
	//	SiadapUniverseWrapper peopleWithoutQuotasSiadap3 = (SiadapUniverseWrapper) getRenderedObject("people-withoutQuotas-SIADAP3");
	siadapUniverseWrappers.add((SiadapUniverseWrapper) getRenderedObject("people-withQuotas-SIADAP2"));
	siadapUniverseWrappers.add((SiadapUniverseWrapper) getRenderedObject("people-withQuotas-SIADAP3"));
	siadapUniverseWrappers.add((SiadapUniverseWrapper) getRenderedObject("people-withoutQuotas-SIADAP2"));
	siadapUniverseWrappers.add((SiadapUniverseWrapper) getRenderedObject("people-withoutQuotas-SIADAP3"));

	for (SiadapUniverseWrapper siadapUniverseWrapper : siadapUniverseWrappers) {
	    if (siadapUniverseWrapper != null) {

		SiadapUniverse siadapUniverseEnum = siadapUniverseWrapper.getSiadapUniverseEnum();
		for (PersonSiadapWrapper personSiadapWrapper : siadapUniverseWrapper.getSiadapUniverse()) {
		    personSiadapWrapper.setHarmonizationCurrentAssessment(siadapUniverseEnum);
		}
	    }

	}

	//	if (peopleWithoutQuotasSiadap2 != null) {
	//	    setHarmonizationCurrentAssessmentFor(SiadapUniverse.SIADAP2,
	//		    (List<PersonSiadapWrapper>) getRenderedObject("people-withoutQuotas-SIADAP2id"));
	//	}
	//	if (peopleWithQuotasSiadap3 != null) {
	//	    setHarmonizationCurrentAssessmentFor(SiadapUniverse.SIADAP3,
	// peopleWithQuotasSiadap3.getSiadapUniverse());
	//	}
	//	if (peopleWithoutQuotasSiadap3 != null) {
	//	    setHarmonizationCurrentAssessmentFor(SiadapUniverse.SIADAP3,
	//		    (List<PersonSiadapWrapper>) getRenderedObject("people-withoutQuotas-SIADAP3id"));
	//	}
	//	if (peopleWithQuotasSiadap2 != null) {
	//	    setHarmonizationCurrentAssessmentFor(SiadapUniverse.SIADAP2,
	//		    (List<PersonSiadapWrapper>) getRenderedObject("people-withQuotas-SIADAP2id"));
	//	}

	RenderUtils.invalidateViewState();
	return viewUnitHarmonizationData(mapping, form, request, response);
    }

    private void setHarmonizationCurrentAssessmentFor(SiadapUniverse siadapUniverse, Set<PersonSiadapWrapper> set) {
	for (PersonSiadapWrapper personSiadapWrapper : set) {
	    personSiadapWrapper.setHarmonizationCurrentAssessment(siadapUniverse);
	}

    }

    public final ActionForward viewUnitHarmonizationData(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	int year = Integer.parseInt(request.getParameter("year"));

	Unit unit = getDomainObject(request, "unitId");
	UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, year);

	request.setAttribute("currentUnit", wrapper);

	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);

	SiadapUniverseWrapper peopleWithQuotasSIADAP2 = new SiadapUniverseWrapper(
		wrapper.getSiadap2AndWorkingRelationWithQuotaUniverse(), "siadap2WithQuotas", SiadapUniverse.SIADAP2,
		configuration.getQuotaExcellencySiadap2WithQuota(), configuration.getQuotaRelevantSiadap2WithQuota());
	SiadapUniverseWrapper peopleWithQuotasSIADAP3 = new SiadapUniverseWrapper(
		wrapper.getSiadap3AndWorkingRelationWithQuotaUniverse(), "siadap3WithQuotas", SiadapUniverse.SIADAP3,
		configuration.getQuotaExcellencySiadap3WithQuota(), configuration.getQuotaRelevantSiadap3WithQuota());
	SiadapUniverseWrapper peopleWithoutQuotasSIADAP2 = new SiadapUniverseWrapper(
		wrapper.getSiadap2AndWorkingRelationWithoutQuotaUniverse(), "siadap2WithoutQuotas", SiadapUniverse.SIADAP2,
		configuration.getQuotaExcellencySiadap2WithoutQuota(), configuration.getQuotaRelevantSiadap2WithoutQuota());
	SiadapUniverseWrapper peopleWithoutQuotasSIADAP3 = new SiadapUniverseWrapper(
		wrapper.getSiadap3AndWorkingRelationWithoutQuotaUniverse(), "siadap3WithoutQuotas", SiadapUniverse.SIADAP3,
		configuration.getQuotaExcellencySiadap3WithoutQuota(), configuration.getQuotaRelevantSiadap3WithoutQuota());

	request.setAttribute("people-withQuotas-SIADAP2", peopleWithQuotasSIADAP2);
	request.setAttribute("people-withQuotas-SIADAP3", peopleWithQuotasSIADAP3);
	request.setAttribute("people-withoutQuotas-SIADAP2", peopleWithoutQuotasSIADAP2);
	request.setAttribute("people-withoutQuotas-SIADAP3", peopleWithoutQuotasSIADAP3);
	//	request.setAttribute("people-withQuotas", wrapper.getUnitEmployeesWithQuotas(false));
	//	request.setAttribute("people-withoutQuotas", wrapper.getUnitEmployeesWithoutQuotas(false));

	List<UnitSiadapWrapper> unitSiadapEvaluations = new ArrayList<UnitSiadapWrapper>();

	for (Unit subUnit : unit.getChildUnits(configuration.getUnitRelations())) {
	    unitSiadapEvaluations.add(new UnitSiadapWrapper(subUnit, year));
	}
	request.setAttribute("subUnits", unitSiadapEvaluations);

	return forward(request, "/module/siadap/harmonization/viewUnit.jsp");
    }

    //    private final ActionForward listGlobalEvaluations(final HttpServletRequest request, Predicate predicate) {
    //
    //	int year = Integer.parseInt(request.getParameter("year"));
    //	Unit unit = getDomainObject(request, "unitId");
    //	UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(unit, year);
    //
    //	request.setAttribute("unit", unitSiadapWrapper);
    //	request.setAttribute("employees", unitSiadapWrapper.getUnitEmployees(predicate));
    //
    //	return forward(request, "/module/siadap/harmonization/listEvaluations.jsp");
    //    }
    //
    //    public final ActionForward listHighGlobalEvaluations(final ActionMapping mapping, final ActionForm form,
    //	    final HttpServletRequest request, final HttpServletResponse response) {
    //	request.setAttribute("label", "relevant");
    //	return listGlobalEvaluations(request, new Predicate() {
    //
    //	    @Override
    //	    public boolean evaluate(Object arg0) {
    //		PersonSiadapWrapper person = (PersonSiadapWrapper) arg0;
    //		return person.getSiadap() != null && person.isQuotaAware() && person.getSiadap().hasRelevantEvaluation();
    //	    }
    //
    //	});
    //
    //    }

    //    public final ActionForward listExcellencyGlobalEvaluations(final ActionMapping mapping, final ActionForm form,
    //	    final HttpServletRequest request, final HttpServletResponse response) {
    //
    //	request.setAttribute("label", "excellency");
    //	return listGlobalEvaluations(request, new Predicate() {
    //
    //	    @Override
    //	    public boolean evaluate(Object arg0) {
    //		PersonSiadapWrapper person = (PersonSiadapWrapper) arg0;
    //		return person.getSiadap() != null && person.isQuotaAware() && person.getSiadap().hasExcellencyAward();
    //	    }
    //
    //	});
    //    }

    public final ActionForward terminateHarmonization(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	Unit unit = getDomainObject(request, "unitId");
	int year = Integer.parseInt(request.getParameter("year"));

	UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, year);
	try {
	    wrapper.finishHarmonization();
	    //	    for (PersonSiadapWrapper person : wrapper.getUnitEmployees(true)) {
	    //
	    //		Siadap siadap = person.getSiadap();
	    //		if (siadap != null) {
	    //		    siadap.markAsHarmonized(localDate);
	    //		}
	    //	    }
	} catch (DomainException e) {
	    addLocalizedMessage(request, e.getLocalizedMessage());
	}

	return viewUnitHarmonizationData(mapping, form, request, response);
    }

    public final ActionForward reOpenHarmonization(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	Unit unit = getDomainObject(request, "unitId");
	int year = Integer.parseInt(request.getParameter("year"));
	UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, year);
	try {
	    wrapper.reOpenHarmonization();

	} catch (DomainException e) {
	    addLocalizedMessage(request, e.getLocalizedMessage());
	}
	return viewUnitHarmonizationData(mapping, form, request, response);
    }

    public final ActionForward manageHarmonizationUnitsForMode(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

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
	RenderUtils.invalidateViewState();

	request.setAttribute("siadapYearWrapper", siadapYearWrapper);
	request.setAttribute("harmonizationUnits",
		SiadapYearConfiguration.getAllHarmonizationUnitsFor(siadapYearWrapper.getChosenYear()));
	String mode = request.getParameter("mode");
	request.setAttribute("mode", mode);
	return forward(request, "/module/siadap/bulkManagement/listHarmonizationUnits.jsp");
    }

    public final ActionForward harmonizationData(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	int year = Integer.parseInt(request.getParameter("year"));
	Unit unit = getDomainObject(request, "unitId");
	String mode = request.getParameter("mode");

	UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(unit, year);
	request.setAttribute("unit", unitSiadapWrapper);
	request.setAttribute("employees", unitSiadapWrapper.getUnitEmployees(true));
	request.setAttribute("mode", mode);
	return forward(request, "/module/siadap/bulkManagement/operateOverHarmonizationUnits.jsp");
    }

    public final ActionForward prepareAddExcedingQuotaSuggestion(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	Unit unit = getDomainObject(request, "unitId");
	SiadapSuggestionBean bean = new SiadapSuggestionBean();
	bean.setUnit(unit);
	bean.setYear(Integer.parseInt(request.getParameter("year")));

	request.setAttribute("bean", bean);
	return forward(request, "/module/siadap/bulkManagement/addSuggestionToUnit.jsp");
    }

    public final ActionForward addExcedingQuotaSuggestion(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	int year = Integer.parseInt(request.getParameter("year"));
	SiadapSuggestionBean bean = getRenderedObject("bean");
	Unit unit = bean.getUnit();

	UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(unit, year);
	unitSiadapWrapper.addExcedingQuotaProposalSuggestion(bean.getPerson(), bean.getType());

	request.setAttribute("unit", unitSiadapWrapper);
	request.setAttribute("employees", unitSiadapWrapper.getUnitEmployees(true));

	return viewUnitHarmonizationData(mapping, form, request, response);
    }

    public final ActionForward removeExcedingQuotaSuggestion(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	ExcedingQuotaProposal proposal = getDomainObject(request, "proposalId");
	proposal.delete();

	return viewUnitHarmonizationData(mapping, form, request, response);
    }

}
