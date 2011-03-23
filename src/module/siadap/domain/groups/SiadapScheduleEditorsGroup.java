package module.siadap.domain.groups;

import java.util.HashSet;
import java.util.Set;

import module.organization.domain.Person;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;
import myorg.domain.User;
import myorg.util.BundleUtil;

import org.joda.time.LocalDate;

public class SiadapScheduleEditorsGroup extends SiadapScheduleEditorsGroup_Base {

    public SiadapScheduleEditorsGroup() {
	super();
    }

    public static SiadapScheduleEditorsGroup getInstance() {
	return SiadapRootModule.getInstance().getSiadapScheduleEditorsGroup();
    }

    @Override
    public boolean isMember(User user) {
	//get the current year and use it to assert
	return isMember(user, new LocalDate().getYear());
    }

    public boolean isMember(User user, Integer year) {
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	return configuration.isPersonMemberOfScheduleExtenders(user.getPerson());
    }

    @Override
    public String getName() {
	try {
	    return BundleUtil.getStringFromResourceBundle("resources/SiadapResources",
		    "siadap.group.name.SiadapScheduleEditorsGroup");
	} catch (java.util.MissingResourceException ex) {
	    return this.getClass().getSimpleName();
	}
    }

    @Override
    public Set<User> getMembers() {
	return getMembers(new LocalDate().getYear());
    }

    public Set<User> getMembers(Integer year) {
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	Set<Person> groupPersons = configuration.getScheduleEditorsSet();

	Set<User> setToReturn = new HashSet<User>();

	for (Person person : groupPersons) {
	    setToReturn.add(person.getUser());
	}

	return setToReturn;

    }

}
