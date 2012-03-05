/*
 * @(#)CleanSiadapRedundantAccs.java
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import module.organization.domain.Accountability;
import module.organization.domain.Person;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.util.SiadapMiscUtilClass;
import myorg.domain.scheduler.WriteCustomTask;
import myorg.util.JavaUtil;

import org.joda.time.DateTime;

/**
 * 
 *         Script used to cleanup some accountabilities that weren't removed
 *         when they should have been due to a buggy PersonnelManagement
 *         interface
 * 
 * @author João Antunes
 * 
 */
public class CleanSiadapRedundantAccs extends WriteCustomTask {

    /*
     * (non-Javadoc)
     * 
     * @see myorg.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {

	Map<Person, Set<Accountability>> duplicateAccsPerEvaluated = new HashMap<Person, Set<Accountability>>();
	Map<Person, DateTime> earliestDateTimeFound = new HashMap<Person, DateTime>();
	//let's use all of the existing SIADAPs to get the accountabilities
	for (Siadap siadap : SiadapRootModule.getInstance().getSiadaps()) {
	    Integer year = siadap.getYear();
	    SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);

	    for (Accountability acc : siadap.getEvaluated().getParentAccountabilities(
		    siadapYearConfiguration.getEvaluationRelation())) {
		if (acc.isActive(SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(year)) && acc.getParent() instanceof Person
			&& acc.getChild() instanceof Person) {
		    DateTime earliestAge = acc.getCreationDate();
		    //so we have a candidate for a duplicate, let's check each against the other
		    for (Accountability innerAcc : siadap.getEvaluated().getParentAccountabilities(
			    siadapYearConfiguration.getEvaluationRelation())) {
			if (!acc.equals(innerAcc)
				&& innerAcc.isActive(SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(year))
				&& innerAcc.getParent() instanceof Person
				&& innerAcc.getChild() instanceof Person
				//				&& SiadapMiscUtilClass.isObjectEqual(acc.getBeginDate(), innerAcc.getBeginDate())
				//				&& SiadapMiscUtilClass.isObjectEqual(acc.getEndDate(), innerAcc.getEndDate())
				&& JavaUtil.isObjectEqualTo(acc.getAccountabilityType(),
					innerAcc.getAccountabilityType())) {
			    //so we have a duplicate one

			    //let's find out wich date we should use
			    if (earliestAge == null && innerAcc.getCreationDate() != null)
				earliestAge = innerAcc.getCreationDate();
			    if (innerAcc.getCreationDate() != null && innerAcc.getCreationDate().isAfter(earliestAge))
				earliestAge = innerAcc.getCreationDate();

			    //so this one is a replicated one, let's add it
			    Set<Accountability> set = duplicateAccsPerEvaluated.get(siadap.getEvaluated());
			    if (set == null) {
				set = new HashSet<Accountability>();
				duplicateAccsPerEvaluated.put(siadap.getEvaluated(), set);
			    }
			    set.add(acc); //we might be repeating, but who cares, it is a set
			    set.add(innerAcc);
			}
		    }
		    earliestDateTimeFound.put(siadap.getEvaluated(), earliestAge);
		}
	    }

	}

	Set<Accountability> accsToDelete = new HashSet<Accountability>();
	//let's infer what we should be deleting
	for (Person person : duplicateAccsPerEvaluated.keySet()) {
	    //we should delete the accountability if it has the same begin and end date as any other, and the one with the oldest creation date

	    for (Accountability acc : duplicateAccsPerEvaluated.get(person)) {
		if (!JavaUtil.isObjectEqualTo(earliestDateTimeFound.get(person), acc.getCreationDate())) {
		    accsToDelete.add(acc);
		}

	    }

	}

	int nrAccsToBeRemovedThatWerePrinted = 0;
	//let's print them before anything else
	for (Person person : duplicateAccsPerEvaluated.keySet()) {
	    out.println("Candidates for duplicate accs for Person: " + person.getName());
	    for (Accountability acc : duplicateAccsPerEvaluated.get(person)) {
		out.print("Acc from: '" + acc.getParent().getPartyName().getContent() + "' to: '"
			+ acc.getChild().getPartyName().getContent() + "' type: '"
			+ acc.getAccountabilityType().getName().getContent() + "' begin date: '" + acc.getBeginDate()
			+ "' end date: '" + acc.getEndDate() + "' Creation Date: '" + acc.getCreationDate() + "'");
		if (accsToDelete.contains(acc)) {
		    nrAccsToBeRemovedThatWerePrinted++;
		    out.print(" *TO BE REMOVED*");
		}
		out.println();
	    }

	}

	out.println("Size array to delete: " + accsToDelete.size() + " nr printed accs: " + nrAccsToBeRemovedThatWerePrinted);

	//relentlessly delete them :) [as we have triple checked they are the right ones to delete]
	int deleted = 0;
	for (Accountability acc : accsToDelete) {
	    deleted++;
	    //	    acc.obliviateIt();
	    throw new IllegalArgumentException("doesnt.do.anything");
	}
	out.println("Deleted " + deleted + " accountabilities");

    }

}
