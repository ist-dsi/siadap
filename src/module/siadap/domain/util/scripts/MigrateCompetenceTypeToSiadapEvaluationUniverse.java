/*
 * @(#)MigrateCompetenceTypeToSiadapEvaluationUniverse.java
 *
 * Copyright 2012 Instituto Superior Tecnico
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
package module.siadap.domain.util.scripts;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapRootModule;
import myorg.domain.scheduler.WriteCustomTask;

/**
 * 
 * @author João Antunes
 * 
 */
public class MigrateCompetenceTypeToSiadapEvaluationUniverse extends WriteCustomTask {

    /* (non-Javadoc)
     * @see myorg.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
	int relationsDone = 0;
	int relationsNotDone = 0;
	//let's get all of the SIADAPs
	for (Siadap siadap : SiadapRootModule.getInstance().getSiadaps()) {
	    SiadapEvaluationUniverse defaultSiadapEvaluationUniverse = siadap.getDefaultSiadapEvaluationUniverse();
	    //let's get its CompetenceType, if it has one, let's set the relation
	    if (defaultSiadapEvaluationUniverse != null && siadap.getDefaultCompetenceType() != null) {
		defaultSiadapEvaluationUniverse.setCompetenceSlashCareerType(siadap.getDefaultCompetenceType());
		relationsDone++;
	    }
 else {
		relationsNotDone++;
	    }
	}

	out.println("Migrated " + relationsDone + " competences and didn't migrate " + relationsNotDone);

    }

}
