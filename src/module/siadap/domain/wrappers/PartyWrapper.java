package module.siadap.domain.wrappers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;

import org.apache.commons.collections.Predicate;
import org.jfree.data.time.Month;
import org.joda.time.LocalDate;

public abstract class PartyWrapper implements Serializable {

    private int year;
    private SiadapYearConfiguration configuration;

    public PartyWrapper(int year) {
	this.year = year;
	this.configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
    }

    public int getYear() {
	return year;
    }

    public void setYear(int year) {
	this.year = year;
    }

    public SiadapYearConfiguration getConfiguration() {
	return configuration;
    }

    protected abstract Party getParty();

    protected List<Unit> getParentUnits(AccountabilityType... types) {
	return getParentUnits(new FilterAccountabilities(getYear(), true), types);
    }

    private List<Unit> getParentUnits(Predicate predicate, AccountabilityType... types) {
	List<Unit> units = new ArrayList<Unit>();
	for (Accountability accountability : getParty().getParentAccountabilities(types)) {
	    if (predicate == null || predicate.evaluate(accountability)) {
		Party parent = accountability.getParent();
		if (parent.isUnit()) {
		    units.add(((Unit) parent));
		}
	    }
	}
	return units;
    }

    protected List<Person> getChildPersons(AccountabilityType... types) {
	return getChildPersons(new FilterAccountabilities(getYear(), true), types);
    }

    protected List<Person> getChildPersons(Predicate predicate, AccountabilityType... types) {
	List<Person> people = new ArrayList<Person>();
	for (Accountability accountability : getParty().getChildrenAccountabilities(types)) {
	    if (predicate == null || predicate.evaluate(accountability)) {
		Party parent = accountability.getChild();
		if (parent.isPerson()) {
		    people.add(((Person) parent));
		}
	    }
	}
	return people;
    }

    protected List<Unit> getChildUnits(AccountabilityType... types) {
	return getChildUnits(new FilterAccountabilities(getYear(), true), types);
    }

    protected List<Unit> getChildUnits(Predicate predicate, AccountabilityType... types) {
	List<Unit> units = new ArrayList<Unit>();
	for (Accountability accountability : getParty().getChildrenAccountabilities(types)) {
	    if (predicate == null || predicate.evaluate(accountability)) {
		Party parent = accountability.getChild();
		if (parent.isUnit()) {
		    units.add(((Unit) parent));
		}
	    }
	}
	return units;
    }

    public static class FilterAccountabilities implements Predicate {
	private LocalDate begin;
	private LocalDate end;
	private boolean skipClosedAccountabilities;

	public FilterAccountabilities(int year, boolean skipClosedAccountabilities) {
	    this.begin = new LocalDate(year, Month.JANUARY, 1);
	    this.end = new LocalDate(year, Month.DECEMBER, 31);
	    this.skipClosedAccountabilities = skipClosedAccountabilities;
	}

	@Override
	public boolean evaluate(Object arg0) {
	    Accountability accountability = (Accountability) arg0;
	    LocalDate accountabilityStart = accountability.getBeginDate();
	    LocalDate accountabilityEnd = accountability.getEndDate();

	    return ((accountabilityEnd == null && accountabilityStart.isBefore(end)) || (!skipClosedAccountabilities && accountability
		    .intersects(begin, end)));
	}
    }
}
