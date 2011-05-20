/**
 * 
 */
package module.siadap.domain.util.scripts;

import module.siadap.domain.SiadapRootModule;
import myorg.domain.User;
import myorg.domain.groups.UnionGroup;
import myorg.domain.scheduler.WriteCustomTask;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt)
 * 
 */
public class GenerateAndGetStatisticsUserGroupUserList extends WriteCustomTask {


    @Override
    protected void doService() {
	SiadapRootModule siadapRootModule = SiadapRootModule.getInstance();
	if (siadapRootModule.getStatisticsAccessUnionGroup() == null) {
	    siadapRootModule.setStatisticsAccessUnionGroup(new UnionGroup(myorg.domain.groups.Role
		    .getRole(myorg.domain.RoleType.MANAGER), siadapRootModule.getSiadapScheduleEditorsGroup(), siadapRootModule
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
