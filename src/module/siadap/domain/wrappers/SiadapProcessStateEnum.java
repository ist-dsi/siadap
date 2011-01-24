package module.siadap.domain.wrappers;

import module.siadap.domain.Siadap;
import myorg.domain.User;
import myorg.util.BundleUtil;
import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

public enum SiadapProcessStateEnum implements IPresentableEnum {
    NOT_CREATED,

    INCOMPLETE_OBJ_OR_COMP,

    NOT_SEALED,

    EVALUATION_NOT_GOING_TO_BE_DONE,

    NOT_YET_SUBMITTED_FOR_ACK,

    WAITING_EVAL_OBJ_ACK,

    WAITING_SELF_EVALUATION,

    NOT_YET_EVALUATED,

    UNIMPLEMENTED_STATE;

    private SiadapProcessStateEnum() {
	// TODO Auto-generated constructor stub
    }

    /**
     * 
     * @return the String representing the state, used on the list of evaluated
     *         persons, currently prepareCreateSiadap.jsp
     */
    public static String getStateForListOfProcessesString(Siadap siadap) {
	return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getLabelPrefix(siadap));
    }

    private static String getLabelPrefix(Siadap siadap) {
	switch (getState(siadap)) {
	case NOT_CREATED:
	    return "siadap.state.not.created";
	case INCOMPLETE_OBJ_OR_COMP:
	    return "siadap.state.incomplete.objectives.or.competences";
	case NOT_SEALED:
	    return "siadap.state.not.sealed";
	case EVALUATION_NOT_GOING_TO_BE_DONE:
	    return "siadap.state.evaluation.not.going.to.be.done";
	case NOT_YET_SUBMITTED_FOR_ACK:
	    return "siadap.state.not.submitted.for.acknowledgement";
	case WAITING_EVAL_OBJ_ACK:
	    return "siadap.state.waiting.evaluation.objectives.acknowledgement";
	case WAITING_SELF_EVALUATION:
	    return "siadap.state.waiting.self.evaluation";
	case NOT_YET_EVALUATED:
	    return "siadap.state.not.evaluted.yet";
	case UNIMPLEMENTED_STATE:
	    return "siadap.state.unimplemented";
	}
	return null;

    }

    public static SiadapProcessStateEnum getState(Siadap siadap) {
	if (siadap == null)
	    return NOT_CREATED;
	else if (siadap.isWithSkippedEvaluation())
	    return EVALUATION_NOT_GOING_TO_BE_DONE;
	else if (!siadap.isWithObjectivesFilled())
	    return INCOMPLETE_OBJ_OR_COMP;
	else if (!siadap.hasSealedObjectivesAndCompetences())
	    return NOT_SEALED;
	else if (siadap.getRequestedAcknowledgeDate() == null)
	    return NOT_YET_SUBMITTED_FOR_ACK;
	else if (!siadap.isEvaluatedWithKnowledgeOfObjectives())
	    return WAITING_EVAL_OBJ_ACK;
	else if (!siadap.isAutoEvaliationDone())
	    return WAITING_SELF_EVALUATION;
	else if (!siadap.isEvaluationDone())
	    return NOT_YET_EVALUATED;
	return UNIMPLEMENTED_STATE;

    }

    /**
     * 
     * @param siadap
     * @param currentUser
     * @return a string with the explanation of what should be done next, based
     *         on the user, if he is an evaluator or an evaluated
     */
    public static String getNextStep(Siadap siadap, User currentUser) {
	if (siadap.getEvaluator().getPerson().getUser().equals(currentUser))
	    return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getLabelPrefix(siadap)
		    + ".nextstep.evaluator");
	else if (siadap.getEvaluated().getUser().equals(currentUser))
	    return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getLabelPrefix(siadap)
		    + ".nextstep.evaluated");
	return null;
    }

    @Override
    public String getLocalizedName() {
	return null;
    }

}
