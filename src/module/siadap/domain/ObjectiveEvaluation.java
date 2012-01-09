package module.siadap.domain;

import java.math.BigDecimal;
import java.util.Comparator;

import module.siadap.domain.scoring.IScoring;
import myorg.domain.exceptions.DomainException;

public class ObjectiveEvaluation extends ObjectiveEvaluation_Base {

    public static Comparator<ObjectiveEvaluation> COMPARATOR_BY_OLDEST_DATE = new Comparator<ObjectiveEvaluation>() {

	@Override
	public int compare(ObjectiveEvaluation o1, ObjectiveEvaluation o2) {
	    return getOldestObjective(o1).getWhenCreated().compareTo(getOldestObjective(o2).getWhenCreated());
	}

	/**
	 * Iterates through the related objectiveEvaluation objects on
	 * objectiveEvaluation and returns the oldest
	 */
	private ObjectiveEvaluation getOldestObjective(ObjectiveEvaluation objectiveEvaluation) {
	    if (objectiveEvaluation.getOldObjectiveEvaluation() == null)
		return objectiveEvaluation;
	    else {
		return getOldestObjective(objectiveEvaluation.getOldObjectiveEvaluation());
	    }
	}

    };

    public ObjectiveEvaluation(Siadap siadap, String objective, SiadapEvaluationObjectivesType type) {
	super();
	setObjective(objective);
	SiadapEvaluationUniverse defaultSiadapEvaluationUniverse = siadap.getDefaultSiadapEvaluationUniverse();
	if (getSiadapEvaluationUniverse() == null)
	    setSiadapEvaluationUniverse(defaultSiadapEvaluationUniverse);
	getSiadapEvaluationUniverse().setSiadap(siadap);
	setFromVersion(getSiadapEvaluationUniverse().getCurrentObjectiveVersion());
	setUntilVersion(null);
	setType(type);
    }

    private ObjectiveEvaluation(Siadap siadap, String objective, String justification, SiadapEvaluationObjectivesType type) {
	this(siadap, objective, type);
	setJustificationForModification(justification);
    }

    public boolean isValidForVersion(Integer version) {
	Integer untilVersion = getUntilVersion();
	return getFromVersion() <= version && (untilVersion == null || untilVersion >= version);
    }

    public ObjectiveEvaluation edit(String objective, String editionJustification, SiadapEvaluationObjectivesType type) {
	Siadap siadap = getSiadap();
	SiadapEvaluationUniverse siadapEvaluationUniverse = getSiadapEvaluationUniverse();
	Integer currentObjectiveVersion = siadapEvaluationUniverse.getCurrentObjectiveVersion();
	int newVersion = currentObjectiveVersion + 1;
	setUntilVersion(currentObjectiveVersion);
	siadapEvaluationUniverse.setCurrentObjectiveVersion(newVersion);
	ObjectiveEvaluation newObjectiveEvaluation = new ObjectiveEvaluation(siadap, objective, editionJustification, type);
	//set the oldest and newest references
	newObjectiveEvaluation.setOldObjectiveEvaluation(this);
	setNewObjectiveEvaluation(newObjectiveEvaluation);
	return newObjectiveEvaluation;
    }

    public Siadap getSiadap() {
	return getSiadapEvaluationUniverse().getSiadap();
    }

    public void addObjectiveIndicator(String measurementIndicator, String superationCriteria, BigDecimal ponderationFactor) {
	BigDecimal sum = BigDecimal.ZERO;
	for (ObjectiveEvaluationIndicator indicator : getIndicators()) {
	    sum = sum.add(indicator.getPonderationFactor());
	}
	if (sum.add(ponderationFactor).compareTo(BigDecimal.ONE) > 0) {
	    throw new DomainException("error.ponderation.cannot.be.over.100",
		    DomainException.getResourceFor("resources/SiadapResources"));
	}
	new ObjectiveEvaluationIndicator(this, measurementIndicator, superationCriteria, ponderationFactor);
    }

    @Override
    public IScoring getItemAutoEvaluation() {
	return new IScoring() {
	    @Override
	    public BigDecimal getPoints() {
		BigDecimal points = new BigDecimal(0);
		for (ObjectiveEvaluationIndicator indicator : getIndicators()) {
		    points = points.add(indicator.getAutoEvaluationPoints());
		}

		return points;
	    }
	};
    }

    @Override
    public IScoring getItemEvaluation() {
	return new IScoring() {
	    @Override
	    public BigDecimal getPoints() {
		BigDecimal points = new BigDecimal(0);
		for (ObjectiveEvaluationIndicator indicator : getIndicators()) {
		    BigDecimal indicatorPoints = indicator.getEvaluationPoints();
		    if (indicatorPoints == null)
			return null;
		    points = points.add(indicatorPoints);
		}

		return points;
	    }
	};
    }

    @Override
    public boolean isValid() {
	BigDecimal sum = BigDecimal.ZERO;
	for (ObjectiveEvaluationIndicator indicator : getIndicators()) {
	    sum = sum.add(indicator.getPonderationFactor());
	}
	return sum.compareTo(BigDecimal.ONE) == 0;
    }

    /**
     * Removes and deletes its indicators, disconnects itself from the world,
     * and removes himself from the DB
     */
    public void delete() {
	for (ObjectiveEvaluationIndicator indicator : getIndicators()) {
	    removeIndicators(indicator);
	    indicator.delete();
	}
	//unlink from the old only, if there is a newer, it should give an exception
	if (getOldObjectiveEvaluation() != null) {
	    getOldObjectiveEvaluation().removeNewObjectiveEvaluation();
	}
	removeOldObjectiveEvaluation();
	removeSiadapEvaluationUniverse();
	removeSiadapRootModule();
	deleteDomainObject();
    }
}