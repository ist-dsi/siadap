/**
 * 
 */
package module.siadap.activities;

import java.util.ArrayList;
import java.util.Collections;

import module.organization.domain.Person;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;
import myorg.domain.VirtualHost;

import org.joda.time.LocalDate;

import pt.ist.emailNotifier.domain.Email;
import pt.ist.fenixframework.plugins.remote.domain.exception.RemoteException;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 2 de Mar de 2012
 * 
 * 
 */
public class SubmitValidatedEvaluation extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return siadap.getEvaluator().getPerson().getUser() == user && siadap.getValidationDateOfDefaultEvaluation() != null
		&& siadap.getRequestedAcknowledegeValidationDate() == null && siadap.getAcknowledgeValidationDate() == null;
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	activityInformation.getProcess().getSiadap().setRequestedAcknowledegeValidationDate(new LocalDate());

    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
	return true;
    }

    @Override
    public String getUsedBundle() {
	return Siadap.SIADAP_BUNDLE_STRING;
    }

    @Override
    protected void notifyUsers(SiadapProcess process) {

	ArrayList<String> toAddress = new ArrayList<String>();
	ArrayList<String> ccAddress = new ArrayList<String>();
	String emailEvaluated = null;
	Person evaluatedPerson = process.getSiadap().getEvaluated();

	try {
	    SiadapProcess.checkEmailExistenceImportAndWarnOnError(evaluatedPerson);
	    emailEvaluated = evaluatedPerson.getRemotePerson().getEmailForSendingEmails();

	    if (emailEvaluated != null) {
		toAddress.add(emailEvaluated);
		Integer year = process.getSiadap().getYear();

		StringBuilder body = new StringBuilder("A nota final (pós-validação) do seu processo SIADAP de " + year
			+ " encontra-se disponível na plataforma. Necessita de tomar conhecimento da validação\n");
		body.append("\nPara mais informações consulte https://dot.ist.utl.pt\n");
		body.append("\n\n---\n");
		body.append("Esta mensagem foi enviada por meio das Aplicações Centrais do IST.\n");

		final VirtualHost virtualHost = VirtualHost.getVirtualHostForThread();
		new Email(virtualHost.getApplicationSubTitle().getContent(), virtualHost.getSystemEmailAddress(),
			new String[] {}, toAddress, ccAddress, Collections.EMPTY_LIST, "SIADAP - " + year
				+ " Nota final disponível", body.toString());
	    }
	} catch (RemoteException ex) {
	    System.out.println("Unable to lookup email address for: " + evaluatedPerson.getUser().getUsername() + " Error: "
		    + ex.getMessage());
	    process.addWarningMessage("warning.message.could.not.send.email.now");
	}
    }

}