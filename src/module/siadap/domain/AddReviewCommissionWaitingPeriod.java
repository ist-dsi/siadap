package module.siadap.domain;

import myorg.domain.MyOrg;
import myorg.domain.scheduler.WriteCustomTask;

public class AddReviewCommissionWaitingPeriod extends WriteCustomTask {

    private static boolean PERFORM_CHANGES = false;

    @Override
    protected void doService() {
	for (SiadapYearConfiguration yearConfig : MyOrg.getInstance().getSiadapRootModule().getYearConfigurations()) {
	    out.println("Processing siadap year config for: " + yearConfig.getYear());
	    if (PERFORM_CHANGES) {
		yearConfig.setReviewCommissionWaitingPeriod(SiadapYearConfiguration.DEFAULT_REVIEW_COMMISSION_WAITING_PERIOD);
	    }
	}
    }
}
