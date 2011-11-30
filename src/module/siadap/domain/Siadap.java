package module.siadap.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import module.organization.domain.Person;
import module.siadap.domain.scoring.IScoring;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.siadap.domain.util.SiadapPendingProcessesCounter;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.workflow.domain.utils.WorkflowCommentCounter;
import module.workflow.widgets.ProcessListWidget;
import module.workflow.widgets.UnreadCommentsWidget;
import myorg.domain.exceptions.DomainException;

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;

import pt.ist.fenixWebFramework.services.Service;

public class Siadap extends Siadap_Base {

    //register itself in the pending processes widget:
    static {
	ProcessListWidget.register(new SiadapPendingProcessesCounter());
	UnreadCommentsWidget.register(new WorkflowCommentCounter(SiadapProcess.class));
    }

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

	}, ObjectiveEvaluation.COMPARATOR_BY_OLDEST_DATE);

    }

    public List<CompetenceEvaluation> getCompetenceEvaluations() {
	return getEvaluations(CompetenceEvaluation.class, null, null);
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
    private <T extends SiadapEvaluationItem> List<T> getEvaluations(Class<T> clazz, Predicate predicate, Comparator<T> comparator) {
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

    public boolean isAutoEvaliationDone() {
	return getAutoEvaluationSealedDate() != null;
    }

    public boolean isEvaluationDone() {
	return getEvaluationSealedDate() != null;
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
	}, null);

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
	if (getCurrentEvaluationItems() == null || getCurrentEvaluationItems().isEmpty())
	    return false;
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
			&& performanceObjectives >= MINIMUM_PERFORMANCE_OBJECTIVES_NUMBER && qualityObjectives >= MINIMUM_QUALITY_OBJECTIVES_NUMBER));
    }

    public boolean hasAllNeededCompetences() {
	return (getEvaluatedOnlyByCompetences() != null && ((getEvaluatedOnlyByCompetences().booleanValue() == false && getCompetences()
		.size() >= MINIMUM_COMPETENCES_WITH_OBJ_EVAL_NUMBER) || (getEvaluatedOnlyByCompetences().booleanValue() == true && getCompetences()
		.size() >= MINIMUM_COMPETENCES_WITHOUT_OBJ_EVAL_NUMBER)));

    }

    public boolean hasRelevantEvaluation() {
	return SiadapGlobalEvaluation.HIGH.accepts(getTotalEvaluationScoring());
    }

    public boolean hasExcellencyAward() {
	if (getEvaluationData() == null || getEvaluationData().getExcellencyAward() == null)
		return false;
	return getEvaluationData().getExcellencyAward();
    }

    public boolean isInadequate() {
	return SiadapGlobalEvaluation.LOW.accepts(getTotalEvaluationScoring())
		|| SiadapGlobalEvaluation.ZERO.accepts(getTotalEvaluationScoring());
    }

    @Override
    public SiadapYearConfiguration getSiadapYearConfiguration() {
	return SiadapYearConfiguration.getSiadapYearConfiguration(getYear());
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
	return new Interval(convertDateToBeginOfDay(begin), convertDateToEndOfDay(end));
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
	return new Interval(convertDateToBeginOfDay(begin), convertDateToEndOfDay(end));
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

	return new Interval(convertDateToBeginOfDay(begin), convertDateToEndOfDay(end));
    }

    /**
     * 
     * @param date
     *            the {@link LocalDate} that will be converted to represent the
     *            date at the beginning of the day
     * @return an {@link ReadableInstant} with the same day/month/year but the
     *         first instant of it, that is the first hour, first minute, first
     *         second etc...
     */
    private ReadableInstant convertDateToBeginOfDay(LocalDate date) {
	ReadableInstant newLocalDate = null;
	if (date != null)
	{
	    return date.toDateTimeAtStartOfDay();
	}
	return newLocalDate;

    }

    /**
     * 
     * @param date
     *            the {@link LocalDate} that will be converted to represent the
     *            date at the beginning of the day
     * @return an {@link ReadableInstant} with the same day/month/year but the
     *         last instant of it, that is the last hour, last minute, last
     *         second etc...
     */
    private ReadableInstant convertDateToEndOfDay(LocalDate date) {
	ReadableInstant newLocalDate = null;
	if (date != null) {
	    return new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 23, 59, 59, 59);

	}
	return newLocalDate;

    }

    //TODO change this appropriately when Issue #31 is resolved
    private boolean isAutoEvaluationScheduleDefined() {

	SiadapYearConfiguration configuration = getSiadapYearConfiguration();
	LocalDate begin = configuration.getAutoEvaluationBegin();
	LocalDate end = configuration.getAutoEvaluationEnd();
	if (end == null || begin == null)
	    return false;
	return true;
    }

    //TODO change this appropriately when Issue #31 is resolved
    private boolean isEvaluationScheduleDefined() {

	SiadapYearConfiguration configuration = getSiadapYearConfiguration();
	LocalDate begin = configuration.getEvaluationBegin();
	LocalDate end = configuration.getEvaluationEnd();
	if (end == null || begin == null)
	    return false;
	return true;
    }

    //TODO change this appropriately when Issue #31 is resolved
    private boolean isObjectiveSpecificationScheduleDefined() {
	SiadapYearConfiguration configuration = getSiadapYearConfiguration();
	LocalDate begin = configuration.getObjectiveSpecificationBegin();
	LocalDate end = configuration.getObjectiveSpecificationEnd();
	if (end == null || begin == null)
	    return false;
	return true;
    }

    public boolean isAutoEvaluationIntervalFinished() {
	//TODO change this appropriately when Issue #31 is resolved
	if (!isAutoEvaluationScheduleDefined())
	    return false;
	return getAutoEvaluationInterval().isBeforeNow();
    }

    public boolean isEvaluationIntervalFinished() {
	if (!isEvaluationScheduleDefined())
	    return false;
	//TODO change this appropriately when Issue #31 is resolved
	return getEvaluationInterval().isBeforeNow();
    }

    public boolean isObjectiveSpecificationIntervalFinished() {
	if (!isObjectiveSpecificationScheduleDefined())
	    return false;
	//TODO change this appropriately when Issue #31 is resolved
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
