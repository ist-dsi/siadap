/**
 * 
 */
package module.siadap.domain.util;

import java.util.ArrayList;

import module.siadap.domain.ImportTestUsers;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.workflow.domain.ProcessCounter;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.ModuleInitializer;
import myorg.domain.MyOrg;
import myorg.domain.User;
import myorg.domain.groups.NamedGroup;
import myorg.domain.groups.PersistentGroup;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt)
 * 
 */
public class SiadapPendingProcessesCounter extends ProcessCounter implements ModuleInitializer {

    private static NamedGroup siadapTestUserGroup;

    public SiadapPendingProcessesCounter() {
	super(SiadapProcess.class);
    }

    @Override
    public int getCount() {
	int result = 0;
	final User user = UserView.getCurrentUser();
	ArrayList<PersonSiadapWrapper> siadapWrappers = SiadapRootModule.getInstance().getAssociatedSiadaps(user.getPerson(),
		false);
	//let's count the ones with pending actions: - anything that the user can do
	for (PersonSiadapWrapper personSiadapWrapper : siadapWrappers) {
	    if (!isToBeExcluded(personSiadapWrapper) && personSiadapWrapper.hasPendingActions())
		result++;
	}

	return result;
    }

    /**
     * Method that excludes the users which aren't part of the test group
     * 
     * @param personSiadapWrapper
     * @return true if the process should be excluded from the count, true
     *         otherwise
     */
    private boolean isToBeExcluded(PersonSiadapWrapper personSiadapWrapper) {
	if (personSiadapWrapper.getYear() != 2010)
	    return false;
	else {
	    MyOrg myOrg = MyOrg.getInstance();
	    //let's check if the user is of the test user group
	    if (siadapTestUserGroup != null && siadapTestUserGroup.hasUsers(UserView.getCurrentUser())) {
		return false;
	    }
	}
	return true;

    }

    @Override
    public void init(MyOrg root) {
	if (siadapTestUserGroup == null) {
	    for (PersistentGroup group : MyOrg.getInstance().getPersistentGroups()) {
		if (group instanceof NamedGroup) {
		    if (((NamedGroup) group).getName().equals(ImportTestUsers.groupName)) {
			siadapTestUserGroup = (NamedGroup) group;
		    }
		}
	    }
	}
    }

}
