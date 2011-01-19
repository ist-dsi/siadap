package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

public class RevertNoEvaluation extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return siadap.isWithSkippedEvaluation() && siadap.getEvaluator().getPerson().getUser() == user
		&& siadap.getValidated() == null && siadap.getEvaluationInterval().containsNow();
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	activityInformation.getProcess().getSiadap().getEvaluationData().setNoEvaluationJustification(null);
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
	return false;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }
}
