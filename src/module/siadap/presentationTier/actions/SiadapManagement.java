/*
 * @(#)SiadapManagement.java
 *
 * Copyright 2010 Instituto Superior Tecnico
 * Founding Authors: Paulo Abrantes
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the SIADAP Module.
 *
 *   The SIADAP Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The SIADAP Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the SIADAP Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.siadap.presentationTier.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.activities.Homologate;
import module.siadap.activities.HomologationActivityInformation;
import module.siadap.activities.ValidationActivityInformation.ValidationSubActivity;
import module.siadap.domain.ExceedingQuotaProposal;
import module.siadap.domain.ExceedingQuotaSuggestionType;
import module.siadap.domain.HomologationDocumentFile;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.exceptions.ValidationTerminationException;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.SiadapSuggestionBean;
import module.siadap.domain.wrappers.SiadapUniverseWrapper;
import module.siadap.domain.wrappers.SiadapUniverseWrapper.UniverseDisplayMode;
import module.siadap.domain.wrappers.SiadapYearWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import module.siadap.presentationTier.renderers.providers.SiadapYearsFromExistingSiadapConfigurations;
import module.workflow.activities.ActivityException;
import module.workflow.domain.WorkflowProcess;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.exceptions.DomainException;
import myorg.presentationTier.actions.ContextBaseAction;
import myorg.util.BundleUtil;
import myorg.util.VariantBean;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;

@Mapping(path = "/siadapManagement")
/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
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

    public final ActionForward applyValidationData(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {
	int year = Integer.parseInt(request.getParameter("year"));
	Unit unit = getDomainObject(request, "unitId");

	List<SiadapUniverseWrapper> siadapUniverseWrapperList = getRenderedObject("siadapUniverseWrappersList");

	RenderUtils.invalidateViewState();

	return executeValidation(mapping, form, request, response, siadapUniverseWrapperList,
		ValidationSubActivity.SET_VALIDATION_DATA, new UnitSiadapWrapper(unit, year));

    }

    private final ActionForward executeValidation(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response,
	    Collection<SiadapUniverseWrapper> siadapUniverseWrappers, ValidationSubActivity validationSubActivity,
	    UnitSiadapWrapper unitWrapper) {

	ArrayList<SiadapException> warningMessages = new ArrayList<SiadapException>();
	try {
	    warningMessages = unitWrapper.executeValidation(siadapUniverseWrappers, validationSubActivity);

	} catch (ValidationTerminationException ex) {
	    addLocalizedMessage(request, ex.getLocalizedMessage());
	    return validateUnit(mapping, form, request, response, unitWrapper, null);
	} catch (DomainException ex) {
	    addLocalizedMessage(request, ex.getLocalizedMessage());
	    return validateUnit(mapping, form, request, response, unitWrapper, siadapUniverseWrappers);
	} catch (ActivityException ex) {
	    addLocalizedMessage(request, ex.getMessage());
	    return validateUnit(mapping, form, request, response, unitWrapper, siadapUniverseWrappers);
	}

	for (SiadapException warningMessage : warningMessages) {
	    addLocalizedWarningMessage(request, warningMessage.getLocalizedMessage());
	}

	return validateUnit(mapping, form, request, response, unitWrapper, null);

    }

    public final ActionForward validate(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) {

	Integer yearInteger = request.getParameter("year") == null ? null : Integer.parseInt(request.getParameter("year"));
	SiadapYearWrapper siadapYearWrapper = (SiadapYearWrapper) getRenderedObject("siadapYearWrapper");
	if (siadapYearWrapper == null && yearInteger == null) {
	    ArrayList<Integer> yearsWithConfigs = SiadapYearsFromExistingSiadapConfigurations.getYearsWithExistingConfigs();
	    if (yearsWithConfigs.contains(new Integer(new LocalDate().getYear()))) {
		int year = new LocalDate().getYear();
		siadapYearWrapper = new SiadapYearWrapper(year);
	    } else {
		siadapYearWrapper = new SiadapYearWrapper(yearsWithConfigs.get(yearsWithConfigs.size() - 1));
	    }
	} else if (yearInteger != null) {
	    siadapYearWrapper = new SiadapYearWrapper(yearInteger);
	}
	Unit unit = (Unit) (getDomainObject(request, "unitId") == null ? siadapYearWrapper.getSiadapYearConfiguration()
		.getSiadapStructureTopUnit() : getDomainObject(request, "unitId"));

	RenderUtils.invalidateViewState();

	return validateUnit(mapping, form, request, response, new UnitSiadapWrapper(unit, siadapYearWrapper.getChosenYear()),
		null);

    }

    public final ActionForward validateUnit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response, UnitSiadapWrapper unitWrapper,
	    Collection<SiadapUniverseWrapper> siadapUniverseWrappers) {

	request.setAttribute("siadapYearWrapper", new SiadapYearWrapper(unitWrapper.getYear()));

	//	if (unitWrapper == null) {
	//	    Unit unit = (Unit) (getDomainObject(request, "unitId") == null ? unitWrapper.getConfiguration()
	//		    .getSiadapStructureTopUnit() : getDomainObject(request, "unitId"));
	//	    unitWrapper = new Siadap
	//	}

	request.setAttribute("unit", unitWrapper);
	if (siadapUniverseWrappers == null)
	    request.setAttribute("siadapUniverseWrappers", unitWrapper.getValidationUniverseWrappers());
	else {
	    request.setAttribute("siadapUniverseWrappers", siadapUniverseWrappers);
	}

	return forward(request, "/module/siadap/validation/validation.jsp");
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

	UniverseDisplayMode displayMode = UniverseDisplayMode.HARMONIZATION;
	SiadapUniverseWrapper peopleWithQuotasSIADAP2 = new SiadapUniverseWrapper(
		wrapper.getSiadap2AndWorkingRelationWithQuotaUniverse(), "siadap2WithQuotas", SiadapUniverse.SIADAP2,
		configuration.getQuotaExcellencySiadap2WithQuota(), configuration.getQuotaRelevantSiadap2WithQuota(),
		displayMode, null, null);
	SiadapUniverseWrapper peopleWithQuotasSIADAP3 = new SiadapUniverseWrapper(
		wrapper.getSiadap3AndWorkingRelationWithQuotaUniverse(), "siadap3WithQuotas", SiadapUniverse.SIADAP3,
		configuration.getQuotaExcellencySiadap3WithQuota(), configuration.getQuotaRelevantSiadap3WithQuota(),
		displayMode, null, null);
	SiadapUniverseWrapper peopleWithoutQuotasSIADAP2 = new SiadapUniverseWrapper(
		wrapper.getSiadap2AndWorkingRelationWithoutQuotaUniverse(), "siadap2WithoutQuotas", SiadapUniverse.SIADAP2,
		configuration.getQuotaExcellencySiadap2WithoutQuota(), configuration.getQuotaRelevantSiadap2WithoutQuota(),
		displayMode, null, null);
	SiadapUniverseWrapper peopleWithoutQuotasSIADAP3 = new SiadapUniverseWrapper(
		wrapper.getSiadap3AndWorkingRelationWithoutQuotaUniverse(), "siadap3WithoutQuotas", SiadapUniverse.SIADAP3,
		configuration.getQuotaExcellencySiadap3WithoutQuota(), configuration.getQuotaRelevantSiadap3WithoutQuota(),
		displayMode, null, null);

	request.setAttribute("people-withQuotas-SIADAP2", peopleWithQuotasSIADAP2);
	request.setAttribute("people-withQuotas-SIADAP3", peopleWithQuotasSIADAP3);
	request.setAttribute("people-withoutQuotas-SIADAP2", peopleWithoutQuotasSIADAP2);
	request.setAttribute("people-withoutQuotas-SIADAP3", peopleWithoutQuotasSIADAP3);
	//	request.setAttribute("people-withQuotas", wrapper.getUnitEmployeesWithQuotas(false));
	//	request.setAttribute("people-withoutQuotas", wrapper.getUnitEmployeesWithoutQuotas(false));

	request.setAttribute("subUnits", wrapper.getSubHarmonizationUnits());

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

    public final ActionForward terminateValidation(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {
	int year = Integer.parseInt(request.getParameter("year"));

	SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	UnitSiadapWrapper unitWrapper = new UnitSiadapWrapper(siadapYearConfiguration.getSiadapStructureTopUnit(), year);

	return executeValidation(mapping, form, request, response, unitWrapper.getAllUniverseWrappersOfAllPeopleInSubUnits(),
		ValidationSubActivity.TERMINATE_VALIDATION, unitWrapper);

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
	    if (yearsWithConfigs.contains(new Integer(new LocalDate().getYear() - 1))) {
		int year = new LocalDate().getYear() - 1;
		siadapYearWrapper = new SiadapYearWrapper(year);
	    } else {
		if (yearsWithConfigs.contains(new Integer(new LocalDate().getYear()))) {
		    int year = new LocalDate().getYear();
		    siadapYearWrapper = new SiadapYearWrapper(year);
		} else {
		    siadapYearWrapper = new SiadapYearWrapper(yearsWithConfigs.get(yearsWithConfigs.size() - 1));
		}
	    }
	}
	RenderUtils.invalidateViewState();

	request.setAttribute("siadapYearWrapper", siadapYearWrapper);
	request.setAttribute("harmonizationUnits",
		SiadapYearConfiguration.getAllHarmonizationUnitsExceptSpecialUnit(siadapYearWrapper.getChosenYear()));
	//	String mode = request.getParameter("mode");
	//	if (mode == null) {
	//	    mode = (String) request.getAttribute("mode");
	//	}
	//	request.setAttribute("mode", mode);
	return forward(request, "/module/siadap/bulkManagement/listHarmonizationUnits.jsp");
    }

    public final ActionForward batchHomologation(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	HomologationActivityInformation homologateInfo = null;
	int homologationCount = 0;

	ArrayList<SiadapException> warningMessages = new ArrayList<SiadapException>();

	try {
	    if (!SiadapYearConfiguration.getHomologationMembersGroup().isMember(UserView.getCurrentUser())) {
		throw new SiadapException("error.onlyCCA.can.homologate");
	    }

	    Homologate homologateActivity = (Homologate) SiadapProcess.getActivityStaticly(Homologate.class.getSimpleName());
	    List<PersonSiadapWrapper> employees = getRenderedObject("employees");
	    for (PersonSiadapWrapper personWrapper : employees) {
		if (personWrapper.isSelectedForHomologation()) {
		    if (!homologateActivity.isActive(personWrapper.getSiadap().getProcess())) {
			warningMessages.add(new SiadapException("error.couldnt.batch.homologate.for.proccess", personWrapper
				.getSiadap().getProcess().getProcessNumber()));
		    } else {
			homologateInfo = new HomologationActivityInformation(personWrapper.getSiadap().getProcess(),
				homologateActivity);
			homologateInfo.setShouldShowChangeGradeInterface(false);
			homologateActivity.execute(homologateInfo);
			homologationCount++;
		    }
		}
	    }
	} catch (SiadapException ex) {
	    warningMessages.add(ex);
	} catch (DomainException ex) {
	    addLocalizedMessage(request, ex.getLocalizedMessage());
	} catch (ActivityException ex) {
	    addLocalizedMessage(request, ex.getMessage());
	}
	for (SiadapException warningMessage : warningMessages) {
	    addLocalizedWarningMessage(request, warningMessage.getLocalizedMessage());
	}

	if (homologationCount != 0) {
	    addLocalizedSuccessMessage(request, BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
		    "label.homologation.success", String.valueOf(homologationCount)));

	}
	request.setAttribute("mode", "homologationDone");
	return manageHarmonizationUnitsForMode(mapping, form, request, response);
    }

    public final ActionForward viewPendingHomologationProcesses(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	int year = Integer.parseInt(request.getParameter("year"));
	Unit unit = getDomainObject(request, "unitId");

	UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(unit, year);
	request.setAttribute("unit", unitSiadapWrapper);
	request.setAttribute("employees", unitSiadapWrapper.getUnitEmployeesWithProcessesPendingHomologation());
	return forward(request, "/module/siadap/bulkManagement/viewPendingHomologationProcesses.jsp");
    }

    public final ActionForward viewOnlyProcesses(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	int year = Integer.parseInt(request.getParameter("year"));
	String mode = request.getParameter("mode");
	Unit unit = getDomainObject(request, "unitId");

	UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(unit, year);
	request.setAttribute("unit", unitSiadapWrapper);
	if (StringUtils.equals(mode, "viewHomologatedProcesses")) {
	    request.setAttribute("employees", unitSiadapWrapper.getUnitEmployeesWithProcessesHomologated());

	} else if (StringUtils.equals(mode, "viewReviewCommission")) {
	    request.setAttribute("employees", unitSiadapWrapper.getUnitEmployeesWithProcessesInReviewCommission());

	}
 else if (StringUtils.equals(mode, "viewOngoingProcesses")) {
	    request.setAttribute("employees", unitSiadapWrapper.getUnitEmployeesWithOngoingProcesses());

	}
	request.setAttribute("mode", mode);
	return forward(request, "/module/siadap/bulkManagement/viewOnlyProcesses.jsp");
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
	    List<ExceedingQuotaProposal> quotaProposals = ExceedingQuotaProposal
		    .getQuotaProposalsFor(unitWrapper.getUnit(), year);

	    Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap2WithQuotas = new HashMap<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>>();

	    Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap3WithQuotas = new HashMap<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>>();

	    Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap3WithoutQuotas = new HashMap<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>>();

	    Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap2WithoutQuotas = new HashMap<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>>();

	    ExceedingQuotaProposal.organizeAndFillExceedingQuotaProposals(unitWrapper.getUnit(), year, siadap2WithQuotas,
		    siadap3WithoutQuotas, siadap2WithoutQuotas, siadap3WithQuotas);

	    //TODO (?) REFACTOR: joantune - this can be remade to use the maps that are used for the validation, it would be cleaner 
	    //let's make the several universes and add them to be rendered in the page
	    siadapUniverseWrappers.add(new SiadapUniverseWrapper(siadap2WithQuotas
		    .get(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION),
		    SiadapUniverseWrapper.SIADAP2_WITH_QUOTAS_EXCELLENT_SUGGESTION, SiadapUniverse.SIADAP2, unitWrapper, true));

	    siadapUniverseWrappers.add(new SiadapUniverseWrapper(siadap2WithQuotas
		    .get(ExceedingQuotaSuggestionType.HIGH_SUGGESTION),
		    SiadapUniverseWrapper.SIADAP2_WITH_QUOTAS_HIGH_SUGGESTION, SiadapUniverse.SIADAP2, unitWrapper, true));

	    siadapUniverseWrappers
		    .add(new SiadapUniverseWrapper(siadap2WithoutQuotas.get(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION),
			    SiadapUniverseWrapper.SIADAP2_WITHOUT_QUOTAS_EXCELLENT_SUGGESTION, SiadapUniverse.SIADAP2,
			    unitWrapper, false));

	    siadapUniverseWrappers.add(new SiadapUniverseWrapper(siadap2WithoutQuotas
		    .get(ExceedingQuotaSuggestionType.HIGH_SUGGESTION),
		    SiadapUniverseWrapper.SIADAP2_WITHOUT_QUOTAS_HIGH_SUGGESTION, SiadapUniverse.SIADAP2, unitWrapper, false));

	    siadapUniverseWrappers.add(new SiadapUniverseWrapper(siadap3WithQuotas
		    .get(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION),
		    SiadapUniverseWrapper.SIADAP3_WITH_QUOTAS_EXCELLENT_SUGGESTION, SiadapUniverse.SIADAP3, unitWrapper, true));

	    siadapUniverseWrappers.add(new SiadapUniverseWrapper(siadap3WithQuotas
		    .get(ExceedingQuotaSuggestionType.HIGH_SUGGESTION),
		    SiadapUniverseWrapper.SIADAP3_WITH_QUOTAS_HIGH_SUGGESTION, SiadapUniverse.SIADAP3, unitWrapper, true));

	    siadapUniverseWrappers
		    .add(new SiadapUniverseWrapper(siadap3WithoutQuotas.get(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION),
			    SiadapUniverseWrapper.SIADAP3_WITHOUT_QUOTAS_EXCELLENT_SUGGESTION, SiadapUniverse.SIADAP3,
			    unitWrapper, false));

	    siadapUniverseWrappers.add(new SiadapUniverseWrapper(siadap3WithoutQuotas
		    .get(ExceedingQuotaSuggestionType.HIGH_SUGGESTION),
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

	return prepareAddExceedingQuotaSuggestion(mapping, form, request, response, year, new UnitSiadapWrapper(unit, year), null);

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

    public final ActionForward downloadAndGenerateSiadapDocument(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	SiadapProcess process = getDomainObject(request, "processId");

	PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(process.getSiadap().getEvaluated(), process.getSiadap()
		.getYear());

	byte[] byteArray = HomologationDocumentFile.generateHomologationDocument(personSiadapWrapper,
		BundleUtil.getStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING, "SiadapProcessDocument.motive.userOrder"));
	return download(response, "SIADAP_" + process.getProcessNumber() + ".pdf", byteArray, "application/pdf");

    }
}
