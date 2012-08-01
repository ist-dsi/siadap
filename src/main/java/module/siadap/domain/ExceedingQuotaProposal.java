/*
 * @(#)ExceedingQuotaProposal.java
 *
 * Copyright 2012 Instituto Superior Tecnico
 * Founding Authors: Paulo Abrantes
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the SIADAP Module.
 *
 *   The SIADAP Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The SIADAP Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the SIADAP Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.siadap.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Predicate;

import pt.ist.fenixWebFramework.services.Service;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.wrappers.UnitSiadapWrapper;

/**
 * 
 * @author João Antunes
 * 
 */
public class ExceedingQuotaProposal extends ExceedingQuotaProposal_Base {

    public static final Comparator<ExceedingQuotaProposal> COMPARATOR_BY_PRIORITY_NUMBER = new Comparator<ExceedingQuotaProposal>() {

	@Override
	public int compare(ExceedingQuotaProposal o1, ExceedingQuotaProposal o2) {
	    if (o1 == null && o2 == null)
		return 0;
	    if (o1 == null && o2 != null)
		return -1;
	    if (o1 != null && o2 == null)
		return 1;
	    Integer priorityNumber1 = o1.getProposalOrder();
	    Integer priorityNumber2 = o2.getProposalOrder();
	    if (priorityNumber1 == null || priorityNumber2 == null) {
		if (priorityNumber1 == null && priorityNumber2 == null)
		    return 0;
		if (priorityNumber1 == null)
		    return -1;
		if (priorityNumber2 == null)
		    return 1;
	    }
	    return priorityNumber1.compareTo(priorityNumber2) == 0 ? o1.getExternalId().compareTo(o2.getExternalId())
		    : priorityNumber1.compareTo(priorityNumber2);
	}
    };

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

