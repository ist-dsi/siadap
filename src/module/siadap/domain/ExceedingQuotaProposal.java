package module.siadap.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.wrappers.SiadapSuggestionBean;
import module.siadap.domain.wrappers.UnitSiadapWrapper;

import org.apache.commons.collections.Predicate;

import pt.ist.fenixWebFramework.services.Service;

public class ExceedingQuotaProposal extends ExceedingQuotaProposal_Base {

    private ExceedingQuotaProposal(SiadapYearConfiguration configuration, Person person, Unit unit,
	    ExceedingQuotaSuggestionType type, int proposalOrder, boolean withinOrganizationQuotaUniverse,
	    SiadapUniverse siadapUniverse) {
	super();
	super.setProposalOrder(proposalOrder);
	super.setYearConfiguration(configuration);
	super.setSuggestion(person);
	super.setUnit(unit);
	super.setSuggestionType(type);
	super.setSiadapRootModule(SiadapRootModule.getInstance());
	super.setWithinOrganizationQuotaUniverse(withinOrganizationQuotaUniverse);
	super.setSiadapUniverse(siadapUniverse);
    }

    public int getYear() {
	return getYearConfiguration().getYear();
    }

    //really kinda useless this method...
    @Deprecated
    private static List<ExceedingQuotaProposal> getQuotaProposalsFor(final Unit unit, int year) {
	//let's go through the SiadapYearConfiguration 
	List<ExceedingQuotaProposal> exceedingQuotaProposalsToReturn = new ArrayList<ExceedingQuotaProposal>();
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	if (configuration == null) {
	    return exceedingQuotaProposalsToReturn;
	}

	return getQuotaProposalsByPredicate(configuration.getExceedingQuotasProposals(), new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		ExceedingQuotaProposal exceedingQuotaProposal = (ExceedingQuotaProposal) arg0;

		return (exceedingQuotaProposal.getUnit().equals(unit));
	    }
	});

    }

    private static List<ExceedingQuotaProposal> getQuotaProposalsByPredicate(List<ExceedingQuotaProposal> quotaProposalsToFilter,
	    Predicate filterPredicate) {
	List<ExceedingQuotaProposal> exceedingQuotaProposalsToReturn = new ArrayList<ExceedingQuotaProposal>();
	for (ExceedingQuotaProposal proposal : quotaProposalsToFilter) {
	    if (filterPredicate.evaluate(proposal)) {
		exceedingQuotaProposalsToReturn.add(proposal);
	    }
	}

	return exceedingQuotaProposalsToReturn;

    }

    public static List<ExceedingQuotaProposal> getQuotaProposalFor(final Unit unit, final int year, final SiadapUniverse siadapUniverse, final boolean quotasUniverse)
    {
	List<ExceedingQuotaProposal> unitProposals = Collections.EMPTY_LIST;
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	if (configuration == null) {
	    return unitProposals;
	}
	unitProposals = new ArrayList<ExceedingQuotaProposal>(getQuotaProposalsByPredicate(unit.getExceedingQuotasProposals(),
		new Predicate() {

		    @Override
		    public boolean evaluate(Object arg0) {
			ExceedingQuotaProposal exceedingQuotaProposal = (ExceedingQuotaProposal) arg0;
			return (exceedingQuotaProposal.getSiadapUniverse().equals(siadapUniverse)
				&& exceedingQuotaProposal.getWithinOrganizationQuotaUniverse() == quotasUniverse
				&& exceedingQuotaProposal.getUnit().equals(unit) && exceedingQuotaProposal.getYear() == year);
		    }

		}));
	
	return unitProposals;

    }
    public static ExceedingQuotaProposal getQuotaProposalFor(final Unit unit, final int year, final Person person,
	    final SiadapUniverse siadapUniverse, final boolean quotasUniverse) {
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	if (configuration == null) {
	    return null;
	}

	List<ExceedingQuotaProposal> personQuotaProposal = new ArrayList<ExceedingQuotaProposal>(

		person.getExceedingQuotasProposals());

	List<ExceedingQuotaProposal> exceedingQuotasProposalsForGivenYear = new ArrayList<ExceedingQuotaProposal>(
		configuration.getExceedingQuotasProposals());

	exceedingQuotasProposalsForGivenYear.retainAll(personQuotaProposal);

	List<ExceedingQuotaProposal> quotaProposalsByPredicate = getQuotaProposalsByPredicate(
		exceedingQuotasProposalsForGivenYear, new Predicate() {

		    @Override
		    public boolean evaluate(Object arg0) {
			ExceedingQuotaProposal exceedingQuotaProposal = (ExceedingQuotaProposal) arg0;
			return (exceedingQuotaProposal.getSiadapUniverse().equals(siadapUniverse)
				&& exceedingQuotaProposal.getWithinOrganizationQuotaUniverse() == quotasUniverse
				&& exceedingQuotaProposal.getUnit().equals(unit) && exceedingQuotaProposal.getSuggestion()
				.equals(person));
		    }
		});

	if (quotaProposalsByPredicate.size() == 0) {
	    return null;
	}
	if (quotaProposalsByPredicate.size() > 1) {
	    throw new SiadapException("siadap.exceedingQuotaProposal.inconsistency.detected");
	}
	return quotaProposalsByPredicate.get(0);

    }

    @Service
    public static void applyGivenProposals(List<SiadapSuggestionBean> quotaSuggestions, SiadapUniverse siadapUniverse,
	    Boolean quotasUniverse, UnitSiadapWrapper unitSiadapWrapper, Integer year) {
	if (quotasUniverse == null || year == null)
	    throw new UnsupportedOperationException("must.not.parse.null.as.argument");
	if (!unitSiadapWrapper.isHarmonizationUnit())
	    throw new SiadapException("error.exceedingQuotaProposal.cannot.assign.proposals.to.non.harmonization.unit");

	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	if (configuration == null) {
	    throw new SiadapException("error.exceedingQuotaProposal.empty.Siadap.configuration");
	}

	Collections.sort(quotaSuggestions, SiadapSuggestionBean.COMPARATOR_BY_PRIORITY_NUMBER);
	//it's all sorted, it's easy to get the maximum and the minimum and to make sure that it has sequential priority numbers
	//	int maximumNumber = -1;
	//	Integer minimumNumber = null;
	int auxI = 0;

	List<SiadapSuggestionBean> suggestionBeansToApply = new ArrayList<SiadapSuggestionBean>();

	for (SiadapSuggestionBean suggestionBean : quotaSuggestions) {
	    if (!suggestionBean.getUnitWrapper().equals(unitSiadapWrapper))
		throw new SiadapException(
			"error.exceedingQuotaProposal.wrong.use.of.method.cannot.give.suggestions.for.different.units");
	    if (suggestionBean.getYear() == null || suggestionBean.getYear().intValue() != year.intValue())
		throw new SiadapException("error.exceedingQuotaProposal.must.use.apply.method.with.suggestions.of.the.same.year");
	    Integer exceedingQuotaPriorityNumber = suggestionBean.getExceedingQuotaPriorityNumber();
	    if (suggestionBean.getPersonWrapper() == null || suggestionBean.getPersonWrapper().getPerson() == null)
		throw new SiadapException("error.exceedingQuotaProposal.must.have.valid.person.associated");
	    if (exceedingQuotaPriorityNumber != null) {
		if (suggestionBean.getType() == null)
		    throw new SiadapException("error.exceedingQuotaProposal.must.have.valid.type.of.sugggestion.associated");
		if (++auxI != exceedingQuotaPriorityNumber.intValue())
		    throw new SiadapException("error.exceedingQuotaProposal.invalid.number.sequence.numbers.missing.or.repeated");
		if (suggestionBean.getCurrentHarmonizationAssessment() == null
			|| suggestionBean.getCurrentHarmonizationAssessment().booleanValue() != false)
		    throw new ConcurrentModificationException("somebody.altered.harmonization.assessment");
		suggestionBeansToApply.add(suggestionBean);
		//		if (minimumNumber == null)
		//		    minimumNumber = exceedingQuotaPriorityNumber;
		//		if (exceedingQuotaPriorityNumber < minimumNumber)
		//		    minimumNumber = exceedingQuotaPriorityNumber;
		//		if (exceedingQuotaPriorityNumber.intValue() > maximumNumber)
		//		    maximumNumber = exceedingQuotaPriorityNumber;
	    }
	}

	//let's apply the proposals as they are validated
	List<ExceedingQuotaProposal> currentProposalsForUniverse = ExceedingQuotaProposal.getQuotaProposalFor(
		unitSiadapWrapper.getUnit(), year, siadapUniverse, quotasUniverse);
	//let's remove them all
	for (ExceedingQuotaProposal currentProposal : currentProposalsForUniverse) {
	    currentProposal.delete();
	}

	//and add them all
	for (SiadapSuggestionBean suggestionBean : suggestionBeansToApply) {
	    new ExceedingQuotaProposal(configuration, suggestionBean.getPersonWrapper().getPerson(), unitSiadapWrapper.getUnit(),
		    suggestionBean.getType(), suggestionBean.getExceedingQuotaPriorityNumber(), quotasUniverse, siadapUniverse);
	}

    }

    private void delete() {
	super.setSuggestion(null);
	super.setUnit(null);
	super.setYearConfiguration(null);
	super.setSiadapRootModule(null);
	super.deleteDomainObject();
    }

    //let's hide the API from the outside
    @Override
    @Deprecated
    public void setProposalOrder(Integer proposalOrder) {
	throw new UnsupportedOperationException("must.not.invoke.this.from.outside.use.apply");
    }

    private void setProposalOrderProtected(Integer proposalOrder) {
	super.setProposalOrder(proposalOrder);
    }

    @Override
    @Deprecated
    public void setSiadapRootModule(SiadapRootModule siadapRootModule) {
	throw new UnsupportedOperationException("must.not.invoke.this.from.outside.use.apply");
    }

    @Override
    @Deprecated
    public void setSiadapUniverse(SiadapUniverse siadapUniverse) {
	throw new UnsupportedOperationException("must.not.invoke.this.from.outside.use.apply");
    }

    @Override
    @Deprecated
    public void setSuggestion(Person suggestion) {
	throw new UnsupportedOperationException("must.not.invoke.this.from.outside.use.apply");
    }

    @Override
    @Deprecated
    public void setUnit(Unit unit) {
	throw new UnsupportedOperationException("must.not.invoke.this.from.outside.use.apply");
    }

    @Override
    @Deprecated
    public void setSuggestionType(ExceedingQuotaSuggestionType suggestionType) {
	throw new UnsupportedOperationException("must.not.invoke.this.from.outside.use.apply");
    }

    @Override
    @Deprecated
    public void setWithinOrganizationQuotaUniverse(boolean withinOrganizationQuotaUniverse) {
	throw new UnsupportedOperationException("must.not.invoke.this.from.outside.use.apply");
    }

    @Override
    @Deprecated
    public void setYearConfiguration(SiadapYearConfiguration yearConfiguration) {
	throw new UnsupportedOperationException("must.not.invoke.this.from.outside.use.apply");
    }

    @Override
    @Deprecated
    public void removeSiadapRootModule() {
	throw new UnsupportedOperationException("must.not.invoke.this.from.outside.use.apply");
    }

    @Override
    @Deprecated
    public void removeSuggestion() {
	throw new UnsupportedOperationException("must.not.invoke.this.from.outside.use.apply");
    }

    @Override
    @Deprecated
    public void removeUnit() {
	throw new UnsupportedOperationException("must.not.invoke.this.from.outside.use.apply");
    }

    @Override
    @Deprecated
    public void removeYearConfiguration() {
	throw new UnsupportedOperationException("must.not.invoke.this.from.outside.use.apply");
    }

    @Service
    public void remove() {
	//let's get the 'sister' proposals to adjust them if needed
	List<ExceedingQuotaProposal> quotaProposalsFor = ExceedingQuotaProposal.getQuotaProposalFor(getUnit(), getYear(),
		getSiadapUniverse(), getWithinOrganizationQuotaUniverse());
	
	
	for (ExceedingQuotaProposal exceedingQuotaProposal : quotaProposalsFor)
	{
	    if (exceedingQuotaProposal.getProposalOrder().intValue() > getProposalOrder().intValue())
	    {
		exceedingQuotaProposal.setProposalOrderProtected(Integer.valueOf(exceedingQuotaProposal.getProposalOrder()) - 1);
	    }
	}
	
	delete();

    }

}
