/*
 * @(#)UnitSiadapWrapper.java
 *
 * Copyright 2010 Instituto Superior Tecnico
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
package module.siadap.domain.wrappers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.PartyType;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.activities.Validation;
import module.siadap.activities.ValidationActivityInformation;
import module.siadap.activities.ValidationActivityInformation.ValidationSubActivity;
import module.siadap.domain.ExceedingQuotaProposal;
import module.siadap.domain.ExceedingQuotaSuggestionType;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.exceptions.ValidationTerminationException;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.siadap.domain.util.SiadapMiscUtilClass;
import module.siadap.domain.wrappers.SiadapUniverseWrapper.UniverseDisplayMode;
import module.workflow.activities.ActivityException;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.exceptions.DomainException;
import myorg.util.BundleUtil;

import org.apache.commons.collections.Predicate;
import org.jfree.data.time.Month;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.services.Service;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public class UnitSiadapWrapper extends PartyWrapper implements Serializable {

    private static final int SCALE = 4;

    public static final String SIADAP_HARMONIZATION_UNIT_TYPE = "module.siadap.harmonization.unit";
    public static final String SIADAP_ORGANIZATION_MODEL_NAME = "SIADAP";

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

    public Set<SiadapUniverseWrapper> getAllUniverseWrappersOfAllPeopleInSubUnits() {

	SiadapYearConfiguration configuration = getConfiguration();

	Set<SiadapUniverseWrapper> universeWrappers = new TreeSet<SiadapUniverseWrapper>(
		SiadapUniverseWrapper.COMPARATOR_BY_UNIVERSE);

	SiadapUniverseWrapper peopleWithQuotasSIADAP2 = new SiadapUniverseWrapper(
		getSiadap2AndWorkingRelationWithQuotaUniverse(), "siadap2WithQuotas", SiadapUniverse.SIADAP2,
		configuration.getQuotaExcellencySiadap2WithQuota(), configuration.getQuotaRelevantSiadap2WithQuota(),
		UniverseDisplayMode.VALIDATION, null,
		null);
	SiadapUniverseWrapper peopleWithQuotasSIADAP3 = new SiadapUniverseWrapper(
		getSiadap3AndWorkingRelationWithQuotaUniverse(), "siadap3WithQuotas", SiadapUniverse.SIADAP3,
		configuration.getQuotaExcellencySiadap3WithQuota(), configuration.getQuotaRelevantSiadap3WithQuota(),
		UniverseDisplayMode.VALIDATION, null,
		null);
	SiadapUniverseWrapper peopleWithoutQuotasSIADAP2 = new SiadapUniverseWrapper(
		getSiadap2AndWorkingRelationWithoutQuotaUniverse(), "siadap2WithoutQuotas", SiadapUniverse.SIADAP2,
		configuration.getQuotaExcellencySiadap2WithoutQuota(), configuration.getQuotaRelevantSiadap2WithoutQuota(),
		UniverseDisplayMode.VALIDATION,
		null, null);
	SiadapUniverseWrapper peopleWithoutQuotasSIADAP3 = new SiadapUniverseWrapper(
		getSiadap3AndWorkingRelationWithoutQuotaUniverse(), "siadap3WithoutQuotas", SiadapUniverse.SIADAP3,
		configuration.getQuotaExcellencySiadap3WithoutQuota(), configuration.getQuotaRelevantSiadap3WithoutQuota(),
		UniverseDisplayMode.VALIDATION,
		null, null);

	universeWrappers.add(peopleWithoutQuotasSIADAP3);
	universeWrappers.add(peopleWithoutQuotasSIADAP2);
	universeWrappers.add(peopleWithQuotasSIADAP3);
	universeWrappers.add(peopleWithQuotasSIADAP2);

	return universeWrappers;

    }

    public Collection<SiadapUniverseWrapper> getValidationUniverseWrappers() {

	Set<SiadapUniverseWrapper> universeWrappers = new TreeSet<SiadapUniverseWrapper>(
		SiadapUniverseWrapper.COMPARATOR_BY_UNIVERSE);

	UniverseDisplayMode universeDisplayMode = UniverseDisplayMode.VALIDATION;

	Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap2WithQuotas = new HashMap<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>>();

	Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap3WithQuotas = new HashMap<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>>();

	Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap3WithoutQuotas = new HashMap<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>>();

	Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap2WithoutQuotas = new HashMap<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>>();

	ExceedingQuotaProposal.organizeAndFillExceedingQuotaProposals(getUnit(), getYear(), siadap2WithQuotas,
		siadap3WithoutQuotas, siadap2WithoutQuotas, siadap3WithQuotas);

	Map<Integer, Collection<PersonSiadapWrapper>> validationSiadap2WithQuotas = getValidationPersonSiadapWrappers(
		SiadapUniverse.SIADAP2, true);
	Map<Integer, Collection<PersonSiadapWrapper>> validationSiadap3WithQuotas = getValidationPersonSiadapWrappers(
		SiadapUniverse.SIADAP3, true);
	Map<Integer, Collection<PersonSiadapWrapper>> validationSiadap2WithoutQuotas = getValidationPersonSiadapWrappers(
		SiadapUniverse.SIADAP2, false);
	Map<Integer, Collection<PersonSiadapWrapper>> validationSiadap3WithoutQuotas = getValidationPersonSiadapWrappers(
		SiadapUniverse.SIADAP3, false);

	universeWrappers.add(new SiadapUniverseWrapper(validationSiadap2WithQuotas.values().iterator().next(),
		SiadapUniverseWrapper.SIADAP2_WITH_QUOTAS, SiadapUniverse.SIADAP2, getConfiguration()
			.getQuotaExcellencySiadap2WithQuota().intValue(), getConfiguration().getQuotaRelevantSiadap2WithQuota()
			.intValue(), universeDisplayMode, siadap2WithQuotas, validationSiadap2WithQuotas.keySet().iterator()
			.next()));

	universeWrappers.add(new SiadapUniverseWrapper(validationSiadap2WithoutQuotas.values().iterator().next(),
		SiadapUniverseWrapper.SIADAP2_WITHOUT_QUOTAS, SiadapUniverse.SIADAP2, getConfiguration()
			.getQuotaExcellencySiadap2WithoutQuota(), getConfiguration().getQuotaRelevantSiadap2WithoutQuota(),
		universeDisplayMode, siadap2WithoutQuotas, validationSiadap2WithoutQuotas.keySet().iterator().next()));

	universeWrappers.add(new SiadapUniverseWrapper(validationSiadap3WithQuotas.values().iterator().next(),
		SiadapUniverseWrapper.SIADAP3_WITH_QUOTAS, SiadapUniverse.SIADAP3, getConfiguration()
			.getQuotaExcellencySiadap3WithQuota(), getConfiguration().getQuotaRelevantSiadap3WithQuota(),
		universeDisplayMode, siadap3WithQuotas, validationSiadap3WithQuotas.keySet().iterator().next()));

	universeWrappers.add(new SiadapUniverseWrapper(validationSiadap3WithoutQuotas.values().iterator().next(),
		SiadapUniverseWrapper.SIADAP3_WITHOUT_QUOTAS, SiadapUniverse.SIADAP3, getConfiguration()
			.getQuotaExcellencySiadap3WithoutQuota(), getConfiguration().getQuotaRelevantSiadap3WithoutQuota(),
		universeDisplayMode, siadap3WithoutQuotas, validationSiadap3WithoutQuotas.keySet().iterator().next()));

	return universeWrappers;
    }

    /**
     * 
     * @param universe
     * @param belongsToInstitutionalQuota
     * @return the set of persons that should be validated i.e. that have been
     *         assigned relevant or above, or inadequate evaluations by the
     *         evaluator, from this unit and those below (using the
     *         {@link SiadapYearConfiguration#getHarmonizationUnitRelations()})
     */
    protected Map<Integer, Collection<PersonSiadapWrapper>> getValidationPersonSiadapWrappers(final SiadapUniverse universe,
	    final boolean belongsToInstitutionalQuota) {

	List<PersonSiadapWrapper> listPeopleToUse = new ArrayList<PersonSiadapWrapper>();

	final int[] counter = new int[1];
	getUnitAttachedPersons(
		getUnit(),
		listPeopleToUse,
		true,
		new Predicate() {

		    @Override
		    public boolean evaluate(Object arg0) {
			PersonSiadapWrapper wrapper = (PersonSiadapWrapper) arg0;
			if (wrapper.isQuotaAware() != belongsToInstitutionalQuota)
			    return false;
			SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse = wrapper.getSiadap()
				.getSiadapEvaluationUniverseForSiadapUniverse(universe);
			if (wrapper.isWithSkippedEval(universe))
			    return false;
			counter[0]++;
			SiadapGlobalEvaluation totalQualitativeEvaluationScoring = wrapper
				.getTotalQualitativeEvaluationScoringObject(universe);
			switch (totalQualitativeEvaluationScoring) {
			case EXCELLENCY:
			case HIGH:
			case LOW:
			case ZERO:
			    return true;
			case MEDIUM:
			case NONEXISTING:
			case WITHSKIPPEDEVAL:
			    return false;
			}
			return false;
		    }
		}, Collections.singleton(getConfiguration().getHarmonizationUnitRelations()),
		universe.getHarmonizationRelation(getConfiguration()));
	HashMap<Integer, Collection<PersonSiadapWrapper>> hashMap = new HashMap<Integer, Collection<PersonSiadapWrapper>>();
	Collections.sort(listPeopleToUse, PersonSiadapWrapper.PERSON_COMPARATOR_BY_NAME_FALLBACK_YEAR_THEN_PERSON_OID);
	hashMap.put(new Integer(counter[0]), listPeopleToUse);
	return hashMap;

    }

    public String getTotalPeopleWorkingInUnitDescriptionString() {
	return getTotalPeopleWorkingInUnitDescriptionString(false);
    }

    public String getTotalPeopleWorkingInUnitDescriptionString(boolean continueToSubUnits) {
	Integer peopleWithQuotas = getTotalPeopleWorkingInUnit(continueToSubUnits);
	int peopleWithNoQuotas = getTotalPeopleWorkingInUnit(getUnit(), continueToSubUnits, getConfiguration()
		.getWorkingRelationWithNoQuota());

	return BundleUtil.getFormattedStringFromResourceBundle("resources/SiadapResources",
		"label.totalWorkingPeopleInUnit.description", String.valueOf(peopleWithQuotas + peopleWithNoQuotas),
		String.valueOf(peopleWithQuotas), String.valueOf(peopleWithNoQuotas));
    }

    public int getTotalPeopleWorkingInUnit() {
	return getTotalPeopleWorkingInUnit(true);
    }

    public int getTotalPeopleHarmonizedInUnit() {
	Set<PersonSiadapWrapper> uniquePersons = new HashSet<PersonSiadapWrapper>();
	Set<PersonSiadapWrapper> siadap2AndWorkingRelationWithQuotaUniverse = getSiadap2AndWorkingRelationWithQuotaUniverse();
	Set<PersonSiadapWrapper> siadap2AndWorkingRelationWithoutQuotaUniverse = getSiadap2AndWorkingRelationWithoutQuotaUniverse();
	Set<PersonSiadapWrapper> siadap3AndWorkingRelationWithoutQuotaUniverse = getSiadap3AndWorkingRelationWithoutQuotaUniverse();
	Set<PersonSiadapWrapper> siadap3AndWorkingRelationWithQuotaUniverse = getSiadap3AndWorkingRelationWithQuotaUniverse();
	uniquePersons.addAll(siadap2AndWorkingRelationWithQuotaUniverse);
	uniquePersons.addAll(siadap2AndWorkingRelationWithoutQuotaUniverse);
	uniquePersons.addAll(siadap3AndWorkingRelationWithQuotaUniverse);
	uniquePersons.addAll(siadap3AndWorkingRelationWithoutQuotaUniverse);
	return uniquePersons.size();

    }

    public int getPeopleHarmonizedInUnitSiadap2WithQuotas() {
	return getSiadap2AndWorkingRelationWithQuotaUniverse().size();
    }

    public int getPeopleHarmonizedInUnitSiadap2WithoutQuotas() {
	return getSiadap2AndWorkingRelationWithoutQuotaUniverse().size();
    }

    public int getPeopleHarmonizedInUnitSiadap3WithQuotas() {
	return getSiadap3AndWorkingRelationWithQuotaUniverse().size();
    }

    public int getPeopleHarmonizedInUnitSiadap3WithoutQuotas() {
	return getSiadap3AndWorkingRelationWithoutQuotaUniverse().size();
    }

    public int getTotalPeopleHarmonizedInUnitWithSiadapStarted() {

	List<PersonSiadapWrapper> personSiadapWrappers = new ArrayList<PersonSiadapWrapper>();
	SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(getYear());

	boolean excludeResponsibles;
	if (!siadapYearConfiguration.getSiadapStructureTopUnit().equals(getUnit()))
	    excludeResponsibles = true;
	else
	    excludeResponsibles = false;

	AccountabilityType siadap2HarmonizationRelation = siadapYearConfiguration.getSiadap2HarmonizationRelation();
	AccountabilityType siadap3HarmonizationRelation = siadapYearConfiguration.getSiadap3HarmonizationRelation();
	getUnitAttachedPersons(getUnit(), personSiadapWrappers, isHarmonizationUnit(),
		new SiadapStateFilter(excludeResponsibles),
		Collections.singleton(getConfiguration().getHarmonizationUnitRelations()), siadap2HarmonizationRelation,
		siadap3HarmonizationRelation);
	return new HashSet(personSiadapWrappers).size();

    }

    public int getTotalPeopleWorkingInUnitIncludingNoQuotaPeople() {
	return getTotalPeopleWorkingInUnitIncludingNoQuotaPeople(true);
    }

    public int getTotalPeopleWorkingInUnitIncludingNoQuotaPeople(boolean continueToSubUnits) {
	SiadapYearConfiguration configuration = getConfiguration();
	return getTotalPeopleWorkingInUnit(getUnit(), continueToSubUnits, configuration.getWorkingRelation(),
		configuration.getWorkingRelationWithNoQuota());
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
		people += getTotalPeopleWorkingInUnit(subUnit, continueToSubUnit, workingRelations);
	    }
	}
	return people;
    }

    public int getQuotaAwareTotalPeopleWorkingInUnit(boolean continueToSubUnits, boolean withQuota) {
	return withQuota ? getTotalPeopleWorkingInUnit(getUnit(), continueToSubUnits, getConfiguration().getWorkingRelation())
		: getTotalPeopleWorkingInUnit(getUnit(), continueToSubUnits, getConfiguration().getWorkingRelationWithNoQuota());
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

    private int getNrEvaluationsBasedOnPredicate(Collection<PersonSiadapWrapper> personsToEvaluatePredicateOn, Predicate predicate) {
	int counter = 0;
	for (PersonSiadapWrapper siadapWrapper : personsToEvaluatePredicateOn) {
	    if (predicate.evaluate(siadapWrapper))
		counter++;
	}
	return counter;
    }

    @Service
    public ArrayList<SiadapException> executeValidation(Collection<SiadapUniverseWrapper> siadapUniverseWrappers,

	    ValidationSubActivity validationSubActivity) throws DomainException, ActivityException {

	ArrayList<SiadapException> warningsToReturn = new ArrayList<SiadapException>();
	HashSet<Person> evaluatorsToNotify = new HashSet<Person>();
	//if we are closing down the validation, we should make sure that this is the top unit
	if (validationSubActivity.equals(ValidationSubActivity.TERMINATE_VALIDATION)) {
	    if (!getUnit().equals(getConfiguration().getSiadapStructureTopUnit())) {
		throw new ValidationTerminationException("error.validation.must.be.closed.on.top.unit.only");
	    }
	    //let's check if it is already closed or not
	    if (getConfiguration().getClosedValidation())
		throw new SiadapException("error.validation.already.closed");
	    //let's check the quotas
	    for (SiadapUniverseWrapper siadapUniverseWrapper : siadapUniverseWrappers) {
		if (siadapUniverseWrapper.isAboveQuotasValidation()) {
		    throw new ValidationTerminationException("error.validation.above.quotas", siadapUniverseWrapper
			    .getSiadapUniverseEnum()
			    .getLocalizedName(),
			    siadapUniverseWrapper.getSiadapUniverse().iterator().next().isQuotaAware() ? "Sim" : "Não");
		}

	    }
	}

	WorkflowActivity activity = SiadapProcess.getActivityStaticly(Validation.class.getSimpleName());

	for (SiadapUniverseWrapper universeWrapper : siadapUniverseWrappers) {
	    for (PersonSiadapWrapper personSiadapWrapper : universeWrapper.getSiadapUniverse()) {
		ActivityInformation<?> validationActivityInformation = new ValidationActivityInformation(personSiadapWrapper,
			activity, validationSubActivity, universeWrapper.getSiadapUniverseEnum());
		if (validationActivityInformation.hasAllneededInfo()) {
		    activity.execute(validationActivityInformation);
		    if (validationSubActivity.equals(ValidationSubActivity.TERMINATE_VALIDATION)) {
			//let's add the evaluator to the list of evaluators to notify
			if (personSiadapWrapper.getEvaluator() != null)
			    evaluatorsToNotify.add(personSiadapWrapper.getEvaluator().getPerson());
		    }
		}
	    }
	}

	//mark as validated
	getConfiguration().setClosedValidation(Boolean.TRUE);

	//notify whoever
	for (Person evaluator : evaluatorsToNotify) {
	    try {
		ValidationSubActivity.notifyEvaluatorOfFinishedValidation(evaluator, getYear());
	    } catch (SiadapException ex) {
		warningsToReturn.add(ex);
	    }

	}
	return warningsToReturn;
    }

    public boolean isClosedValidation() {
	return getConfiguration().getClosedValidation();
    }

    //    public int getTotalRelevantEvaluationsForUnit() {
    //	return getTotalRelevantEvaluationsForUnit(true);
    //    }

    //    public int getTotalRelevantEvaluationsForUnit(boolean continueToSubUnits) {
    //	return getTotalRelevantEvaluationsForUnit(getUnit(), continueToSubUnits);
    //    }

    //    private int getTotalRelevantEvaluationsForUnit(Unit unit, boolean continueToSubUnits) {
    //	return getEvaluationsForUnit(unit, continueToSubUnits, new Predicate() {
    //
    //	    @Override
    //	    public boolean evaluate(Object arg0) {
    //		Siadap siadap = (Siadap) arg0;
    //		return siadap.hasRelevantEvaluation();
    //	    }
    //
    //	});
    //    }

    //    public int getTotalExcellencyEvaluationsForUnit() {
    //	return getTotalExcellencyEvaluationsForUnit(true);
    //    }
    //
    //    public int getTotalExcellencyEvaluationsForUnit(boolean continueToSubUnits) {
    //	return getTotalExcellencyEvaluationsForUnit(getUnit(), continueToSubUnits);
    //    }
    //
    //    private int getTotalExcellencyEvaluationsForUnit(Unit unit, boolean continueToSubUnits) {
    //	return getEvaluationsForUnit(unit, continueToSubUnits, new Predicate() {
    //
    //	    @Override
    //	    public boolean evaluate(Object arg0) {
    //		Siadap siadap = (Siadap) arg0;
    //		return siadap.hasExcellencyAward();
    //	    }
    //
    //	});
    //    }

    //    public BigDecimal getRelevantEvaluationPercentage() {
    //	int totalPeopleWorkingForUnit = getTotalPeopleWorkingInUnit(true);
    //	int totalRelevantEvaluationsForUnit = getCurrentUsedHighGradeQuota();
    //
    //	if (totalRelevantEvaluationsForUnit == 0) {
    //	    return BigDecimal.ZERO;
    //	}
    //
    //	return new BigDecimal(totalRelevantEvaluationsForUnit).divide(new BigDecimal(totalPeopleWorkingForUnit),
    //		UnitSiadapWrapper.SCALE, RoundingMode.HALF_EVEN).multiply(new BigDecimal(100)).stripTrailingZeros();
    //    }

    public Collection<Person> getEvaluationResponsibles() {
	return getChildPersons(getConfiguration().getEvaluationRelation());
    }

    public Collection<Person> getHarmonizationResponsibles() {
	return getChildPersons(getConfiguration().getHarmonizationResponsibleRelation());
    }

    //Quotas SIADAP 2 with quota
    public Integer getExcellencySiadap2WithQuotaQuota() {
	Integer quotaExcellencySiadap2WithQuota = SiadapYearConfiguration.getSiadapYearConfiguration(getYear())
		.getQuotaExcellencySiadap2WithQuota();
	int totalPeople = getPeopleHarmonizedInUnitSiadap2WithQuotas();
	return calculateQuota(totalPeople, quotaExcellencySiadap2WithQuota);
    }

    public Integer getNumberCurrentExcellentsSiadap2WithQuota() {
	return getNrEvaluationsBasedOnPredicate(getSiadap2AndWorkingRelationWithQuotaUniverse(), new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
		Siadap siadap = personSiadapWrapper.getSiadap();
		if (siadap != null
			&& siadap.hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation.EXCELLENCY, SiadapUniverse.SIADAP2)) {
		    return true;
		}
		return false;

	    }
	});

    }

    public Integer getRelevantSiadap2WithQuotaQuota() {
	Integer quotaRelevantSiadap2WithQuota = SiadapYearConfiguration.getSiadapYearConfiguration(getYear())
		.getQuotaRelevantSiadap2WithQuota();
	int totalPeople = getPeopleHarmonizedInUnitSiadap2WithQuotas();
	return calculateQuota(totalPeople, quotaRelevantSiadap2WithQuota);
    }

    public Integer getNumberCurrentRelevantsSiadap2WithQuota() {
	return getNrEvaluationsBasedOnPredicate(getSiadap2AndWorkingRelationWithQuotaUniverse(), new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
		Siadap siadap = personSiadapWrapper.getSiadap();
		if (siadap != null && siadap.hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation.HIGH, SiadapUniverse.SIADAP2))
		    return true;
		return false;
	    }
	});
    }

    //Quotas SIADAP 2 WITHOUT quota
    public Integer getExcellencySiadap2WithoutQuotaQuota() {
	Integer quotaExcellencySiadap2WithoutQuota = SiadapYearConfiguration.getSiadapYearConfiguration(getYear())
		.getQuotaExcellencySiadap2WithoutQuota();
	int totalPeople = getPeopleHarmonizedInUnitSiadap2WithQuotas();
	return calculateQuota(totalPeople, quotaExcellencySiadap2WithoutQuota);
    }

    public Integer getNumberCurrentExcellentsSiadap2WithoutQuota() {
	return getNrEvaluationsBasedOnPredicate(getSiadap2AndWorkingRelationWithoutQuotaUniverse(), new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
		Siadap siadap = personSiadapWrapper.getSiadap();
		if (siadap != null
			&& siadap.hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation.EXCELLENCY, SiadapUniverse.SIADAP2))
		    return true;
		return false;
	    }
	});
    }

    public Integer getRelevantSiadap2WithoutQuotaQuota() {
	Integer quotaRelevantSiadap2WithoutQuota = SiadapYearConfiguration.getSiadapYearConfiguration(getYear())
		.getQuotaRelevantSiadap2WithoutQuota();
	int totalPeople = getPeopleHarmonizedInUnitSiadap2WithQuotas();
	return calculateQuota(totalPeople, quotaRelevantSiadap2WithoutQuota);
    }

    public Integer getNumberCurrentRelevantsSiadap2WithoutQuota() {
	return getNrEvaluationsBasedOnPredicate(getSiadap2AndWorkingRelationWithoutQuotaUniverse(), new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
		Siadap siadap = personSiadapWrapper.getSiadap();
		if (siadap != null && siadap.hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation.HIGH, SiadapUniverse.SIADAP2))
		    return true;
		return false;
	    }
	});
    }

    //Quotas SIADAP 3 WITH quota
    public Integer getExcellencySiadap3WithQuotaQuota() {
	Integer quotaExcellencySiadap3WithQuota = SiadapYearConfiguration.getSiadapYearConfiguration(getYear())
		.getQuotaExcellencySiadap3WithQuota();
	int totalPeople = getPeopleHarmonizedInUnitSiadap3WithQuotas();
	return calculateQuota(totalPeople, quotaExcellencySiadap3WithQuota);
    }

    public Integer getNumberCurrentExcellentsSiadap3WithQuota() {
	return getNrEvaluationsBasedOnPredicate(getSiadap3AndWorkingRelationWithQuotaUniverse(), new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
		Siadap siadap = personSiadapWrapper.getSiadap();
		if (siadap != null
			&& siadap.hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation.EXCELLENCY, SiadapUniverse.SIADAP3))
		    return true;
		return false;
	    }
	});
    }

    public Integer getRelevantSiadap3WithQuotaQuota() {
	Integer quotaRelevantSiadap3WithQuota = SiadapYearConfiguration.getSiadapYearConfiguration(getYear())
		.getQuotaRelevantSiadap3WithQuota();
	int totalPeople = getPeopleHarmonizedInUnitSiadap2WithQuotas();
	return calculateQuota(totalPeople, quotaRelevantSiadap3WithQuota);
    }

    //TODO: joantune: these getNumberCurrent... could be done in a different way with a more generic predicate with a constructor, 
    //buttttt it is done this way because there was a refactor on this and no time to change this
    public Integer getNumberCurrentRelevantsSiadap3WithQuota() {
	return getNrEvaluationsBasedOnPredicate(getSiadap3AndWorkingRelationWithQuotaUniverse(), new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
		Siadap siadap = personSiadapWrapper.getSiadap();
		if (siadap != null && siadap.hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation.HIGH, SiadapUniverse.SIADAP3))
		    return true;
		return false;
	    }
	});
    }

    //Quotas SIADAP 3 WITHOUT quota
    public Integer getExcellencySiadap3WithoutQuotaQuota() {
	Integer quotaExcellencySiadap3WithoutQuota = SiadapYearConfiguration.getSiadapYearConfiguration(getYear())
		.getQuotaExcellencySiadap3WithoutQuota();
	int totalPeople = getPeopleHarmonizedInUnitSiadap3WithQuotas();
	return calculateQuota(totalPeople, quotaExcellencySiadap3WithoutQuota);
    }

    public Integer getNumberCurrentExcellentsSiadap3WithoutQuota() {
	return getNrEvaluationsBasedOnPredicate(getSiadap3AndWorkingRelationWithoutQuotaUniverse(), new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
		Siadap siadap = personSiadapWrapper.getSiadap();
		if (siadap != null
			&& siadap.hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation.EXCELLENCY, SiadapUniverse.SIADAP3))
		    return true;
		return false;
	    }
	});
    }

    public Integer getRelevantSiadap3WithoutQuotaQuota() {
	Integer quotaRelevantSiadap3WithoutQuota = SiadapYearConfiguration.getSiadapYearConfiguration(getYear())
		.getQuotaRelevantSiadap3WithoutQuota();
	int totalPeople = getPeopleHarmonizedInUnitSiadap2WithQuotas();
	return calculateQuota(totalPeople, quotaRelevantSiadap3WithoutQuota);
    }

    public Integer getNumberCurrentRelevantsSiadap3WithoutQuota() {
	return getNrEvaluationsBasedOnPredicate(getSiadap3AndWorkingRelationWithoutQuotaUniverse(), new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
		Siadap siadap = personSiadapWrapper.getSiadap();
		if (siadap != null && siadap.hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation.HIGH, SiadapUniverse.SIADAP3))
		    return true;
		return false;
	    }
	});
    }

    /**
     * @param totalPeople
     *            the number of people to calculate the quota on
     * @param quota
     *            the percentage points of quota
     * @return how many people it represents, it is never 0 due to SIADAPs rules
     */
    private int calculateQuota(int totalPeople, Integer quota) {
	BigDecimal result = new BigDecimal(totalPeople).multiply(new BigDecimal(quota)).divide(new BigDecimal(100));

	int value = result.intValue();
	return value > 0 ? value : 1; //if the quota is 0 the the quota shifts to 1

    }

    //    public Integer getHighGradeQuota() {
    //	int totalPeople = getTotalPeopleWorkingInUnit();
    //
    //	BigDecimal result = new BigDecimal(totalPeople)
    //		.multiply(new BigDecimal(SiadapYearConfiguration.MAXIMUM_HIGH_GRADE_QUOTA)).divide(new BigDecimal(100));
    //	int value = result.intValue();
    //
    //	return value > 0 ? value : 1; // if the quota is 0 then the quota shifts
    //	// to 1
    //    }

    //    public Integer getCurrentUsedHighGradeQuota() {
    //	return getTotalRelevantEvaluationsForUnit(getUnit(), true);
    //    }

    //    public Integer getExcellencyGradeQuota() {
    //	int totalPeople = getTotalPeopleWorkingInUnit();
    //
    //	BigDecimal result = new BigDecimal(totalPeople).multiply(
    //		new BigDecimal(SiadapYearConfiguration.MAXIMUM_EXCELLENCY_GRADE_QUOTA)).divide(new BigDecimal(100));
    //	int value = result.intValue();
    //
    //	return value > 0 ? value : 1; // if the quota is 0 then the quota shifts
    //	// to 1
    //    }

    //    public Integer getCurrentUsedExcellencyGradeQuota() {
    //	return getTotalExcellencyEvaluationsForUnit(getUnit(), true);
    //    }

    //    public BigDecimal getExcellencyEvaluationPercentage() {
    //	int totalPeopleWorkingForUnit = getTotalPeopleWorkingInUnit(true);
    //	int totalExcellencyEvaluationsForUnit = getCurrentUsedExcellencyGradeQuota();
    //
    //	if (totalExcellencyEvaluationsForUnit == 0) {
    //	    return BigDecimal.ZERO;
    //	}
    //
    //	return new BigDecimal(totalExcellencyEvaluationsForUnit).divide(new BigDecimal(totalPeopleWorkingForUnit),
    //		UnitSiadapWrapper.SCALE, RoundingMode.HALF_EVEN).multiply(new BigDecimal(100)).stripTrailingZeros();
    //    }

    public Unit getHarmonizationUnit() {
	return getHarmonizationUnit(getUnit());
    }

    private Unit getHarmonizationUnit(Unit unit) {
	UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, getYear());
	if (!wrapper.getChildPersons(getConfiguration().getHarmonizationResponsibleRelation()).isEmpty()
		&& isHarmonizationUnit(unit)) {
	    return unit;
	}
	Collection<Unit> units = wrapper.getParentUnits(getConfiguration().getHarmonizationUnitRelations());
	return units.isEmpty() ? null : getHarmonizationUnit(units.iterator().next());
    }

    /**
     * 
     * @return true if it is responsible for the harm. of this unit, or if it's
     *         part of the CCA and this is the top unit
     */
    public boolean isResponsibleForHarmonization() {
	return !getChildPersons(getConfiguration().getHarmonizationResponsibleRelation()).isEmpty()
		|| (getUnit().equals(getConfiguration().getSiadapStructureTopUnit()) && getConfiguration()
			.isCurrentUserMemberOfCCA()) || isSpecialHarmonizationUnit()
		&& getConfiguration().isCurrentUserMemberOfCCA();
    }

    /**
     * Convenience method call for {@link #isResponsibleForHarmonization()} to
     * make struts happy
     * 
     * @return the same as {@link #isResponsibleForHarmonization()} public
     *         boolean hasResponsibleForHarmonization() { return
     *         isResponsibleForHarmonization(); }
     */

    public boolean isPersonResponsibleForHarmonization(Person person) {
	return isPersonResponsibleForHarmonization(getUnit(), person);
    }

    private boolean isPersonResponsibleForHarmonization(Unit unit, Person person) {
	Collection<Person> childPersons = getChildPersons(unit, getConfiguration().getHarmonizationResponsibleRelation());
	if (childPersons.contains(person)) {
	    return true;
	} else {
	    Collection<Unit> parentUnits = getParentUnits(unit, getConfiguration().getHarmonizationUnitRelations());
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

	LocalDate end = new LocalDate(getYear(), Month.DECEMBER, 31);

	if (!childrenAccountabilities.isEmpty()) {
	    for (Accountability accountability : childrenAccountabilities) {
		if (accountability.isActive(end)) {
		    //if we already have that person there, let's just return
		    if (accountability.getChild().equals(person))
			return;
		} else if (!accountability.getChild().equals(person)) {
		    accountability.editDates(accountability.getBeginDate(), new LocalDate());
		}
	    }
	}
	getUnit().addChild(person, harmonizationResponsibleRelation, new LocalDate(), end);
    }

    public boolean isSiadap2WithQuotasAboveQuota() {
	return new SiadapUniverseWrapper(getSiadap2AndWorkingRelationWithQuotaUniverse(), "siadap2WithQuotas",
		SiadapUniverse.SIADAP2, getConfiguration().getQuotaExcellencySiadap2WithQuota(), getConfiguration()
			.getQuotaRelevantSiadap2WithQuota(), null, null, null).isAboveQuotasHarmonization();
    }

    public boolean isSiadap2WithoutQuotasAboveQuota() {
	return new SiadapUniverseWrapper(getSiadap2AndWorkingRelationWithoutQuotaUniverse(), "siadap2WithoutQuotas",
		SiadapUniverse.SIADAP2, getConfiguration().getQuotaExcellencySiadap2WithoutQuota(), getConfiguration()
			.getQuotaRelevantSiadap2WithoutQuota(), null, null, null).isAboveQuotasHarmonization();
    }

    public boolean isSiadap3WithQuotasAboveQuota() {

	return new SiadapUniverseWrapper(getSiadap3AndWorkingRelationWithQuotaUniverse(), "siadap3WithQuotas",
		SiadapUniverse.SIADAP3, getConfiguration().getQuotaExcellencySiadap3WithQuota(), getConfiguration()
			.getQuotaRelevantSiadap3WithQuota(), null, null, null).isAboveQuotasHarmonization();
    }

    public boolean isSiadap3WithoutQuotasAboveQuota() {
	return new SiadapUniverseWrapper(getSiadap3AndWorkingRelationWithoutQuotaUniverse(), "siadap3WithoutQuotas",
		SiadapUniverse.SIADAP3, getConfiguration().getQuotaExcellencySiadap3WithoutQuota(), getConfiguration()
			.getQuotaRelevantSiadap3WithoutQuota(), null, null, null).isAboveQuotasHarmonization();
    }

    public UnitSiadapWrapper getSuperiorHarmonizationUnitWrapper() {
	return new UnitSiadapWrapper(getSuperiorHarmonizationUnit(), getYear());
    }

    public Unit getSuperiorHarmonizationUnit() {
	Collection<Unit> parentUnits = getParentUnits(getConfiguration().getHarmonizationUnitRelations());
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

	getUnitAttachedPersons(getUnit(), employees, continueToSubUnits, predicate,
		Collections.singleton(getConfiguration().getUnitRelations()), getConfiguration().getWorkingRelation(),
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

	getUnitAttachedPersons(getUnit(), employees, continueToSubUnits, predicate,
		Collections.singleton(getConfiguration().getUnitRelations()), getConfiguration().getWorkingRelation());
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

	getUnitAttachedPersons(getUnit(), employees, continueToSubUnits, predicate,
		Collections.singleton(getConfiguration().getUnitRelations()), getConfiguration().getWorkingRelationWithNoQuota());
	return employees;
    }

    private void getUnitAttachedPersons(Unit unit, List<PersonSiadapWrapper> employees, boolean continueToSubunits,
	    Predicate predicate, Collection<AccountabilityType> unitAccTypesToUse, AccountabilityType... accountabilities) {

	UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, getYear());
	Collection<Person> children = wrapper.getChildPersons(accountabilities);

	for (Person person : children) {
	    PersonSiadapWrapper personWrapper = new PersonSiadapWrapper(person, getConfiguration().getYear());
	    if (predicate == null || predicate.evaluate(personWrapper)) {
		employees.add(personWrapper);
	    }
	}

	if (continueToSubunits) {
	    for (Unit subUnit : wrapper.getChildUnits(unitAccTypesToUse.toArray(new AccountabilityType[0]))) {
		getUnitAttachedPersons(subUnit, employees, continueToSubunits, predicate, unitAccTypesToUse, accountabilities);
	    }
	}
    }

    public boolean isHarmonizationUnit() {
	return isHarmonizationUnit(getUnit());
    }

    public boolean isHarmonizationUnit(Unit unit) {
	if (unit != null) {
	    for (PartyType partyType : unit.getPartyTypes()) {
		if (partyType.getType().equalsIgnoreCase(SIADAP_HARMONIZATION_UNIT_TYPE)) {
		    //seen that we are in the top harmonization unit, we will show info about all of the persons on the sub units
		    return true;
		}
	    }
	}
	return false;

    }

    class SiadapUniverseFilter implements Predicate {
	private final UnitSiadapWrapper unit;
	private final boolean excludeResponsibles;
	private final int year;
	private final boolean includeQuota;
	private final Collection<Person> harmonizationResponsibles;

	private Boolean includePositivelyHarmonizedOnly = null;

	private SiadapUniverse siadapUniverse;

	/**
	 * 
	 * @param excludeResponsibles
	 * @param year
	 * @param unit
	 * @param includeQuota
	 * @param includePositivelyHarmonizedOnly
	 *            if null, nothing is to be done. if false only the ones
	 *            with negative harmonizations are included, if true, only
	 *            the ones with positive ones
	 * @param siadapUniverse
	 */
	public SiadapUniverseFilter(boolean excludeResponsibles, int year, UnitSiadapWrapper unit, boolean includeQuota,
		Boolean includePositivelyHarmonizedOnly, SiadapUniverse siadapUniverse) {
	    this(excludeResponsibles, year, unit, includeQuota);
	    this.includePositivelyHarmonizedOnly = includePositivelyHarmonizedOnly;
	    this.siadapUniverse = siadapUniverse;
	}

	/**
	 * 
	 * @param excludeResponsibles
	 * @param year
	 * @param unit
	 * @param includeQuota
	 *            if true, only returns true with people that account for
	 *            the quota, if false, it is the opposite
	 */
	public SiadapUniverseFilter(boolean excludeResponsibles, int year, UnitSiadapWrapper unit, boolean includeQuota) {
	    this.unit = unit;
	    this.excludeResponsibles = excludeResponsibles;
	    this.year = year;
	    this.includeQuota = includeQuota;
	    this.harmonizationResponsibles = unit.getHarmonizationResponsibles();
	}

	@Override
	public boolean evaluate(Object arg0) {
	    PersonSiadapWrapper personWrapper = (PersonSiadapWrapper) arg0;
	    boolean quotaAware = personWrapper.isQuotaAware();

	    return (personWrapper.getYear() == year
		    && (!excludeResponsibles || !harmonizationResponsibles.contains(personWrapper.getPerson()))
		    && (includeQuota && quotaAware || !includeQuota && !quotaAware) && (checkOnHarmonizationAssessment(personWrapper)));
	}

	/**
	 * 
	 * @param personWrapper
	 * @return true if
	 */
	private boolean checkOnHarmonizationAssessment(PersonSiadapWrapper personWrapper) {
	    if (includePositivelyHarmonizedOnly != null) {
		//WTF?!?! the greatest boolean riddle... probably should be broken down... but where's the fun in that :P
		switch (siadapUniverse) {
		case SIADAP2:
		    return ((includePositivelyHarmonizedOnly && ((personWrapper.getHarmonizationCurrentAssessmentForSIADAP2() != null && personWrapper
			    .getHarmonizationCurrentAssessmentForSIADAP2().booleanValue()) || (!personWrapper
			    .getWithoutExcellencyAwardForSiadap2()
			    && personWrapper.getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP2() != null && personWrapper
				.getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP2()))) || (!includePositivelyHarmonizedOnly
			    && ((personWrapper.getHarmonizationCurrentAssessmentForSIADAP2() != null && !personWrapper
				    .getHarmonizationCurrentAssessmentForSIADAP2().booleanValue())) || (!personWrapper
			    .getWithoutExcellencyAwardForSiadap3()
			    && personWrapper.getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP3() != null && !personWrapper
				.getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP3())));
		case SIADAP3:
		    return ((includePositivelyHarmonizedOnly && ((personWrapper.getHarmonizationCurrentAssessmentForSIADAP3() != null && personWrapper
			    .getHarmonizationCurrentAssessmentForSIADAP3().booleanValue()) || (!personWrapper
			    .getWithoutExcellencyAwardForSiadap3()
			    && personWrapper.getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP3() != null && personWrapper
				.getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP3()))) || (!includePositivelyHarmonizedOnly
			    && ((personWrapper.getHarmonizationCurrentAssessmentForSIADAP3() != null && !personWrapper
				    .getHarmonizationCurrentAssessmentForSIADAP3().booleanValue())) || (!personWrapper
			    .getWithoutExcellencyAwardForSiadap3()
			    && personWrapper.getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP3() != null && !personWrapper
				.getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP3())));
		default:
		    return false;
		}
	    } else
		return true;
	}
    }

    class SiadapStateFilter implements Predicate {
	private final boolean checkIfExistsOnly = true;
	private final boolean excludeResponsibles;
	private final int year = getYear();
	private final Collection<Person> harmonizationResponsibles;

	public SiadapStateFilter(boolean excludeResponsibles) {
	    this.excludeResponsibles = excludeResponsibles;
	    this.harmonizationResponsibles = getHarmonizationResponsibles();
	}

	@Override
	public boolean evaluate(Object arg0) {
	    PersonSiadapWrapper personWrapper = (PersonSiadapWrapper) arg0;
	    if (checkIfExistsOnly)
		return personWrapper.getYear() == year && personWrapper.getSiadap() != null
			&& (!excludeResponsibles || !harmonizationResponsibles.contains(personWrapper.getPerson()));
	    return false;
	}

    }

    /**
     * 
     * @param includePositivelyHarmonizedOnly
     *            if null, do not consider harmonization. If true, only the ones
     *            harmonized positively are returned, and if false the ones
     *            negatively
     * @return
     */
    public Set<PersonSiadapWrapper> getSiadap2AndWorkingRelationWithQuotaUniverse() {
	return getSiadap2AndWorkingRelationWithQuotaUniverse(null);
    }

    public Set<PersonSiadapWrapper> getSiadap2AndWorkingRelationWithQuotaUniverse(Boolean includePositivelyHarmonizedOnly) {

	SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(getYear());
	AccountabilityType siadap2HarmonizationRelation = siadapYearConfiguration.getSiadap2HarmonizationRelation();
	boolean excludeResponsibles;
	if (!SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getSiadapStructureTopUnit().equals(getUnit()))
	    excludeResponsibles = true;
	else
	    excludeResponsibles = false;

	SiadapUniverseFilter siadapUniverseFilter = new SiadapUniverseFilter(excludeResponsibles, getYear(), this, true,
		includePositivelyHarmonizedOnly, SiadapUniverse.SIADAP2);
	List<PersonSiadapWrapper> universePersons = new ArrayList<PersonSiadapWrapper>();
	getUnitAttachedPersons(unit, universePersons, isHarmonizationUnit() || isSiadapStructureTopUnit(), siadapUniverseFilter,
		Collections.singleton(getConfiguration().getHarmonizationUnitRelations()), siadap2HarmonizationRelation);

	return new HashSet<PersonSiadapWrapper>(universePersons);

    }

    public Set<PersonSiadapWrapper> getSiadap3AndWorkingRelationWithQuotaUniverse() {
	return getSiadap3AndWorkingRelationWithQuotaUniverse(null);
    }

    public Set<PersonSiadapWrapper> getSiadap3AndWorkingRelationWithQuotaUniverse(Boolean includePositivelyHarmonizedOnly) {
	SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(getYear());
	AccountabilityType siadap3HarmonizationRelation = siadapYearConfiguration.getSiadap3HarmonizationRelation();
	AccountabilityType workingRelation = siadapYearConfiguration.getWorkingRelation();
	boolean excludeResponsibles;
	if (!SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getSiadapStructureTopUnit().equals(getUnit()))
	    excludeResponsibles = true;
	else
	    excludeResponsibles = false;

	SiadapUniverseFilter siadapUniverseFilter = new SiadapUniverseFilter(excludeResponsibles, getYear(), this, true,
		includePositivelyHarmonizedOnly, SiadapUniverse.SIADAP3);

	List<PersonSiadapWrapper> universePersons = new ArrayList<PersonSiadapWrapper>();
	getUnitAttachedPersons(unit, universePersons, isHarmonizationUnit() || isSiadapStructureTopUnit(), siadapUniverseFilter,
		Collections.singleton(getConfiguration().getHarmonizationUnitRelations()), siadap3HarmonizationRelation);
	return new HashSet<PersonSiadapWrapper>(universePersons);

    }

    public Set<PersonSiadapWrapper> getSiadap2AndWorkingRelationWithoutQuotaUniverse() {
	return getSiadap2AndWorkingRelationWithoutQuotaUniverse(null);
    }

    public Set<PersonSiadapWrapper> getSiadap2AndWorkingRelationWithoutQuotaUniverse(Boolean includePositivelyHarmonizedOnly) {
	SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(getYear());
	AccountabilityType siadap2HarmonizationRelation = siadapYearConfiguration.getSiadap2HarmonizationRelation();

	boolean excludeResponsibles;
	if (!SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getSiadapStructureTopUnit().equals(getUnit()))
	    excludeResponsibles = true;
	else
	    excludeResponsibles = false;

	SiadapUniverseFilter siadapUniverseFilter = new SiadapUniverseFilter(excludeResponsibles, getYear(), this, false,
		includePositivelyHarmonizedOnly, SiadapUniverse.SIADAP2);

	List<PersonSiadapWrapper> universePersons = new ArrayList<PersonSiadapWrapper>();
	getUnitAttachedPersons(unit, universePersons, isHarmonizationUnit() || isSiadapStructureTopUnit(), siadapUniverseFilter,
		Collections.singleton(getConfiguration().getHarmonizationUnitRelations()), siadap2HarmonizationRelation);
	return new HashSet<PersonSiadapWrapper>(universePersons);

    }

    public Set<PersonSiadapWrapper> getSiadap3AndWorkingRelationWithoutQuotaUniverse() {
	return getSiadap3AndWorkingRelationWithoutQuotaUniverse(null);
    }

    public Set<PersonSiadapWrapper> getSiadap3AndWorkingRelationWithoutQuotaUniverse(Boolean includePositivelyHarmonizedOnly) {
	SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(getYear());
	AccountabilityType siadap3HarmonizationRelation = siadapYearConfiguration.getSiadap3HarmonizationRelation();

	boolean excludeResponsibles;
	if (!SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getSiadapStructureTopUnit().equals(getUnit()))
	    excludeResponsibles = true;
	else
	    excludeResponsibles = false;

	SiadapUniverseFilter siadapUniverseFilter = new SiadapUniverseFilter(excludeResponsibles, getYear(), this, false,
		includePositivelyHarmonizedOnly, SiadapUniverse.SIADAP3);

	List<PersonSiadapWrapper> universePersons = new ArrayList<PersonSiadapWrapper>();
	getUnitAttachedPersons(unit, universePersons, isHarmonizationUnit() || isSiadapStructureTopUnit(), siadapUniverseFilter,
		Collections.singleton(getConfiguration().getHarmonizationUnitRelations()), siadap3HarmonizationRelation);
	return new HashSet<PersonSiadapWrapper>(universePersons);

    }

    /**
     * 
     * @return the next 'tier' of units that are connected tho this one by the
     *         HarmonizationUnitRelation
     */
    public List<UnitSiadapWrapper> getSubHarmonizationUnits() {
	List<UnitSiadapWrapper> unitWrappers = new ArrayList<UnitSiadapWrapper>();
	//	fillSubHarmonizationUnits(this, getConfiguration(), unitWrappers);
	for (Unit unit : this.getChildUnits(getConfiguration().getHarmonizationUnitRelations())) {
	    unitWrappers.add(new UnitSiadapWrapper(unit, getYear()));
	}
	return unitWrappers;
    }

    //    private void fillSubHarmonizationUnits(UnitSiadapWrapper wrapper, SiadapYearConfiguration configuration,
    //	    List<UnitSiadapWrapper> wrappers) {
    //	AccountabilityType unitHarmonizationRelation = configuration.getHarmonizationUnitRelations();
    //	AccountabilityType harmonizationResponsibleRelation = configuration.getHarmonizationResponsibleRelation();
    //	int year = configuration.getYear();
    //
    //	for (Unit unit : wrapper.getChildUnits(unitHarmonizationRelation)) {
    //	    UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(unit, year);
    //	    if (!unitSiadapWrapper.getChildPersons(harmonizationResponsibleRelation).isEmpty()) {
    //		wrappers.add(unitSiadapWrapper);
    //	    }
    //	    fillSubHarmonizationUnits(unitSiadapWrapper, configuration, wrappers);
    //	}
    //
    //    }

    public List<UnitSiadapWrapper> getAllChildUnits(AccountabilityType accTypeToUse) {
	List<UnitSiadapWrapper> unitWrappers = new ArrayList<UnitSiadapWrapper>();
	fillAllChildUnits(this, getConfiguration(), unitWrappers, accTypeToUse);
	return unitWrappers;
    }

    private void fillAllChildUnits(UnitSiadapWrapper wrapper, SiadapYearConfiguration configuration,
	    List<UnitSiadapWrapper> wrappers, AccountabilityType accToUse) {
	int year = configuration.getYear();

	for (Unit unit : wrapper.getChildUnits(accToUse)) {
	    UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(unit, year);
	    wrappers.add(unitSiadapWrapper);
	    fillAllChildUnits(unitSiadapWrapper, configuration, wrappers, accToUse);
	}

    }

    public UnitSiadapWrapper getTopHarmonizationUnit() {
	SiadapYearConfiguration configuration = getConfiguration();
	AccountabilityType harmonizationUnitRelation = configuration.getHarmonizationUnitRelations();
	List<Unit> parentUnits = getParentUnits(harmonizationUnitRelation);
	if (parentUnits.isEmpty()) {
	    return null;
	} else {
	    int year = configuration.getYear();
	    UnitSiadapWrapper topUnit = new UnitSiadapWrapper(parentUnits.iterator().next(), year);
	    return new UnitSiadapWrapper(topUnit.getHarmonizationUnit(), year);
	}

    }

    public Unit getUnitAboveViaHarmRelation() {
	SiadapYearConfiguration configuration = getConfiguration();
	AccountabilityType harmonizationUnitRelation = configuration.getHarmonizationUnitRelations();
	List<Unit> parentUnits = getParentUnits(harmonizationUnitRelation);
	if (parentUnits.isEmpty()) {
	    return null;
	} else {
	    int year = configuration.getYear();
	    return parentUnits.iterator().next();
	}

    }

    public boolean isHarmonizationFinished() {
	Unit unitToUse = getUnit();
	if (!isHarmonizationUnit()) {
	    unitToUse = getHarmonizationUnit();
	}
	return getConfiguration().getHarmonizationClosedUnits().contains(unitToUse);
    }

    public boolean isWithAllSiadapsFilled(boolean checkForUsersWithoutSiadap) {
	for (Person person : getChildPersons(getConfiguration().getWorkingRelation(), getConfiguration()
		.getWorkingRelationWithNoQuota())) {
	    Siadap siadap = new PersonSiadapWrapper(person, getYear()).getSiadap();
	    if ((siadap == null && checkForUsersWithoutSiadap) || (siadap != null && !siadap.isDefaultEvaluationDone())) {
		return false;
	    }
	}
	return true;
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

    public boolean isSpecialHarmonizationUnit() {
	return getUnit().equals(getConfiguration().getSiadapSpecialHarmonizationUnit());
    }

    public Interval getHarmonizationInterval() {
	SiadapYearConfiguration configuration = getConfiguration();
	LocalDate harmonizationBegin = configuration.getFirstLevelHarmonizationBegin();
	LocalDate harmonizationEnd = configuration.getFirstLevelHarmonizationEnd();
	return new Interval(SiadapMiscUtilClass.convertDateToBeginOfDay(harmonizationBegin),
		SiadapMiscUtilClass.convertDateToEndOfDay(harmonizationEnd));
    }

    public boolean isHarmonizationPeriodOpen() {
	return isHarmonizationPeriodOpen(getConfiguration());
    }

    public static boolean isHarmonizationPeriodOpen(SiadapYearConfiguration configuration) {
	if (configuration == null || configuration.getFirstLevelHarmonizationBegin() == null)
	    return false;

	Interval interval = new Interval(SiadapMiscUtilClass.convertDateToBeginOfDay(configuration
		.getFirstLevelHarmonizationBegin()), SiadapMiscUtilClass.convertDateToEndOfDay(configuration
		.getFirstLevelHarmonizationEnd()));
	return interval.containsNow();
    }

    public boolean isHarmonizationActive() {
	//	SiadapYearConfiguration configuration = getConfiguration();
	//	LocalDate harmonizationBegin = configuration.getFirstLevelHarmonizationBegin();
	//	LocalDate harmonizationEnd = configuration.getFirstLevelHarmonizationEnd();
	//	if (!isHarmonizationUnit())
	//	    return false;

	if (!isSpecialHarmonizationUnit()) {
	    //the special harmonization unit has no date constraints,
	    //the others have!
	    return isHarmonizationPeriodOpen();
	}
	return true;
    }

    @Service
    public void finishHarmonization() {
	if (!isHarmonizationUnit()) {
	    throw new SiadapException("error.cannot.close.harmonization.in.a.non-harmonization.unit");
	}
	LocalDate currentDate = new LocalDate();
	SiadapYearConfiguration configuration = getConfiguration();
	if (!isHarmonizationActive())
	    throw new SiadapException("error.harmonization.period.is.closed");
	if (configuration.getLockHarmonizationOnQuota() && (isSiadap2WithQuotasAboveQuota() || isSiadap3WithQuotasAboveQuota())) {
	    throw new SiadapException("error.harmonization.unit.is.not.harmonized.for.quota");
	}
	if (configuration.getLockHarmonizationOnQuotaOutsideOfQuotaUniverses()
		&& (isSiadap2WithoutQuotasAboveQuota() || isSiadap3WithoutQuotasAboveQuota())) {
	    throw new SiadapException("error.harmonization.unit.is.not.harmonized.outside.quotas.universes");
	}

	//let's make sure we harmonize everybody now

	finishHarmonizationFor(SiadapUniverse.SIADAP2, currentDate);
	finishHarmonizationFor(SiadapUniverse.SIADAP3, currentDate);
	getConfiguration().addHarmonizationClosedUnits(getUnit());

	//	if (configuration.getLockHarmonizationOnQuota() && isAboveQuotas()) {
	//	    throw new DomainException("error.canOnlyCloseHarmonizationWhenQuotasDoNotExceedValues", DomainException
	//		    .getResourceFor("resources/SiadapResources"));
	//	}

	//	for (UnitSiadapWrapper wrapper : getSubHarmonizationUnits()) {
	//	    if (!wrapper.isHarmonizationFinished()) {
	//		throw new DomainException("error.tryingToFinishHarmonizationWithSubHarmonizationOpen", DomainException
	//			.getResourceFor("resources/SiadapResources"), getName().getContent(), wrapper.getName().getContent());
	//	    }
	//	}
	//	for (UnitSiadapWrapper wrapper : getAllChildUnits()) {
	//	    if (!wrapper.isWithAllSiadapsFilled(false)) {
	//		throw new DomainException("error.tryingToFinishHarmonizationWithSiadapProcessYetToBeEvaluated", DomainException
	//			.getResourceFor("resources/SiadapResources"), getName().getContent(), wrapper.getName().getContent());
	//	    }
	//	}
    }

    public void finishHarmonizationFor(SiadapUniverse siadapUniverse, LocalDate currentDate) {
	Set<PersonSiadapWrapper> personsToHarmonize = new HashSet<PersonSiadapWrapper>();
	if (siadapUniverse.equals(SiadapUniverse.SIADAP2)) {
	    personsToHarmonize.addAll(getSiadap2AndWorkingRelationWithQuotaUniverse());
	    personsToHarmonize.addAll(getSiadap2AndWorkingRelationWithoutQuotaUniverse());
	} else if (siadapUniverse.equals(SiadapUniverse.SIADAP3)) {
	    personsToHarmonize.addAll(getSiadap3AndWorkingRelationWithoutQuotaUniverse());
	    personsToHarmonize.addAll(getSiadap3AndWorkingRelationWithQuotaUniverse());
	}
	for (PersonSiadapWrapper personToHarmonize : personsToHarmonize) {
	    personToHarmonize.getSiadap().markAsHarmonized(currentDate, siadapUniverse);
	}
    }

    public void reOpenHarmonizationFor(SiadapUniverse siadapUniverse) {
	Set<PersonSiadapWrapper> personsToHarmonize = new HashSet<PersonSiadapWrapper>();
	if (siadapUniverse.equals(SiadapUniverse.SIADAP2)) {
	    personsToHarmonize.addAll(getSiadap2AndWorkingRelationWithQuotaUniverse());
	    personsToHarmonize.addAll(getSiadap2AndWorkingRelationWithoutQuotaUniverse());
	} else if (siadapUniverse.equals(SiadapUniverse.SIADAP3)) {
	    personsToHarmonize.addAll(getSiadap3AndWorkingRelationWithQuotaUniverse());
	    personsToHarmonize.addAll(getSiadap3AndWorkingRelationWithoutQuotaUniverse());
	}

	for (PersonSiadapWrapper personToHarmonize : personsToHarmonize) {
	    personToHarmonize.getSiadap().removeHarmonizationMark(siadapUniverse);
	}

    }

    @Service
    public void reOpenHarmonization() {
	if (!isHarmonizationActive())
	    throw new SiadapException("error.harmonization.period.is.closed");

	if (!isHarmonizationFinished())
	    throw new SiadapException("error.harmonization.can.not.reopen.unclosed.harmonization");

	if (!isHarmonizationUnit()) {
	    throw new SiadapException("error.cannot.close.harmonization.in.a.non-harmonization.unit");
	}
	LocalDate currentDate = new LocalDate();
	reOpenHarmonizationFor(SiadapUniverse.SIADAP2);
	reOpenHarmonizationFor(SiadapUniverse.SIADAP3);

	//	UnitSiadapWrapper topHarmonization = getTopHarmonizationUnit();
	//	if (topHarmonization != null && topHarmonization.isHarmonizationFinished()) {
	//	    throw new DomainException("error.unableToReopenTopUnitHasAlreadyFinishedHarmonization",
	//		    DomainException.getResourceFor("resources/SiadapResources"), getName().getContent(), topHarmonization
	//			    .getName().getContent());
	//	}
	getConfiguration().removeHarmonizationClosedUnits(getUnit());
    }

    public List<PersonSiadapWrapper> getPeopleHarmonizedWithAnyNoAssessment() {
	List<PersonSiadapWrapper> listPeopleToReturn = new ArrayList<PersonSiadapWrapper>();
	SiadapYearConfiguration siadapYearConf = getConfiguration();
	getUnitAttachedPersons(
		unit,
		listPeopleToReturn,
		true,
		new Predicate() {

		    @Override
		    public boolean evaluate(Object arg0) {
			PersonSiadapWrapper personWrapper = (PersonSiadapWrapper) arg0;
			for (SiadapEvaluationUniverse evalUniverse : personWrapper.getSiadap().getSiadapEvaluationUniverses()) {
			    if (evalUniverse.getHarmonizationAssessment() != null
				    && evalUniverse.getHarmonizationAssessment() == false)
				return true;
			    if (evalUniverse.getHarmonizationAssessmentForExcellencyAward() != null
				    && evalUniverse.getHarmonizationAssessmentForExcellencyAward() == false)
				return true;
			}
			return false;
		    }
		}, Collections.singleton(siadapYearConf.getHarmonizationUnitRelations()),
		siadapYearConf.getSiadap2HarmonizationRelation(), siadapYearConf.getSiadap3HarmonizationRelation());
	return listPeopleToReturn;
    }

    public boolean isSiadapStructureTopUnit() {
	Unit siadapStructureTopUnit = getConfiguration().getSiadapStructureTopUnit();
	if (siadapStructureTopUnit == null)
	    return false;
	return siadapStructureTopUnit.equals(getUnit());
    }

    //    public List<ExcedingQuotaProposal> getExcedingQuotaProposalSuggestions() {
    //	return getExcedingQuotaProposalSuggestions(null);
    //    }
    //
    //    public List<ExcedingQuotaProposal> getExcedingQuotaProposalSuggestions(ExceddingQuotaSuggestionType type) {
    //	int year = getYear();
    //	List<ExcedingQuotaProposal> list = new ArrayList<ExcedingQuotaProposal>();
    //	for (ExcedingQuotaProposal suggestion : getUnit().getExcedingQuotasProposals()) {
    //	    if (suggestion.getYear() == year && (type == null || suggestion.getSuggestionType() == type)) {
    //		list.add(suggestion);
    //	    }
    //	}
    //	return list;
    //    }

    //    public void addExcedingQuotaProposalSuggestion(Person person, ExceddingQuotaSuggestionType type) {
    //	new ExcedingQuotaProposal(getConfiguration(), person, getUnit(), type);
    //    }
    //
    //    public List<ExcedingQuotaProposal> getOrderedExcedingQuotaProposalSuggestionsForHighEvaluation() {
    //	return getOrderedExcedingQuotaProposalSuggestions(ExceddingQuotaSuggestionType.HIGH_SUGGESTION);
    //    }
    //
    //    public List<ExcedingQuotaProposal> getOrderedExcedingQuotaProposalSuggestionsForExcellencyAward() {
    //	return getOrderedExcedingQuotaProposalSuggestions(ExceddingQuotaSuggestionType.EXCELLENCY_SUGGESTION);
    //    }
    //
    //    public List<ExcedingQuotaProposal> getOrderedExcedingQuotaProposalSuggestions(ExceddingQuotaSuggestionType type) {
    //
    //	List<ExcedingQuotaProposal> list = getExcedingQuotaProposalSuggestions(type);
    //	Collections.sort(list, new Comparator<ExcedingQuotaProposal>() {
    //
    //	    @Override
    //	    public int compare(ExcedingQuotaProposal o1, ExcedingQuotaProposal o2) {
    //		return o1.getProposalOrder().compareTo(o2.getProposalOrder());
    //	    }
    //
    //	});
    //	return list;
    //    }

}
