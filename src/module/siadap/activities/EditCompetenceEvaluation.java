package module.siadap.activities;

import java.util.List;
import java.util.ResourceBundle;

import module.siadap.domain.Competence;
import module.siadap.domain.CompetenceEvaluation;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;
import myorg.util.BundleUtil;
import pt.utl.ist.fenix.tools.util.i18n.Language;

public class EditCompetenceEvaluation extends
	WorkflowActivity<SiadapProcess, CreateOrEditCompetenceEvaluationActivityInformation> {

    // @Override
    // public boolean isActive(SiadapProcess process, User user) {
    // Siadap siadap = process.getSiadap();
    // return !siadap.isObjectiveSpecificationIntervalFinished()
    // && siadap.getEvaluator().getPerson().getUser() == user
    // && siadap.getCompetenceEvaluations().isEmpty();
    // }

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return siadap.getObjectiveSpecificationInterval().containsNow() && siadap.getEvaluator().getPerson().getUser() == user
		&& siadap.hasAnyCompetencesSet()
		&& SiadapProcessStateEnum.getState(siadap).ordinal() <= SiadapProcessStateEnum.WAITING_EVAL_OBJ_ACK.ordinal();
    }

    @Override
    protected void process(CreateOrEditCompetenceEvaluationActivityInformation activityInformation) {
	int nrRequiredItems = 0;
	if (activityInformation.getEvaluatedOnlyByCompetences() != null) {
	    if (activityInformation.getEvaluatedOnlyByCompetences().booleanValue()) {
		nrRequiredItems = Siadap.MINIMUM_COMPETENCES_WITHOUT_OBJ_EVAL_NUMBER;
	    } else
		nrRequiredItems = Siadap.MINIMUM_COMPETENCES_WITH_OBJ_EVAL_NUMBER;
	}
	if (activityInformation.getEvaluatedOnlyByCompetences() == null
		|| activityInformation.getCompetences().size() < nrRequiredItems) {
	    throw new DomainException("renderers.validator.invalid.nrCompetences", ResourceBundle.getBundle(
		    "resources/SiadapResources", Language.getLocale()), Integer.toString(nrRequiredItems));
	}
	Siadap siadap = activityInformation.getSiadap();
	// TODO ist154457 make this more efficient, for now, let's just remove
	// and set them again
	List<CompetenceEvaluation> previousCompetences = siadap.getCompetenceEvaluations();
	List<Competence> competencesToAdd = activityInformation.getCompetences();
	for (CompetenceEvaluation competence : previousCompetences) {
	    if (!competencesToAdd.contains(competence.getCompetence())) {
		competence.delete();
	    } else {
		competencesToAdd.remove(competence.getCompetence());
	    }
	}
	for (Competence competence : competencesToAdd) {
	    new CompetenceEvaluation(activityInformation.getSiadap(), competence);
	}
	activityInformation.getSiadap().setEvaluatedOnlyByCompetences(activityInformation.getEvaluatedOnlyByCompetences());

	//signal the fact that the evaluation objectives have been changed
	activityInformation.getProcess().signalChangesInEvaluationObjectives();
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new CreateOrEditCompetenceEvaluationActivityInformation(process, this);
    }

    @Override
    protected boolean shouldLogActivity(CreateOrEditCompetenceEvaluationActivityInformation activityInformation) {
	if (activityInformation.getProcess().getSiadap().getObjectivesAndCompetencesSealedDate() != null)
	    return true;
	else
	    return false;
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
	return false;
    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
	if (SiadapProcessStateEnum.getState(process.getSiadap()).ordinal() >= SiadapProcessStateEnum.WAITING_EVAL_OBJ_ACK
		.ordinal())
	    return true;
	return false;
    }

    @Override
    public String getLocalizedConfirmationMessage(SiadapProcess process) {
	switch (SiadapProcessStateEnum.getState(process.getSiadap())) {
	case NOT_CREATED:
	case INCOMPLETE_OBJ_OR_COMP:
	    return null;
	case EVALUATION_NOT_GOING_TO_BE_DONE:
	    return BundleUtil.getStringFromResourceBundle(getUsedBundle(), "edit.warning.evaluation.not.going.to.be.done");
	case NOT_YET_SUBMITTED_FOR_ACK:
	    return null;
	case WAITING_EVAL_OBJ_ACK:
	case WAITING_SELF_EVALUATION:
	    return BundleUtil.getStringFromResourceBundle(getUsedBundle(), "edit.warning.reverts.state");
	}
	return null;
    }

    @Override
    public boolean isVisible() {
	return false;
    };

    @Override
    public boolean isDefaultInputInterfaceUsed() {
	return false;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }
}
