package module.siadap.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.scoring.IScoring;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import myorg.domain.exceptions.DomainException;

import org.apache.commons.collections.Predicate;
import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.services.Service;

public class SiadapEvaluationUniverse extends SiadapEvaluationUniverse_Base {

    private static final int PRECISION = 3;
    private static final int ROUND_MODE = BigDecimal.ROUND_HALF_EVEN;

    public SiadapEvaluationUniverse(Siadap siadap, SiadapUniverse siadapUniverse, boolean defaultUniverse) {
	super();
	setSiadap(siadap);
	setSiadapUniverse(siadapUniverse);
	setCurrentObjectiveVersion(0);
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
		if (isCurriculumPonderation)
		    throw new SiadapException("more.than.one.CurricularPonderationEvaluationItem.per.universe");
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

    public BigDecimal getTotalEvaluationScoring() {
	if (isCurriculumPonderation()) {
	    return CurricularPonderationEvaluationItem.getCurricularPonderationValue(this);
	}
	//let's make see the other special case, i.e. when the evaluated is only evaluated by competences
	if (getSiadap().getEvaluatedOnlyByCompetences()) {
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
	if (getSiadapUniverse() == SiadapUniverse.SIADAP2)
	    return getSiadap().getSiadapYearConfiguration().getSiadap2ObjectivesPonderation();
	if (getSiadapUniverse() == SiadapUniverse.SIADAP3)
	    return getSiadap().getSiadapYearConfiguration().getSiadap3ObjectivesPonderation();
	return 0;
    }

    public int getCompetencesPonderation() {
	if (getSiadapUniverse() == SiadapUniverse.SIADAP2)
	    return getSiadap().getSiadapYearConfiguration().getSiadap2CompetencesPonderation();
	if (getSiadapUniverse() == SiadapUniverse.SIADAP3)
	    return getSiadap().getSiadapYearConfiguration().getSiadap3CompetencesPonderation();
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

	if (evaluations.size() == 0)
	    return BigDecimal.ZERO;
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
     *            want to use the default
     *            {@link SiadapEvaluationItem#COMPARATOR_BY_DATE}
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
	} else
	    Collections.sort(evaluationItems, comparator);
	return evaluationItems;
    }

    public boolean hasExcellencyAwarded() {
	if (isCurriculumPonderation()) {
	    for (CurricularPonderationEvaluationItem curricularPonderationEvaluationItem : getEvaluations(
		    CurricularPonderationEvaluationItem.class, null, null)) {
		Boolean excellencyAward = curricularPonderationEvaluationItem.getExcellencyAward();
		if (excellencyAward == null)
		    return false;
		return excellencyAward.booleanValue();
	    }
	} else {
	    if (getSiadapEvaluation() == null || !getSiadap().isDefaultEvaluationDone())
		return false;
	    Boolean excellencyAward = getSiadapEvaluation().getExcellencyAward();
	    if (excellencyAward == null)
		return false;
	    return excellencyAward.booleanValue();
	}
	return false;
    }

    @Override
    @Service
    public void setHarmonizationAssessment(Boolean harmonizationAssessment) {
	if (getHarmonizationDate() != null)
	    throw new SiadapException("error.harmonization.closed.cannot.change.assessment.reopen.first");
	super.setHarmonizationAssessment(harmonizationAssessment);
    }

    @Override
    public void setHarmonizationDate(LocalDate harmonizationDate) {
	if (getHarmonizationAssessment() == null) {
	    throw new SiadapException("error.harmonization.not.finished.for.person.X"
		    + getSiadap().getEvaluated().getUser().getUsername());
	}
	super.setHarmonizationDate(harmonizationDate);
    }

}
