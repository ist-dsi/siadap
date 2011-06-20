/**
 * 
 */
package module.siadap.domain.util.scripts;

import java.util.ArrayList;

import module.siadap.activities.ChangeCustomSchedule;
import module.siadap.activities.ChangeCustomScheduleActivityInformation;
import module.siadap.activities.ChangeCustomScheduleActivityInformation.CustomScheduleRepresentation;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcessSchedulesEnum;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import myorg.applicationTier.Authenticate;
import myorg.domain.User;
import myorg.domain.scheduler.WriteCustomTask;

import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.security.UserView;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt)
 * 
 */
public class CorrectUserXSchedule extends WriteCustomTask {

    /* (non-Javadoc)
     * @see myorg.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
	String username = "ist24386";
	String operatorUsername = "ist154457";
	User userToAlter = User.findByUsername(username);
	out.println("Retrieving info for user: " + username);
	//let's print all of the schedules:
	PersonSiadapWrapper siadapWrapper = new PersonSiadapWrapper(userToAlter.getPerson(), 2011);
	Siadap siadap = siadapWrapper.getSiadap();
	out.println("Objective specification begin date : " + siadap.getObjectiveSpecificationBeginDate());
	out.println("Objective specification end date : " + siadap.getObjectiveSpecificationEndDate());
	out.println("Auto Evaluation begin date : " + siadap.getAutoEvaluationBeginDate());
	out.println("Auto Evaluation end date : " + siadap.getAutoEvaluationEndDate());
	out.println("Evaluation begin date : " + siadap.getEvaluationBeginDate());
	out.println("Evaluation end date : " + siadap.getEvaluationEndDate());
	
	//let's set the userView to my username
	final User userThatMadeTheChange = User.findByUsername(operatorUsername);
	final myorg.applicationTier.Authenticate.UserView currentUserView = UserView.getUser();
	final myorg.applicationTier.Authenticate.UserView userView = Authenticate.authenticate(userThatMadeTheChange);

	//correcting ObjectiveSpecificationBeginDate
	ChangeCustomSchedule changeCustomScheduleActivity = new ChangeCustomSchedule();

	ChangeCustomScheduleActivityInformation activityInformation = (ChangeCustomScheduleActivityInformation) changeCustomScheduleActivity
		.getActivityInformation(siadap.getProcess());

	ArrayList<CustomScheduleRepresentation> customScheduleRepresentations = new ArrayList<ChangeCustomScheduleActivityInformation.CustomScheduleRepresentation>();
	CustomScheduleRepresentation representation = new CustomScheduleRepresentation(
		SiadapProcessSchedulesEnum.OBJECTIVES_SPECIFICATION_END_DATE, new LocalDate(), "Correcção do sistema", siadap);
	customScheduleRepresentations.add(representation);
	activityInformation.setCustomScheduleRepresentation(customScheduleRepresentations);

	try {
	    UserView.setUser(userView);
	    changeCustomScheduleActivity.execute(activityInformation);
	} finally {
	    UserView.setUser(currentUserView);
	}


    }

}
