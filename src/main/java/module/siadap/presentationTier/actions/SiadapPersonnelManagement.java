/*
 * @(#)SiadapPersonnelManagement.java
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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.activities.ChangePersonnelSituationActivityInformation;
import module.siadap.activities.NoEvaluation;
import module.siadap.activities.NoEvaluationActivityInformation;
import module.siadap.domain.CompetenceEvaluation;
import module.siadap.domain.CompetenceType;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.util.actions.SiadapUtilActions;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.SiadapYearWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import module.siadap.presentationTier.actions.SiadapPersonnelManagement.ActivityInformationBeanWrapper;
import module.siadap.presentationTier.actions.SiadapPersonnelManagement.ChangeEvaluatorBean;
import module.siadap.presentationTier.actions.SiadapPersonnelManagement.ChangeHarmonizationUnitBean;
import module.siadap.presentationTier.actions.SiadapPersonnelManagement.ChangeSiadapUniverseBean;
import module.siadap.presentationTier.actions.SiadapPersonnelManagement.ChangeWorkingUnitBean;
import module.siadap.presentationTier.actions.SiadapPersonnelManagement.ForceChangeCompetenceTypeBean;
import module.siadap.presentationTier.actions.SiadapPersonnelManagement.RemoveCustomEvaluatorBean;
import module.siadap.presentationTier.actions.SiadapPersonnelManagement.SiadapCreationBean;
import module.siadap.presentationTier.renderers.providers.SiadapYearsFromExistingSiadapConfigurations;
import module.workflow.activities.ActivityException;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.record.formula.functions.T;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.joda.time.LocalDate;

import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
import pt.ist.bennu.core.domain.exceptions.DomainException;
import pt.ist.bennu.core.presentationTier.actions.ContextBaseAction;
import pt.ist.bennu.core.util.BundleUtil;
import pt.ist.bennu.core.util.VariantBean;
import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;
import pt.utl.ist.fenix.tools.util.excel.Spreadsheet;
import pt.utl.ist.fenix.tools.util.excel.Spreadsheet.Row;

/**
 * 
 * @author João Antunes
 * @author Luis Cruz
 * @author Paulo Abrantes
 * 
 */
@Mapping(path = "/siadapPersonnelManagement")
public class SiadapPersonnelManagement extends ContextBaseAction {

    private static Logger logger = Logger.getLogger(SiadapPersonnelManagement.class.getName());

    public final ActionForward start(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {

        SiadapYearWrapper siadapYearWrapper = (SiadapYearWrapper) getRenderedObject("siadapYearWrapper");
        String yearString = getAttribute(request, "year");
        if (siadapYearWrapper == null && yearString != null) {
            siadapYearWrapper = new SiadapYearWrapper(Integer.valueOf(yearString));
        }
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

        // let's get all of the people that aren't harmonized for this year
        Set<Siadap> siadapsWithoutValidHarmonizationUnit =
                siadapYearWrapper.getSiadapYearConfiguration().getSiadapsWithoutValidHarmonizationUnit();
        if (siadapsWithoutValidHarmonizationUnit.isEmpty() == false) {
            addLocalizedWarningMessage(request, BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                    "siadapPersonnelManagement.start.warning.withoutValidHarm"));
        }

        request.setAttribute("person", new PersonSiadapWrapper(UserView.getCurrentUser().getPerson(), new LocalDate().getYear()));
        return forward(request, "/module/siadap/management/start.jsp");
    }

    public final ActionForward manageUsersWithoutValidHarmonizationUnit(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        Integer year = Integer.valueOf((String) getAttribute(request, "year"));

        SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);

        Set<Siadap> siadapsWithoutValidHarmonizationUnit = siadapYearConfiguration.getSiadapsWithoutValidHarmonizationUnit();

        request.setAttribute("siadaps", siadapsWithoutValidHarmonizationUnit);
        request.setAttribute("year", year);

