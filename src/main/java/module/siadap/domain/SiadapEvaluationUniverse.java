/*
 * @(#)SiadapEvaluationUniverse.java
 *
 * Copyright 2011 Instituto Superior Tecnico
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.scoring.IScoring;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;

import org.apache.commons.collections.Predicate;
import org.joda.time.LocalDate;

import pt.ist.bennu.core.domain.exceptions.DomainException;
import pt.ist.fenixframework.Atomic;

/**
 * 
 * @author João Antunes
 * @author Luis Cruz
 * 
 */
public class SiadapEvaluationUniverse extends SiadapEvaluationUniverse_Base {

    private static final int PRECISION = 3;
    private static final int ROUND_MODE = BigDecimal.ROUND_HALF_EVEN;

    public SiadapEvaluationUniverse(Siadap siadap, SiadapUniverse siadapUniverse, CompetenceType competenceType,
            boolean defaultUniverse) {
        super();
        setSiadap(siadap);
        if (siadap.getValidationDateOfDefaultEvaluation() != null) {
            throw new SiadapException("cant.assign.more.evaluations.to.this.siadap.proccess");
        }
        setSiadapUniverse(siadapUniverse);
        setCurrentObjectiveVersion(0);
        setCompetenceSlashCareerType(competenceType);
        setDefaultEvaluationUniverse(defaultUniverse);
    }

    // TODO joantune: assim que o Roxo integrar o trabalho da tese dele, isto já funciona :)
    //    @ConsistencyPredicate
    //    public boolean onlyOneDefaultInstancePerSiadap() {
    //	if (getDefaultEvaluationUniverse().booleanValue()) {
    //	    //let's make sure there's no other
    //	    for (SiadapEvaluationUniverse evaluationUniverse : getSiadap().getSiadapEvaluationUniverses()) {
    //		if (!evaluationUniverse.equals(this) && evaluationUniverse.getDefaultEvaluationUniverse())
    //		    return false;
    //	    }
    //	}
    //
    //	return true;
    //    }

    public boolean isCurriculumPonderation() {
        boolean isCurriculumPonderation = false;
        for (SiadapEvaluationItem siadapEvaluationItem : getSiadapEvaluationItems()) {
            if (siadapEvaluationItem.getClass().equals(CurricularPonderationEvaluationItem.class)) {
                if (isCurriculumPonderation) {
                    throw new SiadapException("more.than.one.CurricularPonderationEvaluationItem.per.universe");
                }
                isCurriculumPonderation = true;
            }
        }
        return isCurriculumPonderation;
    }

    public boolean hasRelevantEvaluation() {
        return SiadapGlobalEvaluation.HIGH.accepts(getTotalEvaluationScoring(), hasExcellencyAwarded());
    }

    public boolean isInadequate() {
        return SiadapGlobalEvaluation.LOW.accepts(getTotalEvaluationScoring(), hasExcellencyAwarded())
                || SiadapGlobalEvaluation.ZERO.accepts(getTotalEvaluationScoring(), hasExcellencyAwarded());
    }

    /**
     * 
     * @param considerValidation
     *            if true, it considers the validation grade when returning the
     *            appropriate enum
     * @param getLatestGrade
     *            if true, it ignores #considerValidation and get the latest
     *            grade
     * @return {@link SiadapGlobalEvaluation} enum according with the grade
     */
    public SiadapGlobalEvaluation getSiadapGlobalEvaluationEnum(boolean considerValidation, boolean getLatestGrade) {
        if (getLatestGrade) {
            return SiadapGlobalEvaluation.getGlobalEvaluation(getCurrentGrade(), getCurrentExcellencyAward());
        }
        if (considerValidation && hasValidationAssessment()) {
            return SiadapGlobalEvaluation.getGlobalEvaluation(getCcaClassification(), getCcaClassificationExcellencyAward());
        } else {
            return SiadapGlobalEvaluation.getGlobalEvaluation(getTotalEvaluationScoring(), hasExcellencyAwarded());
        }
    }

    public boolean getCurrentExcellencyAward() {
        if (hasCcaAfterValidationAssignedGrade()) {
            return getCcaAfterValidationExcellencyAward().booleanValue();
        }
        if (hasValidationAssessment()) {
            return getCcaClassificationExcellencyAward().booleanValue();
        }
        return hasExcellencyAwarded();

    }

