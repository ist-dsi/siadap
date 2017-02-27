/**
 * 
 */
package module.siadap.activities;

import org.fenixedu.bennu.core.domain.User;
import org.joda.time.LocalDate;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationItem;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

public class ForceReadinessForEvaluation extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

	@Override
	public boolean isActive(final SiadapProcess process, final User user) {
		final Siadap siadap = process.getSiadap();
		return canForceStateChange(siadap) && isCCAMember(siadap, user);
	}

	private boolean canForceStateChange(final Siadap siadap) {
		final SiadapProcessStateEnum state = siadap.getState();
		return state == SiadapProcessStateEnum.WAITING_EVAL_OBJ_ACK;
	}

	private boolean isCCAMember(final Siadap siadap, final User user) {
		return siadap.getSiadapYearConfiguration().getCcaMembers().contains(user.getPerson());
	}
	
	@Override
	protected void process(ActivityInformation<SiadapProcess> activityInformation) {
		final Siadap siadap = activityInformation.getProcess().getSiadap();
		final LocalDate acknowledgeDate = new LocalDate();
        for (final SiadapEvaluationItem item : siadap.getCurrentEvaluationItems()) {
            if (item.getAcknowledgeDate() == null || acknowledgeDate == null) {
                item.setAcknowledgeDate(acknowledgeDate);
            }
        }
	}

	@Override
	public boolean isConfirmationNeeded(SiadapProcess process) {
		return true;
	}

	@Override
	protected String[] getArgumentsDescription(ActivityInformation<SiadapProcess> activityInformation) {
		return new String[] { "" };

	}

	@Override
	public String getLocalizedConfirmationMessage() {
		return super.getLocalizedConfirmationMessage();
	}

	@Override
	public boolean isUserAwarenessNeeded(SiadapProcess process) {
		return false;
	}

	@Override
	public String getUsedBundle() {
		return Siadap.SIADAP_BUNDLE_STRING;
	}

}
