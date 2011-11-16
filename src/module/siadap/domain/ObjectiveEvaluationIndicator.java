package module.siadap.domain;

import java.math.BigDecimal;

import myorg.domain.exceptions.DomainException;

public class ObjectiveEvaluationIndicator extends ObjectiveEvaluationIndicator_Base {

    public ObjectiveEvaluationIndicator(ObjectiveEvaluation objective, String measurementIndicator, String superationCriteria,
	    BigDecimal ponderationFactor) {
	super();
	if (!(ponderationFactor.compareTo(BigDecimal.ZERO) >= 0 && ponderationFactor.compareTo(BigDecimal.ONE) <= 0)) {
	    throw new DomainException("error.ponderation.has.to.be.between.0.and.1", DomainException
		    .getResourceFor("resources/SiadapResources"));
	}

	setObjectiveEvaluation(objective);
	setMeasurementIndicator(measurementIndicator);
	setSuperationCriteria(superationCriteria);
	setPonderationFactor(ponderationFactor);

	setSiadapRootModule(SiadapRootModule.getInstance());
    }

    public BigDecimal getAutoEvaluationPoints() {
	return getAutoEvaluation().getPoints().multiply(getPonderationFactor());
    }

    /**
     * Disconnects itself from the world, and removes itself from the DB
     */
    public void delete() {
	removeObjectiveEvaluation();
	removeSiadapRootModule();
	deleteDomainObject();
    }

    public BigDecimal getEvaluationPoints() {
	if (getEvaluation() == null)
	    return null;
	return getEvaluation().getPoints().multiply(getPonderationFactor());
    }

}
