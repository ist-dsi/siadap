package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapAutoEvaluation;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;
import myorg.util.BundleUtil;

public class AutoEvaluation extends WorkflowActivity<SiadapProcess, AutoEvaluationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return siadap.getEvaluated().getUser() == user && !siadap.isAutoEvaliationDone()
		&& siadap.isEvaluatedWithKnowledgeOfObjectives() && !siadap.getSiadapEvaluationItems().isEmpty()
		&& siadap.getAutoEvaluationInterval().containsNow();
    }

    @Override
    public boolean isDefaultInputInterfaceUsed() {
	return false;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }


    @Override
    public ActivityInformation getActivityInformation(SiadapProcess process) {
	return new AutoEvaluationActivityInformation(process, this);
    }

    @Override
    protected void process(AutoEvaluationActivityInformation activityInformation) {

	new SiadapAutoEvaluation(activityInformation.getProcess().getSiadap(), activityInformation.getObjectivesJustification(),
		activityInformation.getCompetencesJustification(), activityInformation.getOtherFactorsJustification(),
		activityInformation.getExtremesJustification(), activityInformation.getCommentsAndProposals(),
		activityInformation.getFactorOneClassification(), activityInformation.getFactorTwoClassification(),
		activityInformation.getFactorThreeClassification(), activityInformation.getFactorFourClassification(),
		activityInformation.getFactorFiveClassification(), activityInformation.getFactorSixClassification());

    }
}
