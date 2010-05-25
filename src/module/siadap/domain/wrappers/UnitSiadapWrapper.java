package module.siadap.domain.wrappers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapYearConfiguration;
import myorg.util.BundleUtil;

import org.apache.commons.collections.Predicate;
import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.services.Service;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

public class UnitSiadapWrapper extends PartyWrapper implements Serializable {

    private static final int SCALE = 4;

    private Unit unit;

    public UnitSiadapWrapper(Unit unit, Integer year) {
	super(year);
	this.unit = unit;
    }

    public Unit getUnit() {
	return unit;
    }

    public void setUnit(Unit unit) {
	this.unit = unit;
    }

    @Override
    protected Party getParty() {
	return this.unit;
    }

    public String getTotalPeopleWorkingInUnitDescriptionString() {
	return getTotalPeopleWorkingInUnitDescriptionString(true);
    }

    public String getTotalPeopleWorkingInUnitDescriptionString(boolean continueToSubUnits) {
	Integer peopleWithQuotas = getTotalPeopleWorkingInUnit(continueToSubUnits);
	int peopleWithNoQuotas = getTotalPeopleWorkingInUnit(getUnit(), continueToSubUnits, getConfiguration()
		.getWorkingRelationWithNoQuota());

	return BundleUtil.getFormattedStringFromResourceBundle("resources/SiadapResources",
		"label.totalWorkingPeopleInUnit.description", String.valueOf(peopleWithQuotas + peopleWithNoQuotas), String
			.valueOf(peopleWithQuotas), String.valueOf(peopleWithNoQuotas));
    }

    public int getTotalPeopleWorkingInUnit() {
	return getTotalPeopleWorkingInUnit(true);
    }

    public int getTotalPeopleWorkingInUnitIncludingNoQuotaPeople() {
	return getTotalPeopleWorkingInUnitIncludingNoQuotaPeople(true);
    }

    public int getTotalPeopleWorkingInUnitIncludingNoQuotaPeople(boolean continueToSubUnits) {
	SiadapYearConfiguration configuration = getConfiguration();
	return getTotalPeopleWorkingInUnit(getUnit(), continueToSubUnits, configuration.getWorkingRelation(), configuration
		.getWorkingRelationWithNoQuota());
    }

    public int getTotalPeopleWorkingInUnit(boolean continueToSubUnits) {
	return getTotalPeopleWorkingInUnit(getUnit(), continueToSubUnits, getConfiguration().getWorkingRelation());
    }

