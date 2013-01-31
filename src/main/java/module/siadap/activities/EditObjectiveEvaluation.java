/*
 * @(#)EditObjectiveEvaluation.java
 *
 * Copyright 2010 Instituto Superior Tecnico
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

import module.siadap.activities.CreateObjectiveEvaluationActivityInformation.ObjectiveIndicator;
import module.siadap.domain.ObjectiveEvaluation;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.util.BundleUtil;

/**
 * 
 * @author João Antunes
 * @author Luis Cruz
 * @author Paulo Abrantes
 * 
 */
public class EditObjectiveEvaluation extends WorkflowActivity<SiadapProcess, EditObjectiveEvaluationActivityInformation> {

	@Override
	public boolean isActive(SiadapProcess process, User user) {
		Siadap siadap = process.getSiadap();
		if (siadap.getEvaluator() == null) {
			return false;
		}
		return siadap.getObjectiveSpecificationInterval().containsNow()
				&& SiadapProcessStateEnum.getState(siadap).ordinal() <= SiadapProcessStateEnum.WAITING_SELF_EVALUATION.ordinal()
				&& siadap.getEvaluator().getPerson().getUser() == user;
	}

	@Override
	protected void process(EditObjectiveEvaluationActivityInformation activityInformation) {
		ObjectiveEvaluation evaluation =
				activityInformation.getEvaluation().edit(activityInformation.getObjective(),
						activityInformation.getJustification(), activityInformation.getType());
		for (ObjectiveIndicator indicator : activityInformation.getIndicators()) {
			evaluation.addObjectiveIndicator(indicator.getMeasurementIndicator(), indicator.getSuperationCriteria(),
					indicator.getBigDecimalPonderationFactor());
		}
		//signal the fact that the evaluation objectives have been changed
		activityInformation.getProcess().signalChangesInEvaluationObjectives();
	}

	@Override
	public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
		return new EditObjectiveEvaluationActivityInformation(process, this);
	}

	@Override
	protected boolean shouldLogActivity(EditObjectiveEvaluationActivityInformation activityInformation) {
		return activityInformation.getProcess().getSiadap().getObjectivesAndCompetencesSealedDate() != null;
	}

	@Override
	public boolean isConfirmationNeeded(SiadapProcess process) {
		if (SiadapProcessStateEnum.getState(process.getSiadap()).ordinal() >= SiadapProcessStateEnum.WAITING_EVAL_OBJ_ACK
				.ordinal()) {
			return true;
		}
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
	}

	@Override
	public boolean isUserAwarenessNeeded(SiadapProcess process) {
		return false;
	}

	@Override
	public String getUsedBundle() {
		return "resources/SiadapResources";
	}

	@Override
	protected String[] getArgumentsDescription(EditObjectiveEvaluationActivityInformation activityInformation) {
		String labelBase = "label.description." + getClass().getName();
		if (activityInformation.isEmployJustification()) {
			String[] getBaseArgument =
					new String[] { BundleUtil.getFormattedStringFromResourceBundle(getUsedBundle(), labelBase
							+ ".withJustification", activityInformation.getObjective(), activityInformation.getJustification()) };
			return getBaseArgument;
		} else {
			String[] getBaseArgument =
					new String[] { BundleUtil.getFormattedStringFromResourceBundle(getUsedBundle(), labelBase
							+ ".withoutJustification", activityInformation.getObjective()) };
			return getBaseArgument;
		}
	}

	@Override
	public boolean isDefaultInputInterfaceUsed() {
		return false;
	}
}
