package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapYearConfiguration;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;
import myorg.util.BundleUtil;

public class RemoveObjectiveEvaluation extends WorkflowActivity<SiadapProcess, RemoveObjectiveEvaluationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	SiadapYearConfiguration siadapYearConfiguration = siadap.getSiadapYearConfiguration();
	return siadapYearConfiguration.isCurrentUserMemberOfCCA() || siadap.getObjectiveSpecificationInterval().containsNow()
		&& siadap.getEvaluator().getPerson().getUser() == user
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
	    siadap.getDefaultSiadapEvaluationUniverse().removeSiadapEvaluationItems(activityInformation.getEvaluation());
	    activityInformation.getEvaluation().delete();
	} else {
	    SiadapEvaluationUniverse siadapEvaluationUniverse = activityInformation.getEvaluation().getSiadapEvaluationUniverse();
	    Integer currentObjectiveVersion = siadapEvaluationUniverse.getCurrentObjectiveVersion();
	    int newVersion = currentObjectiveVersion + 1;
	    activityInformation.getEvaluation().setUntilVersion(currentObjectiveVersion);
	    siadapEvaluationUniverse.setCurrentObjectiveVersion(newVersion);

	}
	//notify that there have been changes - if we are not a member of the CCA
	SiadapYearConfiguration siadapYearConfiguration = siadap.getSiadapYearConfiguration();
	if (!siadapYearConfiguration.isCurrentUserMemberOfCCA()
		|| siadap.getEvaluator().getPerson().getUser().equals(UserView.getCurrentUser()))
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
	Siadap siadap = process.getSiadap();
	SiadapYearConfiguration siadapYearConfiguration = siadap.getSiadapYearConfiguration();
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
	    if (!siadapYearConfiguration.isCurrentUserMemberOfCCA()
		    || siadap.getEvaluator().getPerson().getUser().equals(UserView.getCurrentUser()))
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