        return forward(request, "/module/siadap/management/personsWithInvalidHarmonizationUnit.jsp");

    }

    public final ActionForward createNewSiadapProcess(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        int year = Integer.parseInt(request.getParameter("year"));
        SiadapCreationBean siadapCreationBean = getRenderedObject("createSiadapBean");
        Person evaluated = (Person) getDomainObject(request, "personId");

        createSiadapProcess(request, evaluated, year, siadapCreationBean.getDefaultSiadapUniverse(),
                siadapCreationBean.getCompetenceType(), false);
        return viewPerson(mapping, form, request, response);

    }

    private final SiadapProcess createSiadapProcess(HttpServletRequest request, Person evaluated, int year,
            SiadapUniverse siadapUniverse,
            CompetenceType competenceType, boolean skipUniverseCheck) {
        try {
            return SiadapProcess
                    .createNewProcess(evaluated, new Integer(year), siadapUniverse, competenceType,
                            skipUniverseCheck);
        } catch (DomainException ex) {
            addMessage(request, ex.getKey(), ex.getArgs());
        }

        return null;

    }

    public final ActionForward createNewSiadap2ProcessForCurricularPonderation(final ActionMapping mapping,
            final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        int year = Integer.parseInt(request.getParameter("year"));
        SiadapCreationBean siadapCreationBean = getRenderedObject("createSiadapBean");
        Person evaluated = (Person) getDomainObject(request, "personId");

        SiadapProcess recentlyCreatedProcess = createSiadapProcess(request, evaluated, year, SiadapUniverse.SIADAP2, siadapCreationBean.getCompetenceType(), true);

        //now, let's set it as no evaluated with the justification that the
        //process was created only to do the curricular ponderation
        WorkflowActivity<WorkflowProcess, ActivityInformation<WorkflowProcess>> noEvaluationActivity = recentlyCreatedProcess.getActivity(NoEvaluation.class.getSimpleName());
        NoEvaluationActivityInformation noEvaluationActivityInformation = new NoEvaluationActivityInformation(recentlyCreatedProcess, noEvaluationActivity);
        noEvaluationActivityInformation.setNoEvaluationJustification(BundleUtil.getStringFromResourceBundle(
                Siadap.SIADAP_BUNDLE_STRING, "siadap2.process.creation.for.curricularPonderation.noEvaluation.justification"));

        try {
            if (!noEvaluationActivityInformation.hasAllneededInfo()) {
                throw new SiadapException("noEvaluationActivityInformation.needs.more.info");
            }
            noEvaluationActivity.execute(noEvaluationActivityInformation);

        } catch (DomainException ex) {
            addLocalizedMessage(request, ex.getLocalizedMessage());
        } catch (ActivityException e) {
            addLocalizedMessage(request, e.getMessage());
        }

        return viewPerson(mapping, form, request, response);

    }

    private final ActionForward changePersonnelSituation(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response,
            ActivityInformationBeanWrapper informationBeanWrapper) throws Exception {
        int year = Integer.parseInt(request.getParameter("year"));
        Person evaluated = (Person) getDomainObject(request, "personId");
        PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(evaluated, year);
        Siadap siadap = personSiadapWrapper.getSiadap();
        // let's get the activity and the AI
        WorkflowActivity<WorkflowProcess, ActivityInformation<WorkflowProcess>> activity =
                getActivity(siadap.getProcess(), request);
        ActivityInformation activityInformation =
                new ChangePersonnelSituationActivityInformation(siadap.getProcess(), activity, informationBeanWrapper);

        try {
            if (!activityInformation.hasAllneededInfo()) {
                throw new SiadapException(((ChangePersonnelSituationActivityInformation) activityInformation).getBeanWrapper()
                        .getClass().getName()
                        + ".needs.info");
            }
            activity.execute(activityInformation);

            if (informationBeanWrapper.getSuccessWarningMessage() != null) {
                addLocalizedWarningMessage(request, informationBeanWrapper.getSuccessWarningMessage());
            }

        } catch (DomainException ex) {
            addLocalizedMessage(request, ex.getLocalizedMessage());
        } catch (ActivityException e) {
            addLocalizedMessage(request, e.getMessage());
        }

        return viewPerson(mapping, form, request, response);
    }

    public final ActionForward changeCompetenceType(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        CompetenceTypeBean competenceTypeBean = getRenderedObject("changeCompetenceTypeBean");
        return changePersonnelSituation(mapping, form, request, response, competenceTypeBean);
    }

    public final ActionForward forceChangeCompetenceType(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        ForceChangeCompetenceTypeBean competenceTypeBean = getRenderedObject("forceChangeCompetenceTypeBean");
        return changePersonnelSituation(mapping, form, request, response, competenceTypeBean);
    }

    private <T extends WorkflowProcess> WorkflowActivity<T, ActivityInformation<T>> getActivity(WorkflowProcess process,
            HttpServletRequest request) {
        String activityName = request.getParameter("activity");
        return process.getActivity(activityName);
    }

    public final ActionForward viewPerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {

        VariantBean bean = getRenderedObject("searchPerson");
        Person person = (Person) ((bean != null) ? bean.getDomainObject() : getDomainObject(request, "personId"));

        int year = Integer.parseInt(request.getParameter("year"));

        return viewPerson(request, response, person, year);

    }

    public final ActionForward viewPersonLinkAction(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        Person person = getDomainObject(request, "personId");

        int year = Integer.parseInt(request.getParameter("year"));

        return viewPerson(request, response, person, year);

    }

    protected final ActionForward viewPerson(final HttpServletRequest request, final HttpServletResponse response, Person person,
            int year) throws Exception {
        PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(person, year);

        // checking for the existence of the e-mail addresses of the
        // SiadapStructureManagementGroup users and let's warn if they don't
        // exist
        SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
        for (Person structureMngmntMember : configuration.getStructureManagementGroupMembers()) {
            String emailAddress = person.getUser().getEmail();
            if (emailAddress == null || StringUtils.isBlank(emailAddress)) {
                addMessage(request, "messageWarning", "manage.siadapStructure.person.has.no.valid.emailaddress",
                        new String[] { structureMngmntMember.getName() });
            }
        }

        Siadap siadap = personSiadapWrapper.getSiadap();
        if (siadap != null) {
            request.setAttribute("siadapProcess", siadap.getProcess());
        }
        request.setAttribute("person", personSiadapWrapper);
        request.setAttribute("bean", new VariantBean());
        request.setAttribute("changeWorkingUnit", new ChangeWorkingUnitBean());
        request.setAttribute("changeHarmonizationUnit", new ChangeHarmonizationUnitBean());
        request.setAttribute("changeEvaluator", new ChangeEvaluatorBean());
        request.setAttribute("createSiadapBean", new SiadapCreationBean(personSiadapWrapper));
        request.setAttribute("changeSiadapUniverse", new ChangeSiadapUniverseBean(person, year, false));
        request.setAttribute("forceChangeSiadapUniverse", new ChangeSiadapUniverseBean(person, year, true));
        request.setAttribute("changeCompetenceTypeBean", new CompetenceTypeBean(personSiadapWrapper));
        request.setAttribute("forceChangeCompetenceTypeBean", new ForceChangeCompetenceTypeBean(personSiadapWrapper));
        request.setAttribute("history", personSiadapWrapper.getAccountabilitiesHistory());
        return forward(request, "/module/siadap/management/editPerson.jsp");

    }

    public final ActionForward terminateUnitHarmonization(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        LocalDate now = new LocalDate();
        int year = Integer.parseInt(request.getParameter("year"));
        Unit unit = getDomainObject(request, "unitId");
        Person person = getDomainObject(request, "personId");

        new PersonSiadapWrapper(person, year).removeAndNotifyHarmonizationResponsability(unit, person, year, request);

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

        // notify the users who have access to this interface
        SiadapUtilActions.notifyAdditionOfHarmonizationResponsible(person, unitWrapper.getUnit(), year, request);

        return viewPerson(mapping, form, request, response);
    }

    public final ActionForward changeWorkingUnit(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        ChangeWorkingUnitBean bean = getRenderedObject("changeWorkingUnit");

        return changePersonnelSituation(mapping, form, request, response, bean);
    }

    public final ActionForward changeHarmonizationUnit(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        ChangeHarmonizationUnitBean bean = getRenderedObject("changeHarmonizationUnit");

        return changePersonnelSituation(mapping, form, request, response, bean);
    }

    public final ActionForward changeEvaluator(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        ChangeEvaluatorBean changeEvaluatorBean = getRenderedObject("changeEvaluator");
        return changePersonnelSituation(mapping, form, request, response, changeEvaluatorBean);
    }

    public final ActionForward changeSiadapUniverse(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        boolean forceChange = Boolean.parseBoolean(request.getParameter("force"));
        ChangeSiadapUniverseBean changeUniverseBean = null;
        if (forceChange) {
            changeUniverseBean = getRenderedObject("forceChangeSiadapUniverse");
        } else {
            changeUniverseBean = getRenderedObject("changeSiadapUniverse");
        }
        return changePersonnelSituation(mapping, form, request, response, changeUniverseBean);
    }

    public final ActionForward removeCustomEvaluator(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        return changePersonnelSituation(mapping, form, request, response, new RemoveCustomEvaluatorBean());
    }

    public final ActionForward removeFromSiadapStructure(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        int year = Integer.parseInt(request.getParameter("year"));
        Person evaluated = (Person) getDomainObject(request, "personId");

        boolean preserveResponsabilityRelations = Boolean.parseBoolean(request.getParameter("preserveResponsabilityRelations"));

        try {
            new PersonSiadapWrapper(evaluated, year).removeFromSiadapStructure(preserveResponsabilityRelations);
        } catch (DomainException ex) {
            addMessage(request, ex.getKey(), ex.getArgs());
        }

        return viewPerson(mapping, form, request, response);

    }

    public final ActionForward downloadNormalSIADAPStructure(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        SiadapRootModule siadapRootModule = SiadapRootModule.getInstance();
        int year = Integer.parseInt(((String) getAttribute(request, "year")));

        return streamSpreadsheet(response, "SIADAP_hierarquia_" + year,
                siadapRootModule.exportSIADAPHierarchy(year, false, true, false));
    }

    public final ActionForward downloadSIADAPRawData(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        SiadapRootModule siadapRootModule = SiadapRootModule.getInstance();
        int year = Integer.parseInt(((String) getAttribute(request, "year")));

        Spreadsheet siadapRawDataSpreadsheet = new Spreadsheet("SIADAP-" + year);

        siadapRawDataSpreadsheet.setHeader("istId avaliado");
        siadapRawDataSpreadsheet.setHeader("nome");
        siadapRawDataSpreadsheet.setHeader("istId avaliador");
        siadapRawDataSpreadsheet.setHeader("nome avaliador");
        siadapRawDataSpreadsheet.setHeader("unidade onde trabalha");
        siadapRawDataSpreadsheet.setHeader("unidade onde é harmonizado");
        siadapRawDataSpreadsheet.setHeader("categoria SIADAP");
        siadapRawDataSpreadsheet.setHeader("universo SIADAP");
        siadapRawDataSpreadsheet.setHeader("Conta para quotas IST");

        // let's get all of the SIADAPs
        // List<Siadap> siadaps = siadapYearConfiguration.getSiadaps();
        List<Siadap> siadaps = SiadapRootModule.getInstance().getSiadaps();
        for (Siadap siadap : siadaps) {
            if (siadap.getYear().intValue() == year) {
                Row row = siadapRawDataSpreadsheet.addRow();
                // protection against NPEs
                if (siadap.getEvaluated() == null) {
                    row.setCell("-");
                    row.setCell("-");
                } else {
                    row.setCell(siadap.getEvaluated().getUser().getUsername());
                    row.setCell(siadap.getEvaluated().getPresentationName());
                }
                if (siadap.getEvaluator() == null) {
                    row.setCell("-");
                    row.setCell("-");
                } else {
                    row.setCell(siadap.getEvaluator().getPerson().getUser().getUsername());
                    row.setCell(siadap.getEvaluator().getPerson().getPresentationName());
                }
                if (siadap.getEvaluated() == null) {
                    row.setCell("-");
                    row.setCell("-");
                    row.setCell("-");
                    row.setCell("-");
                    row.setCell("-");
                } else {
                    PersonSiadapWrapper evaluatedWrapper = new PersonSiadapWrapper(siadap.getEvaluated(), year);
                    if (evaluatedWrapper.getWorkingUnit() == null) {
                        row.setCell("-");
                    } else {
                        row.setCell(evaluatedWrapper.getWorkingUnit().getUnit().getPresentationName());
                    }
                    row.setCell(evaluatedWrapper.getSiadap() == null
                            || evaluatedWrapper.getUnitWhereIsHarmonized(evaluatedWrapper.getSiadap().getDefaultSiadapUniverse()) == null ? "-" : evaluatedWrapper
                                    .getUnitWhereIsHarmonized(evaluatedWrapper.getSiadap().getDefaultSiadapUniverse())
                                    .getPresentationName());
                    row.setCell(evaluatedWrapper.getCareerName());
                    row.setCell(String.valueOf(siadap.getDefaultSiadapUniverse()));
                    row.setCell(evaluatedWrapper.isQuotaAware() ? "Sim" : "Não");

                }
            }
        }

        return streamSpreadsheet(response, "SIADAP-" + year, siadapRawDataSpreadsheet);
    }

    public final ActionForward downloadSIADAPRawDataWithConfidentialData(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        SiadapRootModule siadapRootModule = SiadapRootModule.getInstance();
        int year = Integer.parseInt(((String) getAttribute(request, "year")));

        Spreadsheet siadapRawDataSpreadsheet = new Spreadsheet("SIADAP-" + year);

        siadapRawDataSpreadsheet.setHeader("istId avaliado");
        siadapRawDataSpreadsheet.setHeader("nome");
        siadapRawDataSpreadsheet.setHeader("istId avaliador");
        siadapRawDataSpreadsheet.setHeader("nome avaliador");
        siadapRawDataSpreadsheet.setHeader("unidade onde trabalha");
        siadapRawDataSpreadsheet.setHeader("unidade onde é harmonizado");
        siadapRawDataSpreadsheet.setHeader("categoria SIADAP");
        siadapRawDataSpreadsheet.setHeader("universo SIADAP");
        siadapRawDataSpreadsheet.setHeader("Conta para quotas IST");
        siadapRawDataSpreadsheet.setHeader("não avaliado");
        siadapRawDataSpreadsheet.setHeader("estado do processo");
        siadapRawDataSpreadsheet.setHeader("nota quantitativa");
        siadapRawDataSpreadsheet.setHeader("nota qualitativa");
        siadapRawDataSpreadsheet.setHeader("parecer harmonização");
        siadapRawDataSpreadsheet.setHeader("parecer excelente harmonização");

        // let's get all of the SIADAPs
        SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
        // List<Siadap> siadaps = siadapYearConfiguration.getSiadaps();
        List<Siadap> siadaps = SiadapRootModule.getInstance().getSiadaps();
        for (Siadap siadap : siadaps) {
            if (siadap.getYear().intValue() == year) {
                Row row = siadapRawDataSpreadsheet.addRow();

                // evaluated basic info
                Person evaluated = siadap.getEvaluated();
                String evaluatedUsername;
                String evaluatedPresentationName;
                if (evaluated != null) {
                    evaluatedUsername = evaluated.getUser().getUsername();
                    evaluatedPresentationName = evaluated.getPresentationName();
                } else {
                    evaluatedUsername = "-";
                    evaluatedPresentationName = "-";
                }

                // evaluator basic info
                Person evaluator = siadap.getEvaluator() == null ? null : siadap.getEvaluator().getPerson();
                String evaluatorUsername;
                String evaluatorPresentationName;
                if (evaluator != null) {
                    evaluatorUsername = evaluator.getUser().getUsername();
                    evaluatorPresentationName = evaluator.getPresentationName();
                } else {
                    evaluatorUsername = "-";
                    evaluatorPresentationName = "-";
                }

                row.setCell(evaluatedUsername);
                row.setCell(evaluatedPresentationName);

                row.setCell(evaluatorUsername);
                row.setCell(evaluatorPresentationName);

                PersonSiadapWrapper evaluatedWrapper = new PersonSiadapWrapper(evaluated, year);
                row.setCell(evaluatedWrapper.getWorkingUnit() == null || evaluatedWrapper.getWorkingUnit().getUnit() == null ? "-" : evaluatedWrapper
                        .getWorkingUnit().getUnit().getPresentationName());
                row.setCell(evaluatedWrapper.getSiadap() == null
                        || evaluatedWrapper.getUnitWhereIsHarmonized(evaluatedWrapper.getSiadap().getDefaultSiadapUniverse()) == null ? "-" : evaluatedWrapper
                                .getUnitWhereIsHarmonized(evaluatedWrapper.getSiadap().getDefaultSiadapUniverse()).getPresentationName());
                row.setCell(evaluatedWrapper.getCareerName());
                row.setCell(String.valueOf(siadap.getDefaultSiadapUniverse()));
                row.setCell(evaluatedWrapper.isQuotaAware() ? "Sim" : "Não");
                row.setCell(siadap.isWithSkippedEvaluation() ? "Sim" : "Não");
                row.setCell(siadap.getState().getLocalizedName());
                if (siadap.getDefaultSiadapUniverse() != null) {
                    SiadapEvaluationUniverse defaultSiadapEvaluationUniverse = siadap.getDefaultSiadapEvaluationUniverse();
                    row.setCell(defaultSiadapEvaluationUniverse.getCurrentGrade());
                    row.setCell(defaultSiadapEvaluationUniverse.getLatestSiadapGlobalEvaluationEnum().getLocalizedName());
                    row.setCell(defaultSiadapEvaluationUniverse.getHarmonizationAssessment() != null
                            && defaultSiadapEvaluationUniverse.getHarmonizationAssessment() ? "Sim" : "Não");
                    row.setCell(defaultSiadapEvaluationUniverse.getHarmonizationAssessmentForExcellencyAward() != null
                            && defaultSiadapEvaluationUniverse.getHarmonizationAssessmentForExcellencyAward() ? "Sim" : "Não");
                } else {
                    row.setCell("-");
                    row.setCell("-");
                    row.setCell("-");
                    row.setCell("-");
                }
            }
        }

        return streamSpreadsheet(response, "SIADAP-" + year + "-all-data", siadapRawDataSpreadsheet);
    }

    public final ActionForward downloadSIADAPStructureWithUniverse(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        SiadapRootModule siadapRootModule = SiadapRootModule.getInstance();
        int year = Integer.parseInt(((String) getAttribute(request, "year")));

        return streamSpreadsheet(response, "SIADAP_hierarquia_" + year,
                siadapRootModule.exportSIADAPHierarchy(year, false, true, true));

    }

    private ActionForward streamSpreadsheet(final HttpServletResponse response, final String fileName,
            final Spreadsheet resultSheet) throws IOException {
        response.setContentType("application/xls ");
        response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");

        ServletOutputStream outputStream = response.getOutputStream();
        resultSheet.exportToXLSSheet(outputStream);
        outputStream.flush();
        outputStream.close();

        return null;
    }

    private ActionForward streamSpreadsheet(final HttpServletResponse response, final String fileName,
            final HSSFWorkbook resultSheet) throws IOException {

        response.setContentType("application/xls ");
        response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");

        ServletOutputStream outputStream = response.getOutputStream();

        resultSheet.write(outputStream);
        outputStream.flush();
        outputStream.close();

        return null;
    }

    public static class RemoveCustomEvaluatorBean extends ActivityInformationBeanWrapper implements Serializable {

        @Override
        public boolean hasAllNeededInfo() {
            return true;
        }

        @Override
        public void execute(SiadapProcess process) throws SiadapException {
            new PersonSiadapWrapper(process.getSiadap().getEvaluated(), process.getSiadap().getYear()).removeCustomEvaluator();

        }

        @Override
        public String[] getArgumentsDescription(SiadapProcess process) {
            return new String[] { BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                    RemoveCustomEvaluatorBean.class.getSimpleName(), process.getSiadap().getEvaluator().getPerson()
                    .getPresentationName()) };
        }

        @Override
        public String getSuccessWarningMessage() {
            return null;
        }

    }

    public static class ChangeSiadapUniverseBean extends ActivityInformationBeanWrapper implements Serializable {
        private SiadapUniverse siadapUniverse;

        private LocalDate dateOfChange;

        private String justificationForForcingChange;

        private final boolean forceChange;

        ChangeSiadapUniverseBean(Person person, int year, boolean forceChange) {
            SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
            Siadap siadapFor = (siadapYearConfiguration == null) ? null : siadapYearConfiguration.getSiadapFor(person);
            if (siadapFor == null) {
                this.setSiadapUniverse(null);
            } else {
                this.setSiadapUniverse(siadapFor.getDefaultSiadapUniverse());
            }
            this.forceChange = forceChange;
        }

        public SiadapUniverse getSiadapUniverse() {
            return siadapUniverse;
        }

        public void setSiadapUniverse(SiadapUniverse siadapUniverse) {
            this.siadapUniverse = siadapUniverse;
        }

        @Override
        public boolean hasAllNeededInfo() {
            return ((siadapUniverse != null && dateOfChange != null) && (!forceChange || !StringUtils
                    .isBlank(justificationForForcingChange)));
        }

        public LocalDate getDateOfChange() {
            return dateOfChange;
        }

        public void setDateOfChange(LocalDate dateOfChange) {
            this.dateOfChange = dateOfChange;
        }

        @Override
        public void execute(SiadapProcess process) throws SiadapException {
            Siadap siadap = process.getSiadap();
            // extra verification
            if (forceChange && !SiadapRootModule.getInstance().getSiadapCCAGroup().isMember(UserView.getCurrentUser())) {
                throw new SiadapException("only.cca.should.be.able.to.force.change");
            }

            new PersonSiadapWrapper(siadap.getEvaluated(), siadap.getYear()).changeDefaultUniverseTo(getSiadapUniverse(),
                    getDateOfChange(), forceChange);

        }

        @Override
        public String[] getArgumentsDescription(SiadapProcess process) {
            if (!forceChange) {
                return new String[] { BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                        ChangeSiadapUniverseBean.class.getSimpleName(), getSiadapUniverse().getLocalizedName(), getDateOfChange()
                        .toString()) };
            } else {
                return new String[] { BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                        ChangeSiadapUniverseBean.class.getSimpleName() + ".forced", getSiadapUniverse().getLocalizedName(),
                        getDateOfChange().toString(), getJustificationForForcingChange()) };
            }
        }

        public String getJustificationForForcingChange() {
            return justificationForForcingChange;
        }

        public void setJustificationForForcingChange(String justificationForForcingChange) {
            this.justificationForForcingChange = justificationForForcingChange;
        }

        @Override
        public String getSuccessWarningMessage() {
            return null;
        }

    }

    public static class SiadapCreationBean implements Serializable {
        /**
         * Default serial version UID
         */
        private static final long serialVersionUID = 1L;
        private SiadapUniverse defaultSiadapUniverse;
        private CompetenceType competenceType;

        public SiadapCreationBean(PersonSiadapWrapper personWrapper) {
            setDefaultSiadapUniverse(personWrapper.getDefaultSiadapUniverse());
            setCompetenceType(personWrapper.getDefaultCompetenceTypeObject());
        }

        public CompetenceType getCompetenceType() {
            return competenceType;
        }

        public void setCompetenceType(CompetenceType competenceType) {
            this.competenceType = competenceType;
        }

        public SiadapUniverse getDefaultSiadapUniverse() {
            return defaultSiadapUniverse;
        }

        public void setDefaultSiadapUniverse(SiadapUniverse defaultSiadapUniverse) {
            this.defaultSiadapUniverse = defaultSiadapUniverse;
        }

    }

    public static class ChangeEvaluatorBean extends ActivityInformationBeanWrapper implements Serializable {
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

        @Override
        public boolean hasAllNeededInfo() {
            return evaluator != null && dateOfChange != null;
        }

        @Override
        public void execute(SiadapProcess process) throws SiadapException {
            Siadap siadap = process.getSiadap();
            new PersonSiadapWrapper(siadap.getEvaluated(), siadap.getYear()).changeEvaluatorTo(getEvaluator(), getDateOfChange());

        }

        @Override
        public String[] getArgumentsDescription(SiadapProcess process) {
            return new String[] { BundleUtil
                    .getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING, ChangeEvaluatorBean.class.getSimpleName(),
                            getEvaluator().getPresentationName(), getDateOfChange().toString()) };
        }

        @Override
        public String getSuccessWarningMessage() {
            return null;
        }
    }

    public static class CompetenceTypeBean extends ActivityInformationBeanWrapper implements Serializable {
        private CompetenceType competenceType;

        public CompetenceTypeBean(PersonSiadapWrapper personSiadapWrapper) {
            this.competenceType = personSiadapWrapper.getDefaultCompetenceTypeObject();
        }

        public CompetenceType getCompetenceType() {
            return competenceType;
        }

        public void setCompetenceType(CompetenceType competenceType) {
            this.competenceType = competenceType;
        }

        @Override
        public boolean hasAllNeededInfo() {
            return competenceType != null;
        }

        @Override
        public void execute(SiadapProcess process) throws SiadapException {
            if (process.getSiadap().getCompetences() != null
                    && process.getSiadap().getCompetences().isEmpty() == false
                    && SiadapProcessStateEnum.getState(process.getSiadap()).ordinal() > SiadapProcessStateEnum.NOT_YET_SUBMITTED_FOR_ACK
                    .ordinal()) {
                throw new SiadapException("error.changing.competence.type.cant.due.to.existing.competences.defined");
            }
            SiadapEvaluationUniverse defaultSiadapEvaluationUniverse = process.getSiadap().getDefaultSiadapEvaluationUniverse();
            defaultSiadapEvaluationUniverse.setCompetenceSlashCareerType(getCompetenceType());
            // we should also remove any existing competences (as long as they
            // have no grades associated with them)
            for (CompetenceEvaluation competenceEvaluation : defaultSiadapEvaluationUniverse.getCompetenceEvaluations()) {
                if (competenceEvaluation.getItemAutoEvaluation() != null || competenceEvaluation.getItemEvaluation() != null) {
                    throw new SiadapException("error.changing.competence.type.due.to.existing.evaluation");
                }
                competenceEvaluation.delete();
            }
        }

        @Override
        public String[] getArgumentsDescription(SiadapProcess process) {
            return new String[] { BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                    CompetenceTypeBean.class.getSimpleName(), competenceType.getName()) };
        }

        @Override
        public String getSuccessWarningMessage() {
            return null;
        }
    }

    public static class ForceChangeCompetenceTypeBean extends ActivityInformationBeanWrapper implements Serializable {
        private CompetenceType competenceType;

        public ForceChangeCompetenceTypeBean(PersonSiadapWrapper personSiadapWrapper) {
            this.competenceType = personSiadapWrapper.getDefaultCompetenceTypeObject();
        }

        public CompetenceType getCompetenceType() {
            return competenceType;
        }

        public void setCompetenceType(CompetenceType competenceType) {
            this.competenceType = competenceType;
        }

        @Override
        public boolean hasAllNeededInfo() {
            return competenceType != null;
        }

        @Override
        public void execute(SiadapProcess process) throws SiadapException {
            if (SiadapProcessStateEnum.getState(process.getSiadap()).ordinal() <= SiadapProcessStateEnum.NOT_YET_SUBMITTED_FOR_ACK
                    .ordinal()) {
                throw new SiadapException("error.changing.competence.type.use.regular.change");
            }

            process.getSiadap().getDefaultSiadapEvaluationUniverse().setCompetenceSlashCareerType(getCompetenceType());
        }

        @Override
        public String[] getArgumentsDescription(SiadapProcess process) {
            return new String[] { BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                    ForceChangeCompetenceTypeBean.class.getSimpleName(), competenceType.getName()) };
        }

        @Override
        public String getSuccessWarningMessage() {
            return null;
        }
    }

    public static class ChangeWorkingUnitBean extends ActivityInformationBeanWrapper implements Serializable {

        private Boolean withQuotas;
        private Unit unit;
        private LocalDate dateOfChange;
        private String justification;

        public ChangeWorkingUnitBean() {
            this.dateOfChange = new LocalDate();
        }

        public Unit getUnit() {
            return unit;
        }

        public void setUnit(Unit unit) {
            this.unit = unit;
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

        @Override
        public boolean hasAllNeededInfo() {
            return (getUnit() != null && getWithQuotas() != null && getDateOfChange() != null);
        }

        @Override
        public void execute(SiadapProcess process) throws SiadapException {
            new PersonSiadapWrapper(process.getSiadap().getEvaluated(), process.getSiadap().getYear()).changeWorkingUnitTo(
                    getUnit(), getWithQuotas(), getDateOfChange(), getJustification());
        }

        @Override
        public String[] getArgumentsDescription(SiadapProcess process) {
            String countsForInstitutionalQuotas =
                    (withQuotas) ? BundleUtil
                            .getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING, "siadap.true.yes") : BundleUtil
                            .getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING, "siadap.false.no");
                            PersonSiadapWrapper evaluator = new PersonSiadapWrapper(process.getSiadap()).getEvaluator();
                            String currentEvaluator = evaluator == null ? "-" : evaluator.getPerson().getPresentationName();
                            return new String[] { BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                                    ChangeWorkingUnitBean.class.getSimpleName(), unit.getPresentationName(), countsForInstitutionalQuotas,
                                    dateOfChange.toString()) };
                            // ,
                            // BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                            // "ChangeWorkingUnitBean.evaluatorSideEffect", currentEvaluator))
                            // };
        }

        @Override
        public String getSuccessWarningMessage() {
            return BundleUtil.getStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                    "warning.changed.working.unit.check.evaluator");
        }

        public String getJustification() {
            return justification;
        }

        public void setJustification(String justification) {
            this.justification = justification;
        }

    }

    public static class ChangeHarmonizationUnitBean extends ActivityInformationBeanWrapper implements Serializable {

        private Unit unit;
        private LocalDate dateOfChange;
        private String justification;

        public ChangeHarmonizationUnitBean() {
            this.dateOfChange = new LocalDate();
        }

        public Unit getUnit() {
            return unit;
        }

        public void setUnit(Unit unit) {
            this.unit = unit;
        }

        public void setDateOfChange(LocalDate dateOfChange) {
            this.dateOfChange = dateOfChange;
        }

        public LocalDate getDateOfChange() {
            return dateOfChange;
        }

        @Override
        public boolean hasAllNeededInfo() {
            return (getUnit() != null && getDateOfChange() != null);
        }

        @Override
        public void execute(SiadapProcess process) throws SiadapException {
            new PersonSiadapWrapper(process.getSiadap().getEvaluated(), process.getSiadap().getYear()).changeHarmonizationUnitTo(
                    getUnit(), getDateOfChange(), getJustification());
        }

        @Override
        public String[] getArgumentsDescription(SiadapProcess process) {
            return new String[] { BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                    ChangeHarmonizationUnitBean.class.getSimpleName(), getUnit().getPresentationName(), dateOfChange.toString()) };
            // ,
            // BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
            // "ChangeWorkingUnitBean.evaluatorSideEffect", currentEvaluator))
            // };
        }

        @Override
        public String getSuccessWarningMessage() {
            return null;
        }

        public String getJustification() {
            return justification;
        }

        public void setJustification(String justification) {
            this.justification = justification;
        }

    }

    public static abstract class ActivityInformationBeanWrapper {

        public abstract boolean hasAllNeededInfo();

        /**
         * Executes the change
         * 
         * @throws SiadapException
         *             if some kind of error was found
         */
        public abstract void execute(SiadapProcess process) throws SiadapException;

        /**
         * 
         * @return an array of strings with the arguments description
         */
        public abstract String[] getArgumentsDescription(SiadapProcess process);

        /**
         * 
         * @return a warning message to be displayed in the interface, if the
         *         activity executes successfully
         */
        public abstract String getSuccessWarningMessage();

    }

}
