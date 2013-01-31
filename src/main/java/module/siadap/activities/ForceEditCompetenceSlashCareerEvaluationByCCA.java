/*
 * @(#)EditCompetenceEvaluation.java
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

import java.util.List;
import java.util.ResourceBundle;

import module.siadap.domain.Competence;
import module.siadap.domain.CompetenceEvaluation;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.exceptions.SiadapException;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.exceptions.DomainException;
import pt.utl.ist.fenix.tools.util.i18n.Language;

/**
 * 
 * @author João Antunes 'special' activity used to edit the competences when we
 *         have a wrong career type defined by the CCA and we don't want to give
 *         the evaluators/evaluateds the hassle to redo the proccess - only done
 *         to the CCA - and should log eventual changes to the competences
 *         (mistakes)
 */
public class ForceEditCompetenceSlashCareerEvaluationByCCA extends
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
		if (!siadap.getSiadapYearConfiguration().getCcaMembers().contains(user.getPerson())) {
			return false;
		}
		SiadapProcessStateEnum state = SiadapProcessStateEnum.getState(siadap);
		if (siadap.hasAnyCompetencesSet() && state.ordinal() <= SiadapProcessStateEnum.WAITING_SELF_EVALUATION.ordinal()
				&& state.ordinal() >= SiadapProcessStateEnum.WAITING_EVAL_OBJ_ACK.ordinal()) {
			//making sure the competences aren't yet evaluated/self-evaluated as that data would be lost
			for (CompetenceEvaluation competenceEvaluation : siadap.getCompetenceEvaluations()) {
				if (competenceEvaluation.getAutoEvaluation() != null || competenceEvaluation.getEvaluation() != null) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	protected void process(CreateOrEditCompetenceEvaluationActivityInformation activityInformation) {
		int nrRequiredItems = 0;
		if (activityInformation.getEvaluatedOnlyByCompetences() != null) {
			if (activityInformation.getEvaluatedOnlyByCompetences().booleanValue()) {
				nrRequiredItems = Siadap.MINIMUM_COMPETENCES_WITHOUT_OBJ_EVAL_NUMBER;
			} else {
				nrRequiredItems = Siadap.MINIMUM_COMPETENCES_WITH_OBJ_EVAL_NUMBER;
			}
		}
		if (activityInformation.getEvaluatedOnlyByCompetences() == null
				|| activityInformation.getCompetences().size() < nrRequiredItems) {
			throw new DomainException("renderers.validator.invalid.nrCompetences", ResourceBundle.getBundle(
					"resources/SiadapResources", Language.getLocale()), Integer.toString(nrRequiredItems));
		}

		Siadap siadap = activityInformation.getSiadap();

		//let's check all of the new competences to make sure that they have a corresponding item on the new one

		List<CompetenceEvaluation> previousCompetences = siadap.getDefaultSiadapEvaluationUniverse().getCompetenceEvaluations();

		if (previousCompetences.size() != activityInformation.getCompetences().size()) {
			throw new SiadapException("ForceEditCompetenceSlashCareerEvaluationByCCA.mismatch.on.the.number.of.competences");
		}

		for (CompetenceEvaluation previousEvaluationCompetence : previousCompetences) {
			if (!activityInformation.hasEquivalentCompetence(previousEvaluationCompetence.getCompetence())) {
				throw new SiadapException("ForceEditCompetenceSlashCareerEvaluationByCCA.could.not.find.matching.competence.for",
						previousEvaluationCompetence.getCompetence().getName());
			}
		}

		//this means that we have equivalent competences :), let's remove and add them
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
		siadap.getDefaultSiadapEvaluationUniverse().setCompetenceSlashCareerType(activityInformation.getCompetenceType());

	}

	@Override
	public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
		return new CreateOrEditCompetenceEvaluationActivityInformation(process, this);
	}

	@Override
	protected String[] getArgumentsDescription(CreateOrEditCompetenceEvaluationActivityInformation activityInformation) {
		return new String[] { activityInformation.getCompetenceType().getName() };
	}

	@Override
	public boolean isUserAwarenessNeeded(SiadapProcess process) {
		return false;
	}

	@Override
	public boolean isVisible() {
		return true;
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
