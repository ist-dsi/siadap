package module.siadap.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import module.organization.domain.Person;
import module.organizationIst.domain.listner.LoginListner;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;

import org.joda.time.LocalDate;

import pt.ist.emailNotifier.domain.Email;
import pt.ist.fenixframework.plugins.remote.domain.exception.RemoteException;
import pt.utl.ist.fenix.tools.util.i18n.Language;

public class SubmitForObjectivesAcknowledge extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return user == siadap.getEvaluator().getPerson().getUser() && siadap.isWithObjectivesFilled()
		&& siadap.isCoherentOnTypeOfEvaluation() && siadap.hasAllEvaluationItemsValid()
		&& siadap.getRequestedAcknowledgeDate() == null && siadap.getObjectivesAndCompetencesSealedDate() != null;
    }

    private void checkEmailExistenceImportAndWarnOnError(Person person) {
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
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	if (!activityInformation.getProcess().getSiadap().hasAllEvaluationItemsValid())
	    throw new DomainException("activity.SealObjectivesAndCompetences.invalid.objectives",
		    DomainException.getResourceFor("resources/SiadapResources"));
	activityInformation.getProcess().getSiadap().setRequestedAcknowledgeDate(new LocalDate());
	Siadap siadap = activityInformation.getProcess().getSiadap();
	ArrayList<String> toAddress = new ArrayList<String>();
	ArrayList<String> ccAddress = new ArrayList<String>();
	Person evaluatorPerson = null;
	String emailEvaluator = null;
	try {
	    evaluatorPerson = activityInformation.getProcess().getSiadap().getEvaluator().getPerson();
	    checkEmailExistenceImportAndWarnOnError(evaluatorPerson);
	    emailEvaluator = evaluatorPerson.getRemotePerson().getEmailForSendingEmails();

	} catch (RemoteException ex) {
	    if (evaluatorPerson != null) {
		System.out.println("Could not get e-mail for evaluator " + evaluatorPerson.getName());
	    } else {
		System.out.println("Could not get e-mail for evaluator which has no Person object associated!");
	    }
	}

	try {
	    final Person evaluatedPerson = activityInformation.getProcess().getSiadap().getEvaluated();
	    checkEmailExistenceImportAndWarnOnError(evaluatedPerson);
	    String emailEvaluated = evaluatedPerson.getRemotePerson().getEmailForSendingEmails();

	    if (emailEvaluated != null) {
		toAddress.add(emailEvaluated);
		if (emailEvaluator != null) {
		    ccAddress.add(emailEvaluator);
		}

		StringBuilder body = new StringBuilder(
			"Encontram-se disponiveis em https://dot.ist.utl.pt os objectivos e competências relativos ao ano de "
				+ siadap.getYear() + ".\n");
		body.append("\nPara mais informações consulte https://dot.ist.utl.pt\n");
		body.append("\n\n---\n");
		body.append("Esta mensagem foi enviada por meio das Aplicações Centrais do IST.\n");

		new Email("Aplicação SIADAP do IST", "noreply@ist.utl.pt", new String[] {}, toAddress, ccAddress,
			Collections.EMPTY_LIST, "SIADAP - Tomada de conhecimento de objectivos e competências", body.toString());
	    }
	} catch (RemoteException ex) {
	    System.out.println("Unable to lookup email address for: "
		    + activityInformation.getProcess().getSiadap().getEvaluated().getUser().getUsername() + " Error: "
		    + ex.getMessage());
	    throw new DomainException("error.message.could.not.send.email.now", ResourceBundle.getBundle(
		    "resources/SiadapResources", Language.getLocale()));
	}

    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }
}
