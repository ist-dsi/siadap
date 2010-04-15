package module.siadap.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import module.organization.domain.Person;
import module.siadap.activities.Evaluation;

import org.apache.commons.collections.Predicate;

public class Siadap extends Siadap_Base {

    private static final int PRECISION = 3;
    private static final int ROUND_MODE = BigDecimal.ROUND_HALF_EVEN;

    public Siadap(int year, Person evaluator, Person evaluated) {
	super();
	setYear(year);
	setEvaluated(evaluated);
	setEvaluator(evaluator);
	setCurrentObjectiveVersion(0);
	setSiadapRootModule(SiadapRootModule.getInstance());
    }

    public List<ObjectiveEvaluation> getObjectiveEvaluations() {
	return getEvaluations(ObjectiveEvaluation.class, new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		ObjectiveEvaluation objective = (ObjectiveEvaluation) arg0;
		return objective.isValidForVersion(getCurrentObjectiveVersion());
	    }

	});

    }

    public List<CompetenceEvaluation> getCompetenceEvaluations() {
	return getEvaluations(CompetenceEvaluation.class, null);
    }

    private <T extends SiadapEvaluationItem> List<T> getEvaluations(Class<T> clazz, Predicate predicate) {
	List<T> evaluationItems = new ArrayList<T>();
	for (SiadapEvaluationItem item : getSiadapEvaluationItems()) {
	    if (item.getClass().isAssignableFrom(clazz) && (predicate == null || predicate.evaluate(item))) {
		evaluationItems.add((T) item);
	    }
	}
	Collections.sort(evaluationItems, SiadapEvaluationItem.COMPARATOR_BY_DATE);
	return evaluationItems;
    }

    public boolean isEvaluationDone() {
	return getProcess().hasBeenExecuted(Evaluation.class);
    }

    private BigDecimal getEvaluationScoring(List<? extends SiadapEvaluationItem> evaluations) {
	if (!isEvaluationDone()) {
	    return BigDecimal.ZERO;
	}

	BigDecimal result = new BigDecimal(0);
	for (SiadapEvaluationItem evaluation : evaluations) {
	    result = result.add(new BigDecimal(evaluation.getEvaluation().getPoints()));
	}

	return result.divide(new BigDecimal(evaluations.size()), Siadap.PRECISION, Siadap.ROUND_MODE);
    }

    public BigDecimal getObjectivesScoring() {
	return getEvaluationScoring(getObjectiveEvaluations());
    }

    private BigDecimal getPonderationResult(BigDecimal scoring, Double usedPercentage) {
	BigDecimal percentage = BigDecimal.valueOf(usedPercentage).divide(new BigDecimal(100));

	BigDecimal result = percentage.multiply(scoring);
	return result.setScale(Siadap.PRECISION, Siadap.ROUND_MODE);
    }

    public BigDecimal getPonderatedObjectivesScoring() {
	return getPonderationResult(getObjectivesScoring(), SiadapRootModule.getInstance().getObjectivesPonderation());
    }

    public BigDecimal getCompetencesScoring() {
	return getEvaluationScoring(getCompetenceEvaluations());
    }

    public BigDecimal getPonderatedCompetencesScoring() {
	return getPonderationResult(getCompetencesScoring(), SiadapRootModule.getInstance().getCompetencesPonderation());
    }

    public BigDecimal getTotalEvaluationScoring() {
	return getPonderatedCompetencesScoring().add(getPonderatedObjectivesScoring());
    }
}
