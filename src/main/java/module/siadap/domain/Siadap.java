/*
 * @(#)Siadap.java
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
package module.siadap.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import jvstm.cps.ConsistencyPredicate;
import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.siadap.domain.util.SiadapMiscUtilClass;
import module.siadap.domain.util.SiadapPendingProcessesCounter;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.webserviceutils.client.JerseyRemoteUser;
import module.workflow.domain.utils.WorkflowCommentCounter;
import module.workflow.widgets.ProcessListWidget;
import module.workflow.widgets.UnreadCommentsWidget;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.dml.runtime.RelationAdapter;

import com.google.common.collect.Iterators;

/**
 * 
 * @author João Antunes
 * @author Luis Cruz
 * @author Paulo Abrantes
 * 
 */
public class Siadap extends Siadap_Base {

    public static class SiadapSiadapYearConfigurationListener extends RelationAdapter<Siadap, SiadapYearConfiguration> {
        @Override
        public void afterAdd(Siadap siadap, SiadapYearConfiguration configuration) {
            if (configuration == null) { //then we are not adding, we are removing
                return;
            }
            if (siadap.CheckOnlyOneSiadapForEachYear() == false) {
                throw new SiadapException("error.user.with.proccess.already.created");
            }
        }

    }

    static {
        Siadap.getRelationSiadapYearConfigurationSiadap().addListener(new SiadapSiadapYearConfigurationListener());
    }

    //@ConsistencyPredicate - in the future
    protected boolean CheckOnlyOneSiadapForEachYear() {
        final Person evaluated = getEvaluated();
        final Integer year = getYear();
        SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
        final Siadap siadap = this;
        return !Iterators.any(siadapYearConfiguration.getSiadaps().iterator(), new com.google.common.base.Predicate<Siadap>() {

            @Override
            public boolean apply(@Nullable Siadap input) {
                //any siadap for this person already
                if (input == null || input == siadap) {
                    return false;
                }
                if (input.getEvaluated().equals(evaluated) && input.getYear().equals(year)) {
                    return true;
                }
                return false;

            }
        });
    }

    public static final String SIADAP_BUNDLE_STRING = "resources/SiadapResources";

    public static final Comparator<Siadap> COMPARATOR_BY_EVALUATED_PRESENTATION_NAME_FALLBACK_YEAR_THEN_OID =
            new Comparator<Siadap>() {

                @Override
                public int compare(Siadap o1, Siadap o2) {

                    int presentationNameComparison = 0;
                    if (o1 == null || o2 == null) {
                        if (o2 == null && o1 == null) {
                            return 0;
                        }
                        if (o2 == null) {
                            return 1;
                        }
                        if (o1 == null) {
                            return -1;
                        }
                    }
                    if (o1.getEvaluated() != null && o2.getEvaluated() != null) {
                        presentationNameComparison =
                                o1.getEvaluated().getPresentationName().compareTo(o2.getEvaluated().getPresentationName());
                    }
                    int yearComparison = o1.getYear().compareTo(o2.getYear());
                    return presentationNameComparison == 0 ? (yearComparison == 0 ? o1.getExternalId().compareTo(
                            o2.getExternalId()) : yearComparison) : presentationNameComparison;
                }
            };

    // register itself in the pending processes widget:
    static {
        ProcessListWidget.register(new SiadapPendingProcessesCounter());
        UnreadCommentsWidget.register(new WorkflowCommentCounter(SiadapProcess.class));
    }

    public static final int MINIMUM_EFICIENCY_OBJECTIVES_NUMBER = 1;
    public static final int MINIMUM_PERFORMANCE_OBJECTIVES_NUMBER = 1;
    public static final int MINIMUM_QUALITY_OBJECTIVES_NUMBER = 1;

    public static final int MINIMUM_COMPETENCES_WITH_OBJ_EVAL_NUMBER = 5;

    public static final int MINIMUM_COMPETENCES_WITHOUT_OBJ_EVAL_NUMBER = 8;

