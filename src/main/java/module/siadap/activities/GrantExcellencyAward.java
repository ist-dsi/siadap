/*
 * @(#)GrantExcellencyAward.java
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

import pt.ist.bennu.core.domain.User;

import module.organization.domain.Person;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public class GrantExcellencyAward extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	//	Siadap siadap = process.getSiadap();
	//	return siadap.getSiadapYearConfiguration().isPersonMemberOfCCA(user.getPerson())
	//		&& Boolean.TRUE.equals(siadap.getValidated())
	//		&& !Boolean.TRUE.equals(siadap.getEvaluationData2().getExcellencyAward());
	//TODO not sure if we actually need this but haven't deleted it yet
	return false;
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
	return false;
    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
	Siadap siadap = process.getSiadap();
	int year = siadap.getSiadapYearConfiguration().getYear();
	Person person = siadap.getEvaluated();
	PersonSiadapWrapper wrapper = new PersonSiadapWrapper(person, year);
	UnitSiadapWrapper workingUnit = new UnitSiadapWrapper(wrapper.getWorkingUnit().getHarmonizationUnit(), year);
	int usedQuota = 0;
	int totalQuota = 0;
	SiadapUniverse defaultSiadapUniverse = siadap.getDefaultSiadapUniverse();
	if (defaultSiadapUniverse.equals(SiadapUniverse.SIADAP2)) {
	    if (wrapper.isQuotaAware()) {
		usedQuota = workingUnit.getNumberCurrentExcellentsSiadap2WithQuota();
		totalQuota = workingUnit.getExcellencySiadap2WithQuotaQuota();
	    } else {
		usedQuota = workingUnit.getNumberCurrentExcellentsSiadap2WithoutQuota();
		totalQuota = workingUnit.getExcellencySiadap2WithoutQuotaQuota();
	    }
	}
	if (defaultSiadapUniverse.equals(SiadapUniverse.SIADAP3)) {
	    if (wrapper.isQuotaAware()) {
		usedQuota = workingUnit.getNumberCurrentExcellentsSiadap3WithQuota();
		totalQuota = workingUnit.getExcellencySiadap3WithQuotaQuota();

	    } else {
		usedQuota = workingUnit.getNumberCurrentExcellentsSiadap3WithoutQuota();
		totalQuota = workingUnit.getExcellencySiadap3WithoutQuotaQuota();
	    }
	}
	return usedQuota + 1 > totalQuota;

    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	activityInformation.getProcess().getSiadap().getEvaluationData2().setExcellencyAward(Boolean.TRUE);
    }

}
