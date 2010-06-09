package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluation;
import module.siadap.domain.SiadapEvaluationItem;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityException;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;
import myorg.util.BundleUtil;

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
	SiadapEvaluation evaluationData = siadap.getEvaluationData();
	if (evaluationData == null) {
	    new SiadapEvaluation(siadap, activityInformation.getEvaluationJustification(), activityInformation
		    .getPersonalDevelopment(), activityInformation.getTrainningNeeds(), activityInformation.getExcellencyAward());
	} else {
	    evaluationData.edit(activityInformation.getEvaluationJustification(), activityInformation.getPersonalDevelopment(),
		    activityInformation.getTrainningNeeds(), activityInformation.getExcellencyAward());
	}

	for (SiadapEvaluationItem item : siadap.getCurrentEvaluationItems()) {
	    if (item.getItemEvaluation() == null) {
		throw new ActivityException(BundleUtil.getStringFromResourceBundle(getUsedBundle(),
			"error.siadapEvaluation.mustFillAllItems"), getLocalizedName());
	    }
	}
    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
	Siadap siadap = process.getSiadap();
	return !siadap.isAutoEvaliationDone() && !siadap.isAutoEvaluationIntervalFinished();
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
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new EvaluationActivityInformation(process, this);
    }
}
