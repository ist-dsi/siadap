package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapYearConfiguration;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

public class ChangeCustomSchedule extends WorkflowActivity<SiadapProcess, ChangeCustomScheduleActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	SiadapYearConfiguration configuration = siadap.getSiadapYearConfiguration();
	//TODO change this into a defined group (?)
	return configuration.isCurrentUserMemberOfScheduleExtenders();
    }

    @Override
    protected void process(ChangeCustomScheduleActivityInformation activityInformation) {
	/*
	 * Siadap.setCustomSchedule(activityInformation.get)
	 * 
	 * 
	 * ObjectiveEvaluation objectiveEvaluation = new
	 * ObjectiveEvaluation(activityInformation.getSiadap(),
	 * activityInformation .getObjective(), activityInformation.getType());
	 * 
	 * for (ObjectiveIndicator indicator :
	 * activityInformation.getIndicators()) {
	 * objectiveEvaluation.addObjectiveIndicator
	 * (indicator.getMeasurementIndicator(),
	 * indicator.getSuperationCriteria(),
	 * indicator.getBigDecimalPonderationFactor()); }
	 */
    }

    @Override
    public boolean isDefaultInputInterfaceUsed() {
	return false;
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
	return false;
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new ChangeCustomScheduleActivityInformation(process, this);
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }

}
