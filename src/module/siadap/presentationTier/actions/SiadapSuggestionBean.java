package module.siadap.presentationTier.actions;

import java.io.Serializable;

import module.siadap.domain.ExceedingQuotaSuggestionType;
import module.siadap.domain.wrappers.PersonSiadapWrapper;

public class SiadapSuggestionBean implements Serializable {

    private ExceedingQuotaSuggestionType type;

    private Integer exceedingQuotaPriorityNumber;

    private final PersonSiadapWrapper personWrapper;

    private Integer year;

    public SiadapSuggestionBean(PersonSiadapWrapper person) {
	this.personWrapper = person;
	//TODO init the exceedingQuotaPriorityNumber
    }

    public ExceedingQuotaSuggestionType getType() {
	return type;
    }

    public void setType(ExceedingQuotaSuggestionType type) {
	this.type = type;
    }

    public void setYear(Integer year) {
	this.year = year;
    }

    public Integer getYear() {
	return year;
    }

    public Integer getExceedingQuotaPriorityNumber() {
	return exceedingQuotaPriorityNumber;
    }

    public void setExceedingQuotaPriorityNumber(Integer exceedingQuotaPriorityNumber) {
	this.exceedingQuotaPriorityNumber = exceedingQuotaPriorityNumber;
    }


}
