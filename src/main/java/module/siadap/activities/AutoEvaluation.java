/*
 * @(#)AutoEvaluation.java
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

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapAutoEvaluation;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import pt.ist.bennu.core.domain.User;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public class AutoEvaluation extends WorkflowActivity<SiadapProcess, AutoEvaluationActivityInformation> {

	@Override
	public boolean isActive(SiadapProcess process, User user) {
		if (!process.isActive()) {
			return false;
		}
		Siadap siadap = process.getSiadap();
		return siadap.getEvaluated().getUser() == user && !siadap.isAutoEvaliationDone()
				&& siadap.isEvaluatedWithKnowledgeOfObjectives()
				&& !siadap.getDefaultSiadapEvaluationUniverse().getSiadapEvaluationItems().isEmpty()
				&& siadap.getAutoEvaluationInterval().containsNow();
	}

	@Override
	public boolean isDefaultInputInterfaceUsed() {
		return false;
	}

	@Override
	public String getUsedBundle() {
		return Siadap.SIADAP_BUNDLE_STRING;
	}

	@Override
	protected boolean shouldLogActivity(AutoEvaluationActivityInformation activityInformation) {
		return false;
	}

	@Override
	public ActivityInformation getActivityInformation(SiadapProcess process) {
		return new AutoEvaluationActivityInformation(process, this);
	}

	@Override
	protected void process(AutoEvaluationActivityInformation activityInformation) {

		new SiadapAutoEvaluation(activityInformation.getProcess().getSiadap(), activityInformation.getProcess().getSiadap()
				.getDefaultSiadapEvaluationUniverse(), activityInformation.getObjectivesJustification(),
				activityInformation.getCompetencesJustification(), activityInformation.getOtherFactorsJustification(),
				activityInformation.getExtremesJustification(), activityInformation.getCommentsAndProposals(),
				activityInformation.getFactorOneClassification(), activityInformation.getFactorTwoClassification(),
				activityInformation.getFactorThreeClassification(), activityInformation.getFactorFourClassification(),
				activityInformation.getFactorFiveClassification(), activityInformation.getFactorSixClassification());

	}
}
