package module.siadap.domain;

import pt.ist.fenixWebFramework.services.Service;
import module.organization.domain.Person;
import module.organization.domain.Unit;

public class ExcedingQuotaProposal extends ExcedingQuotaProposal_Base {

    public ExcedingQuotaProposal(SiadapYearConfiguration configuration, Person person, Unit unit,
	    ExceddingQuotaSuggestionType type) {
	super();
	setProposalOrder(configuration.getSuggestionsForUnit(unit, type).size() + 1);
	setYearConfiguration(configuration);
	setSuggestion(person);
	setUnit(unit);
	setSuggestionType(type);
	setSiadapRootModule(SiadapRootModule.getInstance());
    }

    public int getYear() {
	return getYearConfiguration().getYear();
    }

    @Service
    public void delete() {
	int myOrder = getProposalOrder();
	for (ExcedingQuotaProposal proposal : getYearConfiguration().getSuggestionsForUnit(getUnit(), getSuggestionType())) {
	    Integer otherOrder = proposal.getProposalOrder();
	    if (otherOrder > myOrder) {
		proposal.setProposalOrder(otherOrder - 1);
	    }
	}
	removeSuggestion();
	removeUnit();
	removeYearConfiguration();
	removeSiadapRootModule();
	super.deleteDomainObject();
    }
}
