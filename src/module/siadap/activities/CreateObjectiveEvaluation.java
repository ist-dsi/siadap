package module.siadap.activities;

import module.siadap.activities.CreateObjectiveEvaluationActivityInformation.ObjectiveIndicator;
import module.siadap.domain.ObjectiveEvaluation;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

public class CreateObjectiveEvaluation extends WorkflowActivity<SiadapProcess, CreateObjectiveEvaluationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return siadap.getEvaluationInterval().containsNow() && siadap.getEvaluator().getPerson().getUser() == user
		&& siadap.getRequestedAcknowledgeDate() == null
		&& (siadap.getEvaluatedOnlyByCompetences() == null || !siadap.getEvaluatedOnlyByCompetences());
    }

    @Override
    protected void process(CreateObjectiveEvaluationActivityInformation activityInformation) {
	ObjectiveEvaluation objectiveEvaluation = new ObjectiveEvaluation(activityInformation.getSiadap(), activityInformation
		.getObjective(), activityInformation.getType());

	for (ObjectiveIndicator indicator : activityInformation.getIndicators()) {
	    objectiveEvaluation.addObjectiveIndicator(indicator.getMeasurementIndicator(), indicator.getSuperationCriteria(),
		   indicator.getBigDecimalPonderationFactor());
	}
    }

    @Override
    protected boolean shouldLogActivity(CreateObjectiveEvaluationActivityInformation activityInformation) {
	if (activityInformation.getProcess().getSiadap().getObjectivesAndCompetencesSealedDate() != null)
	    return true;
	else
	    return false;
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new CreateObjectiveEvaluationActivityInformation(process, this);
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }

    @Override
    public boolean isDefaultInputInterfaceUsed() {
	return false;
    }
}
