/*
 * @(#)SiadapStructureManagementGroup.java
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
import java.util.List;
import java.util.Set;

import module.organization.domain.Person;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.util.SiadapMiscUtilClass;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.util.BundleUtil;

import org.joda.time.LocalDate;

/**
 * 
 * @author João Antunes
 * @author Luis Cruz
 * 
 */
public class SiadapStructureManagementGroup extends SiadapStructureManagementGroup_Base {
    
    public  SiadapStructureManagementGroup() {
        super();
    }

    // annotated as deprecated to avoid being wrongly used, because it makes more sense to use the year variant
    @Override
    @Deprecated
    public boolean isMember(User user) {
	return isMember(user, SiadapMiscUtilClass.returnLastUsableYear());
    }



    public static boolean isMember(User user, int year) {
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	if (configuration == null || configuration.getStructureManagementGroupMembers() == null)
	    return false;
	return configuration.getStructureManagementGroupMembers().contains(user.getPerson());
    }

    @Override
    public String getName() {
	try {
	    return BundleUtil.getStringFromResourceBundle("resources/SiadapResources",
		    "siadap.group.name.SiadapStructureManagementGroup");
	} catch (java.util.MissingResourceException ex) {
	    return this.getClass().getSimpleName();
	}
    }

    // annotated as deprecated to avoid being wrongly used, because it makes more sense to use the year variant
    @Override
    @Deprecated
    public Set<User> getMembers() {
	return getMembers(new LocalDate().getYear());
    }

    static public List<Person> getListOfMembers(int year) {
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	return configuration.getStructureManagementGroupMembers();

    }
    static public Set<User> getMembers(int year) {
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	Set<Person> groupPersons = configuration.getStructureManagementGroupMembersSet();

	Set<User> setToReturn = new HashSet<User>();

	for (Person person : groupPersons) {
	    setToReturn.add(person.getUser());
	}

	return setToReturn;
    }
    
}
