/**
 * 
 */
package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapYearConfiguration;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt)
 * 
 */
public class RevertState extends WorkflowActivity<SiadapProcess, RevertStateActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	return shouldBeAbleToRevertState(process, user);
    }

    private static boolean shouldBeAbleToRevertState(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	SiadapYearConfiguration configuration = siadap.getSiadapYearConfiguration();
	return configuration.isPersonMemberOfRevertStateGroup(user.getPerson());
    }

    @Override
    protected void process(RevertStateActivityInformation activityInformation) {
	//let's revert the process to the given state
	SiadapProcessStateEnum processStateEnumToRevertTo = activityInformation.getSiadapProcessStateEnum();
	boolean auxNotifyIntervenients = true;

	switch (processStateEnumToRevertTo) {
	case NOT_SEALED:
	    SealObjectivesAndCompetences.revertProcess(activityInformation);
	case NOT_YET_SUBMITTED_FOR_ACK:
	    SubmitForObjectivesAcknowledge.revertProcess(activityInformation, auxNotifyIntervenients);
	    auxNotifyIntervenients = false;
	case WAITING_EVAL_OBJ_ACK:
	    AcknowledgeEvaluationObjectives.revertProcess(activityInformation, auxNotifyIntervenients);
	    break;
	case INCOMPLETE_OBJ_OR_COMP:
	case EVALUATION_NOT_GOING_TO_BE_DONE:
	case NOT_CREATED:
	case UNIMPLEMENTED_STATE:
	default:
	    throw new DomainException("activity.RevertState.error.invalidStateToChangeTo",
		    DomainException.getResourceFor("resources/SiadapResources"));

	}

    }

    @Override
    protected String[] getArgumentsDescription(RevertStateActivityInformation activityInformation) {
	return new String[] { activityInformation.getSiadapProcessStateEnum().getLocalizedName(),
		activityInformation.getJustification() };
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new RevertStateActivityInformation(process, this);
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
	return false;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }

}