    private int getTotalPeopleWorkingInUnit(Unit unit, boolean continueToSubUnit, AccountabilityType... workingRelations) {
	int people = 0;
	UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, getYear());
	Collection<Person> childPersons = wrapper.getChildPersons(workingRelations);
	people += childPersons.size();
	if (continueToSubUnit) {
	    for (Unit subUnit : wrapper.getChildUnits(getConfiguration().getUnitRelations())) {
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
	Collection<Person> childPersons = unit.getChildPersons(getConfiguration().getWorkingRelation());
	for (Person person : childPersons) {
	    if (getConfiguration().getSiadapFor(person, year) != null) {
		people++;
	    }
	}
	if (continueToSubUnit) {
	    for (Unit subUnit : unit.getChildUnits(getConfiguration().getUnitRelations())) {
		people += getTotalPeopleWithSiadapWorkingInUnit(subUnit, continueToSubUnit);
	    }
	}
	return people;
    }

    private int getEvaluationsForUnit(Unit unit, boolean continueToSubUnits, Predicate predicate) {
	int counter = 0;
	for (Person person : unit.getChildPersons(getConfiguration().getWorkingRelation())) {
	    Siadap siadap = getConfiguration().getSiadapFor(person, getYear());
	    if (siadap != null) {
		if (predicate.evaluate(siadap)) {
		    counter++;
		}
	    }
	}
	if (continueToSubUnits) {
	    for (Unit subUnit : unit.getChildUnits(getConfiguration().getUnitRelations())) {
		counter += getTotalRelevantEvaluationsForUnit(subUnit, continueToSubUnits);
	    }
	}

	return counter;
    }

    public int getTotalRelevantEvaluationsForUnit() {
	return getTotalRelevantEvaluationsForUnit(true);
    }

    public int getTotalRelevantEvaluationsForUnit(boolean continueToSubUnits) {
	return getTotalRelevantEvaluationsForUnit(getUnit(), continueToSubUnits);
    }

    private int getTotalRelevantEvaluationsForUnit(Unit unit, boolean continueToSubUnits) {
	return getEvaluationsForUnit(unit, continueToSubUnits, new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		Siadap siadap = (Siadap) arg0;
		return siadap.hasRelevantEvaluation();
	    }

	});
    }

    public int getTotalExcellencyEvaluationsForUnit() {
	return getTotalExcellencyEvaluationsForUnit(true);
    }

    public int getTotalExcellencyEvaluationsForUnit(boolean continueToSubUnits) {
	return getTotalExcellencyEvaluationsForUnit(getUnit(), continueToSubUnits);
    }

    private int getTotalExcellencyEvaluationsForUnit(Unit unit, boolean continueToSubUnits) {
	return getEvaluationsForUnit(unit, continueToSubUnits, new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		Siadap siadap = (Siadap) arg0;
		return siadap.hasExcellencyAward();
	    }

	});
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
	return getUnit().getChildPersons(getConfiguration().getEvaluationRelation());
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

    public Integer getCurrentUsedExcellencyGradeQuota() {
	return getTotalExcellencyEvaluationsForUnit(getUnit(), true);
    }

    public BigDecimal getExcellencyEvaluationPercentage() {
	int totalPeopleWorkingForUnit = getTotalPeopleWorkingInUnit(true);
	int totalExcellencyEvaluationsForUnit = getCurrentUsedExcellencyGradeQuota();

	if (totalExcellencyEvaluationsForUnit == 0) {
	    return BigDecimal.ZERO;
	}

	return new BigDecimal(totalExcellencyEvaluationsForUnit).divide(new BigDecimal(totalPeopleWorkingForUnit),
		UnitSiadapWrapper.SCALE, RoundingMode.HALF_EVEN).multiply(new BigDecimal(100)).stripTrailingZeros();
    }

    public Unit getHarmonizationUnit() {
	return getHarmonizationUnit(getUnit());
    }

    private Unit getHarmonizationUnit(Unit unit) {
	UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, getYear());
	if (!wrapper.getChildPersons(getConfiguration().getHarmonizationResponsibleRelation()).isEmpty()) {
	    return unit;
	}
	Collection<Unit> units = wrapper.getParentUnits(getConfiguration().getUnitRelations());
	return units.isEmpty() ? null : getHarmonizationUnit(units.iterator().next());
    }

    public boolean isResponsibleForHarmonization() {
	return !getChildPersons(getConfiguration().getHarmonizationResponsibleRelation()).isEmpty();
    }

    public boolean isPersonResponsibleForHarmonization(Person person) {
	return isPersonResponsibleForHarmonization(getUnit(), person);
    }

    private boolean isPersonResponsibleForHarmonization(Unit unit, Person person) {
	Collection<Person> childPersons = getChildPersons(getConfiguration().getHarmonizationResponsibleRelation());
	if (childPersons.contains(person)) {
	    return true;
	} else {
	    Collection<Unit> parentUnits = getParentUnits(getConfiguration().getHarmonizationResponsibleRelation());
	    if (parentUnits.isEmpty()) {
		return false;
	    } else {
		return isPersonResponsibleForHarmonization(parentUnits.iterator().next(), person);
	    }
	}
    }

    public void addResponsibleForHarmonization(Person person) {
	AccountabilityType harmonizationResponsibleRelation = getConfiguration().getHarmonizationResponsibleRelation();
	Collection<Accountability> childrenAccountabilities = getUnit().getChildrenAccountabilities(
		Collections.singleton(harmonizationResponsibleRelation));

	if (!childrenAccountabilities.isEmpty()) {
	    for (Accountability accountability : childrenAccountabilities) {
		if (accountability.getEndDate() == null) {
		    accountability.editDates(accountability.getBeginDate(), new LocalDate());
		}
	    }
	}
	getUnit().addChild(person, harmonizationResponsibleRelation, new LocalDate(), null);
    }

    public boolean isAboveQuotas() {
	return getCurrentUsedHighGradeQuota() > getHighGradeQuota()
		|| getCurrentUsedExcellencyGradeQuota() > getExcellencyGradeQuota();
    }

    public Unit getSuperiorUnit() {
	Collection<Unit> parentUnits = getParentUnits(getConfiguration().getUnitRelations());
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

	getUnitEmployees(getUnit(), employees, continueToSubUnits, predicate, getConfiguration().getWorkingRelation(),
		getConfiguration().getWorkingRelationWithNoQuota());
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

	getUnitEmployees(getUnit(), employees, continueToSubUnits, predicate, getConfiguration().getWorkingRelation());
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

	getUnitEmployees(getUnit(), employees, continueToSubUnits, predicate, getConfiguration().getWorkingRelationWithNoQuota());
	return employees;
    }

    private void getUnitEmployees(Unit unit, List<PersonSiadapWrapper> employees, boolean continueToSubunits,
	    Predicate predicate, AccountabilityType... accountabilities) {

	UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, getYear());
	Collection<Person> children = wrapper.getChildPersons(accountabilities);

	for (Person person : children) {
	    PersonSiadapWrapper personWrapper = new PersonSiadapWrapper(person, getConfiguration().getYear());
	    if (predicate == null || predicate.evaluate(personWrapper)) {
		employees.add(personWrapper);
	    }
	}

	if (continueToSubunits) {
	    for (Unit subUnit : wrapper.getChildUnits(getConfiguration().getUnitRelations())) {
		getUnitEmployees(subUnit, employees, continueToSubunits, predicate, accountabilities);
	    }
	}
    }

    public boolean isHarmonizationFinished() {
	return getConfiguration().getHarmonizationClosedUnits().contains(getUnit());
    }

    public MultiLanguageString getName() {
	return getUnit().getPartyName();
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

    @Service
    public void finishHarmonization() {
	getConfiguration().addHarmonizationClosedUnits(getUnit());
    }

    @Service
    public void reOpenHarmonization() {
	getConfiguration().removeHarmonizationClosedUnits(getUnit());
    }

}
