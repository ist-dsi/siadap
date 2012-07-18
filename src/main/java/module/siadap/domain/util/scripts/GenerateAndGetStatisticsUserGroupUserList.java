/*
 * @(#)GenerateAndGetStatisticsUserGroupUserList.java
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
package module.siadap.domain.util.scripts;

import module.siadap.domain.SiadapRootModule;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.groups.UnionGroup;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;

/**
 * 
 * @author João Antunes
 * 
 */
public class GenerateAndGetStatisticsUserGroupUserList extends WriteCustomTask {


    @Override
    protected void doService() {
	SiadapRootModule siadapRootModule = SiadapRootModule.getInstance();
	if (siadapRootModule.getStatisticsAccessUnionGroup() == null) {
	    siadapRootModule.setStatisticsAccessUnionGroup(new UnionGroup(pt.ist.bennu.core.domain.groups.Role
		    .getRole(pt.ist.bennu.core.domain.RoleType.MANAGER), siadapRootModule.getSiadapScheduleEditorsGroup(), siadapRootModule
		    .getSiadapCCAGroup(), siadapRootModule.getSiadapStructureManagementGroup()));
	}
	UnionGroup group = SiadapRootModule.getInstance().getStatisticsAccessUnionGroup();
	out.println("Group name: " + group.getName());
	out.println("List of users follows: ");
	for(User user : group.getMembers())
	{
	    out.println(user.getPerson().getName() + " (" + user.getUsername() + ")");
		    
 	}

    }

}
