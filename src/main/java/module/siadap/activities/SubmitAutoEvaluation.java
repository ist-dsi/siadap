/*
 * @(#)SubmitAutoEvaluation.java
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
import module.siadap.domain.exceptions.SiadapException;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

import org.fenixedu.bennu.core.domain.User;
import org.joda.time.LocalDate;

/**
 * 
 * This activity allows you to have intermediate states when doing the
 * auto evaluation so that the evaluatee can save intermediate states of
 * his auto evaluation and iterate at will before making it public
 * 
 * @author João Antunes
 * 
 */
public class SubmitAutoEvaluation extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
        if (!process.isActive()) {
            return false;
        }
        Siadap siadap = process.getSiadap();
        return siadap.getEvaluated().getUser().equals(user) && !siadap.isAutoEvaliationDone()
                && new AutoEvaluation().isActive(process, user) && siadap.getAutoEvaluationData2() != null;
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
        Siadap siadap = activityInformation.getProcess().getSiadap();

        siadap.getAutoEvaluationData2().validateData();
        activityInformation.getProcess().getSiadap().setAutoEvaluationSealedDate(new LocalDate());
    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
        return true;
    }

    @Override
    public String getUsedBundle() {
        return "resources/SiadapResources";
    }

    protected static void revertProcess(ActivityInformation<SiadapProcess> activityInformation) {
        Siadap siadap = activityInformation.getProcess().getSiadap();
        if (siadap.isHarmonizationOfDefaultUniverseDone()) {
            throw new SiadapException("error.cannot.revert.harmonized.to.no.self.evaluation");
        }
        siadap.setAutoEvaluationSealedDate(null);
        siadap.getDefaultSiadapEvaluationUniverse().removeHarmonizationAssessments();

    }

}
