package module.siadap.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.Predicate;

import module.organization.domain.Person;

public class Siadap extends Siadap_Base {

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
}
