package module.siadap.activities;

import java.util.ArrayList;
import java.util.Collections;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;

import org.joda.time.LocalDate;

import pt.ist.emailNotifier.domain.Email;
import pt.ist.fenixframework.plugins.remote.domain.exception.RemoteException;

public class SubmitForObjectivesAcknowledge extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return user == siadap.getEvaluator().getPerson().getUser() && siadap.isWithObjectivesFilled()
		&& siadap.isCoherentOnTypeOfEvaluation() && siadap.hasAllEvaluationItemsValid()
		&& siadap.getRequestedAcknowledgeDate() == null && siadap.getObjectivesAndCompetencesSealedDate() != null;
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	if (!activityInformation.getProcess().getSiadap().hasAllEvaluationItemsValid())
	    throw new DomainException("activity.SealObjectivesAndCompetences.invalid.objectives",
		    DomainException.getResourceFor("resources/SiadapResources"));
	activityInformation.getProcess().getSiadap().setRequestedAcknowledgeDate(new LocalDate());
	Siadap siadap = activityInformation.getProcess().getSiadap();
	ArrayList<String> toAddress = new ArrayList<String>();
	try {
	    final String email = activityInformation.getProcess().getSiadap().getEvaluated().getRemotePerson()
		    .getEmailForSendingEmails();
	    if (email != null) {
		toAddress.add(email);
		StringBuilder body = new StringBuilder(
			"Encontram-se disponiveis em https://dot.ist.utl.pt os objectivos e competências relativos ao ano de "
				+ siadap.getYear() + ".\n");
		body.append("\nPara mais informações consulte https://dot.ist.utl.pt\n");
		body.append("\n\n---\n");
		body.append("Esta mensagem foi enviada por meio das Aplicações Centrais do IST.\n");

		new Email("Aplicação SIADAP do IST", "noreply@ist.utl.pt", new String[] {}, toAddress, Collections.EMPTY_LIST,
			Collections.EMPTY_LIST, "SIADAP - Tomada de conhecimento de objectivos e competências", body.toString());
	    }
	} catch (final RemoteException ex) {
	    System.out.println("Unable to lookup email address for: "
		    + activityInformation.getProcess().getSiadap().getEvaluated().getUser().getUsername()
 + " Error: "
		    + ex.getMessage());
	}
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }
}
