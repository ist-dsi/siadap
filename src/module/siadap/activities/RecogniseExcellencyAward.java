package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

public class RecogniseExcellencyAward extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	// This has to be the CCA though
	return siadap.getEvaluator().getPerson().getUser() == user && siadap.getValidationDate() != null
		&& siadap.getEvaluationData().getExcellencyAward() && siadap.getAcknowledgeValidationDate() == null;
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	activityInformation.getProcess().getSiadap().getEvaluationData().setExcellencyAwardRecognized(Boolean.TRUE);
    }

     @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }
}
