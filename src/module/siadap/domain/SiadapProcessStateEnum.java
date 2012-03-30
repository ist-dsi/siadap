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

    VALIDATION_ACKNOWLEDGED,

    WAITING_HOMOLOGATION,

<<<<<<< OURS
    WAITING_FOR_REVIEW_COMMISSION,

=======
>>>>>>> THEIRS
    HOMOLOGATED,

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


    public String getLabelPrefix() {
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
	case VALIDATION_ACKNOWLEDGED:
	    return "siadap.state.validation.acked";
	case WAITING_HOMOLOGATION:
	    return "siadap.state.waiting.homologation";
<<<<<<< OURS
	case WAITING_FOR_REVIEW_COMMISSION:
	    return "siadap.state.waiting.review.commission";
=======
>>>>>>> THEIRS
	case HOMOLOGATED:
	    return "siadap.state.homologated";
	    //	case UNIMPLEMENTED_STATE:
	    //	    return "siadap.state.unimplemented";
	}
	return null;
    }

    public String getDescription() {
	return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getLabelPrefix());
    }


    public static SiadapProcessStateEnum getState(Siadap siadap) {
	if (siadap == null) {
	    return NOT_CREATED;
	} else {
	    return siadap.getState();
	}
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
	if (siadap.getEvaluator().getPerson().getUser().equals(currentUser)) {
	    return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getState(siadap).getLabelPrefix()
		    + ".nextstep.evaluator");
	}
	if (siadap.getEvaluated().getUser().equals(currentUser)) {
	    return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getState(siadap).getLabelPrefix()
		    + ".nextstep.evaluated");
	}
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
