/*
 * @(#)Evaluation.java
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
import module.siadap.domain.SiadapEvaluation;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

import org.fenixedu.bennu.core.domain.User;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public class Evaluation extends WorkflowActivity<SiadapProcess, EvaluationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
        Siadap siadap = process.getSiadap();
        if (siadap.getEvaluator() == null) {
            return false;
        }
        return !siadap.isWithSkippedEvaluation() && siadap.getEvaluator().getPerson().getUser() == user
                && siadap.getValidationDateOfDefaultEvaluation() == null && siadap.isEvaluatedWithKnowledgeOfObjectives()
                && siadap.getEvaluationInterval().containsNow()
                && siadap.getDefaultSiadapEvaluationUniverse().getHarmonizationAssessment() == null;
    }

    @Override
    protected void process(EvaluationActivityInformation activityInformation) {
        Siadap siadap = activityInformation.getProcess().getSiadap();
        SiadapEvaluation evaluationData = siadap.getEvaluationData2();
        if (evaluationData == null) {
            new SiadapEvaluation(siadap, activityInformation.getEvaluationJustification(),
                    activityInformation.getPersonalDevelopment(), activityInformation.getTrainningNeeds(),
                    activityInformation.getExcellencyAward(), activityInformation.getExcellencyAwardJustification(),
                    siadap.getDefaultSiadapEvaluationUniverse());
        } else {
            evaluationData.editWithoutValidation(activityInformation.getEvaluationJustification(),
                    activityInformation.getPersonalDevelopment(), activityInformation.getTrainningNeeds(),
                    activityInformation.getExcellencyAward(), activityInformation.getExcellencyAwardJustification());
        }

        //revert the submitted state to unsubmitted
        if (siadap.isDefaultEvaluationDone()) {
            siadap.setEvaluationSealedDate(null);
        }

    }

    @Override
    public boolean isDefaultInputInterfaceUsed() {
        return false;
    }

    @Override
    protected boolean shouldLogActivity(EvaluationActivityInformation activityInformation) {
        return false;
    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
        Siadap siadap = process.getSiadap();
        if (siadap.isDefaultEvaluationDone()) {
            return true;
        }
        return false;
    }

    @Override
    public String getUsedBundle() {
        return "resources/SiadapResources";
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
        return new EvaluationActivityInformation(process, this);
    }
}
