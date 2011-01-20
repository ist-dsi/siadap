package module.siadap.activities;

import module.siadap.activities.CreateObjectiveEvaluationActivityInformation.ObjectiveIndicator;
import module.siadap.domain.ObjectiveEvaluation;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

public class EditObjectiveEvaluation extends WorkflowActivity<SiadapProcess, EditObjectiveEvaluationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return siadap.getObjectiveSpecificationInterval().containsNow() && !process.hasBeenExecuted(AutoEvaluation.class)
		&& siadap.getEvaluator().getPerson().getUser() == user;
    }

    @Override
    protected void process(EditObjectiveEvaluationActivityInformation activityInformation) {
	ObjectiveEvaluation evaluation = activityInformation.getEvaluation().edit(activityInformation.getObjective(),
		activityInformation.getJustification(), activityInformation.getType());
	for (ObjectiveIndicator indicator : activityInformation.getIndicators()) {
	    evaluation.addObjectiveIndicator(indicator.getMeasurementIndicator(), indicator.getSuperationCriteria(), indicator
		    .getBigDecimalPonderationFactor());
	}
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new EditObjectiveEvaluationActivityInformation(process, this);
    }
    
    @Override
    protected boolean shouldLogActivity(EditObjectiveEvaluationActivityInformation activityInformation) {
    	if (activityInformation.getProcess().getSiadap().getObjectivesAndCompetencesSealedDate() != null)
    		return true;
    	else return false;
    }

    @Override
    public boolean isVisible() {
	return false;
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
	return false;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }

    @Override
    protected String[] getArgumentsDescription(EditObjectiveEvaluationActivityInformation activityInformation) {
	if (activityInformation.isEmployJustification()) {
	return new String[] { activityInformation.getObjective(), activityInformation.getJustification() };
	} else
	    return new String[] { activityInformation.getObjective() };
    }

    @Override
    public boolean isDefaultInputInterfaceUsed() {
	return false;
    }
}
