package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

import org.apache.commons.lang.StringUtils;

public class CreateObjectiveEvaluationActivityInformation extends ActivityInformation<SiadapProcess> {

    private Siadap siadap;
    private String objective;
    private String measurementIndicator;
    private String superationCriteria;

    public CreateObjectiveEvaluationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
    }

    @Override
    public void setProcess(SiadapProcess process) {
	super.setProcess(process);
	setSiadap(process.getSiadap());
    }

    public Siadap getSiadap() {
	return siadap;
    }

    public void setSiadap(Siadap siadap) {
	this.siadap = siadap;
    }

    public String getObjective() {
	return objective;
    }

    public void setObjective(String objective) {
	this.objective = objective;
    }

    public String getMeasurementIndicator() {
	return measurementIndicator;
    }

    public void setMeasurementIndicator(String measurementIndicator) {
	this.measurementIndicator = measurementIndicator;
    }

    public String getSuperationCriteria() {
	return superationCriteria;
    }

    public void setSuperationCriteria(String superationCriteria) {
	this.superationCriteria = superationCriteria;
    }

    @Override
    public boolean hasAllneededInfo() {
	return getSiadap() != null && !StringUtils.isEmpty(getObjective()) && !StringUtils.isEmpty(getMeasurementIndicator())
		&& !StringUtils.isEmpty(getSuperationCriteria());
    }

}
