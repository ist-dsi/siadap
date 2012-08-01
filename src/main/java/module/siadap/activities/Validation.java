/*
 * @(#)Validation.java
 *
 * Copyright 2012 Instituto Superior Tecnico
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

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

/**
 * 
 *         Activity responsible for all validation related activities
 * 
 * @author João Antunes
 * 
 */
public class Validation extends WorkflowActivity<SiadapProcess, ValidationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	if (!process.isActive())
	    return false;
	// joantune : TODO (?? not sure)  check the process state
	try {

	    int year = process.getSiadap().getYear();
	    if (SiadapRootModule.getInstance().getSiadapCCAGroup().isMember(user)
		    && !SiadapYearConfiguration.getSiadapYearConfiguration(year).getClosedValidation())
		return true;
	    return false;
	} catch (NullPointerException ex) {
	    return false;
	}
    }

    @Override
    public String getUsedBundle() {
	return Siadap.SIADAP_BUNDLE_STRING;
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
	return false;
    }

    @Override
    public boolean isVisible() {
	return false;
    }

    @Override
    protected void process(ValidationActivityInformation activityInformation) {
	activityInformation.getSubActivity().process(activityInformation.getPersonSiadapWrapper(),
		activityInformation.getSiadapUniverse());
    }

    @Override
    protected String[] getArgumentsDescription(ValidationActivityInformation activityInformation) {
	return activityInformation.getSubActivity().getArgumentsDescription(activityInformation);
    }

    @Override
    @Deprecated
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	throw new UnsupportedOperationException("activity.not.to.be.used.in.the.regular.way.use.AI.constructor.instead");
    }
}
