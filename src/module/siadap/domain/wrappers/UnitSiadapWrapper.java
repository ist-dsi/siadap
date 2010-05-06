package module.siadap.domain.wrappers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;

public class UnitSiadapWrapper implements Serializable {

    private static final int SCALE = 4;

    private Unit unit;
    private int year;
    private SiadapYearConfiguration configuration;

    public UnitSiadapWrapper(Unit unit, int year) {
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

    public BigDecimal getRelevantEvaluationPercentage() {
	int totalPeopleWorkingForUnit = configuration.getTotalPeopleWorkingFor(getUnit(), true);
	int totalRelevantEvaluationsForUnit = getCurrentUsedHighGradeQuota();

	if (totalRelevantEvaluationsForUnit == 0) {
	    return BigDecimal.ZERO;
	}

	return new BigDecimal(totalRelevantEvaluationsForUnit).divide(new BigDecimal(totalPeopleWorkingForUnit),
		UnitSiadapWrapper.SCALE, RoundingMode.HALF_EVEN).multiply(new BigDecimal(100)).setScale(UnitSiadapWrapper.SCALE);
    }

    public int getTotalPeople() {
	return configuration.getTotalPeopleWorkingFor(getUnit(), true);
    }

    public int getTotalPeopleWithSiadap() {
	return configuration.getTotalPeopleWithSiadapWorkingFor(getUnit(), true);
    }

    public Collection<Person> getEvaluationResponsibles() {
	return getUnit().getChildPersons(this.configuration.getEvaluationRelation());
    }

    public int getHighGradeQuota() {
	int totalPeople = getTotalPeople();

	BigDecimal result = new BigDecimal(totalPeople)
		.multiply(new BigDecimal(SiadapYearConfiguration.MAXIMUM_HIGH_GRADE_QUOTA)).divide(new BigDecimal(100));
	int value = result.intValue();

	return value > 0 ? value : 1; // if the quota is 0 then the quota shifts
	// to 1
    }

    public int getCurrentUsedHighGradeQuota() {
	return configuration.getTotalRelevantEvaluationsForUnit(getUnit(), true);
    }

    public int getExcellencyGradeQuota() {
	int totalPeople = getTotalPeople();

	BigDecimal result = new BigDecimal(totalPeople).multiply(
		new BigDecimal(SiadapYearConfiguration.MAXIMUM_EXCELLENCY_GRADE_QUOTA)).divide(new BigDecimal(100));
	int value = result.intValue();

	return value > 0 ? value : 1; // if the quota is 0 then the quota shifts
	// to 1
    }

    // TODO: implement this method after implementing excellency.
    public int getCurrentUsedExcellencyGradeQuota() {
	return 0;
    }

    public Unit getHarmonizationUnit() {
	return this.configuration.getHarmonizationUnitToWhichUnitBelongs(getUnit());
    }

    public boolean isUnitAnHarmonizationUnit() {
	return !getUnit().getChildPersons(this.configuration.getHarmonizationResponsibleRelation()).isEmpty();
    }

    public Unit getSuperiorUnit() {
	Collection<Unit> parentUnits = getUnit().getParentUnits(this.configuration.getUnitRelations());
	return parentUnits.isEmpty() ? null : parentUnits.iterator().next();
    }
}
