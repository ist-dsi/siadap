package module.siadap.activities;

import module.siadap.domain.Competence;
import module.siadap.domain.CompetenceEvaluation;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;
import myorg.util.BundleUtil;

public class CreateCompetenceEvaluation extends
	WorkflowActivity<SiadapProcess, CreateOrEditCompetenceEvaluationActivityInformation> {

    // @Override
//    public boolean isActive(SiadapProcess process, User user) {
//	Siadap siadap = process.getSiadap();
//	return !siadap.isObjectiveSpecificationIntervalFinished()
//		&& siadap.getEvaluator().getPerson().getUser() == user
//		&& siadap.getCompetenceEvaluations().isEmpty();
//    }
    

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return !siadap.isObjectiveSpecificationIntervalFinished()
		&& siadap.getEvaluator().getPerson().getUser() == user
		&& !siadap.hasAnyCompetencesSet() && !process.hasBeenExecuted(AutoEvaluation.class);
    }

    @Override
    protected void process(CreateOrEditCompetenceEvaluationActivityInformation activityInformation) {
	for (Competence competence : activityInformation.getCompetences()) {
	    new CompetenceEvaluation(activityInformation.getSiadap(), competence);
	}
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new CreateOrEditCompetenceEvaluationActivityInformation(process, this);
    }


    @Override
    public boolean isDefaultInputInterfaceUsed() {
	return false;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }
}
