/*
 * @(#)SiadapPendingProcessesCounter.java
 *
 * Copyright 2011 Instituto Superior Tecnico
 * Founding Authors: Paulo Abrantes
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the SIADAP Module.
 *
 *   The SIADAP Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The SIADAP Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the SIADAP Module. If not, see <http://www.gnu.org/licenses/>.
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
 * 
 * @author João Antunes
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
	try {
	    ArrayList<PersonSiadapWrapper> siadapWrappers = SiadapRootModule.getInstance().getAssociatedSiadaps(user.getPerson(),
		false);
	    //let's count the ones with pending actions: - anything that the user can do
	    for (PersonSiadapWrapper personSiadapWrapper : siadapWrappers) {
		if (!isToBeExcluded(personSiadapWrapper) && personSiadapWrapper.hasPendingActions())
		    result++;
	    }
	} catch (final Throwable t) {
	    t.printStackTrace();
	    //throw new Error(t);
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
	    //let's check if the user is of the test user group
	    if (SiadapRootModule.getInstance().getSiadapTestUserGroup().hasUsers(UserView.getCurrentUser())) {
		return false;
	    }
	}
	return true;

    }



}
