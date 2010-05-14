package module.siadap.activities;

import module.siadap.domain.Competence;
import module.siadap.domain.CompetenceEvaluation;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

public class CreateCompetenceEvaluation extends WorkflowActivity<SiadapProcess, CreateCompetenceEvaluationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	return process.getSiadap().getEvaluator().getPerson().getUser() == user && process.getSiadap().getCompetenceEvaluations().isEmpty();
    }

    @Override
    protected void process(CreateCompetenceEvaluationActivityInformation activityInformation) {
	for (Competence competence : activityInformation.getCompetences()) {
	    new CompetenceEvaluation(activityInformation.getSiadap(), competence);
	}
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new CreateCompetenceEvaluationActivityInformation(process, this);
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }
}
