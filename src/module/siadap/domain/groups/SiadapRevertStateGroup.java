package module.siadap.domain.groups;

import java.util.HashSet;
import java.util.Set;

import module.organization.domain.Person;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.util.SiadapMiscUtilClass;
import myorg.domain.User;
import myorg.util.BundleUtil;

import org.joda.time.LocalDate;

public class SiadapRevertStateGroup extends SiadapRevertStateGroup_Base {
    
    public  SiadapRevertStateGroup() {
        super();
    }

    @Override
    public boolean isMember(User user) {
	return isMember(user, SiadapMiscUtilClass.returnLastUsableYear());
    }

    public boolean isMember(User user, Integer year) {
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	return configuration.isPersonMemberOfRevertStateGroup(user.getPerson());
    }

    @Override
    public String getName() {
	try {
	    return BundleUtil.getStringFromResourceBundle("resources/SiadapResources",
 "siadap.group.name.SiadapRevertStateGroup");
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
	Set<Person> groupPersons = configuration.getRevertStateGroupMemberSet();

	Set<User> setToReturn = new HashSet<User>();

	for (Person person : groupPersons) {
	    setToReturn.add(person.getUser());
	}

	return setToReturn;

    }
    
}
