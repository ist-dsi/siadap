package module.siadap.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import module.organization.domain.Person;
import module.siadap.activities.AutoEvaluation;
import module.siadap.activities.Evaluation;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.siadap.domain.wrappers.PersonSiadapWrapper;

import org.apache.commons.collections.Predicate;
import org.joda.time.LocalDate;

public class Siadap extends Siadap_Base {

    private static final int PRECISION = 3;
    private static final int ROUND_MODE = BigDecimal.ROUND_HALF_EVEN;

    public static final int MINIMUM_EFICIENCY_OBJECTIVES_NUMBER = 1;
    public static final int MINIMUM_PERFORMANCE_OBJECTIVES_NUMBER = 1;
    public static final int MINIMUM_INOVATION_OBJECTIVES_NUMBER = 1;

    public static final int MINIMUM_COMPETENCES_NUMBER = 6;

    public Siadap(int year, Person evaluated) {
	super();
	setYear(year);
	setEvaluated(evaluated);
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
	    if (clazz.isAssignableFrom(item.getClass()) && (predicate == null || predicate.evaluate(item))) {
		evaluationItems.add((T) item);
	    }
	}
	Collections.sort(evaluationItems, SiadapEvaluationItem.COMPARATOR_BY_DATE);
	return evaluationItems;
    }

    public boolean isAutoEvaliationDone() {
	return getProcess().hasBeenExecuted(AutoEvaluation.class);
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
	    result = result.add(new BigDecimal(evaluation.getItemEvaluation().getPoints()));
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
	return getPonderationResult(getObjectivesScoring(), getObjectivesPonderation());
    }

    public BigDecimal getCompetencesScoring() {
	return getEvaluationScoring(getCompetenceEvaluations());
    }

    public BigDecimal getPonderatedCompetencesScoring() {
	return getPonderationResult(getCompetencesScoring(), getCompetencesPonderation());
    }

    public BigDecimal getTotalEvaluationScoring() {
	return getPonderatedCompetencesScoring().add(getPonderatedObjectivesScoring());
    }

    public Double getObjectivesPonderation() {
	return SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getObjectivesPonderation();
    }

    public Double getCompetencesPonderation() {
	return SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getCompetencesPonderation();
    }

    public List<SiadapEvaluationItem> getCurrentEvaluationItems() {
	return getEvaluations(SiadapEvaluationItem.class, new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		return (arg0 instanceof ObjectiveEvaluation) ? ((ObjectiveEvaluation) arg0)
			.isValidForVersion(getCurrentObjectiveVersion()) : true;
	    }
	});

    }

    public boolean isEvaluatedWithKnowledgeOfObjectives() {
	for (SiadapEvaluationItem item : getCurrentEvaluationItems()) {
	    if (item.getAcknowledgeDate() == null) {
		return false;
	    }
	}
	return true;
    }

    public void setAcknowledgeDate(LocalDate acknowledgeDate) {
	for (SiadapEvaluationItem item : getCurrentEvaluationItems()) {
	    if (item.getAcknowledgeDate() == null) {
		item.setAcknowledgeDate(acknowledgeDate);
	    }
	}
    }

    public Person getEvaluator() {
	return new PersonSiadapWrapper(getEvaluated(), getYear()).getEvaluator();
    }

    public boolean isWithObjectivesFilled() {
	int competencesCounter = 0;
	int efficiencyObjectives = 0;
	int performanceObjectives = 0;
	int inovationObjectives = 0;

	Integer currentObjectiveVersion = getCurrentObjectiveVersion();

	for (SiadapEvaluationItem item : getSiadapEvaluationItems()) {

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
		    case INOVATION:
			inovationObjectives++;
			break;
		    }
		}
	    }
	}
	return competencesCounter >= MINIMUM_COMPETENCES_NUMBER && efficiencyObjectives >= MINIMUM_EFICIENCY_OBJECTIVES_NUMBER
		&& performanceObjectives >= MINIMUM_PERFORMANCE_OBJECTIVES_NUMBER
		&& inovationObjectives >= MINIMUM_INOVATION_OBJECTIVES_NUMBER;
    }

    public boolean hasRelevantEvaluation() {
	return isEvaluationDone() && SiadapGlobalEvaluation.HIGH.accepts(getTotalEvaluationScoring());
    }
}