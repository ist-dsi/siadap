package module.siadap.activities;

import java.util.ResourceBundle;

import module.siadap.activities.ChangeCustomScheduleActivityInformation.CustomScheduleRepresentation;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapYearConfiguration;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;
import myorg.util.BundleUtil;

public class ChangeCustomSchedule extends WorkflowActivity<SiadapProcess, ChangeCustomScheduleActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	return shouldBeAbleToChangeSchedules(process, user);
    }

    public static boolean shouldBeAbleToChangeSchedules(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	SiadapYearConfiguration configuration = siadap.getSiadapYearConfiguration();
	return configuration.isPersonMemberOfScheduleExtenders(user.getPerson());
    }

    /*
     * @Override protected String[]
     * getArgumentsDescription(ChangeCustomScheduleActivityInformation
     * activityInformation) { return new String[] { "teste" }; }
     */

    //check if each existing CustomScheduleRepresentation is complete and only one of each kind exists
    private void checkCustomScheduleRepresentations(ChangeCustomScheduleActivityInformation activityInformation) {
	boolean foundObjectiveSpecificationBeginPeriod = false;
	boolean foundObjectiveSpecificationEndPeriod = false;

	boolean foundValidAutoEvaluationBeginPeriod = false;
	boolean foundValidAutoEvaluationEndPeriod = false;

	boolean foundEvaluationBeginPeriod = false;
	boolean foundEvaluationEndPeriod = false;

	for (CustomScheduleRepresentation customScheduleRepresentation : activityInformation.getCustomScheduleRepresentations()) {
	    ResourceBundle resourceBundle = DomainException.getResourceFor("resources/SiadapResources");

	    if (!customScheduleRepresentation.isComplete())
		throw new DomainException("error.incomplete.CustomScheduleRepresentation", resourceBundle);

	    switch (customScheduleRepresentation.getTypeOfSchedule()) {
	    case OBJECTIVES_SPECIFICATION_BEGIN_DATE:
		if (!foundObjectiveSpecificationBeginPeriod)
		    foundEvaluationBeginPeriod = true;
		else
		    throw new DomainException("error.duplicated.CustomScheduleRepresentation", resourceBundle,
			    customScheduleRepresentation.getTypeOfSchedule().getLocalizedName());
		break;
	    case OBJECTIVES_SPECIFICATION_END_DATE:
		if (!foundObjectiveSpecificationEndPeriod)
		    foundObjectiveSpecificationEndPeriod = true;
		else
		    throw new DomainException("error.duplicated.CustomScheduleRepresentation", resourceBundle,
			    customScheduleRepresentation.getTypeOfSchedule().getLocalizedName());
		break;
	    case AUTOEVALUATION_BEGIN_DATE:
		if (!foundValidAutoEvaluationBeginPeriod)
		    foundValidAutoEvaluationBeginPeriod = true;
		else
		    throw new DomainException("error.duplicated.CustomScheduleRepresentation", resourceBundle,
			    customScheduleRepresentation.getTypeOfSchedule().getLocalizedName());
		break;
	    case AUTOEVALUATION_END_DATE:
		if (!foundValidAutoEvaluationEndPeriod)
		    foundValidAutoEvaluationEndPeriod = true;
		else
		    throw new DomainException("error.duplicated.CustomScheduleRepresentation", resourceBundle,
			    customScheduleRepresentation.getTypeOfSchedule().getLocalizedName());
		break;
	    case EVALUATION_BEGIN_DATE:
		if (!foundEvaluationBeginPeriod)
		    foundEvaluationBeginPeriod = true;
		else
		    throw new DomainException("error.duplicated.CustomScheduleRepresentation", resourceBundle,
			    customScheduleRepresentation.getTypeOfSchedule().getLocalizedName());
		break;
	    case EVALUATION_END_DATE:
		if (!foundEvaluationEndPeriod)
		    foundEvaluationEndPeriod = true;
		else
		    throw new DomainException("error.duplicated.CustomScheduleRepresentation", resourceBundle,
			    customScheduleRepresentation.getTypeOfSchedule().getLocalizedName());
		break;
	    }

	}

    }

    @Override
    protected void process(ChangeCustomScheduleActivityInformation activityInformation) {

	//let's check for duplicates of deadlines
	checkCustomScheduleRepresentations(activityInformation);

	//ok, we have no duplicates, we can proccess each representation
	for (CustomScheduleRepresentation scheduleRepresentation : activityInformation.getCustomScheduleRepresentations()) {
	    activityInformation.getProcess().changeCustomSiadapSchedule(scheduleRepresentation.getTypeOfSchedule(),
		    scheduleRepresentation.getNewDeadlineDate());

	}

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
    protected String[] getArgumentsDescription(ChangeCustomScheduleActivityInformation activityInformation) {
	String labelBase = "label.description." + getClass().getName();
	String[] stringToReturn = { "" };
	for (CustomScheduleRepresentation scheduleRepresentation : activityInformation.getCustomScheduleRepresentations()) {
	    stringToReturn[0] = stringToReturn[0].concat(BundleUtil.getFormattedStringFromResourceBundle(getUsedBundle(),
		    labelBase + ".oneChangeSchedule", scheduleRepresentation.getTypeOfSchedule().getLocalizedName(),
		    scheduleRepresentation.getNewDeadlineDate().toString(), scheduleRepresentation.getJustification()));

	}
	return stringToReturn;
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
