package module.siadap.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import module.organization.domain.Person;
import module.siadap.activities.AutoEvaluation;
import module.siadap.activities.Evaluation;
import module.siadap.domain.scoring.IScoring;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import myorg.domain.exceptions.DomainException;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.services.Service;

public class Siadap extends Siadap_Base {

    private static final int PRECISION = 3;
    private static final int ROUND_MODE = BigDecimal.ROUND_HALF_EVEN;

    public static final int MINIMUM_EFICIENCY_OBJECTIVES_NUMBER = 1;
    public static final int MINIMUM_PERFORMANCE_OBJECTIVES_NUMBER = 1;
    public static final int MINIMUM_QUALITY_OBJECTIVES_NUMBER = 1;

    public static final int MINIMUM_COMPETENCES_WITH_OBJ_EVAL_NUMBER = 6;

    public static final int MINIMUM_COMPETENCES_WITHOUT_OBJ_EVAL_NUMBER = 8;

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
	    IScoring itemEvaluation = evaluation.getItemEvaluation();
	    if (itemEvaluation == null) {
		throw new DomainException("error.siadapEvaluation.mustFillAllItems",
			DomainException.getResourceFor("resources/SiadapResources"));
	    }
	    result = result.add(itemEvaluation.getPoints());
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
	return getSiadapYearConfiguration().getObjectivesPonderation();
    }

    public Double getCompetencesPonderation() {
	return getSiadapYearConfiguration().getCompetencesPonderation();
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

    public PersonSiadapWrapper getEvaluatedWrapper() {
	return new PersonSiadapWrapper(getEvaluated(), getYear());
    }
    public PersonSiadapWrapper getEvaluator() {
	return new PersonSiadapWrapper(getEvaluated(), getYear()).getEvaluator();
    }

    public boolean hasAnyCompetencesSet() {
	if (!hasAnySiadapEvaluationItems())
	    return false;
	ArrayList<SiadapEvaluationItem> evaluationItems = new ArrayList<SiadapEvaluationItem>(getSiadapEvaluationItems());
	for (SiadapEvaluationItem siadapEvaluationItem : evaluationItems) {
	    if (siadapEvaluationItem instanceof CompetenceEvaluation)
		return true;
	}
	return false;
    }

    /**
     * 
     * @return the CompetenceType associated with this process or null if it
     *         hasn't been set yet
     * @author João André Pereira Antunes (joao.antunes@tagus.ist.utl.pt)
     */
    public CompetenceType getCompetenceType() {
	if (!hasAnySiadapEvaluationItems())
	    return null;
	ArrayList<SiadapEvaluationItem> evaluationItems = new ArrayList<SiadapEvaluationItem>(getSiadapEvaluationItems());
	for (SiadapEvaluationItem siadapEvaluationItem : evaluationItems) {
	    if (siadapEvaluationItem instanceof CompetenceEvaluation)
		return ((CompetenceEvaluation) siadapEvaluationItem).getCompetence().getCompetenceType();
	}
	return null;
    }

    /**
     * @return An ArrayList with the competences attributed to this Siadap
     *         process. If it has none set it will return an empty ArrayList
     * @author João André Pereira Antunes (joao.antunes@tagus.ist.utl.pt)
     */
    public ArrayList<Competence> getCompetences() {
	ArrayList<Competence> arrayCompetences = new ArrayList<Competence>();
	if (!hasAnySiadapEvaluationItems())
	    return arrayCompetences;
	ArrayList<SiadapEvaluationItem> evaluationItems = new ArrayList<SiadapEvaluationItem>(getSiadapEvaluationItems());
	for (SiadapEvaluationItem siadapEvaluationItem : evaluationItems) {
	    if (siadapEvaluationItem instanceof CompetenceEvaluation)
		arrayCompetences.add(((CompetenceEvaluation) siadapEvaluationItem).getCompetence());
	}
	return arrayCompetences;
    }

    public boolean hasSealedObjectivesAndCompetences() {
	if (getObjectivesAndCompetencesSealedDate() == null)
	    return false;
	return true;
    }

    public boolean isWithObjectivesFilled() {
	int competencesCounter = 0;
	int efficiencyObjectives = 0;
	int performanceObjectives = 0;
	int qualityObjectives = 0;

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
		    case QUALITY:
			qualityObjectives++;
			break;
		    }
		}
	    }
	}
	return hasAllNeededCompetences()
		&& ((getEvaluatedOnlyByCompetences() == null || getEvaluatedOnlyByCompetences()) || (efficiencyObjectives >= MINIMUM_EFICIENCY_OBJECTIVES_NUMBER
		&& performanceObjectives >= MINIMUM_PERFORMANCE_OBJECTIVES_NUMBER
 && qualityObjectives >= MINIMUM_QUALITY_OBJECTIVES_NUMBER));
    }

    public boolean hasAllNeededCompetences() {
	return (getEvaluatedOnlyByCompetences() != null && ((getEvaluatedOnlyByCompetences().booleanValue() == false && getCompetences()
		.size() >= MINIMUM_COMPETENCES_WITH_OBJ_EVAL_NUMBER) || (getEvaluatedOnlyByCompetences().booleanValue() == true && getCompetences()
		.size() >= MINIMUM_COMPETENCES_WITHOUT_OBJ_EVAL_NUMBER)));

    }

    public boolean hasRelevantEvaluation() {
	return isEvaluationDone() && SiadapGlobalEvaluation.HIGH.accepts(getTotalEvaluationScoring());
    }

    public boolean hasExcellencyAward() {
	return isEvaluationDone() && getEvaluationData().getExcellencyAward();
    }

    public boolean isInadequate() {
	return isEvaluationDone() && SiadapGlobalEvaluation.LOW.accepts(getTotalEvaluationScoring());
    }

    public SiadapYearConfiguration getSiadapYearConfiguration() {
	return SiadapYearConfiguration.getSiadapYearConfiguration(getYear());
    }

    public Interval getAutoEvaluationInterval() {
	SiadapYearConfiguration configuration = getSiadapYearConfiguration();
	LocalDate begin = configuration.getAutoEvaluationBegin();
	LocalDate end = configuration.getAutoEvaluationEnd();
	return new Interval(begin.toDateMidnight(), end.toDateMidnight());
    }

    public Interval getEvaluationInterval() {
	SiadapYearConfiguration configuration = getSiadapYearConfiguration();
	LocalDate begin = configuration.getEvaluationBegin();
	LocalDate end = configuration.getEvaluationEnd();
	return new Interval(begin.toDateMidnight(), end.toDateMidnight());
    }

    public Interval getObjectiveSpecificationInterval() {
	SiadapYearConfiguration configuration = getSiadapYearConfiguration();
	LocalDate begin = configuration.getObjectiveSpecificationBegin();
	LocalDate end = configuration.getObjectiveSpecificationEnd();
	return new Interval(begin.toDateMidnight(), end.toDateMidnight());
    }

    public boolean isAutoEvaluationIntervalFinished() {
	return getAutoEvaluationInterval().isBeforeNow();
    }

    public boolean isEvaluationIntervalFinished() {
	return getEvaluationInterval().isBeforeNow();
    }

    public boolean isObjectiveSpecificationIntervalFinished() {
	return getObjectiveSpecificationInterval().isBeforeNow();
    }

    @Service
    public void markAsHarmonized(LocalDate harmonizationDate) {
	setHarmonizationDate(harmonizationDate);
	getProcess().markAsHarmonized();
    }

    @Service
    public void removeHarmonizationMark() {
	setHarmonizationDate(null);
	getProcess().removeHarmonizationMark();
    }

    public boolean isHomologated() {
	return getHomologationDate() != null;
    }

    public boolean isSuggestedForExcellencyAward() {
	SiadapEvaluation evaluationData = getEvaluationData();
	return evaluationData != null && evaluationData.getExcellencyAward() == Boolean.TRUE;
    }

    public boolean isWithSkippedEvaluation() {
	SiadapEvaluation evaluationData = getEvaluationData();
	return evaluationData != null && !StringUtils.isEmpty(evaluationData.getNoEvaluationJustification());
    }

    public boolean hasAllEvaluationItemsValid() {
	for (ObjectiveEvaluation objectiveEvaluation : getObjectiveEvaluations()) {
	    if (!objectiveEvaluation.isValid())
		return false;

	}
	return true;
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
	if ((getEvaluatedOnlyByCompetences() != null && getEvaluatedOnlyByCompetences()) && getObjectiveEvaluations() != null
		&& getObjectiveEvaluations().size() != 0)
	    return false;
	return true;
    }
}