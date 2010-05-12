package module.siadap.domain.wrappers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.Predicate;

import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.organization.domain.predicates.PartyPredicate;
import module.organization.domain.predicates.PartyPredicate.PartyByAccountabilityType;
import module.organization.domain.predicates.PartyPredicate.PartyByPartyType;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;

public class UnitSiadapWrapper implements Serializable {

    private static final int SCALE = 4;

    private Unit unit;
    private Integer year;
    private SiadapYearConfiguration configuration;

    public UnitSiadapWrapper(Unit unit, Integer year) {
	this.unit = unit;
	this.year = year;
	this.configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
    }

    public Unit getUnit() {
	return unit;
    }

    public void setUnit(Unit unit) {
	this.unit = unit;
    }

    public Integer getYear() {
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

    public int getTotalPeopleWorkingInUnit() {
	return getTotalPeopleWorkingInUnit(true);
    }

    public int getTotalPeopleWorkingInUnit(boolean continueToSubUnits) {
	return getTotalPeopleWorkingInUnit(getUnit(), continueToSubUnits);
    }

    private int getTotalPeopleWorkingInUnit(Unit unit, boolean continueToSubUnit) {
	int people = 0;
	Collection<Person> childPersons = unit.getChildPersons(configuration.getWorkingRelation());
	people += childPersons.size();
	if (continueToSubUnit) {
	    for (Unit subUnit : unit.getChildUnits(configuration.getUnitRelations())) {
		people += getTotalPeopleWorkingInUnit(subUnit, continueToSubUnit);
	    }
	}
	return people;
    }

    public int getTotalPeopleWithSiadapWorkingInUnit() {
	return getTotalPeopleWithSiadapWorkingInUnit(true);
    }

    public int getTotalPeopleWithSiadapWorkingInUnit(boolean continueToSubUnits) {
	return getTotalPeopleWithSiadapWorkingInUnit(getUnit(), continueToSubUnits);
    }

    private int getTotalPeopleWithSiadapWorkingInUnit(Unit unit, boolean continueToSubUnit) {
	int people = 0;
	int year = getYear();
	Collection<Person> childPersons = unit.getChildPersons(configuration.getWorkingRelation());
	for (Person person : childPersons) {
	    if (configuration.getSiadapFor(person, year) != null) {
		people++;
	    }
	}
	if (continueToSubUnit) {
	    for (Unit subUnit : unit.getChildUnits(configuration.getUnitRelations())) {
		people += getTotalPeopleWithSiadapWorkingInUnit(subUnit, continueToSubUnit);
	    }
	}
	return people;
    }

    public int getTotalRelevantEvaluationsForUnit() {
	return getTotalRelevantEvaluationsForUnit(true);
    }

    public int getTotalRelevantEvaluationsForUnit(boolean continueToSubUnits) {
	return getTotalRelevantEvaluationsForUnit(getUnit(), continueToSubUnits);
    }

    private int getTotalRelevantEvaluationsForUnit(Unit unit, boolean continueToSubUnits) {
	int counter = 0;
	for (Person person : unit.getChildPersons(configuration.getWorkingRelation())) {
	    Siadap siadap = configuration.getSiadapFor(person, getYear());
	    if (siadap != null) {
		if (siadap.hasRelevantEvaluation()) {
		    counter++;
		}
	    }
	}
	if (continueToSubUnits) {
	    for (Unit subUnit : unit.getChildUnits(configuration.getUnitRelations())) {
		counter += getTotalRelevantEvaluationsForUnit(subUnit, continueToSubUnits);
	    }
	}

	return counter;
    }

    public BigDecimal getRelevantEvaluationPercentage() {
	int totalPeopleWorkingForUnit = getTotalPeopleWorkingInUnit(true);
	int totalRelevantEvaluationsForUnit = getCurrentUsedHighGradeQuota();

	if (totalRelevantEvaluationsForUnit == 0) {
	    return BigDecimal.ZERO;
	}

	return new BigDecimal(totalRelevantEvaluationsForUnit).divide(new BigDecimal(totalPeopleWorkingForUnit),
		UnitSiadapWrapper.SCALE, RoundingMode.HALF_EVEN).multiply(new BigDecimal(100)).stripTrailingZeros();
    }

    public Collection<Person> getEvaluationResponsibles() {
	return getUnit().getChildPersons(this.configuration.getEvaluationRelation());
    }

    public Integer getHighGradeQuota() {
	int totalPeople = getTotalPeopleWorkingInUnit();

	BigDecimal result = new BigDecimal(totalPeople)
		.multiply(new BigDecimal(SiadapYearConfiguration.MAXIMUM_HIGH_GRADE_QUOTA)).divide(new BigDecimal(100));
	int value = result.intValue();

	return value > 0 ? value : 1; // if the quota is 0 then the quota shifts
	// to 1
    }

    public Integer getCurrentUsedHighGradeQuota() {
	return getTotalRelevantEvaluationsForUnit(getUnit(), true);
    }

    public Integer getExcellencyGradeQuota() {
	int totalPeople = getTotalPeopleWorkingInUnit();

	BigDecimal result = new BigDecimal(totalPeople).multiply(
		new BigDecimal(SiadapYearConfiguration.MAXIMUM_EXCELLENCY_GRADE_QUOTA)).divide(new BigDecimal(100));
	int value = result.intValue();

	return value > 0 ? value : 1; // if the quota is 0 then the quota shifts
	// to 1
    }

    // TODO: implement this method after implementing excellency.
    public Integer getCurrentUsedExcellencyGradeQuota() {
	return 0;
    }

    public Unit getHarmonizationUnit() {
	return getHarmonizationUnit(getUnit());
    }

    private Unit getHarmonizationUnit(Unit unit) {
	if (!unit.getChildPersons(configuration.getHarmonizationResponsibleRelation()).isEmpty()) {
	    return unit;
	}
	Collection<Unit> units = unit.getParentUnits(configuration.getUnitRelations());
	return units.isEmpty() ? null : getHarmonizationUnit(units.iterator().next());
    }

    public boolean isResponsibleForHarmonization() {
	return !getUnit().getChildPersons(this.configuration.getHarmonizationResponsibleRelation()).isEmpty();
    }

    public boolean isPersonResponsibleForHarmonization(Person person) {
	return isPersonResponsibleForHarmonization(getUnit(), person);
    }

    private boolean isPersonResponsibleForHarmonization(Unit unit, Person person) {
	Collection<Person> childPersons = getUnit().getChildPersons(this.configuration.getHarmonizationResponsibleRelation());
	if (childPersons.contains(person)) {
	    return true;
	} else {
	    Collection<Unit> parentUnits = getUnit().getParentUnits(this.configuration.getHarmonizationResponsibleRelation());
	    if (parentUnits.isEmpty()) {
		return false;
	    } else {
		return isPersonResponsibleForHarmonization(parentUnits.iterator().next(), person);
	    }
	}
    }

    public boolean isAboveQuotas() {
	return getCurrentUsedHighGradeQuota() > getHighGradeQuota()
		|| getCurrentUsedExcellencyGradeQuota() > getExcellencyGradeQuota();
    }

    public Unit getSuperiorUnit() {
	Collection<Unit> parentUnits = getUnit().getParentUnits(this.configuration.getUnitRelations());
	return parentUnits.isEmpty() ? null : parentUnits.iterator().next();
    }

    public List<PersonSiadapWrapper> getUnitEmployees() {
	return getUnitEmployees(null);
    }

    public List<PersonSiadapWrapper> getUnitEmployees(boolean continueToSubUnits) {
	return getUnitEmployees(continueToSubUnits, null);
    }

    public List<PersonSiadapWrapper> getUnitEmployees(Predicate predicate) {
	return getUnitEmployees(true, predicate);
    }

    public List<PersonSiadapWrapper> getUnitEmployees(boolean continueToSubUnits, Predicate predicate) {
	List<PersonSiadapWrapper> employees = new ArrayList<PersonSiadapWrapper>();
	List<AccountabilityType> accountabilities = new ArrayList<AccountabilityType>();
	accountabilities.add(configuration.getWorkingRelation());
	accountabilities.add(configuration.getWorkingRelationWithNoQuota());

	getUnitEmployees(getUnit(), employees, continueToSubUnits, accountabilities, predicate);
	return employees;
    }

    public List<PersonSiadapWrapper> getUnitEmployeesWithQuotas(boolean continueToSubUnits) {
	return getUnitEmployeesWithQuotas(continueToSubUnits, null);
    }

    public List<PersonSiadapWrapper> getUnitEmployeesWithQuotas(Predicate predicate) {
	return getUnitEmployeesWithQuotas(true, predicate);
    }

    public List<PersonSiadapWrapper> getUnitEmployeesWithQuotas(boolean continueToSubUnits, Predicate predicate) {
	List<PersonSiadapWrapper> employees = new ArrayList<PersonSiadapWrapper>();
	List<AccountabilityType> accountabilities = new ArrayList<AccountabilityType>();
	accountabilities.add(configuration.getWorkingRelation());

	getUnitEmployees(getUnit(), employees, continueToSubUnits, accountabilities, predicate);
	return employees;
    }

    public List<PersonSiadapWrapper> getUnitEmployeesWithoutQuotas(boolean continueToSubUnits) {
	return getUnitEmployeesWithoutQuotas(continueToSubUnits, null);
    }

    public List<PersonSiadapWrapper> getUnitEmployeesWithoutQuotas(Predicate predicate) {
	return getUnitEmployeesWithoutQuotas(true, predicate);
    }

    public List<PersonSiadapWrapper> getUnitEmployeesWithoutQuotas(boolean continueToSubUnits, Predicate predicate) {
	List<PersonSiadapWrapper> employees = new ArrayList<PersonSiadapWrapper>();
	List<AccountabilityType> accountabilities = new ArrayList<AccountabilityType>();
	accountabilities.add(configuration.getWorkingRelationWithNoQuota());

	getUnitEmployees(getUnit(), employees, continueToSubUnits, accountabilities, predicate);
	return employees;
    }

    private void getUnitEmployees(Unit unit, List<PersonSiadapWrapper> employees, boolean continueToSubunits,
	    List<AccountabilityType> accountabilities, Predicate predicate) {

	Collection<Person> children = unit.getChildren(new PartyPredicate.PartyByAccountabilityType(Person.class,
		accountabilities));

	for (Person person : children) {
	    PersonSiadapWrapper personWrapper = new PersonSiadapWrapper(person, configuration.getYear());
	    if (predicate == null || predicate.evaluate(personWrapper)) {
		employees.add(personWrapper);
	    }
	}

	if (continueToSubunits) {
	    for (Unit subUnit : unit.getChildUnits(configuration.getUnitRelations())) {
		getUnitEmployees(subUnit, employees, continueToSubunits, accountabilities, predicate);
	    }
	}
    }

    @Override
    public int hashCode() {
	return getUnit().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof UnitSiadapWrapper) {
	    return ((UnitSiadapWrapper) obj).getUnit() == getUnit();
	}
	return false;
    }
}
