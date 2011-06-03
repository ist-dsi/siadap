package module.siadap.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.MissingResourceException;

import module.organization.domain.Person;
import module.organizationIst.domain.listner.LoginListner;
import module.siadap.activities.AcknowledgeEvaluationObjectives;
import module.siadap.activities.AcknowledgeEvaluationValidation;
import module.siadap.activities.AcknowledgeHomologation;
import module.siadap.activities.AutoEvaluation;
import module.siadap.activities.ChangeCustomSchedule;
import module.siadap.activities.CreateCompetenceEvaluation;
import module.siadap.activities.CreateObjectiveEvaluation;
import module.siadap.activities.EditCompetenceEvaluation;
import module.siadap.activities.EditObjectiveEvaluation;
import module.siadap.activities.Evaluation;
import module.siadap.activities.GrantExcellencyAward;
import module.siadap.activities.Homologate;
import module.siadap.activities.NoEvaluation;
import module.siadap.activities.NotValidateEvaluation;
import module.siadap.activities.RemoveCustomSchedule;
import module.siadap.activities.RemoveObjectiveEvaluation;
import module.siadap.activities.RevertNoEvaluation;
import module.siadap.activities.RevertState;
import module.siadap.activities.RevertStateActivityInformation;
import module.siadap.activities.RevokeExcellencyAward;
import module.siadap.activities.SealObjectivesAndCompetences;
import module.siadap.activities.SubmitForObjectivesAcknowledge;
import module.siadap.activities.ValidateEvaluation;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.LabelLog;
import module.workflow.domain.WorkflowProcess;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.RoleType;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;
import myorg.domain.groups.Role;
import myorg.util.BundleUtil;
import myorg.util.ClassNameBundle;

import org.joda.time.LocalDate;

import pt.ist.emailNotifier.domain.Email;
import pt.ist.fenixWebFramework.services.Service;

@ClassNameBundle(bundle = "resources/SiadapResources", key = "label.process.siadap")
public class SiadapProcess extends SiadapProcess_Base {

    private static List<WorkflowActivity<SiadapProcess, ? extends ActivityInformation<SiadapProcess>>> activities = new ArrayList<WorkflowActivity<SiadapProcess, ? extends ActivityInformation<SiadapProcess>>>();

    static {
	activities.add(new ChangeCustomSchedule());
	activities.add(new RevertState());
	activities.add(new RemoveCustomSchedule());
	activities.add(new CreateObjectiveEvaluation());
	activities.add(new RemoveObjectiveEvaluation());
	activities.add(new CreateCompetenceEvaluation());
	activities.add(new EditCompetenceEvaluation());
	activities.add(new SealObjectivesAndCompetences());
	activities.add(new AcknowledgeEvaluationObjectives());
	activities.add(new AutoEvaluation());
	activities.add(new Evaluation());
	activities.add(new ValidateEvaluation());
	activities.add(new AcknowledgeEvaluationValidation());
	activities.add(new Homologate());
	activities.add(new AcknowledgeHomologation());
	activities.add(new EditObjectiveEvaluation());
	activities.add(new SubmitForObjectivesAcknowledge());
	activities.add(new NotValidateEvaluation());
	activities.add(new NoEvaluation());
	activities.add(new RevertNoEvaluation());
	activities.add(new GrantExcellencyAward());
	activities.add(new RevokeExcellencyAward());
    }

    private HashMap<User, ArrayList<String>> userWarningsKey = new HashMap<User, ArrayList<String>>();

    public SiadapProcess(Integer year, Person evaluated) {
	super();

	User currentUser = UserView.getCurrentUser();
	Person possibleEvaluator = currentUser.getPerson();
	PersonSiadapWrapper evaluator = new PersonSiadapWrapper(evaluated, year).getEvaluator();
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);

	boolean belongsToASuperGroup = false;
	if ((configuration.getCcaMembers() != null && configuration.getCcaMembers().contains(currentUser.getPerson()))
		|| (configuration.getScheduleEditors() != null && configuration.getScheduleEditors().contains(
			currentUser.getPerson())) || Role.getRole(RoleType.MANAGER).isMember(currentUser)) {
	    belongsToASuperGroup = true;
	}
	if (!belongsToASuperGroup) {
	    if (evaluator == null || evaluator.getPerson() != possibleEvaluator) {
		throw new DomainException("error.onlyEvaluatorCanCreateSiadap");
	    }
	}

	setSiadap(new Siadap(year, evaluated));
	setProcessNumber("S" + year + "/" + evaluated.getUser().getUsername());

