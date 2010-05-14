package module.siadap.activities;

import org.joda.time.LocalDate;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

public class SubmitForObjectivesAcknowledge extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return user == siadap.getEvaluator().getPerson().getUser() && siadap.isWithObjectivesFilled()
		&& siadap.getRequestedAcknowledgeDate() == null;
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	activityInformation.getProcess().getSiadap().setRequestedAcknowledgeDate(new LocalDate());
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }
}
