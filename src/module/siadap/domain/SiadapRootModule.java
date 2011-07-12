package module.siadap.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.groups.SiadapCCAGroup;
import module.siadap.domain.groups.SiadapScheduleEditorsGroup;
import module.siadap.domain.groups.SiadapStructureManagementGroup;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import myorg.domain.ModuleInitializer;
import myorg.domain.MyOrg;
import myorg.domain.groups.NamedGroup;
import myorg.domain.groups.PersistentGroup;
import myorg.domain.groups.UnionGroup;
import pt.ist.fenixWebFramework.services.Service;

public class SiadapRootModule extends SiadapRootModule_Base implements ModuleInitializer {

    private static boolean isInitialized = false;

    private static NamedGroup siadapTestUserGroup;

    private static ThreadLocal<SiadapRootModule> init = null;

    private SiadapRootModule() {
	super();
	setMyOrg(MyOrg.getInstance());
	setNumber(0);
    }

    public static SiadapRootModule getInstance() {
	if (init != null) {
	    return init.get();
	}

	if (!isInitialized) {
	    initialize();
	}
	final MyOrg myOrg = MyOrg.getInstance();
	return myOrg.getSiadapRootModule();
    }

    @Override
    public void init(MyOrg root) {
	if (getSiadapTestUserGroup() == null) {
	    initializeSiadapGroups(root);
	}
    }

    /**
     * 
     * @param forPerson
     *            the person to whom we should be returning all of the
     *            PersonSiadapWrapper instances related with him
     * @param includeClosedYears
     *            if true, it will return all of the years, even the closed ones
     *            (where nothing should be able to be done for), false isn't
     *            implemented yet, but should return only for the open years
     *            TODO related with Issue #31
     * @return a set of PersonSiadapWrapper with all of the PersonSiadapWrapper
     *         instances associated with the given forPerson person and
     *         including or not closed years TODO depending on the
     *         includeClosedYears parameter. Or an empty list if none are
     *         available
     */
    public ArrayList<PersonSiadapWrapper> getAssociatedSiadaps(Person forPerson, boolean includeClosedYears) {
	ArrayList<PersonSiadapWrapper> personSiadapWrapperToReturn = new ArrayList<PersonSiadapWrapper>();
	//get all of the years
	//TODO implement the includeClosedYears functionality related with Issue #31
	for (SiadapYearConfiguration yearConfiguration : getYearConfigurations()) {
	    personSiadapWrapperToReturn.addAll(getAssociatedSiadaps(forPerson, yearConfiguration.getYear(), includeClosedYears));
	}

	return personSiadapWrapperToReturn;

    }

    public ArrayList<PersonSiadapWrapper> getAssociatedSiadaps(Person forPerson, int year, boolean includeClosedYears) {
	SiadapYearConfiguration yearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	ArrayList<PersonSiadapWrapper> personSiadapWrapperToReturn = new ArrayList<PersonSiadapWrapper>();
	//TODO Related with Issue #31 take the includeClosedYears into account
	if (yearConfiguration == null) {
	    return personSiadapWrapperToReturn;
	}
	PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(forPerson, year);
	// now let's add all of the evaluated persons to the list
	Set<PersonSiadapWrapper> peopleToEvaluate = personSiadapWrapper.getPeopleToEvaluate();
	if (peopleToEvaluate != null) {
	    personSiadapWrapperToReturn.addAll(peopleToEvaluate);
	}
	personSiadapWrapperToReturn.add(personSiadapWrapper);
	return personSiadapWrapperToReturn;

    }

    @Service
    public synchronized static void initialize() {
	if (!isInitialized) {
	    try {
		final MyOrg myOrg = MyOrg.getInstance();
		final SiadapRootModule system = myOrg.getSiadapRootModule();
		if (system == null) {
		    new SiadapRootModule();
		}
		init = new ThreadLocal<SiadapRootModule>();
		init.set(myOrg.getSiadapRootModule());

		isInitialized = true;
	    } finally {
		init = null;
	    }
	}

    }

    private void initializeSiadapGroups(MyOrg root) {
	SiadapRootModule.getInstance();
	for (PersistentGroup group : root.getPersistentGroups()) {
		if (group instanceof NamedGroup) {
		//init the named groups
		    if (((NamedGroup) group).getGroupName().equals(ImportTestUsers.groupName)) {
		    //init the test user group
			setSiadapTestUserGroup((NamedGroup) group);
		    }
		}
	    }

	if (getSiadapTestUserGroup() == null) {
	    //TODO create it ?!
	}
	if (getSiadapCCAGroup() == null) {
	    setSiadapCCAGroup(new SiadapCCAGroup());
	}
	if (getSiadapScheduleEditorsGroup() == null) {
	    setSiadapScheduleEditorsGroup(new SiadapScheduleEditorsGroup());
	}
	if (getSiadapStructureManagementGroup() == null) {
	    setSiadapStructureManagementGroup(new SiadapStructureManagementGroup());
	}
	if (getStatisticsAccessUnionGroup() == null)
	{
	    setStatisticsAccessUnionGroup(new UnionGroup(myorg.domain.groups.Role.getRole(myorg.domain.RoleType.MANAGER),
		    getSiadapScheduleEditorsGroup(), getSiadapCCAGroup(), getSiadapStructureManagementGroup()));
	}

    }

    @Override
    public Integer getNumber() {
	throw new UnsupportedOperationException("Use getNumberAndIncrement instead");
    }

    public Integer getNumberAndIncrement() {
	Integer processNumber = super.getNumber();
	setNumber(processNumber + 1);
	return processNumber;
    }

    private void addHarmonizationUnits(Set<Unit> set, SiadapYearConfiguration siadapYearConfiguration, Unit unit) {
	set.add(unit);
	for (Unit iteratingUnit : unit.getChildUnits(siadapYearConfiguration.getUnitRelations())) {
	    if (!iteratingUnit.getChildPersons(siadapYearConfiguration.getHarmonizationResponsibleRelation()).isEmpty()) {
		addHarmonizationUnits(set, siadapYearConfiguration, iteratingUnit);
	    }
	}
    }

    public Set<Unit> getHarmonizationUnits(Integer year) {
	SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	Unit topUnit = siadapYearConfiguration.getSiadapStructureTopUnit();
	Set<Unit> units = new HashSet<Unit>();
	addHarmonizationUnits(units, siadapYearConfiguration, topUnit);
	return units;
    }

    private static void setSiadapTestUserGroup(NamedGroup siadapTestUserGroup) {
	SiadapRootModule.siadapTestUserGroup = siadapTestUserGroup;
    }


    public NamedGroup getSiadapTestUserGroup() {
	return siadapTestUserGroup;
    }
}
