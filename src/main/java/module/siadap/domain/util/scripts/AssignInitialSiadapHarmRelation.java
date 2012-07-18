/*
 * @(#)AssignInitialSiadapHarmRelation.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.util.SiadapMiscUtilClass;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import pt.ist.bennu.core.domain.scheduler.ReadCustomTask;
import pt.ist.bennu.core.domain.scheduler.TransactionalThread;

import org.joda.time.LocalDate;

/**
 * 
 *         Script that will use the default {@link SiadapEvaluationUniverse},
 *         the working relations defined in the configuration (
 *         {@link SiadapYearConfiguration#getWorkingRelation()},
 *         {@link SiadapYearConfiguration#getWorkingRelationWithNoQuota()}) to
 *         infer the configured defined SiadapHarmonizationRelations
 *         {@link SiadapYearConfiguration#getSiadap2HarmonizationRelation()}
 *         {@link SiadapYearConfiguration#getSiadap3HarmonizationRelation()})
 * 
 * @author João Antunes
 * 
 */
public class AssignInitialSiadapHarmRelation extends ReadCustomTask {
    
    private static final LocalDate DATE_TO_USE = new LocalDate(2011, 12, 20);
    int accApplied = 0;

    class AccountabilityAssignmentWrapper {
	final AccountabilityType accTypeToUse;
	final LocalDate startDate;
	final LocalDate endDate;
	final Unit unit;
	final Person person;

	public AccountabilityAssignmentWrapper(AccountabilityType accType, LocalDate startDate, LocalDate endDate, Unit unit,
		Person person) {
	    this.accTypeToUse = accType;
	    this.startDate = startDate;
	    this.endDate = endDate;
	    this.unit = unit;
	    this.person = person;
	}

	public void applyAcc() {
	    unit.addChild(person, accTypeToUse, startDate, endDate);
	    accApplied++;

	}
    }

    final List<AccountabilityAssignmentWrapper> accountabilitiesToApply = new ArrayList<AccountabilityAssignmentWrapper>();
    /* (non-Javadoc)
     * @see jvstm.TransactionalCommand#doIt()
     */
    @Override
    public void doIt() {
	//let's infer the accountabilities to Assign
	int totalNrSiadaps = 0;
	int totalNrSiadapsWithoutWorkingUnit = 0;
	int totalNrSiadapsToInfer = 0;
	int totalNrSiadapsAlreadyInfered = 0;
	int totalNrSiadapsSkippedDueToDifferentYear = 0;
	for (Siadap siadap : SiadapRootModule.getInstance().getSiadaps())
	{
	    ++totalNrSiadaps;
	    Integer year = siadap.getYear();
	    if (year < DATE_TO_USE.getYear()) {
		totalNrSiadapsSkippedDueToDifferentYear++;
		continue;
	    }
	    SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	    AccountabilityType siadap2HarmonizationRelation = siadapYearConfiguration.getSiadap2HarmonizationRelation();
	    AccountabilityType siadap3HarmonizationRelation = siadapYearConfiguration.getSiadap3HarmonizationRelation();
	    if (siadap2HarmonizationRelation == null || siadap3HarmonizationRelation == null)
	    {
		out.println("Could not assign H. relations for SIADAPs in the year " + year + " because the relations where not configured");
	    }
	    
		SiadapEvaluationUniverse defaultSiadapEvaluationUniverse = siadap.getDefaultSiadapEvaluationUniverse();
	    if (defaultSiadapEvaluationUniverse != null && defaultSiadapEvaluationUniverse.getSiadapUniverse() != null) {
		    Person person = siadap.getEvaluated();
		    UnitSiadapWrapper workingUnit = new PersonSiadapWrapper(person, year).getWorkingUnit();
		    Unit unit = workingUnit.getUnit();
		    if (unit == null)
		    {
			totalNrSiadapsWithoutWorkingUnit++;
		    }
		    else {
		    Collection<Accountability> currentAccountabilities = person.getParentAccountabilities(
			    siadap2HarmonizationRelation, siadap3HarmonizationRelation);
		    if (currentAccountabilities != null && currentAccountabilities.size() > 0)
			totalNrSiadapsAlreadyInfered++;
		    else {

			totalNrSiadapsToInfer++;
			//let's add this accountability
			AccountabilityType accTypeToUse = defaultSiadapEvaluationUniverse.getSiadapUniverse() == SiadapUniverse.SIADAP2 ? siadap2HarmonizationRelation : siadap3HarmonizationRelation;
			accountabilitiesToApply.add(new AccountabilityAssignmentWrapper(accTypeToUse, DATE_TO_USE,
				SiadapMiscUtilClass.lastDayOfYear(year), unit, person));
		    }
			
		    }
		}

	}
	out.println("Nr SIADAPs: " + totalNrSiadaps + " Nr SIADAPs without working unit: " + totalNrSiadapsWithoutWorkingUnit
		+ " Nr SIADAPs to infer: " + totalNrSiadapsToInfer + " Nr SIADAPS already infered: "
		+ totalNrSiadapsAlreadyInfered + " going to skip " + totalNrSiadapsSkippedDueToDifferentYear
		+ " because they are set in a past year");

	//gonna apply the accs (let's try to do it all at once!)
	ApplyAccountabilities applyAccountabilities = new ApplyAccountabilities(accountabilitiesToApply);
	applyAccountabilities.start();
	try {
	    applyAccountabilities.join();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    out.println("Error! printing status");
	    printStatus();
	    throw new Error(e);
	}
	printStatus();

    }

    private void printStatus() {
	out.println("Applied the following number of accountabilities: " + accApplied);

    }

    class ApplyAccountabilities extends TransactionalThread {

	final List<AccountabilityAssignmentWrapper> accountabilitiesToApply;

	public ApplyAccountabilities(List<AccountabilityAssignmentWrapper> accountabilitiesToApply) {
	    this.accountabilitiesToApply = accountabilitiesToApply;
	}

	@Override
	public void transactionalRun() {
	    for (AccountabilityAssignmentWrapper accWrapper : accountabilitiesToApply) {
		accWrapper.applyAcc();
		accApplied++;
	    }

	}

    }

}
