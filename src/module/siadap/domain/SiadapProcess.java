package module.siadap.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import module.organization.domain.Person;
import module.organizationIst.domain.listner.LoginListner;
import module.siadap.activities.AcknowledgeEvaluationObjectives;
import module.siadap.activities.AcknowledgeEvaluationValidation;
import module.siadap.activities.AcknowledgeHomologation;
import module.siadap.activities.AutoEvaluation;
import module.siadap.activities.CreateCompetenceEvaluation;
import module.siadap.activities.CreateObjectiveEvaluation;
import module.siadap.activities.EditCompetenceEvaluation;
import module.siadap.activities.EditObjectiveEvaluation;
import module.siadap.activities.Evaluation;
import module.siadap.activities.GrantExcellencyAward;
import module.siadap.activities.Homologate;
import module.siadap.activities.NoEvaluation;
import module.siadap.activities.NotValidateEvaluation;
import module.siadap.activities.RemoveObjectiveEvaluation;
import module.siadap.activities.RevertNoEvaluation;
import module.siadap.activities.RevokeExcellencyAward;
import module.siadap.activities.SealObjectivesAndCompetences;
import module.siadap.activities.SubmitForObjectivesAcknowledge;
import module.siadap.activities.ValidateEvaluation;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.SiadapProcessStateEnum;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.LabelLog;
import module.workflow.domain.WorkflowProcess;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;
import myorg.util.ClassNameBundle;
import pt.ist.emailNotifier.domain.Email;
import pt.ist.fenixWebFramework.services.Service;

@ClassNameBundle(bundle = "resources/SiadapResources", key = "label.process.siadap")
public class SiadapProcess extends SiadapProcess_Base {

    private static List<WorkflowActivity<SiadapProcess, ? extends ActivityInformation<SiadapProcess>>> activities = new ArrayList<WorkflowActivity<SiadapProcess, ? extends ActivityInformation<SiadapProcess>>>();

    static {
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

    public SiadapProcess(Integer year, Person evaluated) {
	super();

	User currentUser = UserView.getCurrentUser();
	Person possibleEvaluator = currentUser.getPerson();
	PersonSiadapWrapper evaluator = new PersonSiadapWrapper(evaluated, year).getEvaluator();

	if (evaluator == null || evaluator.getPerson() != possibleEvaluator) {
	    throw new DomainException("error.onlyEvaluatorCanCreateSiadap");
	}

	setSiadap(new Siadap(year, evaluated));
	setProcessNumber("S" + year + "/" + evaluated.getUser().getUsername());

	new LabelLog(this, currentUser, this.getClass().getName() + ".creation", "resources/SiadapResources",
		evaluated.getName(), year.toString());
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
		|| configuration.isPersonResponsibleForHomologation(person);
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

}
