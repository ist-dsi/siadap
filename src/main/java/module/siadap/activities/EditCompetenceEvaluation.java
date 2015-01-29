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

import module.siadap.domain.Competence;
import module.siadap.domain.CompetenceEvaluation;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.exceptions.SiadapException;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.i18n.BundleUtil;

/**
 * 
 * @author João Antunes
 * 
 */
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
        if (!process.isActive()) {
            return false;
        }
        Siadap siadap = process.getSiadap();
        if (siadap.getEvaluator() == null) {
            return false;
        }
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
            } else {
                nrRequiredItems = Siadap.MINIMUM_COMPETENCES_WITH_OBJ_EVAL_NUMBER;
            }
        }
        if (activityInformation.getEvaluatedOnlyByCompetences() == null
                || activityInformation.getCompetences().size() < nrRequiredItems) {
            throw new SiadapException("renderers.validator.invalid.nrCompetences", Integer.toString(nrRequiredItems));
        }
        Siadap siadap = activityInformation.getSiadap();
        // TODO ist154457 make this more efficient, for now, let's just remove
        // and set them again
        List<CompetenceEvaluation> previousCompetences = siadap.getDefaultSiadapEvaluationUniverse().getCompetenceEvaluations();
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
        if (activityInformation.getProcess().getSiadap().getObjectivesAndCompetencesSealedDate() != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
        return false;
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
            return BundleUtil.getString(getUsedBundle(), "edit.warning.evaluation.not.going.to.be.done");
        case NOT_YET_SUBMITTED_FOR_ACK:
            return null;
        case WAITING_EVAL_OBJ_ACK:
        case WAITING_SELF_EVALUATION:
            return BundleUtil.getString(getUsedBundle(), "edit.warning.reverts.state");
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
