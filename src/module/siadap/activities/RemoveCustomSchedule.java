package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessSchedulesEnum;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;

public class RemoveCustomSchedule extends WorkflowActivity<SiadapProcess, RemoveCustomScheduleActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	return ChangeCustomSchedule.shouldBeAbleToChangeSchedules(process, user);
    }

    @Override
    protected void process(RemoveCustomScheduleActivityInformation activityInformation) {
	SiadapProcessSchedulesEnum processSchedulesEnum = SiadapProcessSchedulesEnum.valueOf(activityInformation
		.getSiadapProcessSchedulesEnumToRemove());
	Siadap siadap = activityInformation.getSiadap();
	if (processSchedulesEnum == null || siadap == null)
	    throw new DomainException("error.could.not.remove.custom.schedule");
	activityInformation.getProcess().changeCustomSiadapSchedule(processSchedulesEnum, null);

    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
	return true;
    }

    @Override
    public boolean isVisible() {
	return false;
    }

    @Override
    public boolean isDefaultInputInterfaceUsed() {
	return true;
    }

    @Override
    protected String[] getArgumentsDescription(RemoveCustomScheduleActivityInformation activityInformation) {
	SiadapProcessSchedulesEnum processSchedulesEnum = SiadapProcessSchedulesEnum.valueOf(activityInformation
		.getSiadapProcessSchedulesEnumToRemove());
	return new String[] { processSchedulesEnum.getLocalizedName() };
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new RemoveCustomScheduleActivityInformation(process, this);
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process, User user) {
	return false;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }

}
