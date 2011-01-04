package module.siadap.presentationTier.actions;

import java.io.Serializable;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.ExceddingQuotaSuggestionType;

public class SiadapSuggestionBean implements Serializable {

    private Person person;
    private Unit unit;
    private ExceddingQuotaSuggestionType type;

    private Integer year;

    public SiadapSuggestionBean() {

    }

    public Person getPerson() {
	return person;
    }

    public void setPerson(Person person) {
	this.person = person;
    }

    public Unit getUnit() {
	return unit;
    }

    public void setUnit(Unit unit) {
	this.unit = unit;
    }

    public ExceddingQuotaSuggestionType getType() {
	return type;
    }

    public void setType(ExceddingQuotaSuggestionType type) {
	this.type = type;
    }

    public void setYear(Integer year) {
	this.year = year;
    }

    public Integer getYear() {
	return year;
    }


}
