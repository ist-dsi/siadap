package module.siadap.domain;

import java.util.ArrayList;
import java.util.List;

import module.organization.domain.Person;
import module.siadap.activities.AcknowledgeEvaluationObjectives;
import module.siadap.activities.AcknowledgeEvaluationValidation;
import module.siadap.activities.AcknowledgeHomologation;
import module.siadap.activities.AutoEvaluation;
import module.siadap.activities.CreateCompetenceEvaluation;
import module.siadap.activities.CreateObjectiveEvaluation;
import module.siadap.activities.EditObjectiveEvaluation;
import module.siadap.activities.Evaluation;
import module.siadap.activities.Homologate;
import module.siadap.activities.SubmitForObjectivesAcknowledge;
import module.siadap.activities.ValidateEvaluation;
import module.siadap.domain.scoring.SiadapCompetencesEvaluation;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.LabelLog;
import module.workflow.domain.WorkflowProcess;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;

import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.services.Service;

public class SiadapProcess extends SiadapProcess_Base {

    private static List<WorkflowActivity<SiadapProcess, ? extends ActivityInformation<SiadapProcess>>> activities = new ArrayList<WorkflowActivity<SiadapProcess, ? extends ActivityInformation<SiadapProcess>>>();

    static {
	activities.add(new CreateObjectiveEvaluation());
	activities.add(new CreateCompetenceEvaluation());
	activities.add(new AcknowledgeEvaluationObjectives());
	activities.add(new AutoEvaluation());
	activities.add(new Evaluation());
	activities.add(new ValidateEvaluation());
	activities.add(new AcknowledgeEvaluationValidation());
	activities.add(new Homologate());
	activities.add(new AcknowledgeHomologation());
	activities.add(new EditObjectiveEvaluation());
	activities.add(new SubmitForObjectivesAcknowledge());
    }

    public SiadapProcess(Integer year, Person evaluated) {
	super();

	User currentUser = UserView.getCurrentUser();
	Person possibleEvaluator = currentUser.getPerson();

	if (SiadapYearConfiguration.getSiadapYearConfiguration(year).getEvaluatorFor(evaluated) != possibleEvaluator) {
	    throw new DomainException("error.onlyEvaluatorCanCreateSiadap");
	}

	setSiadap(new Siadap(year, evaluated));
	setProcessNumber("S" + SiadapRootModule.getInstance().getNumberAndIncrement());

	new LabelLog(this, currentUser, this.getClass().getName() + ".creation", "resources/SiadapResources",
		evaluated.getName(), year.toString());
    }

    @Override
    public <T extends WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>> List<T> getActivities() {
	return (List<T>) activities;
    }

    @Override
    public User getProcessCreator() {
	return getSiadap().getEvaluator().getUser();
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
    public static SiadapProcess createNewProcess(Person evaluated) {
	return new SiadapProcess(new LocalDate().getYear(), evaluated);
    }

    @Override
    public boolean isAccessible(User user) {
	Person accessor = user.getPerson();
	return accessor == getSiadap().getEvaluated() || accessor == getSiadap().getEvaluator() ||

	isResponsibleForHarmonization(accessor, getSiadap().getEvaluated());
    }

    private boolean isResponsibleForHarmonization(Person accessor, Person evaluated) {
	PersonSiadapWrapper wrapper = new PersonSiadapWrapper(evaluated, getSiadap().getYear());
	return wrapper.isResponsibleForHarmonization(accessor);
    }
}