    public BigDecimal getCurrentGrade() {
        if (hasCcaAfterValidationAssignedGrade()) {
            return getCcaAfterValidationGrade();
        }
        if (hasValidationAssessment()) {
            return getCcaClassification();
        }
        return getTotalEvaluationScoring();

    }

    private boolean hasCcaAfterValidationAssignedGrade() {
        if (getCcaAfterValidationGrade() == null || getCcaAfterValidationExcellencyAward() == null) {
            return false;
        }
        return true;
    }

    public SiadapGlobalEvaluation getSiadapGlobalEvaluationEnumAfterValidation() {
        return getSiadapGlobalEvaluationEnum(true, false);
    }

    public SiadapGlobalEvaluation getSiadapGlobalEvaluationEnum() {
        return getSiadapGlobalEvaluationEnum(false, false);

    }

    public SiadapGlobalEvaluation getLatestSiadapGlobalEvaluationEnum() {
        return getSiadapGlobalEvaluationEnum(false, true);

    }

    public BigDecimal getTotalEvaluationScoring() {
        if (isWithSkippedEvaluation()) {
            return null;
        }
        if (isCurriculumPonderation()) {
            return CurricularPonderationEvaluationItem.getCurricularPonderationValue(this);
        }
        //let's make see the other special case, i.e. when the evaluated is only evaluated by competences
        final Boolean evaluatedOnlyByCompetences = getSiadap().getEvaluatedOnlyByCompetences();
        if (evaluatedOnlyByCompetences != null && evaluatedOnlyByCompetences.booleanValue()) {
            return getEvaluationScoring(getCompetenceEvaluations());
        }

        return getPonderatedCompetencesScoring().add(getPonderatedObjectivesScoring());
    }

    public BigDecimal getPonderatedCompetencesScoring() {
        return getPonderationResult(getEvaluationScoring(getCompetenceEvaluations()), getCompetencesPonderation());
    }

    public List<CompetenceEvaluation> getCompetenceEvaluations() {
        return getEvaluations(CompetenceEvaluation.class, null, null);
    }

    /**
     * 
     * @return true if the evaluation needs no more validation assessments
     *         (either because it has a skipped eval or is a MEDIUM or it has a
     *         CcaAssessment, classification, and excellencyAward)
     */
    protected boolean hasCompleteValidationAssessment() {
        if (isWithSkippedEvaluation() || (getSiadap().getNulled() != null && getSiadap().getNulled() == true)) {
            return true;
        }
        //it depends on the grade, so if we have a grade of regular, we won't need an assessment
        if (SiadapGlobalEvaluation.MEDIUM.accepts(getTotalEvaluationScoring(), false)) {
            return true;
        }
        return hasValidationAssessment();

    }

    protected boolean hasValidationAssessment() {
        if (getCcaAssessment() == null || getCcaClassificationExcellencyAward() == null || getCcaClassification() == null) {
            return false;
        }
        return true;
    }

    public BigDecimal getPonderatedObjectivesScoring() {
        return getPonderationResult(getObjectivesScoring(), getObjectivesPonderation());
    }

    public BigDecimal getObjectivesScoring() {
        return getEvaluationScoring(getObjectiveEvaluations());
    }

    public List<ObjectiveEvaluation> getObjectiveEvaluations() {
        return getEvaluations(ObjectiveEvaluation.class, new Predicate() {

            @Override
            public boolean evaluate(Object arg0) {
                ObjectiveEvaluation objective = (ObjectiveEvaluation) arg0;
                return objective.isValidForVersion(getCurrentObjectiveVersion());
            }

        }, ObjectiveEvaluation.COMPARATOR_BY_OLDEST_DATE);

    }

    public int getObjectivesPonderation() {
        final Boolean evaluatedOnlyByCompetences = getSiadap().getEvaluatedOnlyByCompetences();
        if (evaluatedOnlyByCompetences != null && evaluatedOnlyByCompetences.booleanValue()) {
            return 0;
        }
        if (getSiadapUniverse() == SiadapUniverse.SIADAP2) {
            return getSiadap().getSiadapYearConfiguration().getSiadap2ObjectivesPonderation();
        }
        if (getSiadapUniverse() == SiadapUniverse.SIADAP3) {
            return getSiadap().getSiadapYearConfiguration().getSiadap3ObjectivesPonderation();
        }
        return 0;
    }

