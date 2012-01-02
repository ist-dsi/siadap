package module.siadap.domain.groups;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import module.organization.domain.Person;
import module.siadap.domain.SiadapYearConfiguration;
import myorg.domain.User;
import myorg.util.BundleUtil;

import org.joda.time.LocalDate;

public class SiadapStructureManagementGroup extends SiadapStructureManagementGroup_Base {
    
    public  SiadapStructureManagementGroup() {
        super();
    }

    // annotated as deprecated to avoid being wrongly used, because it makes more sense to use the year variant
    @Override
    @Deprecated
    public boolean isMember(User user) {
	return isMember(user, new LocalDate().getYear());
    }

    public static boolean isMember(User user, int year) {
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	return configuration != null && configuration.getStructureManagementGroupMembers().contains(user.getPerson());
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
