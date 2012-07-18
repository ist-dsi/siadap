/*
 * @(#)SealObjectivesAndCompetences.java
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
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import pt.ist.bennu.core.domain.User;

import org.joda.time.LocalDate;

/**
 * 
 * @author João Antunes
 * 
 */
public class SealObjectivesAndCompetences extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	if (!process.isActive())
	    return false;
	Siadap siadap = process.getSiadap();
	if (siadap.getEvaluator() == null)
	    return false;
	return user == siadap.getEvaluator().getPerson().getUser() && siadap.getObjectiveSpecificationInterval().containsNow()
		&& siadap.getObjectivesAndCompetencesSealedDate() == null;
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	activityInformation.getProcess().getSiadap().setObjectivesAndCompetencesSealedDate(new LocalDate());
    }

    protected static void revertProcess(ActivityInformation<SiadapProcess> activityInformation) {
	activityInformation.getProcess().getSiadap().setObjectivesAndCompetencesSealedDate(null);
    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
    	return true;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }
}
