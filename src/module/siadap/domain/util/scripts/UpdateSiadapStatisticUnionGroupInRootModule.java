package module.siadap.domain.util.scripts;

import module.siadap.domain.SiadapRootModule;
import myorg.domain.groups.PersistentGroup;
import myorg.domain.groups.UnionGroup;
import myorg.domain.scheduler.WriteCustomTask;

public class UpdateSiadapStatisticUnionGroupInRootModule extends WriteCustomTask {

    @Override
    protected void doService() {
	UnionGroup statisticsGroup = SiadapRootModule.getInstance().getStatisticsAccessUnionGroup();
	for (PersistentGroup group : statisticsGroup.getPersistentGroups()) {
	    statisticsGroup.removePersistentGroups(group);
	}
	SiadapRootModule.getInstance().removeStatisticsAccessUnionGroup();
	statisticsGroup.delete();

    }

}
