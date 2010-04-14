package module.siadap.domain;

public class ObjectiveEvaluation extends ObjectiveEvaluation_Base {

    public ObjectiveEvaluation(Siadap siadap, String objective, String measurementIndicator, String superationCriteria) {
	super();
	setObjective(objective);
	setMeasurementIndicator(measurementIndicator);
	setSuperationCriteria(superationCriteria);
	setSiadap(siadap);
	setFromVersion(siadap.getCurrentObjectiveVersion());
	setUntilVersion(null);
    }

    public boolean isValidForVersion(Integer version) {
	Integer untilVersion = getUntilVersion();
	return getFromVersion() >= version && (untilVersion == null || untilVersion <= version);
    }

}