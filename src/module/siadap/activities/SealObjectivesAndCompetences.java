package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

import org.joda.time.LocalDate;

public class SealObjectivesAndCompetences extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return user == siadap.getEvaluator().getPerson().getUser()
		&& siadap.getObjectivesAndCompetencesSealedDate() == null;
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	activityInformation.getProcess().getSiadap().setObjectivesAndCompetencesSealedDate(new LocalDate());
    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
    	return true;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }
}
