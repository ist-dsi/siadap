/*
 * @(#)MakeXRelationsEndAtEndYear.java
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
package module.siadap.domain.util.scripts;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.siadap.domain.SiadapYearConfiguration;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;

import org.joda.time.LocalDate;

/**
 * 
 * @author João Antunes
 * 
 */
public class MakeXRelationsEndAtEndYear extends WriteCustomTask {

    int nrOfEvalAccEnded = 0;

    @Override
    protected void doService() {

	out.println("STarting");

	LocalDate today = new LocalDate();
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(today.getYear());
	AccountabilityType evaluationRelation = configuration.getEvaluationRelation();
	AccountabilityType workingRelation = configuration.getWorkingRelation();
	AccountabilityType workingRelationWithNoQuota = configuration.getWorkingRelationWithNoQuota();

	//let's get all of the evaluation relations already set

	endUnfinishedAcc(workingRelationWithNoQuota);
	endUnfinishedAcc(workingRelation);
	endUnfinishedAcc(evaluationRelation);



	out.println("Done. Converted " + nrOfEvalAccEnded + " eval relations");

    }

    private void endUnfinishedAcc(AccountabilityType accType) {
	for (Accountability accountability : accType.getAccountabilities()) {
	    LocalDate endDate = accountability.getEndDate();
	    if (endDate == null) {
		endDate = new LocalDate((new LocalDate()).getYear(), 12, 31);
		accountability.editDates(accountability.getBeginDate(), endDate);
		nrOfEvalAccEnded++;
		out.println("Ended an accountability where parent is " + accountability.getParent().getPartyName()
			+ " and child is " + accountability.getChild().getPartyName() + " now the accountability is: "
			+ accountability.getDetailsString());
	    }
	}
    }


}
