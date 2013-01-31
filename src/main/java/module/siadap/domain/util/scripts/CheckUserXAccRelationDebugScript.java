/*
 * @(#)CheckUserXAccRelationDebugScript.java
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
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.wrappers.PartyWrapper.FilterAccountabilities;
import module.siadap.domain.wrappers.UnitSiadapWrapper;

import org.apache.commons.collections.Predicate;

import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.scheduler.ReadCustomTask;

/**
 * Debug script to be used in the production/fenix-tests version
 * 
 * @author João Antunes
 * 
 */
public class CheckUserXAccRelationDebugScript extends ReadCustomTask {
	User user;

	private SiadapYearConfiguration getConfiguration() {
		return SiadapYearConfiguration.getSiadapYearConfiguration(new Integer(2011));
	}

	protected List<Person> getParentPersons(AccountabilityType... types) {
		return getParentPersons(new FilterAccountabilities(2011, true), types);
	}

	private List<Person> getParentPersons(Predicate predicate, AccountabilityType... types) {
		List<Person> person = new ArrayList<Person>();
		for (Accountability accountability : user.getPerson().getParentAccountabilities(types)) {
			if (predicate == null || predicate.evaluate(accountability)) {
				Party parent = accountability.getParent();
				if (parent.isPerson()) {
					person.add(((Person) parent));
				}
			}
		}
		return person;
	}

	public UnitSiadapWrapper getWorkingUnit() {
		Collection<Unit> parentUnits =
				getParentUnits(getConfiguration().getWorkingRelation(), getConfiguration().getWorkingRelationWithNoQuota());
		return parentUnits.isEmpty() ? null : new UnitSiadapWrapper(parentUnits.iterator().next(), getConfiguration().getYear());
	}

	protected List<Unit> getParentUnits(AccountabilityType... types) {
		return getParentUnits(new FilterAccountabilities(2011, true), types);
	}

	private List<Unit> getParentUnits(Predicate predicate, AccountabilityType... types) {
		List<Unit> units = new ArrayList<Unit>();
		for (Accountability accountability : user.getPerson().getParentAccountabilities(types)) {
			if (predicate == null || predicate.evaluate(accountability)) {
				Party parent = accountability.getParent();
				if (parent.isUnit()) {
					units.add(((Unit) parent));
				}
			}
		}
		return units;
	}

	private void printAllAcc(User user) {
		out.println("Username: " + user.getUsername() + " Name: " + user.getPerson().getName());
		out.println("Parent:");

		for (Accountability acc : user.getPerson().getParentAccountabilities()) {
			out.println("\t" + acc.getDetailsString() + " Parent:" + acc.getParent().getPartyName());

		}
		out.println("Child:");
		for (Accountability acc : user.getPerson().getChildAccountabilities()) {
			out.println("\t" + acc.getDetailsString() + " Parent:" + acc.getChild().getPartyName());
		}

	}

	@Override
	public void doIt() {
		//get the user to check
		User userA = User.findByUsername("ist12048");
		User userB = User.findByUsername("ist21846");

		User userC = User.findByUsername("ist24439");
		User userD = User.findByUsername("ist24616");

		User userE = User.findByUsername("ist149676");
		User userF = User.findByUsername("ist151339");

		User userG = User.findByUsername("ist12889");
		User userH = User.findByUsername("ist22353");

		//	User userI = User.findByUsername("ist12470");
		//	User userJ = User.findByUsername("ist23053");
		//	User userK = User.findByUsername("ist22237");

		printAllAcc(userA);
		printAllAcc(userB);
		printAllAcc(userC);
		printAllAcc(userD);
		printAllAcc(userE);
		printAllAcc(userF);
		printAllAcc(userG);
		printAllAcc(userH);
		//	printAllAcc(userI);
		//	printAllAcc(userJ);
		//	printAllAcc(userK);

	}

}
