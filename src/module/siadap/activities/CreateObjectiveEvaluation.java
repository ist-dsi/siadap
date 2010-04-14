package module.siadap.activities;

import module.siadap.domain.ObjectiveEvaluation;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

public class CreateObjectiveEvaluation extends WorkflowActivity<SiadapProcess, CreateObjectiveEvaluationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	return process.getSiadap().getEvaluator().getUser() == user;
    }

    @Override
    protected void process(CreateObjectiveEvaluationActivityInformation activityInformation) {
	new ObjectiveEvaluation(activityInformation.getSiadap(), activityInformation.getObjective(), activityInformation
		.getMeasurementIndicator(), activityInformation.getSuperationCriteria());
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new CreateObjectiveEvaluationActivityInformation(process, this);
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }
}