    public Siadap(int year, Person evaluated, SiadapUniverse siadapUniverse, CompetenceType competenceType) {
        super();
        setYear(year);
        setEvaluated(evaluated);
        setSiadapRootModule(SiadapRootModule.getInstance());
        SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).addSiadaps(this);
        SiadapEvaluationUniverse siadapEvaluationUniverse =
                new SiadapEvaluationUniverse(this, siadapUniverse, competenceType, true);
    }

    @ConsistencyPredicate
    public boolean hasSiadapYearConfigurationObject() {
        return getSiadapYearConfiguration() != null;
    }

    @ConsistencyPredicate
    public boolean hasSiadapRootModuleObject() {
        return getSiadapRootModule() != null;
    }

    public SiadapProcessStateEnum getState() {
        if (getNulled() == Boolean.TRUE) {
            return SiadapProcessStateEnum.NULLED;
        } else if (isWithSkippedEvaluation()) {
            return SiadapProcessStateEnum.EVALUATION_NOT_GOING_TO_BE_DONE;
        } else if (!isWithObjectivesFilled()) {
            return SiadapProcessStateEnum.INCOMPLETE_OBJ_OR_COMP;
        } else if (!hasSealedObjectivesAndCompetences()) {
            return SiadapProcessStateEnum.NOT_SEALED;
        } else if (getRequestedAcknowledgeDate() == null) {
            return SiadapProcessStateEnum.NOT_YET_SUBMITTED_FOR_ACK;
        } else if (!isEvaluatedWithKnowledgeOfObjectives()) {
            return SiadapProcessStateEnum.WAITING_EVAL_OBJ_ACK;
        } else if (!isAutoEvaliationDone() && !isDefaultEvaluationDone()) {
            return SiadapProcessStateEnum.WAITING_SELF_EVALUATION;
        } else if (!isDefaultEvaluationDone()) {
            return SiadapProcessStateEnum.NOT_YET_EVALUATED;
        } else if (getHarmonizationDate() == null) {
            return SiadapProcessStateEnum.WAITING_HARMONIZATION;
        } else if (getValidationDateOfDefaultEvaluation() == null) {
            return SiadapProcessStateEnum.WAITING_VALIDATION;
        } else if (getRequestedAcknowledegeValidationDate() == null && !getForcedReadinessToHomologation()) {
            return SiadapProcessStateEnum.WAITING_SUBMITTAL_BY_EVALUATOR_AFTER_VALIDATION;
        } else if (getAcknowledgeValidationDate() == null && !getForcedReadinessToHomologation()) {
            return SiadapProcessStateEnum.WAITING_VALIDATION_ACKNOWLEDGMENT_BY_EVALUATED;
        } else if (getHomologationDate() == null) {
            if (getAssignedToReviewCommissionDate() == null) {
                if (isDuringReviewCommissionWaitingPeriod()) {
                    return SiadapProcessStateEnum.VALIDATION_ACKNOWLEDGED;
                } else {
                    return SiadapProcessStateEnum.WAITING_HOMOLOGATION;
                }
            } else {
                return SiadapProcessStateEnum.WAITING_FOR_REVIEW_COMMISSION;
            }
        } else if (getAcknowledgeHomologationDate() == null) {
            return SiadapProcessStateEnum.HOMOLOGATED;
        } else if (getAcknowledgeHomologationDate() != null) {
            return SiadapProcessStateEnum.FINAL_STATE;
        }
        return SiadapProcessStateEnum.UNIMPLEMENTED_STATE;
    }

    private boolean isDuringReviewCommissionWaitingPeriod() {
        if (getAcknowledgeValidationDate() == null) {
            return false;
        }

        if (getForcedReadinessToHomologation()) {
            return false;
        }

        LocalDate limitDate =
                getAcknowledgeValidationDate().plusDays(getSiadapYearConfiguration().getReviewCommissionWaitingPeriod());
        LocalDate today = new LocalDate();
        return !today.isAfter(limitDate);
    }

    public boolean isOngoing() {
        return getState().equals(SiadapProcessStateEnum.EVALUATION_NOT_GOING_TO_BE_DONE)
                || getState().equals(SiadapProcessStateEnum.INCOMPLETE_OBJ_OR_COMP)
                || getState().equals(SiadapProcessStateEnum.NOT_SEALED)
                || getState().equals(SiadapProcessStateEnum.NOT_YET_SUBMITTED_FOR_ACK)
                || getState().equals(SiadapProcessStateEnum.WAITING_EVAL_OBJ_ACK)
                || getState().equals(SiadapProcessStateEnum.WAITING_SELF_EVALUATION)
                || getState().equals(SiadapProcessStateEnum.NOT_YET_EVALUATED)
                || getState().equals(SiadapProcessStateEnum.WAITING_HARMONIZATION)
                || getState().equals(SiadapProcessStateEnum.WAITING_VALIDATION)
                || getState().equals(SiadapProcessStateEnum.WAITING_SUBMITTAL_BY_EVALUATOR_AFTER_VALIDATION)
                || getState().equals(SiadapProcessStateEnum.WAITING_VALIDATION_ACKNOWLEDGMENT_BY_EVALUATED)
                || getState().equals(SiadapProcessStateEnum.VALIDATION_ACKNOWLEDGED);
    }

    public boolean isWaitingForReviewCommission() {
        return getState().equals(SiadapProcessStateEnum.WAITING_FOR_REVIEW_COMMISSION);
    }

    public boolean isWaitingHomologation() {
        return getState().equals(SiadapProcessStateEnum.WAITING_HOMOLOGATION);
    }

    public SiadapUniverse getDefaultSiadapUniverse() {
        SiadapEvaluationUniverse defaultSiadapEvaluationUniverse = getDefaultSiadapEvaluationUniverse();
        if (defaultSiadapEvaluationUniverse == null) {
            return null;
        }
        return defaultSiadapEvaluationUniverse.getSiadapUniverse();

    }

    /**
     * 
     * @return the HarmonizationDate of the default SiadapEvaluationUniverse
     */
    public LocalDate getHarmonizationDate() {
        return getDefaultSiadapEvaluationUniverse().getHarmonizationDate();
    }

    public SiadapEvaluationUniverse getDefaultSiadapEvaluationUniverse() {
        for (SiadapEvaluationUniverse evaluationUniverse : getSiadapEvaluationUniverses()) {
            final Boolean defaultEvaluationUniverse = evaluationUniverse.getDefaultEvaluationUniverse();
            if (defaultEvaluationUniverse != null && defaultEvaluationUniverse.booleanValue()) {
                return evaluationUniverse;
            }
        }
        return null;
    }

    // public List<CompetenceEvaluation> getCompetenceEvaluations() {
    // return getEvaluations(CompetenceEvaluation.class, null, null);
    // }

    public List<CompetenceEvaluation> getCompetenceEvaluations() {
        return getDefaultSiadapEvaluationUniverse().getCompetenceEvaluations();
    }

    public boolean isAutoEvaliationDone() {
        return getAutoEvaluationSealedDate() != null;
    }

    public boolean isDefaultEvaluationDone() {
        return getEvaluationSealedDate() != null;
    }

    public LocalDate getValidationDateOfDefaultEvaluation() {
        SiadapEvaluationUniverse defaultSiadapEvaluationUniverse = getDefaultSiadapEvaluationUniverse();
        if (defaultSiadapEvaluationUniverse == null) {
            return null;
        }
        return defaultSiadapEvaluationUniverse.getValidationDate();
    }

    public boolean isEvaluationDone(SiadapUniverse siadapUniverse) {
        SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse =
                getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
        return siadapEvaluationUniverseForSiadapUniverse.isEvaluationDone();
    }

    // public BigDecimal getPonderatedObjectivesScoring() {
    // return getPonderationResult(getObjectivesScoring(),
    // getObjectivesPonderation());
    // }
    //
    // public BigDecimal getCompetencesScoring() {
    // return getEvaluationScoring(getCompetenceEvaluations());
    // }
    //
    // public BigDecimal getPonderatedCompetencesScoring() {
    // return getPonderationResult(getCompetencesScoring(),
    // getCompetencesPonderation());
    // }
    //
    // public BigDecimal getTotalEvaluationScoring() {
    // return
    // getPonderatedCompetencesScoring().add(getPonderatedObjectivesScoring());
    // }
    //
    // public Double getCompetencesPonderation() {
    // return getSiadapYearConfiguration().getCompetencesPonderation();
    // }
    //
    public List<SiadapEvaluationItem> getCurrentEvaluationItems() {

        ArrayList<SiadapEvaluationItem> currentEvaluationItems = new ArrayList<SiadapEvaluationItem>();
        final SiadapEvaluationUniverse evalUniverse = getDefaultSiadapEvaluationUniverse();
        currentEvaluationItems.addAll(evalUniverse.getEvaluations(SiadapEvaluationItem.class, new Predicate() {

            @Override
            public boolean evaluate(Object arg0) {
                return (arg0 instanceof ObjectiveEvaluation) ? ((ObjectiveEvaluation) arg0).isValidForVersion(evalUniverse
                        .getCurrentObjectiveVersion()) : true;
            }
        }, null));
        return currentEvaluationItems;
    }

    /**
     * @param processSchedulesEnum
     *            the {@link SiadapProcessSchedulesEnum} which represents the
     *            type of customschedule to change
     * @param newDate
     *            the new {@link LocalDate} which should be defined for the
     *            given processSchedulesEnum
     */
    public void setCustomSchedule(SiadapProcessSchedulesEnum processSchedulesEnum, LocalDate newDate) {
        switch (processSchedulesEnum) {
        case OBJECTIVES_SPECIFICATION_BEGIN_DATE:
            setCustomObjectiveSpecificationBegin(newDate);
            break;
        case OBJECTIVES_SPECIFICATION_END_DATE:
            setCustomObjectiveSpecificationEnd(newDate);
            break;
        case AUTOEVALUATION_BEGIN_DATE:
            setCustomAutoEvaluationBegin(newDate);
            break;
        case AUTOEVALUATION_END_DATE:
            setCustomAutoEvaluationEnd(newDate);
            break;
        case EVALUATION_BEGIN_DATE:
            setCustomEvaluationBegin(newDate);
            break;
        case EVALUATION_END_DATE:
            setCustomEvaluationEnd(newDate);
            break;
        }

    }

    public boolean isEvaluatedWithKnowledgeOfObjectives() {
        if (getCurrentEvaluationItems() == null || getCurrentEvaluationItems().isEmpty()) {
            return false;
        }
        for (SiadapEvaluationItem item : getCurrentEvaluationItems()) {
            if (item.getAcknowledgeDate() == null) {
                return false;
            }
        }
        return true;
    }

    public void setAcknowledgeDate(LocalDate acknowledgeDate) {
        for (SiadapEvaluationItem item : getCurrentEvaluationItems()) {
            if (item.getAcknowledgeDate() == null || acknowledgeDate == null) {
                item.setAcknowledgeDate(acknowledgeDate);
            }
        }
    }

    public PersonSiadapWrapper getEvaluatedWrapper() {
        return new PersonSiadapWrapper(getEvaluated(), getYear());
    }

    public PersonSiadapWrapper getEvaluator() {
        return new PersonSiadapWrapper(getEvaluated(), getYear()).getEvaluator();
    }

    public boolean hasAnyCompetencesSet() {
        if (getDefaultSiadapEvaluationUniverse().getSiadapEvaluationItemsSet().isEmpty()) {
            return false;
        }
        ArrayList<SiadapEvaluationItem> evaluationItems = new ArrayList<SiadapEvaluationItem>(getSiadapEvaluationItems2());
        for (SiadapEvaluationItem siadapEvaluationItem : evaluationItems) {
            if (siadapEvaluationItem instanceof CompetenceEvaluation) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @return the default CompetenceType associated with this process or null
     *         if it hasn't been set yet
     * @author João André Pereira Antunes (joao.antunes@tagus.ist.utl.pt)
     */
    public CompetenceType getDefaultCompetenceType() {
        if (getDefaultSiadapEvaluationUniverse() == null) {
            return null;
        }
        return getDefaultSiadapEvaluationUniverse().getCompetenceSlashCareerType();
    }

    @Atomic
    public void createCurricularPonderation(SiadapUniverse siadapUniverse, BigDecimal gradeToAssign, Boolean assignedExcellency,
            String excellencyAwardJustification, String curricularPonderationJustification, Person evaluator) {
        // let's validate everything
        if (siadapUniverse == null || assignedExcellency == null || evaluator == null
                || !SiadapGlobalEvaluation.isValidGrade(gradeToAssign, assignedExcellency.booleanValue())
                || (assignedExcellency.booleanValue() && StringUtils.isEmpty(excellencyAwardJustification))
                || StringUtils.isEmpty(curricularPonderationJustification)) {
            throw new SiadapException("invalid.data.for.creation.of.a.curricular.ponderation");
        }

        // let's if we don't have an evaluation for the given universe
        if (getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse) != null) {
            throw new SiadapException("error.curricular.ponderation.cannot.have.more.than.one.eval.for.universe");
        }

        SiadapYearConfiguration siadapYearConfiguration = getSiadapYearConfiguration();
        Unit siadapSpecialHarmonizationUnit = siadapYearConfiguration.getSiadapSpecialHarmonizationUnit();
        if (siadapSpecialHarmonizationUnit == null) {
            throw new SiadapException("error.must.configure.special.harmonnization.unit.first");
        }

        AccountabilityType accTypeToReplace = null;
        if (siadapUniverse.equals(SiadapUniverse.SIADAP2)) {
            accTypeToReplace = siadapYearConfiguration.getSiadap2HarmonizationRelation();
        } else if (siadapUniverse.equals(SiadapUniverse.SIADAP3)) {
            accTypeToReplace = siadapYearConfiguration.getSiadap3HarmonizationRelation();
        }

        if (accTypeToReplace == null) {
            throw new SiadapException("error.must.configure.SIADAP.2.and.3.harm.relation.types.first");
        }
        // let's create the new SiadapEvaluationUniverse
        SiadapEvaluationUniverse siadapEvaluationUniverse = new SiadapEvaluationUniverse(this, siadapUniverse, null, false);
        CurricularPonderationEvaluationItem curricularPonderationEvaluationItem =
                new CurricularPonderationEvaluationItem(gradeToAssign, assignedExcellency, excellencyAwardJustification,
                        curricularPonderationJustification, siadapEvaluationUniverse, evaluator);
        // let's connect this SiadapEvaluationUniverse with the specialunit
        Person evaluated = getEvaluated();
        // let's remove the current accountability that it might have for the
        // given SiadapUniverse

        Accountability accToRemove = null;
        LocalDate dateToUse = null;
        // let's search for the previous accountability
        for (Accountability accountability : evaluated.getParentAccountabilities(accTypeToReplace)) {
            // let's confirm that in the other end there's a unit, and that the
            // accountability is for this year
            if (accountability.getParent() instanceof Unit) {
                if (accountability.isActive(SiadapMiscUtilClass.lastDayOfYear(getYear()))) {
                    // this is the one to replace, let's get its begindate
                    dateToUse = accountability.getBeginDate();
                    accToRemove = accountability;
                    // let's actually be conservative here. If we already have
                    // one, let's just abort
                    throw new SiadapException("already.with.a.curricular.ponderation.attributed");
                }
            }
        }

        if (dateToUse == null) {
            // let's get a viable date here 30th December of the year
            dateToUse = getSiadapYearConfiguration().getLastDayForAccountabilities();
        }

        if (accToRemove != null) {
            accToRemove.delete();
        }
        evaluated.addParent(siadapSpecialHarmonizationUnit, accTypeToReplace, dateToUse,
                SiadapMiscUtilClass.lastDayOfYear(getYear()));

    }

    /**
     * @return An ArrayList with the competences attributed to this Siadap
     *         process. If it has none set it will return an empty ArrayList
     * @author João André Pereira Antunes (joao.antunes@tagus.ist.utl.pt)
     */
    public ArrayList<Competence> getCompetences() {
        ArrayList<Competence> arrayCompetences = new ArrayList<Competence>();
        if (!hasAnySiadapEvaluationItems2()) {
            return arrayCompetences;
        }
        ArrayList<SiadapEvaluationItem> evaluationItems = new ArrayList<SiadapEvaluationItem>(getSiadapEvaluationItems2());
        for (SiadapEvaluationItem siadapEvaluationItem : evaluationItems) {
            if (siadapEvaluationItem instanceof CompetenceEvaluation) {
                arrayCompetences.add(((CompetenceEvaluation) siadapEvaluationItem).getCompetence());
            }
        }
        return arrayCompetences;
    }

    public boolean hasAnySiadapEvaluationItems2() {
        Collection<SiadapEvaluationItem> siadapEvaluationItems = getSiadapEvaluationItems2();
        if (siadapEvaluationItems != null && siadapEvaluationItems.size() > 0) {
            return true;
        }
        return false;
    }

    public boolean hasSealedObjectivesAndCompetences() {
        if (getObjectivesAndCompetencesSealedDate() == null) {
            return false;
        }
        return true;
    }

    public boolean isWithObjectivesFilled() {
        int competencesCounter = 0;
        int efficiencyObjectives = 0;
        int performanceObjectives = 0;
        int qualityObjectives = 0;

        Integer currentObjectiveVersion = getDefaultSiadapEvaluationUniverse().getCurrentObjectiveVersion();

        for (SiadapEvaluationItem item : getSiadapEvaluationItems2()) {

            if (item instanceof CompetenceEvaluation) {
                competencesCounter++;
            } else {
                ObjectiveEvaluation objectiveEvaluation = (ObjectiveEvaluation) item;
                if (objectiveEvaluation.isValidForVersion(currentObjectiveVersion)) {
                    switch (objectiveEvaluation.getType()) {

                    case EFICIENCY:
                        efficiencyObjectives++;
                        break;
                    case PERFORMANCE:
                        performanceObjectives++;
                        break;
                    case QUALITY:
                        qualityObjectives++;
                        break;
                    }
                }
            }
        }
        return hasAllNeededCompetences()
                && ((getEvaluatedOnlyByCompetences() == null || getEvaluatedOnlyByCompetences()) || (efficiencyObjectives >= MINIMUM_EFICIENCY_OBJECTIVES_NUMBER
                        && performanceObjectives >= MINIMUM_PERFORMANCE_OBJECTIVES_NUMBER && qualityObjectives >= MINIMUM_QUALITY_OBJECTIVES_NUMBER));
    }

    public boolean hasAllNeededCompetences() {
        return (getEvaluatedOnlyByCompetences() != null && ((getEvaluatedOnlyByCompetences().booleanValue() == false && getCompetences()
                .size() >= MINIMUM_COMPETENCES_WITH_OBJ_EVAL_NUMBER) || (getEvaluatedOnlyByCompetences().booleanValue() == true && getCompetences()
                .size() >= MINIMUM_COMPETENCES_WITHOUT_OBJ_EVAL_NUMBER)));

    }

    public boolean hasCompleteValidationAssessment(SiadapUniverse siadapUniverse) {
        SiadapEvaluationUniverse siadapEvaluationUniverse = getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
        if (siadapEvaluationUniverse == null) {
            return false;
        }
        return siadapEvaluationUniverse.hasCompleteValidationAssessment();
    }

    public BigDecimal getDefaultTotalEvaluationScoring() {
        return getDefaultSiadapEvaluationUniverse().getTotalEvaluationScoring();
    }

    public SiadapEvaluationUniverse getSiadapEvaluationUniverseForSiadapUniverse(SiadapUniverse siadapUniverse) {
        for (SiadapEvaluationUniverse siadapEvaluationUniverse : getSiadapEvaluationUniverses()) {
            if (siadapEvaluationUniverse.getSiadapUniverse() != null
                    && siadapEvaluationUniverse.getSiadapUniverse().equals(siadapUniverse)) {
                return siadapEvaluationUniverse;
            }
        }
        return null;
    }

    // public boolean hasRelevantSiadapEvaluation(SiadapUniverse
    // siadapUniverseToConsider) {
    // SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse =
    // getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverseToConsider);
    // if (siadapEvaluationUniverseForSiadapUniverse == null) {
    // return false;
    // }
    // return
    // SiadapGlobalEvaluation.HIGH.accepts(siadapEvaluationUniverseForSiadapUniverse.getTotalEvaluationScoring());
    //
    // }

    public SiadapGlobalEvaluation getSiadapGlobalEvaluationEnum(SiadapUniverse siadapUniverse) {
        return getSiadapGlobalEvaluationEnum(siadapUniverse, false);

    }

    public SiadapGlobalEvaluation getSiadapGlobalEvaluationEnum(SiadapUniverse siadapUniverse, boolean considerValidation) {
        return getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse).getSiadapGlobalEvaluationEnum(considerValidation,
                false);

    }

    /**
     * @param siadapGlobalEvaluation
     *            the {@link SiadapGlobalEvaluation} which we are testing
     * @param siadapUniverseToConsider
     *            the siadap universe to test for
     * @param relaxedAccepts
     *            if true, a HIGH and an EXCELLENT will be the same, if false,
     *            they won't
     * @return true if the parsed siadapGlobalEvaluation is the global
     *         evaluation for the given siadapUniverseToConsider
     */
    public boolean hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation siadapGlobalEvaluation,
            SiadapUniverse siadapUniverseToConsider) {
        return hasGivenSiadapGlobalEvaluation(siadapGlobalEvaluation, siadapUniverseToConsider, false);
    }

    public boolean hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation siadapGlobalEvaluation,
            SiadapUniverse siadapUniverseToConsider, boolean considerValidation) {
        SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse =
                getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverseToConsider);
        if (siadapEvaluationUniverseForSiadapUniverse == null
                && siadapGlobalEvaluation.equals(SiadapGlobalEvaluation.NONEXISTING)) {
            return true;
        }
        if (siadapEvaluationUniverseForSiadapUniverse == null) {
            return false;
        }

        // if we are on the default evaluation universe, we should check if this
        // one is finished or not
        if (siadapEvaluationUniverseForSiadapUniverse.getDefaultEvaluationUniverse() && !isDefaultEvaluationDone())

        {
            if (siadapGlobalEvaluation.equals(SiadapGlobalEvaluation.NONEXISTING)) {
                return true;
            } else {
                return false;
            }
        }
        BigDecimal gradeToUse = null;
        boolean excellencyAward = false;
        // if we have a compelte validation assessment, we should use that
        if (considerValidation && siadapEvaluationUniverseForSiadapUniverse.hasValidationAssessment()) {
            gradeToUse = siadapEvaluationUniverseForSiadapUniverse.getCcaClassification();
            excellencyAward = siadapEvaluationUniverseForSiadapUniverse.getCcaClassificationExcellencyAward();
        } else {
            gradeToUse = siadapEvaluationUniverseForSiadapUniverse.getTotalEvaluationScoring();
            excellencyAward = siadapEvaluationUniverseForSiadapUniverse.hasExcellencyAwarded();
        }

        return siadapGlobalEvaluation.accepts(gradeToUse, excellencyAward);

    }

    // public boolean hasExcellentSiadapEvaluation(SiadapUniverse
    // siadapUniverseToConsider) {
    // SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse =
    // getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverseToConsider);
    // if (siadapEvaluationUniverseForSiadapUniverse == null) {
    // return false;
    // }
    // return siadapEvaluationUniverseForSiadapUniverse.hasExcellencyAwarded()
    // &&
    // SiadapGlobalEvaluation.EXCELLENCY.accepts(siadapEvaluationUniverseForSiadapUniverse
    // .getTotalEvaluationScoring());
    // }

    public boolean hasExcellencyAward() {
        if (getEvaluationData2() == null || getEvaluationData2().getExcellencyAward() == null) {
            return false;
        }
        return getEvaluationData2().getExcellencyAward();
    }

    public List<ObjectiveEvaluation> getObjectiveEvaluations() {
        return getDefaultSiadapEvaluationUniverse().getObjectiveEvaluations();
    }

    public LocalDate getAutoEvaluationEndDate() {
        SiadapYearConfiguration configuration = getSiadapYearConfiguration();
        LocalDate end = configuration.getAutoEvaluationEnd();
        if (getCustomAutoEvaluationEnd() != null) {
            end = getCustomAutoEvaluationEnd();
        }
        return end;

    }

    public LocalDate getAutoEvaluationBeginDate() {
        SiadapYearConfiguration configuration = getSiadapYearConfiguration();
        LocalDate begin = configuration.getAutoEvaluationBegin();
        if (getCustomAutoEvaluationBegin() != null) {
            begin = getCustomAutoEvaluationBegin();
        }
        return begin;

    }

    public Interval getAutoEvaluationInterval() {
        LocalDate begin = getAutoEvaluationBeginDate();
        LocalDate end = getAutoEvaluationEndDate();
        return new Interval(SiadapMiscUtilClass.convertDateToBeginOfDay(begin), SiadapMiscUtilClass.convertDateToEndOfDay(end));
    }

    public LocalDate getEvaluationEndDate() {
        SiadapYearConfiguration configuration = getSiadapYearConfiguration();
        LocalDate end = configuration.getEvaluationEnd();
        if (getCustomEvaluationEnd() != null) {
            end = getCustomEvaluationEnd();
        }
        return end;

    }

    public LocalDate getEvaluationBeginDate() {
        SiadapYearConfiguration configuration = getSiadapYearConfiguration();
        LocalDate begin = configuration.getEvaluationBegin();
        if (getCustomEvaluationBegin() != null) {
            begin = getCustomEvaluationBegin();
        }
        return begin;

    }

    public Interval getEvaluationInterval() {
        LocalDate begin = getEvaluationBeginDate();
        LocalDate end = getEvaluationEndDate();
        return new Interval(SiadapMiscUtilClass.convertDateToBeginOfDay(begin), SiadapMiscUtilClass.convertDateToEndOfDay(end));
    }

    public LocalDate getObjectiveSpecificationEndDate() {
        SiadapYearConfiguration configuration = getSiadapYearConfiguration();
        LocalDate end = configuration.getObjectiveSpecificationEnd();
        if (getCustomObjectiveSpecificationEnd() != null) {
            end = getCustomObjectiveSpecificationEnd();
        }
        return end;

    }

    public LocalDate getObjectiveSpecificationBeginDate() {
        SiadapYearConfiguration configuration = getSiadapYearConfiguration();
        LocalDate begin = configuration.getObjectiveSpecificationBegin();
        if (getCustomObjectiveSpecificationBegin() != null) {
            begin = getCustomObjectiveSpecificationBegin();
        }
        return begin;
    }

    public Interval getObjectiveSpecificationInterval() {
        LocalDate begin = getObjectiveSpecificationBeginDate();

        LocalDate end = getObjectiveSpecificationEndDate();

        return new Interval(SiadapMiscUtilClass.convertDateToBeginOfDay(begin), SiadapMiscUtilClass.convertDateToEndOfDay(end));
    }

    // TODO change this appropriately when Issue #31 is resolved
    private boolean isAutoEvaluationScheduleDefined() {

        SiadapYearConfiguration configuration = getSiadapYearConfiguration();
        LocalDate begin = configuration.getAutoEvaluationBegin();
        LocalDate end = configuration.getAutoEvaluationEnd();
        if (end == null || begin == null) {
            return false;
        }
        return true;
    }

    // TODO change this appropriately when Issue #31 is resolved
    private boolean isEvaluationScheduleDefined() {

        SiadapYearConfiguration configuration = getSiadapYearConfiguration();
        LocalDate begin = configuration.getEvaluationBegin();
        LocalDate end = configuration.getEvaluationEnd();
        if (end == null || begin == null) {
            return false;
        }
        return true;
    }

    // TODO change this appropriately when Issue #31 is resolved
    private boolean isObjectiveSpecificationScheduleDefined() {
        SiadapYearConfiguration configuration = getSiadapYearConfiguration();
        LocalDate begin = configuration.getObjectiveSpecificationBegin();
        LocalDate end = configuration.getObjectiveSpecificationEnd();
        if (end == null || begin == null) {
            return false;
        }
        return true;
    }

    public boolean isAutoEvaluationIntervalFinished() {
        // TODO change this appropriately when Issue #31 is resolved
        if (!isAutoEvaluationScheduleDefined()) {
            return false;
        }
        return getAutoEvaluationInterval().isBeforeNow();
    }

    public boolean isEvaluationIntervalFinished() {
        if (!isEvaluationScheduleDefined()) {
            return false;
        }
        // TODO change this appropriately when Issue #31 is resolved
        return getEvaluationInterval().isBeforeNow();
    }

    public boolean isObjectiveSpecificationIntervalFinished() {
        if (!isObjectiveSpecificationScheduleDefined()) {
            return false;
        }
        // TODO change this appropriately when Issue #31 is resolved
        return getObjectiveSpecificationInterval().isBeforeNow();
    }

    @Atomic
    public void removeHarmonizationMark(SiadapUniverse siadapUniverse) {
        SiadapEvaluationUniverse evaluationUniverse = getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
        evaluationUniverse.setHarmonizationDate(null);
        getProcess().removeHarmonizationMark(evaluationUniverse);
    }

    public boolean hasAnAssociatedCurricularPonderationEval() {
        for (SiadapEvaluationUniverse evaluationUniverse : getSiadapEvaluationUniverses()) {
            if (evaluationUniverse.isCurriculumPonderation()) {
                return true;
            }
        }
        return false;
    }

    // public SiadapEvaluationUniverse getCurr

    @Atomic
    public void markAsHarmonized(LocalDate harmonizationDate, SiadapUniverse siadapUniverse) {
        SiadapEvaluationUniverse evaluationUniverse = getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
        if (!(evaluationUniverse.isWithSkippedEvaluation() || getState().equals(SiadapProcessStateEnum.NULLED))) {
            //only if we don't have a nulled or skipped evaluation
            if (evaluationUniverse.getHarmonizationAssessment() == null
                    || (evaluationUniverse.hasExcellencyAwardedFromEvaluator() && evaluationUniverse
                            .getHarmonizationAssessmentForExcellencyAward() == null)) {
                //and only if we have no harmonization assessment, or an excellent and no harmonization for that one
                throw new SiadapException("harmonization.error.there.are.people.not.harmonized");

            }
        }
        // let's also make sure that this person either has been marked as not
        // having an evaluation or has the evaluation done
        if (!isEvaluationDone(siadapUniverse) && !getState().equals(SiadapProcessStateEnum.NULLED)) {
            if (evaluationUniverse.getDefaultEvaluationUniverse() && isWithSkippedEvaluation()) {
                // do nothing :)
            } else {
                throw new SiadapException("error.harmonization.can't.harmonize.with.users.without.grade");
            }
        }

        // let's also make sure there's consistency between the excellency
        // assessment and the regular.
        // you cannot have the case where excellencyAssessment = true and
        // regularAssessment = false
        if (evaluationUniverse.getHarmonizationAssessment() != null
                && evaluationUniverse.getHarmonizationAssessmentForExcellencyAward() != null
                && evaluationUniverse.getHarmonizationAssessmentForExcellencyAward() == true
                && evaluationUniverse.getHarmonizationAssessment() == false) {
            throw new SiadapException("error.harmonization.inconsistency.between.excellency.and.regular.assessment");
        }
        evaluationUniverse.setHarmonizationDate(harmonizationDate);
        getProcess().markAsHarmonized(evaluationUniverse);
    }

    public boolean isHomologated() {
        return getHomologationDate() != null;
    }

    public boolean isSuggestedForExcellencyAward() {
        SiadapEvaluation evaluationData = getEvaluationData2();
        return evaluationData != null && evaluationData.getExcellencyAward() == Boolean.TRUE;
    }

    public boolean isWithSkippedEvaluation() {
        SiadapEvaluation evaluationData = getEvaluationData2();
        return evaluationData != null && !StringUtils.isEmpty(evaluationData.getNoEvaluationJustification());
    }

    public SiadapEvaluation getEvaluationData2() {
        if (getDefaultSiadapEvaluationUniverse() == null) {
            return null;
        }
        return getDefaultSiadapEvaluationUniverse().getSiadapEvaluation();
    }

    public boolean hasAllEvaluationItemsValid() {
        for (SiadapEvaluationUniverse evaluationUniverse : getSiadapEvaluationUniverses()) {
            for (ObjectiveEvaluation objectiveEvaluation : evaluationUniverse.getObjectiveEvaluations()) {
                if (!objectiveEvaluation.isValid()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isHarmonizationOfDefaultUniverseDone() {
        if (getDefaultSiadapEvaluationUniverse() != null) {
            return getDefaultSiadapEvaluationUniverse().getHarmonizationDate() != null
                    && getDefaultSiadapEvaluationUniverse().getHarmonizationAssessment() != null;
        }
        return false;
    }

    /**
     * convenience method to allow this to be called by the JSPs
     * 
     * @return the same as {@link #hasAllEvaluationItemsValid()}
     */
    public boolean isAllEvaluationItemsValid() {
        return hasAllEvaluationItemsValid();
    }

    /**
     * 
     * @return true if the evaluated person is evaluated only by competences and
     *         has no objectives, false otherwise
     */
    public boolean isCoherentOnTypeOfEvaluation() {
        SiadapEvaluationUniverse defaultSiadapEvaluationUniverse = getDefaultSiadapEvaluationUniverse();
        if ((getEvaluatedOnlyByCompetences() != null && getEvaluatedOnlyByCompetences())
                && defaultSiadapEvaluationUniverse.getObjectiveEvaluations() != null
                && defaultSiadapEvaluationUniverse.getObjectiveEvaluations().size() != 0) {
            return false;
        }
        return true;
    }

    // TODO: joantune: uncomment once Roxo's work is put in production. For now
    // it's impossible to check this because one cannot access the information
    // in other objects
    // @ConsistencyPredicate
    // public boolean
    // validateExistenceOfOnlyOneSetOfEvaluationItemsPerUniverse() {
    // boolean foundOneSet = false;
    // for (SiadapEvaluationUniverse evaluationUniverse :
    // getSiadapEvaluationUniverses()) {
    // List<SiadapEvaluationItem> siadapEvaluationItems =
    // evaluationUniverse.getSiadapEvaluationItems();
    // if (siadapEvaluationItems != null && siadapEvaluationItems.size() > 0) {
    // if (foundOneSet) {
    // return false;
    // }
    // foundOneSet = true;
    // }
    // }
    // return true;
    // }

    public boolean validateExistenceOfOnlyOneSetOfEvaluationAndAutoEvaluation() {
        boolean foundOneSet = false;
        for (SiadapEvaluationUniverse evaluationUniverse : getSiadapEvaluationUniverses()) {
            SiadapEvaluation siadapEvaluation = evaluationUniverse.getSiadapEvaluation();
            SiadapAutoEvaluation siadapAutoEvaluation = evaluationUniverse.getSiadapAutoEvaluation();
            if (siadapEvaluation != null || siadapAutoEvaluation != null) {
                if (foundOneSet) {
                    return false;
                }
                foundOneSet = true;
            }
        }
        return true;
    }

    public boolean hasAnySiadapEvaluationItemsInAnyUniverse() {
        for (SiadapEvaluationUniverse siadapEvalUniverse : getSiadapEvaluationUniverses()) {
            if (!siadapEvalUniverse.getSiadapEvaluationItems().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public Collection<SiadapEvaluationItem> getSiadapEvaluationItems2() {
        Collection<SiadapEvaluationItem> siadapEvaluationItems = getDefaultSiadapEvaluationUniverse().getSiadapEvaluationItems();
        if (siadapEvaluationItems != null && siadapEvaluationItems.size() > 0) {
            return siadapEvaluationItems;
        }
        return Collections.EMPTY_LIST;
    }

    public SiadapAutoEvaluation getAutoEvaluationData2() {
        for (SiadapEvaluationUniverse evaluationUniverse : getSiadapEvaluationUniverses()) {
            SiadapAutoEvaluation siadapAutoEvaluation = evaluationUniverse.getSiadapAutoEvaluation();
            if (siadapAutoEvaluation != null) {
                return siadapAutoEvaluation;
            }
        }
        return null;
    }

    public void setDefaultSiadapUniverse(SiadapUniverse siadapUniverseToChangeTo) {
        // if we have another one, we should throw an exception
        if (getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverseToChangeTo) != null
                && !getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverseToChangeTo).equals(
                        getDefaultSiadapEvaluationUniverse())) {
            throw new SiadapException("error.cant.change.default.universe.because.theres.another.eval.in.that.universe");
        }
        getDefaultSiadapEvaluationUniverse().setSiadapUniverse(siadapUniverseToChangeTo);

    }

    public void delete() {
        delete(false);
    }

    /**
     * Deletes the proccess and everything which is associated with it
     */
    public void delete(boolean neglectLogSize) {
        setEvaluated(null);
        SiadapProcess process = getProcess();
        setProcess(null);
        process.delete(neglectLogSize);
        for (SiadapEvaluationUniverse siadapEvalUni : getSiadapEvaluationUniverses()) {
            removeSiadapEvaluationUniverses(siadapEvalUni);
            siadapEvalUni.delete();
        }
        setSiadapRootModule(null);
        setYear(null);
        setSiadapYearConfiguration(null);
        deleteDomainObject();
    }

    /**
     * 
     * @param person
     *            the person to search for
     * @return the last used SiadapUniverse by the given person
     */
    public static SiadapUniverse getLastSiadapUniverseUsedBy(Person person) {
        SiadapUniverse siadapUniverseToReturn = null;
        int mostRecentYear = 0;
        for (Siadap previousSiadap : person.getSiadapsAsEvaluatedSet()) {
            SiadapUniverse defaultSiadapUniverse = previousSiadap.getDefaultSiadapUniverse();
            if (defaultSiadapUniverse != null && (mostRecentYear == 0 || previousSiadap.getYear() < mostRecentYear)) {
                siadapUniverseToReturn = previousSiadap.getDefaultSiadapUniverse();
            }
        }

        return siadapUniverseToReturn;

    }

    public static String getRemoteEmail(Person person) {
        return new JerseyRemoteUser(person.getUser()).getEmailForSendingEmails();
    }

    @Deprecated
    public java.util.Set<module.siadap.domain.SiadapEvaluationUniverse> getSiadapEvaluationUniverses() {
        return getSiadapEvaluationUniversesSet();
    }

}
