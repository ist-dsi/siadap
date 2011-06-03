package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;
import myorg.util.BundleUtil;

public class RemoveObjectiveEvaluation extends WorkflowActivity<SiadapProcess, RemoveObjectiveEvaluationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return siadap.getObjectiveSpecificationInterval().containsNow() && siadap.getEvaluator().getPerson().getUser() == user
		&& SiadapProcessStateEnum.getState(siadap).ordinal() <= SiadapProcessStateEnum.WAITING_EVAL_OBJ_ACK.ordinal();
    }

    @Override
    protected void process(RemoveObjectiveEvaluationActivityInformation activityInformation) {
	Siadap siadap = activityInformation.getSiadap();
	SiadapProcess process = siadap.getProcess();
	// TODO probably will have to alter this in the near future, related
	// with Issue #2, most likely depending if it is sealed or not, and if
	// it is we will want to add it to some object that will store the old
	// objectives
	if (siadap.getObjectivesAndCompetencesSealedDate() == null) {
	    siadap.removeSiadapEvaluationItems(activityInformation.getEvaluation());
	    activityInformation.getEvaluation().delete();
	} else {
	    Integer currentObjectiveVersion = siadap.getCurrentObjectiveVersion();
	    int newVersion = currentObjectiveVersion + 1;
	    activityInformation.getEvaluation().setUntilVersion(currentObjectiveVersion);
	    siadap.setCurrentObjectiveVersion(newVersion);

	}
	//whatever the case, notify that there have been changes
	process.signalChangesInEvaluationObjectives();
    }

    @Override
    protected boolean shouldLogActivity(RemoveObjectiveEvaluationActivityInformation activityInformation) {
	if (activityInformation.getProcess().getSiadap().getObjectivesAndCompetencesSealedDate() != null)
	    return true;
	else
	    return false;
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new RemoveObjectiveEvaluationActivityInformation(process, this);
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
	return false;
    }

    @Override
    public boolean isVisible() {
	return false;
    }

    @Override
    public String getLocalizedConfirmationMessage(SiadapProcess process) {
	SiadapProcessStateEnum currentState = SiadapProcessStateEnum.getState(process.getSiadap());
	switch (currentState) {
	case NOT_CREATED:
	case INCOMPLETE_OBJ_OR_COMP:
	case NOT_SEALED:
	case EVALUATION_NOT_GOING_TO_BE_DONE:
	case NOT_YET_SUBMITTED_FOR_ACK:
	    break;
	case WAITING_EVAL_OBJ_ACK:
	case WAITING_SELF_EVALUATION:
	case NOT_YET_EVALUATED:
	case UNIMPLEMENTED_STATE:
	    return BundleUtil.getStringFromResourceBundle(getUsedBundle(), "edit.warning.removing.will.change.state");
	}
	return super.getLocalizedConfirmationMessage(process);

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
