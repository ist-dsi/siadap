package module.siadap.domain;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import pt.ist.fenixWebFramework.services.Service;

public class SiadapYearConfiguration extends SiadapYearConfiguration_Base {

    private static final Double DEFAULT_OBJECTIVES_PONDERATION = 75.0;
    private static final Double DEFAULT_COMPETENCES_PONDERATION = 25.0;

    public SiadapYearConfiguration(Integer year, Double objectivesPonderation, Double competencesPonderation) {
	super();
	setYear(year);
	setObjectivesPonderation(objectivesPonderation);
	setCompetencesPonderation(competencesPonderation);
	setSiadapRootModule(SiadapRootModule.getInstance());
    }

    public static SiadapYearConfiguration getSiadapYearConfiguration(Integer year) {
	for (SiadapYearConfiguration configuration : SiadapRootModule.getInstance().getYearConfigurations()) {
	    if (configuration.getYear() == year) {
		return configuration;
	    }
	}
	return null;
    }

    @Service
    public static SiadapYearConfiguration createNewSiadapYearConfiguration(Integer year) {
	SiadapYearConfiguration configuration = getSiadapYearConfiguration(year);
	if (configuration != null) {
	    return configuration;
	}
	return new SiadapYearConfiguration(year, DEFAULT_OBJECTIVES_PONDERATION, DEFAULT_COMPETENCES_PONDERATION);
    }

    public Set<Person> getEvalutedFor(Person evaluator) {
	Set<Person> people = new HashSet<Person>();

	return people;
    }

    public Person getEvaluatorFor(Person evaluated) {
	Person evaluator = null;

	Collection<Party> parents = evaluated.getParents(getEvaluationRelation());
	if (!parents.isEmpty()) {
	    evaluator = (Person) parents.iterator().next();
	} else {
	    Collection<Party> workingPlaces = evaluated.getParents(getWorkingRelation());
	    Unit workingUnit = (Unit) workingPlaces.iterator().next();
	    Collection<Person> childPersons = workingUnit.getChildPersons(getEvaluationRelation());
	    evaluator = childPersons.iterator().next();
	}

	return evaluator;
    }

    public Collection<Unit> getHarmozationUnitsFor(Person loggedPerson) {
	return loggedPerson.getParentUnits(getHarmonizationResponsibleRelation());
    }

    public BigDecimal getRelevantEvaluationPercentageFor(Unit unit) {
	int count = 0;
	Collection<Person> childPersons = unit.getChildPersons(getWorkingRelation());
	int totalPeople = childPersons.size();

	for (Person person : childPersons) {
	    Siadap siadap = getSiadapFor(person, getYear());
	    if (siadap.getQualitativeEvaluation() == SiadapGlobalEvaluation.HIGH) {
		count++;
	    }
	}
	return new BigDecimal(count).divide(new BigDecimal(totalPeople)).multiply(new BigDecimal(100));
    }

    public int getTotalPeopleWorkingFor(Unit unit, boolean continueToSubUnit) {
	int people = 0;
	Collection<Person> childPersons = unit.getChildPersons(getWorkingRelation());
	people += childPersons.size();
	if (continueToSubUnit) {
	    for (Unit subUnit : unit.getChildUnits(getUnitRelations())) {
		people += getTotalPeopleWorkingFor(subUnit, continueToSubUnit);
	    }
	}
	return people;
    }

    public int getTotalPeopleWithSiadapWorkingFor(Unit unit, boolean continueToSubUnit) {
	int people = 0;
	int year = getYear();
	Collection<Person> childPersons = unit.getChildPersons(getWorkingRelation());
	for (Person person : childPersons) {
	    if (getSiadapFor(person, year) != null) {
		people++;
	    }
	}
	if (continueToSubUnit) {
	    for (Unit subUnit : unit.getChildUnits(getUnitRelations())) {
		people += getTotalPeopleWithSiadapWorkingFor(subUnit, continueToSubUnit);
	    }
	}
	return people;
    }

    public int getTotalRelevantEvaluationsForUnit(Unit unit, boolean continueToSubUnits) {
	int counter = 0;
	for (Person person : unit.getChildPersons(getWorkingRelation())) {
	    Siadap siadap = getSiadapFor(person, getYear());
	    if (siadap != null) {
		if (siadap.isEvaluationDone() && siadap.getQualitativeEvaluation() == SiadapGlobalEvaluation.HIGH) {
		    counter++;
		}
	    }
	}
	if (continueToSubUnits) {
	    for (Unit subUnit : unit.getChildUnits(getUnitRelations())) {
		counter += getTotalRelevantEvaluationsForUnit(subUnit, continueToSubUnits);
	    }
	}

	return counter;
    }

    public Siadap getSiadapFor(Person person, Integer year) {
	for (Siadap siadap : person.getSiadapsAsEvaluated()) {
	    if (siadap.getYear().equals(year)) {
		return siadap;
	    }
	}
	return null;
    }

}
