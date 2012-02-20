package module.siadap.activities;

import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

public class RevokeExcellencyAward extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	//	Siadap siadap = process.getSiadap();
	//	return siadap.getSiadapYearConfiguration().isPersonMemberOfCCA(user.getPerson())
	//		&& Boolean.TRUE.equals(siadap.getValidated())
	//		&& Boolean.TRUE.equals(siadap.getEvaluationData2().getExcellencyAward());
	//TODO joantune: I think this activity is obsolete, just keeping it here untill everything is done before removing it, as it might still prove useful
	return false;
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
	return false;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	activityInformation.getProcess().getSiadap().getEvaluationData2().setExcellencyAward(Boolean.FALSE);
    }

}
