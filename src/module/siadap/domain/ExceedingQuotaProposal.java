package module.siadap.domain;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import pt.ist.fenixWebFramework.services.Service;

public class ExceedingQuotaProposal extends ExceedingQuotaProposal_Base {

    public ExceedingQuotaProposal(SiadapYearConfiguration configuration, Person person, Unit unit,
	    ExceedingQuotaSuggestionType type) {
	super();
	//TODO joantune
	//	setProposalOrder(configuration.getSuggestionsForUnit(unit, type).size() + 1);
	//	setYearConfiguration(configuration);
	//	setSuggestion(person);
	//	setUnit(unit);
	//	setSuggestionType(type);
	//	setSiadapRootModule(SiadapRootModule.getInstance());
    }

    public int getYear() {
	return getYearConfiguration().getYear();
    }

    @Service
    public void delete() {
	//TODO joantune
	//	int myOrder = getProposalOrder();
	//	for (ExceedingQuotaProposal proposal : getYearConfiguration().getSuggestionsForUnit(getUnit(), getSuggestionType())) {
	//	    Integer otherOrder = proposal.getProposalOrder();
	//	    if (otherOrder > myOrder) {
	//		proposal.setProposalOrder(otherOrder - 1);
	//	    }
	//	}
	removeSuggestion();
	removeUnit();
	removeYearConfiguration();
	removeSiadapRootModule();
	super.deleteDomainObject();
    }
}
