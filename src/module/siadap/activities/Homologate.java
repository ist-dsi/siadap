package module.siadap.activities;

import org.joda.time.LocalDate;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

public class Homologate extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	// This has to be done by whom? CCA?
	return siadap.getEvaluator().getPerson().getUser() == user && siadap.getAcknowledgeValidationDate() != null
		&& siadap.getHomologationDate() == null;
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	activityInformation.getProcess().getSiadap().setHomologationDate(new LocalDate());
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }
}