    public int getCompetencesPonderation() {
        final Boolean evaluatedOnlyByCompetences = getSiadap().getEvaluatedOnlyByCompetences();
        if (evaluatedOnlyByCompetences != null && evaluatedOnlyByCompetences.booleanValue()) {
            return 100;
        }
        if (getSiadapUniverse() == SiadapUniverse.SIADAP2) {
            return getSiadap().getSiadapYearConfiguration().getSiadap2CompetencesPonderation();
        }
        if (getSiadapUniverse() == SiadapUniverse.SIADAP3) {
            return getSiadap().getSiadapYearConfiguration().getSiadap3CompetencesPonderation();
        }
        return 0;
    }

    public BigDecimal getCompetencesScoring() {
        return getEvaluationScoring(getCompetenceEvaluations());
    }

    private boolean isEvaluationScoringComplete(List<? extends SiadapEvaluationItem> evaluations) {
        for (SiadapEvaluationItem evaluation : evaluations) {
            IScoring itemEvaluation = evaluation.getItemEvaluation();
            if (itemEvaluation == null || itemEvaluation.getPoints() == null) {
                return false;
            }
        }
        return true;
    }

    private BigDecimal getEvaluationScoring(List<? extends SiadapEvaluationItem> evaluations) {

        if (!isEvaluationScoringComplete(evaluations)) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = new BigDecimal(0);
        for (SiadapEvaluationItem evaluation : evaluations) {
            IScoring itemEvaluation = evaluation.getItemEvaluation();
            if (itemEvaluation == null) {
                throw new DomainException("error.siadapEvaluation.mustFillAllItems",
                        DomainException.getResourceFor("resources/SiadapResources"));
            }
            result = result.add(itemEvaluation.getPoints());
        }

        if (evaluations.size() == 0) {
            return BigDecimal.ZERO;
        }
        return result.divide(new BigDecimal(evaluations.size()), PRECISION, ROUND_MODE);
    }

    private BigDecimal getPonderationResult(BigDecimal scoring, int usedPercentage) {
        BigDecimal percentage = BigDecimal.valueOf(usedPercentage).divide(new BigDecimal(100));

        BigDecimal result = percentage.multiply(scoring);
        return result.setScale(PRECISION, ROUND_MODE);
    }

    /**
     * 
     * @param <T>
     * @param clazz
     *            the class of the SiadapEvaluationItem that one is interested
     *            in getting
     * @param predicate
     *            the predicate that is evaluated for each of the
     *            SiadapEvaluationItem
     * @param comparator
     *            a comparator of {@link SiadapEvaluationItem} or null if we
     *            want to use the default {@link SiadapEvaluationItem#COMPARATOR_BY_DATE}
     * @return a list of <T> elements
     */
    protected <T extends SiadapEvaluationItem> List<T> getEvaluations(Class<T> clazz, Predicate predicate,
            Comparator<T> comparator) {
        List<T> evaluationItems = new ArrayList<T>();
        for (SiadapEvaluationItem item : getSiadapEvaluationItems()) {
            if (clazz.isAssignableFrom(item.getClass()) && (predicate == null || predicate.evaluate(item))) {
                evaluationItems.add((T) item);
            }
        }
        if (comparator == null) {
            Collections.sort(evaluationItems, SiadapEvaluationItem.COMPARATOR_BY_DATE);
        } else {
            Collections.sort(evaluationItems, comparator);
        }
        return evaluationItems;
    }

    public boolean hasExcellencyAwardedFromEvaluator() {
        if (getEvaluatorClassificationExcellencyAward() == null) {
            return false;
        }
        return getEvaluatorClassificationExcellencyAward();
    }

    public boolean hasExcellencyAwarded() {
        if (isCurriculumPonderation()) {
            for (CurricularPonderationEvaluationItem curricularPonderationEvaluationItem : getEvaluations(
                    CurricularPonderationEvaluationItem.class, null, null)) {
                Boolean excellencyAward = curricularPonderationEvaluationItem.getExcellencyAward();
                if (excellencyAward == null) {
                    return false;
                }
                return excellencyAward.booleanValue();
            }
        } else {
            if (getSiadapEvaluation() == null || !getSiadap().isDefaultEvaluationDone()) {
                return false;
            }
            Boolean excellencyAward = getSiadapEvaluation().getExcellencyAward();
            if (excellencyAward == null) {
                return false;
            }
            return excellencyAward.booleanValue();
        }
        return false;
    }

    public boolean isEvaluationDone() {
        if (getDefaultEvaluationUniverse()) {
            return getSiadap().isDefaultEvaluationDone();
        }
        return isCurriculumPonderation() && getSiadapEvaluationItems() != null && getSiadapEvaluationItems().size() > 0;
    }

