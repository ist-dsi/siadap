package module.siadap.domain.wrappers;

import java.io.Serializable;
import java.util.Comparator;

import module.organization.domain.Unit;
import module.siadap.domain.ExceedingQuotaProposal;
import module.siadap.domain.ExceedingQuotaSuggestionType;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.exceptions.SiadapException;

public class SiadapSuggestionBean implements Serializable {

    public static final Comparator<SiadapSuggestionBean> COMPARATOR_BY_PRIORITY_NUMBER = new Comparator<SiadapSuggestionBean>() {

	@Override
	public int compare(SiadapSuggestionBean o1, SiadapSuggestionBean o2) {
	    if (o1 == null && o2 == null)
		return 0;
	    if (o1 == null && o2 != null)
		return -1;
	    if (o1 != null && o2 == null)
		return 1;
	    Integer priorityNumber1 = o1.getExceedingQuotaPriorityNumber();
	    Integer priorityNumber2 = o2.getExceedingQuotaPriorityNumber();
	    if (priorityNumber1 == null || priorityNumber2 == null) {
		if (priorityNumber1 == null && priorityNumber2 == null)
		    return 0;
		if (priorityNumber1 == null)
		    return -1;
		if (priorityNumber2 == null)
		    return 1;
	    }
	    return priorityNumber1.compareTo(priorityNumber2);
	}
    };

    /**
     * Default serial version
     */
    private static final long serialVersionUID = 1L;

    private ExceedingQuotaSuggestionType type;

    private Integer exceedingQuotaPriorityNumber;

    private final SiadapUniverse siadapUniverse;

    private final PersonSiadapWrapper personWrapper;

    private final UnitSiadapWrapper unitWrapper;

    private final Integer year;

    private final boolean withinQuotasUniverse;

    public SiadapSuggestionBean(PersonSiadapWrapper person, UnitSiadapWrapper unitWrapper, boolean quotasUniverse,
	    SiadapUniverse siadapUniverse) {
	this.personWrapper = person;
	this.unitWrapper = unitWrapper;
	this.year = person.getYear();
	//init the exceedingQuotaPriorityNumber
	Unit unit = unitWrapper.getUnit();
	
	if (unit == null) {
	    throw new SiadapException("error.illegal.use.of.SiadapSuggestionBean");
	}

	ExceedingQuotaProposal exceedingQuotaProposal = ExceedingQuotaProposal.getQuotaProposalFor(unit, year,
		person.getPerson(), siadapUniverse, quotasUniverse);
	if (exceedingQuotaProposal == null) {
	    this.exceedingQuotaPriorityNumber = null;
	    this.type = null;
	} else {
	    this.exceedingQuotaPriorityNumber = exceedingQuotaProposal.getProposalOrder();
	    this.type = exceedingQuotaProposal.getSuggestionType();
	}

	this.withinQuotasUniverse = quotasUniverse;
	this.siadapUniverse = siadapUniverse;


    }

    public Boolean getCurrentHarmonizationAssessment() {
	return personWrapper.getHarmonizationCurrentAssessmentFor(siadapUniverse);
    }

    public Boolean getCurrentHarmonizationExcellencyAssessment() {
	return personWrapper.getHarmonizationCurrentExcellencyAssessmentFor(siadapUniverse);
    }

    public ExceedingQuotaSuggestionType getType() {
	return type;
    }

    public void setType(ExceedingQuotaSuggestionType type) {
	this.type = type;
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

    public PersonSiadapWrapper getPersonWrapper() {
	return personWrapper;
    }

    public UnitSiadapWrapper getUnitWrapper() {
	return unitWrapper;
    }

    public boolean isWithinQuotasUniverse() {
	return withinQuotasUniverse;
    }


}
