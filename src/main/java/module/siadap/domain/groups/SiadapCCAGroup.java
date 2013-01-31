/*
 * @(#)SiadapCCAGroup.java
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
package module.siadap.domain.groups;

import java.util.HashSet;
import java.util.Set;

import module.organization.domain.Person;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.util.SiadapMiscUtilClass;

import org.joda.time.LocalDate;

import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.util.BundleUtil;

/**
 * 
 * @author João Antunes
 * 
 */
public class SiadapCCAGroup extends SiadapCCAGroup_Base {

	public SiadapCCAGroup() {
		super();
	}

	@Override
	public boolean isMember(User user) {
		return isMember(user, SiadapMiscUtilClass.returnLastUsableYear());
	}

	private static boolean isMember(User user, Integer year) {
		SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
		return configuration.getCcaMembers().contains(user.getPerson());
	}

	@Override
	public String getName() {
		try {
			return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", "siadap.group.name.SiadapCCAGroup");
		} catch (java.util.MissingResourceException ex) {
			return this.getClass().getSimpleName();
		}
	}

	@Override
	public Set<User> getMembers() {
		return getMembers(new LocalDate().getYear());
	}

	private Set<User> getMembers(Integer year) {
		SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
		Set<Person> groupPersons = configuration.getCcaMembersSet();

		Set<User> setToReturn = new HashSet<User>();

		for (Person person : groupPersons) {
			setToReturn.add(person.getUser());
		}

		return setToReturn;
	}

}
