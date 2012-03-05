/*
 * @(#)SiadapProcessStateEnum.java
 *
 * Copyright 2011 Instituto Superior Tecnico
 * Founding Authors: Paulo Abrantes
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the SIADAP Module.
 *
 *   The SIADAP Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The SIADAP Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the SIADAP Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.siadap.domain;

import myorg.domain.User;
import myorg.util.BundleUtil;

import org.apache.commons.lang.StringUtils;

import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

/**
 * 
 * @author João Antunes
 * 
 */
public enum SiadapProcessStateEnum implements IPresentableEnum {
    NOT_CREATED,

    INCOMPLETE_OBJ_OR_COMP,

    NOT_SEALED,

    EVALUATION_NOT_GOING_TO_BE_DONE,

    NOT_YET_SUBMITTED_FOR_ACK,

    WAITING_EVAL_OBJ_ACK,

    WAITING_SELF_EVALUATION,

    NOT_YET_EVALUATED,

    WAITING_HARMONIZATION, //

    WAITING_VALIDATION,

    WAITING_SUBMITTAL_BY_EVALUATOR_AFTER_VALIDATION,

    WAITING_VALIDATION_ACKNOWLEDGMENT_BY_EVALUATED,

    WAITING_HOMOLOGATION,

    WAITING_ACKNOWLEDGEMENT_OF_HOMOLOGATION,

    FINAL_STATE,

    UNIMPLEMENTED_STATE;

    private SiadapProcessStateEnum() {
    }

    /**
     * 
     * @return the String representing the state, used on the list of evaluated
     *         persons, currently prepareCreateSiadap.jsp
     */
    public static String getStateForListOfProcessesString(Siadap siadap) {
	return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getState(siadap).getLabelPrefix());
    }


    private String getLabelPrefix() {
	switch (this) {
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
	case WAITING_HARMONIZATION:
	    return "siadap.state.waiting.harmonization";
	case WAITING_VALIDATION:
	    return "siadap.state.waiting.validation";
	case WAITING_SUBMITTAL_BY_EVALUATOR_AFTER_VALIDATION:
	    return "siadap.state.waiting.evaluation.submittal.after.validation";
	case WAITING_VALIDATION_ACKNOWLEDGMENT_BY_EVALUATED:
	    return "siadap.state.waiting.evaluation.ack.after.validation";
	case WAITING_HOMOLOGATION:
	    return "siadap.state.waiting.homologation";
	    //	case UNIMPLEMENTED_STATE:
	    //	    return "siadap.state.unimplemented";
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
	else if (!siadap.isAutoEvaliationDone() && !siadap.isDefaultEvaluationDone())
	    return WAITING_SELF_EVALUATION;
	else if (!siadap.isDefaultEvaluationDone())
	    return NOT_YET_EVALUATED;
	else if (siadap.getHarmonizationDate() == null)
	    return WAITING_HARMONIZATION;
	else if (siadap.getValidationDateOfDefaultEvaluation() == null)
	    return WAITING_VALIDATION;
	else if (siadap.getRequestedAcknowledegeValidationDate() == null)
	    return WAITING_SUBMITTAL_BY_EVALUATOR_AFTER_VALIDATION;
	else if (siadap.getAcknowledgeValidationDate() == null)
	    return WAITING_VALIDATION_ACKNOWLEDGMENT_BY_EVALUATED;
	else if (siadap.getHomologationDate() == null)
	    return WAITING_HOMOLOGATION;
	return UNIMPLEMENTED_STATE;

    }

    public static boolean isNotSubmittedForConfirmation(final Siadap siadap) {
	final SiadapProcessStateEnum state = getState(siadap);
	return state.ordinal() <= NOT_YET_SUBMITTED_FOR_ACK.ordinal();
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
	    return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getState(siadap).getLabelPrefix()
		    + ".nextstep.evaluator");
	else if (siadap.getEvaluated().getUser().equals(currentUser))
	    return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getState(siadap).getLabelPrefix()
		    + ".nextstep.evaluated");
	return null;
    }

    @Override
    public String getLocalizedName() {
	if (getLabelPrefix() != null)
	return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getLabelPrefix());
	else
	    return StringUtils.EMPTY;

    }


}
