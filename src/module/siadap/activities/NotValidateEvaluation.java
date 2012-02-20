package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

public class NotValidateEvaluation extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	//	return siadap.getSiadapYearConfiguration().isPersonMemberOfCCA(user.getPerson()) && siadap.getHarmonizationDate() != null
	//		&& siadap.getValidationDateOfDefaultEvaluation() == null;
	//TODO joantune: remove this, it has been replaced by the more generic validation activity
	return false;
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	//	activityInformation.getProcess().getSiadap().setValidated(Boolean.FALSE);
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }
}
