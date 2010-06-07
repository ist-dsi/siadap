package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

public class EditObjectiveEvaluation extends WorkflowActivity<SiadapProcess, EditObjectiveEvaluationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return !siadap.isObjectiveSpecificationIntervalFinished() && !process.hasBeenExecuted(AutoEvaluation.class)
		&& siadap.getEvaluator().getPerson().getUser() == user;
    }

    @Override
    protected void process(EditObjectiveEvaluationActivityInformation activityInformation) {
	activityInformation.getEvaluation().edit(activityInformation.getObjective(),
		activityInformation.getMeasurementIndicator(), activityInformation.getSuperationCriteria(),
		activityInformation.getJustification(), activityInformation.getType());
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new EditObjectiveEvaluationActivityInformation(process, this);
    }

    @Override
    public boolean isVisible() {
	return false;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }

    @Override
    protected String[] getArgumentsDescription(EditObjectiveEvaluationActivityInformation activityInformation) {
	return new String[] { activityInformation.getObjective(), activityInformation.getJustification() };
    }
}
