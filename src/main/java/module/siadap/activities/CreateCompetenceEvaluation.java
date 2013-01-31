/*
 * @(#)CreateCompetenceEvaluation.java
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

import java.util.ResourceBundle;

import module.siadap.domain.Competence;
import module.siadap.domain.CompetenceEvaluation;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.exceptions.DomainException;
import pt.utl.ist.fenix.tools.util.i18n.Language;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public class CreateCompetenceEvaluation extends
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
		if (!process.isActive()) {
			return false;
		}
		Siadap siadap = process.getSiadap();
		if (siadap.getEvaluator() == null) {
			return false;
		}
		return siadap.getObjectiveSpecificationInterval().containsNow() && siadap.getEvaluator().getPerson().getUser() == user
				&& !siadap.hasAnyCompetencesSet() && !process.hasLogOfBeingExecuted(AutoEvaluation.class);
	}

	@Override
	protected void process(CreateOrEditCompetenceEvaluationActivityInformation activityInformation) {
		int nrRequiredItems;
		if (activityInformation.getEvaluatedOnlyByCompetences() != null) {
			if (activityInformation.getEvaluatedOnlyByCompetences().booleanValue()) {
				nrRequiredItems = Siadap.MINIMUM_COMPETENCES_WITHOUT_OBJ_EVAL_NUMBER;
			} else {
				nrRequiredItems = Siadap.MINIMUM_COMPETENCES_WITH_OBJ_EVAL_NUMBER;
			}
		} else {
			nrRequiredItems = Integer.MAX_VALUE;
		}
		if (activityInformation.getCompetences().size() < nrRequiredItems) {
			throw new DomainException("renderers.validator.invalid.nrCompetences", ResourceBundle.getBundle(
					"resources/SiadapResources", Language.getLocale()), Integer.toString(nrRequiredItems));
		}
		for (Competence competence : activityInformation.getCompetences()) {
			new CompetenceEvaluation(activityInformation.getSiadap(), competence);
		}
		activityInformation.getSiadap().setEvaluatedOnlyByCompetences(activityInformation.getEvaluatedOnlyByCompetences());
	}

	@Override
	public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
		return new CreateOrEditCompetenceEvaluationActivityInformation(process, this);
	}

	@Override
	protected boolean shouldLogActivity(CreateOrEditCompetenceEvaluationActivityInformation activityInformation) {
		if (activityInformation.getProcess().getSiadap().getObjectivesAndCompetencesSealedDate() != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isDefaultInputInterfaceUsed() {
		return false;
	}

	@Override
	public String getUsedBundle() {
		return "resources/SiadapResources";
	}
}
