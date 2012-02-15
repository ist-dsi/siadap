/**
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
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 14 de Fev de 2012
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
