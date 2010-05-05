package module.siadap.domain.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;

public class UnitSiadapEvaluation implements Serializable {

    private Unit unit;
    private int year;
    private SiadapYearConfiguration configuration;

    public UnitSiadapEvaluation(Unit unit, int year) {
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
	int totalRelevantEvaluationsForUnit = configuration.getTotalRelevantEvaluationsForUnit(getUnit(), true);

	return new BigDecimal(totalRelevantEvaluationsForUnit).divide(new BigDecimal(totalPeopleWorkingForUnit)).multiply(
		new BigDecimal(100));
    }

    public int getTotalPeople() {
	return configuration.getTotalPeopleWorkingFor(getUnit(), true);
    }

    public int getTotalPeopleEvaluated() {
	return configuration.getTotalPeopleWithSiadapWorkingFor(getUnit(), true);
    }
}
