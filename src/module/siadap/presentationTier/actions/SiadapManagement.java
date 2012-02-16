package module.siadap.presentationTier.actions;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.ExceedingQuotaProposal;
import module.siadap.domain.ExceedingQuotaSuggestionType;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.SiadapSuggestionBean;
import module.siadap.domain.wrappers.SiadapUniverseWrapper;
import module.siadap.domain.wrappers.SiadapYearWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import module.siadap.presentationTier.renderers.providers.SiadapYearsFromExistingSiadapConfigurations;
import module.workflow.domain.WorkflowProcess;
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

    //TODO joantune: assert if this is used at all! (and what it was used for..)
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


    //    public final ActionForward createNewSiadapProcess(final ActionMapping mapping, final ActionForm form,
    //	    final HttpServletRequest request, final HttpServletResponse response) {
    //
    //	Person person = getDomainObject(request, "personId");
    //	Integer year = Integer.parseInt(request.getParameter("year"));
    //	//let's try to assert the universe by getting previous SIADAPs, if any exist,
    //	//otherwise, let's assign null here
    //	SiadapProcess siadapProcess = SiadapProcess.createNewProcess(person, year, Siadap.getLastSiadapUniverseUsedBy(person));
    //
    //	return ProcessManagement.forwardToProcess(siadapProcess);
    //    }

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
	try {

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
			personSiadapWrapper.setHarmonizationCurrentAssessments(siadapUniverseEnum);
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

	} catch (DomainException e) {
	    addLocalizedMessage(request, e.getLocalizedMessage());
	}
	RenderUtils.invalidateViewState();
	return viewUnitHarmonizationData(mapping, form, request, response);
    }

    public final ActionForward viewUnitHarmonizationData(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	int year = Integer.parseInt(request.getParameter("year"));

	Unit unit = getDomainObject(request, "unitId");
	return viewUnitHarmonizationData(mapping, form, request, response, year, unit);

    }

    private final ActionForward viewUnitHarmonizationData(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response, int year, Unit unit) {
	UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, year);

	RenderUtils.invalidateViewState();

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

	for (Unit subUnit : unit.getChildUnits(configuration.getHarmonizationUnitRelations())) {
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

    public final ActionForward removeHarmonizationAssessments(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {
	Person person = getDomainObject(request, "personId");
	Unit unit = getDomainObject(request, "unitId");
	int year = Integer.parseInt(request.getParameter("year"));
	SiadapUniverse enumToUse = SiadapUniverse.valueOf(request.getParameter("siadapUniverse"));

	PersonSiadapWrapper personWrapper = new PersonSiadapWrapper(person, year);
	UnitSiadapWrapper unitWrapper = new UnitSiadapWrapper(unit, year);

	personWrapper.removeHarmonizationAssessments(enumToUse, unitWrapper.getHarmonizationUnit());
	return viewUnitHarmonizationData(mapping, form, request, response, year, unit);

    }

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

    // NOTE: Interface with the sequential numbers thing
    //    public final ActionForward editExceedingQuotaSuggestion(final ActionMapping mapping, final ActionForm form,
    //	    final HttpServletRequest request, final HttpServletResponse response) {
    //
    //	int year = Integer.parseInt(request.getParameter("year"));
    //	Unit unit = getDomainObject(request, "unitId");
    //	UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, year);
    //
    //	//let's get the several siadapUniverseWrapper(s)
    //	List<SiadapUniverseWrapper> siadapUniverseWrappers = getRenderedObject("siadapUniverseWrappersList");
    //
    //	RenderUtils.invalidateViewState();
    //
    //	try {
    //	    for (SiadapUniverseWrapper siadapWrapper : siadapUniverseWrappers) {
    //		if (siadapWrapper != null && siadapWrapper.getSiadapUniverseForSuggestions() != null
    //			&& !siadapWrapper.getSiadapUniverseForSuggestions().isEmpty()) {
    //		    ExceedingQuotaProposal.applyGivenProposals(siadapWrapper.getSiadapUniverseForSuggestions(),
    //			    siadapWrapper.getSiadapUniverseEnum(), siadapWrapper.getSiadapUniverseForSuggestions().get(0)
    //				    .isWithinQuotasUniverse(), wrapper, year);
    //		}
    //
    //	    }
    //
    //	} catch (DomainException ex) {
    //	    addLocalizedMessage(request, ex.getLocalizedMessage());
    //	    return prepareAddExceedingQuotaSuggestion(mapping, form, request, response, year, wrapper, siadapUniverseWrappers);
    //	} catch (ConcurrentModificationException ex) {
    //	    addLocalizedMessage(request,
    //		    BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING, ex.getMessage()));
    //	}
    //
    //	return viewUnitHarmonizationData(mapping, form, request, response, year, unit);
    //    }

    public final ActionForward prepareAddExceedingQuotaSuggestion(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {
	int year = Integer.parseInt(request.getParameter("year"));
	Unit unit = getDomainObject(request, "unitId");
	UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, year);
	return prepareAddExceedingQuotaSuggestion(mapping, form, request, response, year, wrapper, null);

    }

    //    private final ActionForward prepareAddExceedingQuotaSuggestion(final ActionMapping mapping, final ActionForm form,
    //	    final HttpServletRequest request, final HttpServletResponse response, int year, UnitSiadapWrapper wrapper,
    //	    List<SiadapUniverseWrapper> listToPreserve) {
    //
    //	if (!wrapper.isHarmonizationUnit()) {
    //	    addLocalizedMessage(request, BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
    //		    "error.must.provide.a.harmonization.unit"));
    //	    RenderUtils.invalidateViewState();
    //	    return viewUnitHarmonizationData(mapping, form, request, response);
    //	}
    //
    //	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
    //
    //	List<SiadapUniverseWrapper> siadapUniverseWrappers = null;
    //	if (listToPreserve == null) {
    //	    siadapUniverseWrappers = new ArrayList<SiadapUniverseWrapper>();
    //	    siadapUniverseWrappers.add(new SiadapUniverseWrapper(wrapper
    //		    .getSiadap2AndWorkingRelationWithQuotaUniverse(Boolean.FALSE), SiadapUniverseWrapper.SIADAP2_WITH_QUOTAS,
    //		    SiadapUniverse.SIADAP2, wrapper, true));
    //	    siadapUniverseWrappers.add(new SiadapUniverseWrapper(wrapper
    //		    .getSiadap3AndWorkingRelationWithQuotaUniverse(Boolean.FALSE), SiadapUniverseWrapper.SIADAP3_WITH_QUOTAS,
    //		    SiadapUniverse.SIADAP3, wrapper, true));
    //	    siadapUniverseWrappers.add(new SiadapUniverseWrapper(wrapper
    //		    .getSiadap2AndWorkingRelationWithoutQuotaUniverse(Boolean.FALSE),
    //		    SiadapUniverseWrapper.SIADAP2_WITHOUT_QUOTAS, SiadapUniverse.SIADAP2, wrapper, false));
    //	    siadapUniverseWrappers.add(new SiadapUniverseWrapper(wrapper
    //		    .getSiadap3AndWorkingRelationWithoutQuotaUniverse(Boolean.FALSE),
    //		    SiadapUniverseWrapper.SIADAP3_WITHOUT_QUOTAS, SiadapUniverse.SIADAP3, wrapper, false));
    //
    //	} else
    //	    siadapUniverseWrappers = listToPreserve;
    //
    //	request.setAttribute("unit", wrapper);
    //	request.setAttribute("siadapUniverseWrappers", siadapUniverseWrappers);
    //
    //	return forward(request, "/module/siadap/bulkManagement/addSugestionToUnit.jsp");
    //    }

    private final ActionForward prepareAddExceedingQuotaSuggestion(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response, int year, UnitSiadapWrapper unitWrapper,
	    List<SiadapUniverseWrapper> universeWrappers) {


	List<SiadapUniverseWrapper> siadapUniverseWrappers = null;
	if (universeWrappers == null) {
	    siadapUniverseWrappers = new ArrayList<SiadapUniverseWrapper>();

	//let's get all of the quota proposals and put them in the right beans
	List<ExceedingQuotaProposal> quotaProposals = ExceedingQuotaProposal.getQuotaProposalsFor(unitWrapper.getUnit(), year);
	
	List<ExceedingQuotaProposal> siadap2WithQuotasExcellents = new ArrayList<ExceedingQuotaProposal>();
	List<ExceedingQuotaProposal> siadap2WithQuotasRelevants = new ArrayList<ExceedingQuotaProposal>();
	
	List<ExceedingQuotaProposal> siadap3WithoutQuotasExcellents = new ArrayList<ExceedingQuotaProposal>();
	List<ExceedingQuotaProposal> siadap3WithoutQuotasRelevants = new ArrayList<ExceedingQuotaProposal>();
	
	List<ExceedingQuotaProposal> siadap2WithoutQuotasExcellents = new ArrayList<ExceedingQuotaProposal>();
	List<ExceedingQuotaProposal> siadap2WithoutQuotasRelevants = new ArrayList<ExceedingQuotaProposal>();
	
	List<ExceedingQuotaProposal> siadap3WithQuotasExcellents = new ArrayList<ExceedingQuotaProposal>();
	List<ExceedingQuotaProposal> siadap3WithQuotasRelevants = new ArrayList<ExceedingQuotaProposal>();
	
	for (ExceedingQuotaProposal proposal : quotaProposals)
	{
	    switch (proposal.getSiadapUniverse()) {
	    case SIADAP2:
		if (proposal.getWithinOrganizationQuotaUniverse())
		{
		    if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION))
			    siadap2WithQuotasExcellents.add(proposal);
		    else if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.HIGH_SUGGESTION))
			siadap2WithQuotasRelevants.add(proposal);
		}
		else {
		    if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION))
			    siadap2WithoutQuotasExcellents.add(proposal);
		    else if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.HIGH_SUGGESTION))
			siadap2WithoutQuotasRelevants.add(proposal);
		}
		break;
	    case SIADAP3:
		if (proposal.getWithinOrganizationQuotaUniverse())
		{
		    if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION))
			    siadap3WithQuotasExcellents.add(proposal);
		    else if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.HIGH_SUGGESTION))
			siadap3WithQuotasRelevants.add(proposal);
		}
		else {
		    if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION))
			    siadap3WithoutQuotasExcellents.add(proposal);
		    else if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.HIGH_SUGGESTION))
			siadap3WithoutQuotasRelevants.add(proposal);
		}
		break;
	    }
	}
	
	//let's make the several universes and add them to be rendered in the page
	siadapUniverseWrappers.add(new SiadapUniverseWrapper(siadap2WithQuotasExcellents,
		SiadapUniverseWrapper.SIADAP2_WITH_QUOTAS_EXCELLENT_SUGGESTION, SiadapUniverse.SIADAP2, unitWrapper, true));

	siadapUniverseWrappers.add(new SiadapUniverseWrapper(siadap2WithQuotasRelevants,
		SiadapUniverseWrapper.SIADAP2_WITH_QUOTAS_HIGH_SUGGESTION, SiadapUniverse.SIADAP2, unitWrapper, true));

	siadapUniverseWrappers.add(new SiadapUniverseWrapper(siadap2WithoutQuotasExcellents,
		SiadapUniverseWrapper.SIADAP2_WITHOUT_QUOTAS_EXCELLENT_SUGGESTION, SiadapUniverse.SIADAP2, unitWrapper, false));

	siadapUniverseWrappers.add(new SiadapUniverseWrapper(siadap2WithoutQuotasRelevants,
		SiadapUniverseWrapper.SIADAP2_WITHOUT_QUOTAS_HIGH_SUGGESTION, SiadapUniverse.SIADAP2, unitWrapper, false));

	siadapUniverseWrappers.add(new SiadapUniverseWrapper(siadap3WithQuotasExcellents,
		SiadapUniverseWrapper.SIADAP3_WITH_QUOTAS_EXCELLENT_SUGGESTION, SiadapUniverse.SIADAP3, unitWrapper, true));

	siadapUniverseWrappers.add(new SiadapUniverseWrapper(siadap3WithQuotasRelevants,
		SiadapUniverseWrapper.SIADAP3_WITH_QUOTAS_HIGH_SUGGESTION, SiadapUniverse.SIADAP3, unitWrapper, true));

	siadapUniverseWrappers.add(new SiadapUniverseWrapper(siadap3WithoutQuotasExcellents,
		SiadapUniverseWrapper.SIADAP3_WITHOUT_QUOTAS_EXCELLENT_SUGGESTION, SiadapUniverse.SIADAP3, unitWrapper, false));

	siadapUniverseWrappers.add(new SiadapUniverseWrapper(siadap3WithoutQuotasRelevants,
		SiadapUniverseWrapper.SIADAP3_WITHOUT_QUOTAS_HIGH_SUGGESTION, SiadapUniverse.SIADAP3, unitWrapper, false));

	} else
	    siadapUniverseWrappers = universeWrappers;
	request.setAttribute("unit", unitWrapper);
	request.setAttribute("siadapUniverseWrappers", siadapUniverseWrappers);

	//now let's add an emptySuggestionBean - to make possibly to add new suggestions
	request.setAttribute("bean", new SiadapSuggestionBean(unitWrapper));

	return forward(request, "/module/siadap/bulkManagement/addSugestionToUnit.jsp");

    }

    public final ActionForward invalidateAddExceedingQuotaSuggestion(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	SiadapSuggestionBean bean = getRenderedObject("bean");
	request.setAttribute("bean", bean);

	return forward(request, "/module/siadap/bulkManagement/addSugestionToUnit.jsp");
    }

    public final ActionForward removeExceedingQuotaProposal(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {
	ExceedingQuotaProposal proposal = getDomainObject(request, "proposalId");
	Unit unit = getDomainObject(request, "unitId");
	int year = Integer.parseInt(request.getParameter("year"));
	//	List<SiadapUniverseWrapper> siadapUniverseWrapperList = getRenderedObject("siadapUniverseWrappersList");


	RenderUtils.invalidateViewState();

	proposal.remove();

	return prepareAddExceedingQuotaSuggestion(mapping, form, request, response, year, new UnitSiadapWrapper(unit, year),
 null);

    }

    public final ActionForward addExceedingQuotaSuggestion(final ActionMapping mapping, final ActionForm form,
    	    final HttpServletRequest request, final HttpServletResponse response) {
            int year = Integer.parseInt(request.getParameter("year"));
	SiadapSuggestionBean bean = getRenderedObject("bean");

	List<SiadapUniverseWrapper> siadapUniverseWrapperList = getRenderedObject("siadapUniverseWrappersList");

	RenderUtils.invalidateViewState();

    	UnitSiadapWrapper unitSiadapWrapper = bean.getUnitWrapper();
    	
	PersonSiadapWrapper personWrapper = new PersonSiadapWrapper(bean.getAutoCompletePerson(), bean.getYear());

	ExceedingQuotaProposal.createAndAppendProposal(
		personWrapper.getSiadapUniverseWhichIsBeingHarmonized(unitSiadapWrapper.getUnit()), personWrapper.isQuotaAware(),
		bean.getType(), year, unitSiadapWrapper, personWrapper.getPerson());


	return prepareAddExceedingQuotaSuggestion(mapping, form, request, response, year, unitSiadapWrapper,
		siadapUniverseWrapperList);
            
        }

    //
    //    public final ActionForward removeExcedingQuotaSuggestion(final ActionMapping mapping, final ActionForm form,
    //	    final HttpServletRequest request, final HttpServletResponse response) {
    //
    //	ExcedingQuotaProposal proposal = getDomainObject(request, "proposalId");
    //	proposal.delete();
    //
    //	return viewUnitHarmonizationData(mapping, form, request, response);
    //    }

}
