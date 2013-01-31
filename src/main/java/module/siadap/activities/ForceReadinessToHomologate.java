/**
 * 
 */
package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapYearConfiguration;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import pt.ist.bennu.core.domain.User;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 19 de Dez de 2012
 * 
 *         If the Siadap is validated, executing this will make the process ready to be homologated
 * 
 */
public class ForceReadinessToHomologate extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

	@Override
	public boolean isActive(SiadapProcess process, User user) {
		Siadap siadap = process.getSiadap();
		SiadapYearConfiguration siadapYearConfiguration = siadap.getSiadapYearConfiguration();
		SiadapProcessStateEnum state = siadap.getState();

		return siadapYearConfiguration.getCcaMembers().contains(user.getPerson())
				&& state.ordinal() >= SiadapProcessStateEnum.WAITING_SUBMITTAL_BY_EVALUATOR_AFTER_VALIDATION.ordinal()
				&& state.ordinal() < SiadapProcessStateEnum.WAITING_HOMOLOGATION.ordinal();
	}

	@Override
	protected void process(ActivityInformation<SiadapProcess> activityInformation) {
		Siadap siadap = activityInformation.getProcess().getSiadap();
		siadap.setForcedReadinessToHomologation(true);
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