	new LabelLog(this, currentUser, this.getClass().getName() + ".creation", "resources/SiadapResources",
		evaluated.getName(), year.toString());
    }

    public List<String> getAndClearWarningMessages() {
	List<String> warningMessages = getWarningMessages();
	clearWarningMessagesForCurrentUser();
	return warningMessages;
    }

    public List<String> getWarningMessages() {
	User currentUser = UserView.getCurrentUser();
	ArrayList<String> warningMessagesToReturn = new ArrayList<String>();
	//for each let's try to translate it using the resources, case it can't be found we print it as it is
	ArrayList<String> warningMessages = getUserWarningsKey().get(currentUser);
	if (warningMessages == null) {
	    warningMessages = new ArrayList<String>();
	    getUserWarningsKey().put(currentUser, warningMessages);
	}
	for (String string : warningMessages) {
	    try {
		warningMessagesToReturn.add(BundleUtil.getFormattedStringFromResourceBundle("resources/SiadapResources", string));
	    } catch (MissingResourceException e) {
		warningMessagesToReturn.add(string);
	    }
	}

	return warningMessagesToReturn;
    }

    public void addWarningMessage(String messageKeyOrContent) {
	User currentUser = UserView.getCurrentUser();
	addWarningMessage(currentUser, messageKeyOrContent);
    }

    protected void addWarningMessage(User user, String messageKeyOrContent) {
	ArrayList<String> warningMessages = getUserWarningsKey().get(user);
	if (warningMessages == null) {
	    warningMessages = new ArrayList<String>();
	}
	warningMessages.add(messageKeyOrContent);
	getUserWarningsKey().put(user, warningMessages);
    }

    protected void clearWarningMessagesForCurrentUser() {
	User currentUser = UserView.getCurrentUser();
	clearWarningMessages(currentUser);
    }

    protected void clearWarningMessages(User user) {
	getUserWarningsKey().put(user, new ArrayList<String>());
    }

    @Override
    public <T extends WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>> List<T> getActivities() {
	return (List<T>) activities;
    }

    @Override
    public User getProcessCreator() {
	return getSiadap().getEvaluator().getPerson().getUser();
    }

    @Override
    public boolean isActive() {
	return true;
    }

    @Override
    public void notifyUserDueToComment(User user, String comment) {
	// TODO Auto-generated method stub
    }

    @Service
    public static SiadapProcess createNewProcess(Person evaluated, Integer year) {
	return new SiadapProcess(year, evaluated);
    }

    @Override
    public boolean isAccessible(User user) {
	Person person = user.getPerson();
	SiadapYearConfiguration configuration = getSiadap().getSiadapYearConfiguration();
	return person == getSiadap().getEvaluated() || person == getSiadap().getEvaluator().getPerson()
		|| isResponsibleForHarmonization(person, getSiadap().getEvaluated()) || configuration.isPersonMemberOfCCA(person)
		|| configuration.isPersonResponsibleForHomologation(person)
		|| configuration.isPersonMemberOfScheduleExtenders(person);
    }

    private boolean isResponsibleForHarmonization(Person accessor, Person evaluated) {
	PersonSiadapWrapper wrapper = new PersonSiadapWrapper(evaluated, getSiadap().getYear());
	return wrapper.isResponsibleForHarmonization(accessor);
    }

    public void markAsHarmonized() {
	new LabelLog(this, UserView.getCurrentUser(), "label.terminateHarmonization", "resources/SiadapResources");
    }

    public void removeHarmonizationMark() {
	new LabelLog(this, UserView.getCurrentUser(), "label.reOpenHarmonization", "resources/SiadapResources");
    }

    public boolean isUserEvaluated(User user) {
	return getSiadap().getEvaluated().getUser() == user;
    }

    public boolean isProcessSealed() {
	return getSiadap().getObjectivesAndCompetencesSealedDate() != null;
    }

    public boolean isCurrentUserEvaluated() {
	return isUserEvaluated(UserView.getCurrentUser());
    }

    public void checkEmailExistenceImportAndWarnOnError(Person person) {
	//if we have no info about the person, let's import it
	if (person.getRemotePerson() == null || person.getRemotePerson().getEmailForSendingEmails() == null) {
	    LoginListner.importUserInformation(person.getUser().getUsername());
	}
	//if that didn't solved it, let's warn the admin by e-mail
	if (person.getRemotePerson() == null || person.getRemotePerson().getEmailForSendingEmails() == null) {
	    StringBuilder message = new StringBuilder("Error, could not import e-mail/info for person " + person.getName() + "\n");
	    if (person.getUser() != null && person.getUser().getUsername() != null) {
		message.append("the username is: " + person.getUser().getUsername() + "\n");

	    }
	    message.append("Please take appropriate actions\n");
	    notifyAdmin("[Bennu/Myorg] - Error retrieving remote information from fenix for a user", message.toString());

	}
    }

    //TODO change this so that the e-mail isn't hardcoded and there is a batch sent not for each error an e-mail
    private void notifyAdmin(String subject, String message) {
	ArrayList<String> toAddress = new ArrayList<String>();
	toAddress.add("joao.antunes@tagus.ist.utl.pt");
	new Email("Aplicação SIADAP", "noreply@ist.utl.pt", new String[] {}, toAddress, Collections.EMPTY_LIST,
		Collections.EMPTY_LIST, subject, message);
    }

    @Override
    public boolean isTicketSupportAvailable() {
	return false;
    }

    public boolean isNotSubmittedForConfirmation() {
	return SiadapProcessStateEnum.isNotSubmittedForConfirmation(getSiadap());
    }

    public HashMap<User, ArrayList<String>> getUserWarningsKey() {
	if (userWarningsKey == null)
	    userWarningsKey = new HashMap<User, ArrayList<String>>();
	return userWarningsKey;
    }

    /**
     * Changes the custom deadlines as specified by the parameter
     * processSchedulesEnum which must be one of
     * {@link SiadapProcessSchedulesEnum}
     * 
     * @param processSchedulesEnum
     *            the representation of which schedule
     *            {@link SiadapProcessSchedulesEnum}
     * @param newDate
     *            the new date to use for this schedule
     */
    @Service
    public void changeCustomSiadapSchedule(SiadapProcessSchedulesEnum processSchedulesEnum, LocalDate newDate) {
	Siadap siadap = getSiadap();
	switch (processSchedulesEnum) {
	case OBJECTIVES_SPECIFICATION_BEGIN_DATE:
	    siadap.setCustomObjectiveSpecificationBegin(newDate);
	    break;
	case OBJECTIVES_SPECIFICATION_END_DATE:
	    siadap.setCustomObjectiveSpecificationEnd(newDate);
	    break;
	case AUTOEVALUATION_BEGIN_DATE:
	    siadap.setCustomAutoEvaluationBegin(newDate);
	    break;
	case AUTOEVALUATION_END_DATE:
	    siadap.setCustomAutoEvaluationEnd(newDate);
	    break;
	case EVALUATION_BEGIN_DATE:
	    siadap.setCustomEvaluationBegin(newDate);
	    break;
	case EVALUATION_END_DATE:
	    siadap.setCustomEvaluationEnd(newDate);
	    break;
	default:
	    throw new DomainException();
	}

    }

    /**
     * Method that is called whenever there are changes to the evaluation
     * objectives (either the objectives or the competences got deleted/edited)
     * in which case, this method triggers the side effects of doing so. At the
     * moment it will only revert the status from submitted to the evaluatee to
     * not submitted and possibly warn the user about it
     */
    public void signalChangesInEvaluationObjectives() {
	int stateOrdinal = SiadapProcessStateEnum.getState(getSiadap()).ordinal();
	if (stateOrdinal == SiadapProcessStateEnum.WAITING_EVAL_OBJ_ACK.ordinal()
		|| stateOrdinal == SiadapProcessStateEnum.WAITING_SELF_EVALUATION.ordinal()) {
	    RevertState revertState = null;
	    for (WorkflowActivity activity : activities) {
		if (RevertState.class.isAssignableFrom(activity.getClass())) {
		    revertState = (RevertState) activity;
		    break;
		}
	    }
	    if (revertState == null)
		revertState = new RevertState();
	    revertState.setSideEffect(true);
	    RevertStateActivityInformation activityInformation = (RevertStateActivityInformation) revertState
		    .getActivityInformation(this);
	    activityInformation.setSiadapProcessStateEnum(SiadapProcessStateEnum.NOT_YET_SUBMITTED_FOR_ACK);
	    activityInformation.setJustification("Edição/remoção de objectivos/competências");
	    revertState.execute(activityInformation);
	    return;
	}
    }

    public boolean isSubmittedForEvalObjsConfirmation() {
	return getSiadap().getRequestedAcknowledgeDate() != null;
    }

    public boolean isEvalObjectivesAcknowledged() {
	return getSiadap().isEvaluatedWithKnowledgeOfObjectives();
    }

}
