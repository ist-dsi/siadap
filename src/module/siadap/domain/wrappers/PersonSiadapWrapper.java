package module.siadap.domain.wrappers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.Siadap;
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

    public Person getEvaluator() {
	Person evaluator = null;
	Person evaluated = getPerson();

	Collection<Party> parents = evaluated.getParents(configuration.getEvaluationRelation());
	if (!parents.isEmpty()) {
	    evaluator = (Person) parents.iterator().next();
	} else {
	    Collection<Party> workingPlaces = evaluated.getParents(configuration.getWorkingRelation());
	    if (workingPlaces.isEmpty()) {
		workingPlaces = evaluated.getParents(configuration.getWorkingRelationWithNoQuota());
	    }
	    Unit workingUnit = (Unit) workingPlaces.iterator().next();
	    Collection<Person> childPersons = workingUnit.getChildPersons(configuration.getEvaluationRelation());
	    evaluator = childPersons.iterator().next();
	}

	return evaluator;
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
	return getEvaluator() == UserView.getCurrentUser().getPerson();
    }

    public boolean isCurrentUserAbleToCreateProcess() {
	return getSiadap() == null && isCurrentUserAbleToEvaluate();
    }

    public BigDecimal getTotalEvaluationScoring() {
	Siadap siadap = getSiadap();
	return siadap != null ? (siadap.isEvaluationDone() ? siadap.getTotalEvaluationScoring() : null) : null;
    }

    public UnitSiadapWrapper getWorkingUnit() {
	return new UnitSiadapWrapper(getPerson().getParentUnits(configuration.getWorkingRelation()).iterator().next(),
		configuration.getYear());
    }

    public boolean isResponsibleForHarmonization(Person accessor) {
	return getWorkingUnit().isPersonResponsibleForHarmonization(accessor);
    }
}