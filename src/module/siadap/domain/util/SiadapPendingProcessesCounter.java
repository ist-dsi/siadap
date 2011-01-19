/**
 * 
 */
package module.siadap.domain.util;

import java.util.ArrayList;

import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.workflow.domain.ProcessCounter;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt)
 * 
 */
public class SiadapPendingProcessesCounter extends ProcessCounter {

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
	    if (personSiadapWrapper.hasPendingActions())
		result++;
	}
	
	return result;
    }

}