    public static List<ExceedingQuotaProposal> getQuotaProposalsFor(final Unit unit, int year) {
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

    /**
     * Fills the appropriate provided maps with the suggestions separated
     * 
     * @param unit
     *            the unit for which to fill them
     * @param year
     *            the year
     * @param siadap2WithQuotas
     *            the Map with the SIADAP2 With Quotas
     * @param siadap3WithoutQuotas
     *            - self explanatory
     * @param siadap2WithoutQuotas
     *            - self explanatory
     * @param siadap3WithQuotas
     *            - self explanatory
     */
    public static void organizeAndFillExceedingQuotaProposals(final Unit unit, final int year,
	    Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap2WithQuotas,
	    Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap3WithoutQuotas,
	    Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap2WithoutQuotas,
	    Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap3WithQuotas) {
	List<ExceedingQuotaProposal> quotaProposals = ExceedingQuotaProposal.getQuotaProposalsFor(unit, year);

	List<ExceedingQuotaProposal> siadap2WithQuotasExcellents = new ArrayList<ExceedingQuotaProposal>();
	List<ExceedingQuotaProposal> siadap2WithQuotasRelevants = new ArrayList<ExceedingQuotaProposal>();

	List<ExceedingQuotaProposal> siadap3WithoutQuotasExcellents = new ArrayList<ExceedingQuotaProposal>();
	List<ExceedingQuotaProposal> siadap3WithoutQuotasRelevants = new ArrayList<ExceedingQuotaProposal>();

	List<ExceedingQuotaProposal> siadap2WithoutQuotasExcellents = new ArrayList<ExceedingQuotaProposal>();
	List<ExceedingQuotaProposal> siadap2WithoutQuotasRelevants = new ArrayList<ExceedingQuotaProposal>();

	List<ExceedingQuotaProposal> siadap3WithQuotasExcellents = new ArrayList<ExceedingQuotaProposal>();
	List<ExceedingQuotaProposal> siadap3WithQuotasRelevants = new ArrayList<ExceedingQuotaProposal>();

	for (ExceedingQuotaProposal proposal : quotaProposals) {
	    switch (proposal.getSiadapUniverse()) {
	    case SIADAP2:
		if (proposal.getWithinOrganizationQuotaUniverse()) {
		    if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION))
			siadap2WithQuotasExcellents.add(proposal);
		    else if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.HIGH_SUGGESTION))
			siadap2WithQuotasRelevants.add(proposal);
		} else {
		    if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION))
			siadap2WithoutQuotasExcellents.add(proposal);
		    else if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.HIGH_SUGGESTION))
			siadap2WithoutQuotasRelevants.add(proposal);
		}
		break;
	    case SIADAP3:
		if (proposal.getWithinOrganizationQuotaUniverse()) {
		    if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION))
			siadap3WithQuotasExcellents.add(proposal);
		    else if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.HIGH_SUGGESTION))
			siadap3WithQuotasRelevants.add(proposal);
		} else {
		    if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION))
			siadap3WithoutQuotasExcellents.add(proposal);
		    else if (proposal.getSuggestionType().equals(ExceedingQuotaSuggestionType.HIGH_SUGGESTION))
			siadap3WithoutQuotasRelevants.add(proposal);
		}
		break;
	    }
	}

	siadap2WithoutQuotas.put(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION, siadap2WithoutQuotasExcellents);
	siadap2WithoutQuotas.put(ExceedingQuotaSuggestionType.HIGH_SUGGESTION, siadap2WithoutQuotasRelevants);

	siadap3WithoutQuotas.put(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION, siadap3WithoutQuotasExcellents);
	siadap3WithoutQuotas.put(ExceedingQuotaSuggestionType.HIGH_SUGGESTION, siadap3WithoutQuotasRelevants);

	siadap3WithQuotas.put(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION, siadap3WithQuotasExcellents);
	siadap3WithQuotas.put(ExceedingQuotaSuggestionType.HIGH_SUGGESTION, siadap3WithQuotasRelevants);

	siadap2WithQuotas.put(ExceedingQuotaSuggestionType.EXCELLENCY_SUGGESTION, siadap2WithQuotasExcellents);
	siadap2WithQuotas.put(ExceedingQuotaSuggestionType.HIGH_SUGGESTION, siadap2WithQuotasRelevants);

    }

    public static List<ExceedingQuotaProposal> getQuotaProposalFor(final Unit unit, final int year,
	    final SiadapUniverse siadapUniverse, final boolean quotasUniverse, final ExceedingQuotaSuggestionType type)
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
			if (exceedingQuotaProposal.getSiadapUniverse() == null)
			    return false;
			return (exceedingQuotaProposal.getSiadapUniverse().equals(siadapUniverse)
				&& exceedingQuotaProposal.getWithinOrganizationQuotaUniverse() == quotasUniverse
				&& exceedingQuotaProposal.getUnit().equals(unit) && exceedingQuotaProposal.getYear() == year && exceedingQuotaProposal
				.getSuggestionType().equals(type));
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
    public static void createAndAppendProposal(SiadapUniverse siadapUniverse, Boolean quotasApply,
	    ExceedingQuotaSuggestionType type, Integer year, UnitSiadapWrapper unitSiadapWrapper, Person person) {
	if (quotasApply == null || year == null || person == null || siadapUniverse == null)
	    throw new UnsupportedOperationException("must.not.parse.null.as.argument");
	if (!unitSiadapWrapper.isHarmonizationUnit())
	    throw new SiadapException("error.exceedingQuotaProposal.cannot.assign.proposals.to.non.harmonization.unit");

	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	if (configuration == null) {
	    throw new SiadapException("error.exceedingQuotaProposal.empty.Siadap.configuration");
	}

	//let's get all of the existing quotas to figure out the proposal order
	List<ExceedingQuotaProposal> currentProposalsForUniverse = ExceedingQuotaProposal.getQuotaProposalFor(
		unitSiadapWrapper.getUnit(), year, siadapUniverse, quotasApply, type);
	int propOrder = 0;
	for (ExceedingQuotaProposal proposal : currentProposalsForUniverse) {
	    if (proposal.getProposalOrder().intValue() > propOrder) {
		propOrder = proposal.getProposalOrder().intValue();
	    }
	}

	propOrder++;

	new ExceedingQuotaProposal(configuration, person, unitSiadapWrapper.getUnit(), type, propOrder,
		quotasApply.booleanValue(),
		siadapUniverse);

    }

    //    @Service
    //    public static void applyGivenProposals(List<SiadapSuggestionBean> quotaSuggestions, SiadapUniverse siadapUniverse,
    //	    Boolean quotasUniverse, UnitSiadapWrapper unitSiadapWrapper, Integer year) {
    //	if (quotasUniverse == null || year == null)
    //	    throw new UnsupportedOperationException("must.not.parse.null.as.argument");
    //	if (!unitSiadapWrapper.isHarmonizationUnit())
    //	    throw new SiadapException("error.exceedingQuotaProposal.cannot.assign.proposals.to.non.harmonization.unit");
    //
    //	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
    //	if (configuration == null) {
    //	    throw new SiadapException("error.exceedingQuotaProposal.empty.Siadap.configuration");
    //	}
    //
    //	Collections.sort(quotaSuggestions, SiadapSuggestionBean.COMPARATOR_BY_PRIORITY_NUMBER);
    //	//it's all sorted, it's easy to get the maximum and the minimum and to make sure that it has sequential priority numbers
    //	//	int maximumNumber = -1;
    //	//	Integer minimumNumber = null;
    //	int auxI = 0;
    //
    //	List<SiadapSuggestionBean> suggestionBeansToApply = new ArrayList<SiadapSuggestionBean>();
    //
    //	for (SiadapSuggestionBean suggestionBean : quotaSuggestions) {
    //	    if (!suggestionBean.getUnitWrapper().equals(unitSiadapWrapper))
    //		throw new SiadapException(
    //			"error.exceedingQuotaProposal.wrong.use.of.method.cannot.give.suggestions.for.different.units");
    //	    if (suggestionBean.getYear() == null || suggestionBean.getYear().intValue() != year.intValue())
    //		throw new SiadapException("error.exceedingQuotaProposal.must.use.apply.method.with.suggestions.of.the.same.year");
    //	    Integer exceedingQuotaPriorityNumber = suggestionBean.getExceedingQuotaPriorityNumber();
    //	    if (suggestionBean.getPersonWrapper() == null || suggestionBean.getPersonWrapper().getPerson() == null)
    //		throw new SiadapException("error.exceedingQuotaProposal.must.have.valid.person.associated");
    //	    if (exceedingQuotaPriorityNumber != null) {
    //		if (suggestionBean.getType() == null)
    //		    throw new SiadapException("error.exceedingQuotaProposal.must.have.valid.type.of.sugggestion.associated");
    //		if (++auxI != exceedingQuotaPriorityNumber.intValue())
    //		    throw new SiadapException("error.exceedingQuotaProposal.invalid.number.sequence.numbers.missing.or.repeated");
    //		if (suggestionBean.getCurrentHarmonizationAssessment() == null && suggestionBean.getCurrentHarmonizationExcellencyAssessment() == null)
    //		    throw new ConcurrentModificationException("somebody.altered.harmonization.assessment");
    //		//if the regular assessment is not null and the is true and the other is null or true, then somebody changed it
    //		if (suggestionBean.getCurrentHarmonizationAssessment() != null
    //			&& suggestionBean.getCurrentHarmonizationAssessment().booleanValue() != false
    //			&& (suggestionBean.getCurrentHarmonizationExcellencyAssessment() == null || (suggestionBean
    //				.getCurrentHarmonizationExcellencyAssessment() != null && suggestionBean
    //				.getCurrentHarmonizationExcellencyAssessment())))
    //		    throw new ConcurrentModificationException("somebody.altered.harmonization.assessment");
    //		if (suggestionBean.getCurrentHarmonizationExcellencyAssessment() != null
    //			&& suggestionBean.getCurrentHarmonizationExcellencyAssessment().booleanValue() != false
    //			&& (suggestionBean.getCurrentHarmonizationAssessment() == null || (suggestionBean
    //				.getCurrentHarmonizationAssessment() != null && suggestionBean
    //				.getCurrentHarmonizationAssessment())))
    //		    throw new ConcurrentModificationException("somebody.altered.harmonization.assessment");
    //		suggestionBeansToApply.add(suggestionBean);
    //		//		if (minimumNumber == null)
    //		//		    minimumNumber = exceedingQuotaPriorityNumber;
    //		//		if (exceedingQuotaPriorityNumber < minimumNumber)
    //		//		    minimumNumber = exceedingQuotaPriorityNumber;
    //		//		if (exceedingQuotaPriorityNumber.intValue() > maximumNumber)
    //		//		    maximumNumber = exceedingQuotaPriorityNumber;
    //	    }
    //	}
    //
    //	//let's apply the proposals as they are validated
    //	List<ExceedingQuotaProposal> currentProposalsForUniverse = ExceedingQuotaProposal.getQuotaProposalFor(
    //		unitSiadapWrapper.getUnit(), year, siadapUniverse, quotasUniverse);
    //	//let's remove them all
    //	for (ExceedingQuotaProposal currentProposal : currentProposalsForUniverse) {
    //	    currentProposal.delete();
    //	}
    //
    //	//and add them all
    //	for (SiadapSuggestionBean suggestionBean : suggestionBeansToApply) {
    //	    new ExceedingQuotaProposal(configuration, suggestionBean.getPersonWrapper().getPerson(), unitSiadapWrapper.getUnit(),
    //		    suggestionBean.getType(), suggestionBean.getExceedingQuotaPriorityNumber(), quotasUniverse, siadapUniverse);
    //	}
    //
    //    }

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

    @Deprecated
    /**
     * Should only be used by MigrateExceedingQuotaProposals
     * @param proposalOrder
     */
    public void setProposalOrderProtectedForScript(int proposalOrder) {
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
		getSiadapUniverse(), getWithinOrganizationQuotaUniverse(), getSuggestionType());
	
	
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
