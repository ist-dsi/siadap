/*
 * @(#)CreateObjectiveEvaluation.java
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

import org.fenixedu.bennu.core.domain.User;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public class CreateObjectiveEvaluation extends WorkflowActivity<SiadapProcess, CreateObjectiveEvaluationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
        Siadap siadap = process.getSiadap();
        if (!process.isActive()) {
            return false;
        }
        Integer maximumNumberOfObjectives = siadap.getSiadapYearConfiguration().getMaximumNumberOfObjectives();
        if (maximumNumberOfObjectives != null && siadap.getObjectiveEvaluations().size() >= maximumNumberOfObjectives)
            return false;
        if (siadap.getObjectiveSpecificationInterval() == null || siadap.getEvaluator() == null) {
            return false;
        }
        return siadap.getObjectiveSpecificationInterval().containsNow() && siadap.getEvaluator().getPerson().getUser() == user
                && siadap.getState().ordinal() <= SiadapProcessStateEnum.WAITING_EVAL_OBJ_ACK.ordinal()
                && (siadap.getEvaluatedOnlyByCompetences() == null || !siadap.getEvaluatedOnlyByCompetences());
    }

    @Override
    protected void process(CreateObjectiveEvaluationActivityInformation activityInformation) {
        ObjectiveEvaluation objectiveEvaluation =
                new ObjectiveEvaluation(activityInformation.getSiadap(), activityInformation.getObjective(),
                        activityInformation.getType());

        for (ObjectiveIndicator indicator : activityInformation.getIndicators()) {
            objectiveEvaluation.addObjectiveIndicator(indicator.getMeasurementIndicator(), indicator.getSuperationCriteria(),
                    indicator.getBigDecimalPonderationFactor());
        }
    }

    @Override
    protected boolean shouldLogActivity(CreateObjectiveEvaluationActivityInformation activityInformation) {
        if (activityInformation.getProcess().getSiadap().getObjectivesAndCompetencesSealedDate() != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
        return new CreateObjectiveEvaluationActivityInformation(process, this);
    }

    @Override
    public String getUsedBundle() {
        return "resources/SiadapResources";
    }

    @Override
    public boolean isDefaultInputInterfaceUsed() {
        return false;
    }
}
