package module.siadap.domain;

import java.math.BigDecimal;

import module.siadap.domain.scoring.IScoring;
import myorg.domain.exceptions.DomainException;

public class ObjectiveEvaluation extends ObjectiveEvaluation_Base {

    public ObjectiveEvaluation(Siadap siadap, String objective, SiadapEvaluationObjectivesType type) {
	super();
	setObjective(objective);
	setSiadap(siadap);
	setFromVersion(siadap.getCurrentObjectiveVersion());
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
	Integer currentObjectiveVersion = siadap.getCurrentObjectiveVersion();
	int newVersion = currentObjectiveVersion + 1;
	setUntilVersion(currentObjectiveVersion);
	siadap.setCurrentObjectiveVersion(newVersion);
	return new ObjectiveEvaluation(siadap, objective, editionJustification, type);
    }

    public void addObjectiveIndicator(String measurementIndicator, String superationCriteria, BigDecimal ponderationFactor) {
	BigDecimal sum = BigDecimal.ZERO;
	for (ObjectiveEvaluationIndicator indicator : getIndicators()) {
	    sum = sum.add(indicator.getPonderationFactor());
	}
	if (sum.add(ponderationFactor).compareTo(BigDecimal.ONE) > 0) {
	    throw new DomainException("error.ponderation.cannot.be.over.1", DomainException
		    .getResourceFor("resources/SiadapResources"));
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
		    points = points.add(indicator.getEvaluationPoints());
		}

		return points;
	    }
	};
    }
}