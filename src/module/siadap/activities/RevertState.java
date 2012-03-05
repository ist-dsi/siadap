/*
 * @(#)RevertState.java
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
package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapYearConfiguration;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;

/**
 * 
 * @author João Antunes
 * 
 */
public class RevertState extends WorkflowActivity<SiadapProcess, RevertStateActivityInformation> {

    private boolean isSideEffect = false;

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	if (isSideEffect())
	    return true;
	return shouldBeAbleToRevertState(process, user);
    }

    private static boolean shouldBeAbleToRevertState(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	SiadapYearConfiguration configuration = siadap.getSiadapYearConfiguration();
	return configuration.isPersonMemberOfRevertStateGroup(user.getPerson());
    }

    @Override
    protected void process(RevertStateActivityInformation activityInformation) {
	//let's revert the process to the given state
	SiadapProcessStateEnum processStateEnumToRevertTo = activityInformation.getSiadapProcessStateEnum();
	boolean auxNotifyIntervenients = true;

	switch (processStateEnumToRevertTo) {
	case NOT_SEALED:
	    SealObjectivesAndCompetences.revertProcess(activityInformation);
	case NOT_YET_SUBMITTED_FOR_ACK:
	    SubmitForObjectivesAcknowledge.revertProcess(activityInformation, auxNotifyIntervenients);
	    auxNotifyIntervenients = false;
	case WAITING_EVAL_OBJ_ACK:
	    AcknowledgeEvaluationObjectives.revertProcess(activityInformation, auxNotifyIntervenients);
	    break;
	case WAITING_SELF_EVALUATION:
	    SubmitAutoEvaluation.revertProcess(activityInformation);
	    break;
	case NOT_YET_EVALUATED:
	    SubmitEvaluation.revertProcess(activityInformation);
	    break;
	case INCOMPLETE_OBJ_OR_COMP:
	case EVALUATION_NOT_GOING_TO_BE_DONE:
	case NOT_CREATED:
	case UNIMPLEMENTED_STATE:
	default:
	    if (isSideEffect())
		setSideEffect(false);
	    throw new DomainException("activity.RevertState.error.invalidStateToChangeTo",
		    DomainException.getResourceFor("resources/SiadapResources"));

	}
	if (isSideEffect())
	    setSideEffect(false);

    }

    @Override
    public boolean isDefaultInputInterfaceUsed() {
	return false;
    }

    @Override
    protected String[] getArgumentsDescription(RevertStateActivityInformation activityInformation) {
	return new String[] { activityInformation.getSiadapProcessStateEnum().getLocalizedName(),
		activityInformation.getJustification() };
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new RevertStateActivityInformation(process, this);
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
	return false;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }

    public void setSideEffect(boolean isSideEffect) {
	this.isSideEffect = isSideEffect;
    }

    public boolean isSideEffect() {
	return isSideEffect;
    }

}
