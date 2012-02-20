/**
 * 
 */
package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 17 de Fev de 2012
 * 
 *         Activity responsible for all validation related activities
 */
public class Validation extends WorkflowActivity<SiadapProcess, ValidationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
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
