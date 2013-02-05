/**
 * 
 */
package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapYearConfiguration;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
import pt.ist.bennu.core.domain.User;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 4 de Jul de 2012
 * 
 *         Reverts the {@link NullifyProcess} activity
 * 
 */
public class RectifyNullifiedProcess extends WorkflowActivity<SiadapProcess, NullifyRatifyActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
        Boolean nulled = process.getSiadap().getNulled();
        return ((nulled != null && nulled) && (SiadapYearConfiguration.getStructureManagementGroup().isMember(
                UserView.getCurrentUser()) || SiadapYearConfiguration.getCcaMembersGroup().isMember(UserView.getCurrentUser())));
    }

    @Override
    protected void process(NullifyRatifyActivityInformation activityInformation) {
        activityInformation.getProcess().getSiadap().setNulled(Boolean.FALSE);

    }

    @Override
    protected String[] getArgumentsDescription(NullifyRatifyActivityInformation activityInformation) {
        return new String[] { activityInformation.getJustification() };
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
        return false;
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
        return new NullifyRatifyActivityInformation(process, this);
    }

    @Override
    public String getUsedBundle() {
        return Siadap.SIADAP_BUNDLE_STRING;
    }

}
