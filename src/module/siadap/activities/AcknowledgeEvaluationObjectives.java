package module.siadap.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import module.organization.domain.Person;
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

public class AcknowledgeEvaluationObjectives extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return siadap.getEvaluated().getUser() == user && !siadap.isEvaluatedWithKnowledgeOfObjectives()
		&& siadap.getRequestedAcknowledgeDate() != null;
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	SiadapProcess siadapProcess = activityInformation.getProcess();
	Siadap siadap = siadapProcess.getSiadap();
	siadap.setAcknowledgeDate(new LocalDate());
	ArrayList<String> toAddress = new ArrayList<String>();
	ArrayList<String> ccAddress = new ArrayList<String>();
	Person evaluatorPerson = null;
	String emailEvaluator = null;
	Person evaluatedPerson = null;
	String emailEvaluated = null;
	try {

	    evaluatedPerson = activityInformation.getProcess().getSiadap().getEvaluated();
	    siadapProcess.checkEmailExistenceImportAndWarnOnError(evaluatedPerson);
	    emailEvaluated = evaluatedPerson.getRemotePerson().getEmailForSendingEmails();

	} catch (RemoteException ex) {
	    if (evaluatorPerson != null) {
		System.out.println("Could not get e-mail for evaluator " + evaluatorPerson.getName());
	    } else {
		System.out.println("Could not get e-mail for evaluator which has no Person object associated!");
	    }
	}

	try {
	    evaluatorPerson = activityInformation.getProcess().getSiadap().getEvaluator().getPerson();
	    siadapProcess.checkEmailExistenceImportAndWarnOnError(evaluatorPerson);
	    emailEvaluator = evaluatorPerson.getRemotePerson().getEmailForSendingEmails();
	    if (emailEvaluator != null) {
		toAddress.add(emailEvaluator);
		if (emailEvaluated != null) {
		    ccAddress.add(emailEvaluated);
		}

		StringBuilder body = new StringBuilder("O avaliado '"
			+ activityInformation.getProcess().getSiadap().getEvaluated().getName()
			+ "' declarou que tomou conhecimento dos seus objectivos e competências.");
		body.append("\nPara mais informações consulte https://dot.ist.utl.pt\n");
		body.append("\n\n---\n");
		body.append("Esta mensagem foi enviada por meio das Aplicações Centrais do IST.\n");

		new Email("Aplicação SIADAP do IST", "noreply@ist.utl.pt", new String[] {}, toAddress, ccAddress,
			Collections.EMPTY_LIST, "SIADAP - Tomada de conhecimento de objectivos e competências", body.toString());
	    }
	} catch (final RemoteException ex) {
	    System.out.println("Unable to lookup email address for: "
		    + activityInformation.getProcess().getSiadap().getEvaluated().getUser().getUsername() + " Error: "
		    + ex.getMessage());
	    throw new DomainException("error.message.could.not.send.email.now", ResourceBundle.getBundle(
		    "resources/SiadapResources", Language.getLocale()));
	}
    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
	return true;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }
}