    @Atomic
    public void setHarmonizationAssessments(Boolean harmonizationAssessment, Boolean excellencyHarmonizationAssessment) {
        if (getHarmonizationDate() != null) {
            throw new SiadapException("error.harmonization.closed.cannot.change.assessment.reopen.first");
        }
        if (!isEvaluationDone() && !(isWithSkippedEvaluation()) && harmonizationAssessment != null) {
            throw new SiadapException("error.harmonization.assessment.cant.be.done.with.non.existing.evaluation");
        }
        super.setHarmonizationAssessment(harmonizationAssessment);
        super.setHarmonizationAssessmentForExcellencyAward(excellencyHarmonizationAssessment);
        //let's copy the grade to the respective place if we are in position to do that, i.e. if we have a full harm. assessment i.e. excellency and normal one
        if (isWithFullAssessments(harmonizationAssessment, excellencyHarmonizationAssessment)) {
            if (!isWithSkippedEvaluation()) {
                setHarmonizationClassification(getTotalEvaluationScoring());
                if (isCurriculumPonderation()) {
                    for (SiadapEvaluationItem evaluationItem : getSiadapEvaluationItems()) {
                        CurricularPonderationEvaluationItem ponderationEvaluationItem =
                                (CurricularPonderationEvaluationItem) evaluationItem;
                        setHarmonizationClassificationExcellencyAward(ponderationEvaluationItem.getExcellencyAward());

                    }
                } else {
                    setHarmonizationClassificationExcellencyAward(getSiadapEvaluation().getExcellencyAward());
                }
            } else {
                setHarmonizationClassification(new BigDecimal(0));
                setHarmonizationClassificationExcellencyAward(null);
            }
            getSiadap().getProcess().markAsHarmonizationAssessmentGiven(this);

        }
    }

    /**
     * 
     * @param newHarmonizationAssessment
     * @param newExcellencyHarmonizationAssessment
     * @return true if we have all the assessments we need, false otherwise
     */
    private boolean isWithFullAssessments(Boolean newHarmonizationAssessment, Boolean newExcellencyHarmonizationAssessment) {
        if (newHarmonizationAssessment == null) {
            return false;
        }
        if (hasExcellencyAwardedFromEvaluator() && newExcellencyHarmonizationAssessment == null) {
            return false;
        }
        return true;
    }

    @Deprecated
    @Override
    public void setHarmonizationAssessment(Boolean harmonizationAssessment) {
        throw new UnsupportedOperationException(
                "don't use this method, set both assessments with setHarmonizationAssessments instead");
    }

    public boolean isWithSkippedEvaluation() {
        if (getDefaultEvaluationUniverse()) {
            return getSiadap().isWithSkippedEvaluation();
        }
        return false;
    }

    @Override
    public void setHarmonizationDate(LocalDate harmonizationDate) {
        if ((getHarmonizationAssessment() == null || (getCurrentExcellencyAward() == true && getHarmonizationAssessmentForExcellencyAward() == null))
                && !(isWithSkippedEvaluation() || getSiadap().getState().equals(SiadapProcessStateEnum.NULLED))) {
            throw new SiadapException("error.harmonization.not.finished.for.person.X"
                    + getSiadap().getEvaluated().getUser().getUsername());
        }
        super.setHarmonizationDate(harmonizationDate);
    }

    @Atomic
    public void removeHarmonizationAssessments() {
        super.setHarmonizationAssessment(null);
        setHarmonizationClassification(null);
        setHarmonizationClassificationExcellencyAward(null);
        super.setHarmonizationAssessmentForExcellencyAward(null);
        getSiadap().getProcess().removeHarmonizationAssessments(this);

    }

    /**
     * Removes itself if there are no SiadapEvaluationItems associated or
     * SiadapAutoEvaluation or SiadapEvaluation
     */
    public void delete() {

        for (SiadapEvaluationItem siadapEvaluationItems : getSiadapEvaluationItems()) {
            removeSiadapEvaluationItems(siadapEvaluationItems);
            siadapEvaluationItems.delete();
        }
        setCompetenceSlashCareerType(null);
        setSiadap(null);
        deleteDomainObject();
    }

    @Deprecated
    public java.util.Set<module.siadap.domain.SiadapEvaluationItem> getSiadapEvaluationItems() {
        return getSiadapEvaluationItemsSet();
    }

}
