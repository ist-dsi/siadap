package module.siadap.activities;

import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

public abstract class GenericEvaluationActivity extends WorkflowActivity<SiadapProcess, EvaluationActivityInformation> {

    @Override
    protected void process(EvaluationActivityInformation activityInformation) {
	// actually we do not nothing here because in the interface
	// we've directly edited the domain object.
    }

    @Override
    public boolean isDefaultInputInterfaceUsed() {
	return false;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new EvaluationActivityInformation(process, this);
    }

}