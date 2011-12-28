package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluation;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

public class Evaluation extends WorkflowActivity<SiadapProcess, EvaluationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return !siadap.isWithSkippedEvaluation() && siadap.getEvaluator().getPerson().getUser() == user
		&& siadap.getValidated() == null && siadap.isEvaluatedWithKnowledgeOfObjectives()
		&& siadap.getEvaluationInterval().containsNow();
    }

    @Override
    protected void process(EvaluationActivityInformation activityInformation) {
	Siadap siadap = activityInformation.getProcess().getSiadap();
	SiadapEvaluation evaluationData = siadap.getEvaluationData2();
	if (evaluationData == null) {
	    new SiadapEvaluation(siadap, activityInformation.getEvaluationJustification(), activityInformation
.getPersonalDevelopment(), activityInformation.getTrainningNeeds(),
		    activityInformation.getExcellencyAward(), activityInformation.getExcellencyAwardJustification(),
		    siadap.getDefaultSiadapEvaluationUniverse());
	} else {
	    evaluationData.editWithoutValidation(activityInformation.getEvaluationJustification(),
		    activityInformation.getPersonalDevelopment(),
 activityInformation.getTrainningNeeds(),
		    activityInformation.getExcellencyAward(), activityInformation.getExcellencyAwardJustification());
	}

	//revert the submitted state to unsubmitted
	if (siadap.isEvaluationDone()) {
	    siadap.setEvaluationSealedDate(null);
	}

    }

    @Override
    public boolean isDefaultInputInterfaceUsed() {
	return false;
    }

    @Override
    protected boolean shouldLogActivity(EvaluationActivityInformation activityInformation) {
	return false;
    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
	Siadap siadap = process.getSiadap();
	if (siadap.isEvaluationDone())
	    return true;
	return false;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new EvaluationActivityInformation(process, this);
    }
}
