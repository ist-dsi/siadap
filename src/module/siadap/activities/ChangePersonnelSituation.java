/*
 * @(#)ChangePersonnelSituation.java
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

import module.siadap.domain.CompetenceType;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.RoleType;
import myorg.domain.User;
import myorg.domain.groups.Role;

/**
 * 
 *         Activity used to change a person's:
 *         <ul>
 *         <li>Working unit;
 *         <li>Default SIADAPUniverse {@link SiadapUniverse};
 *         <li>Evaluator;
 *         <li>Default {@link CompetenceType} / Career name;
 *         </ul>
 * 
 *         Thus logging the action and providing the extra control that we might
 *         need instead of doing this directly
 *         
 * @author João Antunes
 * 
 */
public class ChangePersonnelSituation extends
 WorkflowActivity<SiadapProcess, ChangePersonnelSituationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(process.getSiadap().getYear());
	return Role.getRole(RoleType.MANAGER).isMember(user) || configuration.isUserMemberOfStructureManagementGroup(user);
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
    protected void process(ChangePersonnelSituationActivityInformation activityInformation) {

	activityInformation.getBeanWrapper().execute(activityInformation.getProcess());

    }

    @Override
    protected String[] getArgumentsDescription(ChangePersonnelSituationActivityInformation activityInformation) {
	return (activityInformation.getBeanWrapper().getArgumentsDescription(activityInformation.getProcess()));
    }


    @Override
    @Deprecated
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	throw new UnsupportedOperationException("activity.not.to.be.used.in.the.regular.way.use.AI.constructor.instead");
    }

}
