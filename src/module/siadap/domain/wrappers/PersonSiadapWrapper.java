package module.siadap.domain.wrappers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.Predicate;

import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapYearConfiguration;
import myorg.applicationTier.Authenticate.UserView;

public class PersonSiadapWrapper implements Serializable {

    private Person person;
    private int year;
    private SiadapYearConfiguration configuration;

    public PersonSiadapWrapper(Person person, int year) {
	this.person = person;
	this.year = year;
	this.configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
    }

    public Person getPerson() {
	return person;
    }

    public void setPerson(Person person) {
	this.person = person;
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

    public void setConfiguration(SiadapYearConfiguration configuration) {
	this.configuration = configuration;
    }

    public boolean isEvaluationStarted() {
	return getSiadap() != null;
    }

    public Siadap getSiadap() {
	return this.configuration.getSiadapFor(getPerson(), getYear());
    }

    public PersonSiadapWrapper getEvaluator() {
	Person evaluator = null;
	Person evaluated = getPerson();

	Collection<Party> parents = evaluated.getParents(configuration.getEvaluationRelation());
	Party party = parents.isEmpty() ? null : parents.iterator().next();
	if (party instanceof Person) {
	    evaluator = (Person) party;
	} else {
	    if (getWorkingUnit() != null) {
		Collection<Party> workingPlaces = evaluated.getParents(configuration.getWorkingRelation());
		if (workingPlaces.isEmpty()) {
		    workingPlaces = evaluated.getParents(configuration.getWorkingRelationWithNoQuota());
		}
		Unit workingUnit = (Unit) workingPlaces.iterator().next();
		Collection<Person> childPersons = workingUnit.getChildPersons(configuration.getEvaluationRelation());
		evaluator = childPersons.iterator().next();
	    }
	}

	return evaluator != null ? new PersonSiadapWrapper(evaluator, getYear()) : null;
    }

    public Collection<UnitSiadapWrapper> getHarmozationUnits() {
	List<UnitSiadapWrapper> units = new ArrayList<UnitSiadapWrapper>();
	for (Unit unit : getPerson().getParentUnits(configuration.getHarmonizationResponsibleRelation())) {
	    units.add(new UnitSiadapWrapper(unit, getYear()));
	}
	return units;
    }

    public boolean isAccessibleToCurrentUser() {
	Siadap siadap = getSiadap();
	if (siadap == null) {
	    return false;
	}
	return siadap.getProcess().isAccessibleToCurrentUser();
    }

    public boolean isCurrentUserAbleToEvaluate() {
	return getEvaluator() != null && getEvaluator().getPerson() == UserView.getCurrentUser().getPerson();
    }

    public boolean isCurrentUserAbleToCreateProcess() {
	return getSiadap() == null && isCurrentUserAbleToEvaluate();
    }

    public BigDecimal getTotalEvaluationScoring() {
	Siadap siadap = getSiadap();
	return siadap != null ? (siadap.isEvaluationDone() ? siadap.getTotalEvaluationScoring() : null) : null;
    }

    public UnitSiadapWrapper getWorkingUnit() {
	Collection<Unit> parentUnits = getPerson().getParentUnits(configuration.getWorkingRelation());
	if (parentUnits.isEmpty()) {
	    parentUnits = getPerson().getParentUnits(configuration.getWorkingRelationWithNoQuota());
	}
	return parentUnits.isEmpty() ? null : new UnitSiadapWrapper(parentUnits.iterator().next(), configuration.getYear());
    }

    public boolean isQuotaAware() {
	return getPerson().getParentUnits(configuration.getWorkingRelationWithNoQuota()).isEmpty() ? true : false;
    }

    public boolean isResponsibleForHarmonization(Person accessor) {
	return getWorkingUnit().isPersonResponsibleForHarmonization(accessor);
    }

    public Set<PersonSiadapWrapper> getPeopleToEvaluate() {
	Set<PersonSiadapWrapper> people = new HashSet<PersonSiadapWrapper>();
	final Person evaluator = getPerson();
	final AccountabilityType evaluationRelation = configuration.getEvaluationRelation();

	for (Person person : evaluator.getChildPersons(evaluationRelation)) {
	    people.add(new PersonSiadapWrapper(person, getYear()));
	}
	for (Unit unit : evaluator.getParentUnits(evaluationRelation)) {
	    people.addAll(new UnitSiadapWrapper(unit, getYear()).getUnitEmployees(new Predicate() {

		@Override
		public boolean evaluate(Object arg0) {
		    PersonSiadapWrapper wrapper = (PersonSiadapWrapper) arg0;
		    PersonSiadapWrapper evaluatorWrapper = wrapper.getEvaluator();
		    return evaluatorWrapper != null && evaluatorWrapper.getPerson() == evaluator;
		}

	    }));
	}
	return people;

    }

    public Set<Siadap> getAllSiadaps() {
	Set<Siadap> processes = new TreeSet<Siadap>(new Comparator<Siadap>() {

	    @Override
	    public int compare(Siadap o1, Siadap o2) {
		return o1.getYear().compareTo(o2.getYear());
	    }

	});

	processes.addAll(getPerson().getSiadapsAsEvaluated());
	return processes;
    }

    public MultiLanguageString getName() {
	return getPerson().getPartyName();
    }

    @Override
    public int hashCode() {
	return getPerson().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof PersonSiadapWrapper) {
	    return ((PersonSiadapWrapper) obj).getPerson() == getPerson();
	}
	return false;
    }
}