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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nullable;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.OrganizationalModel;
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

import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import pt.ist.bennu.core.domain.MyOrg;
import pt.ist.bennu.core.domain.exceptions.DomainException;
import pt.ist.bennu.core.util.BundleUtil;
import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public class UnitSiadapWrapper extends PartyWrapper implements Serializable {

	public static final Comparator<UnitSiadapWrapper> COMPARATOR_BY_UNIT_NAME = new Comparator<UnitSiadapWrapper>() {

		@Override
		public int compare(UnitSiadapWrapper o1, UnitSiadapWrapper o2) {
			return o1.getName().compareTo(o2.getName()) != 0 ? o1.getName().compareTo(o2.getName()) : o1.getUnit()
					.getExternalId().compareTo(o2.getUnit().getExternalId());
		}
	};
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

		Set<SiadapUniverseWrapper> universeWrappers =
				new TreeSet<SiadapUniverseWrapper>(SiadapUniverseWrapper.COMPARATOR_BY_UNIVERSE);

		SiadapUniverseWrapper peopleWithQuotasSIADAP2 =
				new SiadapUniverseWrapper(getSiadap2AndWorkingRelationWithQuotaUniverse(), "siadap2WithQuotas",
						SiadapUniverse.SIADAP2, configuration.getQuotaExcellencySiadap2WithQuota(),
						configuration.getQuotaRelevantSiadap2WithQuota(), UniverseDisplayMode.VALIDATION, null, null);
		SiadapUniverseWrapper peopleWithQuotasSIADAP3 =
				new SiadapUniverseWrapper(getSiadap3AndWorkingRelationWithQuotaUniverse(), "siadap3WithQuotas",
						SiadapUniverse.SIADAP3, configuration.getQuotaExcellencySiadap3WithQuota(),
						configuration.getQuotaRelevantSiadap3WithQuota(), UniverseDisplayMode.VALIDATION, null, null);
		SiadapUniverseWrapper peopleWithoutQuotasSIADAP2 =
				new SiadapUniverseWrapper(getSiadap2AndWorkingRelationWithoutQuotaUniverse(), "siadap2WithoutQuotas",
						SiadapUniverse.SIADAP2, configuration.getQuotaExcellencySiadap2WithoutQuota(),
						configuration.getQuotaRelevantSiadap2WithoutQuota(), UniverseDisplayMode.VALIDATION, null, null);
		SiadapUniverseWrapper peopleWithoutQuotasSIADAP3 =
				new SiadapUniverseWrapper(getSiadap3AndWorkingRelationWithoutQuotaUniverse(), "siadap3WithoutQuotas",
						SiadapUniverse.SIADAP3, configuration.getQuotaExcellencySiadap3WithoutQuota(),
						configuration.getQuotaRelevantSiadap3WithoutQuota(), UniverseDisplayMode.VALIDATION, null, null);

		universeWrappers.add(peopleWithoutQuotasSIADAP3);
		universeWrappers.add(peopleWithoutQuotasSIADAP2);
		universeWrappers.add(peopleWithQuotasSIADAP3);
		universeWrappers.add(peopleWithQuotasSIADAP2);

		return universeWrappers;

	}

	public Collection<SiadapUniverseWrapper> getValidationUniverseWrappers() {

		Set<SiadapUniverseWrapper> universeWrappers =
				new TreeSet<SiadapUniverseWrapper>(SiadapUniverseWrapper.COMPARATOR_BY_UNIVERSE);

		UniverseDisplayMode universeDisplayMode = UniverseDisplayMode.VALIDATION;

		Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap2WithQuotas =
				new HashMap<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>>();

		Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap3WithQuotas =
				new HashMap<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>>();

		Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap3WithoutQuotas =
				new HashMap<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>>();

		Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> siadap2WithoutQuotas =
				new HashMap<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>>();

		ExceedingQuotaProposal.organizeAndFillExceedingQuotaProposals(getUnit(), getYear(), siadap2WithQuotas,
				siadap3WithoutQuotas, siadap2WithoutQuotas, siadap3WithQuotas);

		Map<Integer, Collection<PersonSiadapWrapper>> validationSiadap2WithQuotas =
				getValidationPersonSiadapWrappers(SiadapUniverse.SIADAP2, true);
		Map<Integer, Collection<PersonSiadapWrapper>> validationSiadap3WithQuotas =
				getValidationPersonSiadapWrappers(SiadapUniverse.SIADAP3, true);
		Map<Integer, Collection<PersonSiadapWrapper>> validationSiadap2WithoutQuotas =
				getValidationPersonSiadapWrappers(SiadapUniverse.SIADAP2, false);
		Map<Integer, Collection<PersonSiadapWrapper>> validationSiadap3WithoutQuotas =
				getValidationPersonSiadapWrappers(SiadapUniverse.SIADAP3, false);

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
	 * It checks the unit bottom up - i.e. traverse to its relations up (if the
	 * provided unit is not an harmonization unit itself) until it finds an
	 * harmonization unit, which must be connected to the top unit of the given
	 * year
	 * 
	 * @param unit
	 *            the unit to check if it's a valid harmonization unit
	 * @param year
	 *            the year to consider for accountability's and accountability
	 *            type sake
	 * @return true, it is, false, it isn't
	 */
	@SuppressWarnings("boxing")
	public static boolean isValidSIADAPHarmonizationUnit(Unit unit, int year) {

		if (unit == null) {
			return false;
		}
		SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);

		AccountabilityType harmonizationUnitRelation = siadapYearConfiguration.getHarmonizationUnitRelations();

		UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(unit, year);

		if (unitSiadapWrapper.isHarmonizationUnit() && unitSiadapWrapper.isConnectedToTopUnit(harmonizationUnitRelation)) {
			return true;
		} else {
			return isValidSIADAPHarmonizationUnit(unitSiadapWrapper.getSuperiorHarmonizationUnit(), year);
		}
	}

	/**
	 * 
	 * @param unit
	 *            the unit to check
	 * @param year
	 * @return true if the given unit is in the hierarchical model (using the
	 *         siadap unit relations) of the SIADAP for the given year
	 */
	public static boolean isValidSIADAPUnit(Unit unit, int year) {
		AccountabilityType unitRelations = SiadapYearConfiguration.getSiadapYearConfiguration(year).getUnitRelations();
		// let's get the unit and check to see if it has a unit relationship
		// with some other unit or not
		Collection<Party> children = unit.getChildren(unitRelations);
		Collection<Party> parents = unit.getParents(unitRelations);
		if (children.size() == 0 && parents.size() == 0) {
			return false;
		} else {
			return new UnitSiadapWrapper(unit, year).isConnectedToTopUnit(unitRelations);
		}

	}

	/**
	 * 
	 * @param accountabilityType
	 *            the accountabilityType to check for
	 * @return true if this unit is connected to the top unit through the given
	 *         accountabilityType, false otherwise
	 */
	public boolean isConnectedToTopUnit(AccountabilityType accountabilityType) {
		Preconditions.checkNotNull(accountabilityType, "accountabilityType mustn't be null");
		Unit siadapStructureTopUnit = getConfiguration().getSiadapStructureTopUnit();
		return getUnit().hasPartyAsAncestor(siadapStructureTopUnit, Collections.singleton(accountabilityType),
				getConfiguration().getLastDayForAccountabilities());

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
						if (wrapper.isQuotaAware() != belongsToInstitutionalQuota) {
							return false;
						}
						SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse =
								wrapper.getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(universe);
						if (wrapper.isWithSkippedEval(universe)) {
							return false;
						}
						counter[0]++;
						SiadapGlobalEvaluation totalQualitativeEvaluationScoring =
								wrapper.getTotalQualitativeEvaluationScoringObject(universe);
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
	
	/**
	 * 
	 * @return the number of the harmonization unit, or null if there is no H.U.
	 */
	public Integer getHarmonizationUnitNumber() {
		Unit harmonizationUnit = getHarmonizationUnit();
		if (harmonizationUnit == null)
			return null;
		return Integer.valueOf(StringUtils.remove(harmonizationUnit.getAcronym(), HARMONIZATION_UNIT_NAME_PREFIX));
	}

	public String getTotalPeopleWorkingInUnitDescriptionString(boolean continueToSubUnits) {
		Integer peopleWithQuotas = getTotalPeopleWorkingInUnit(continueToSubUnits);
		int peopleWithNoQuotas =
				getTotalPeopleWorkingInUnit(getUnit(), continueToSubUnits, getConfiguration().getWorkingRelationWithNoQuota());

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
		Set<PersonSiadapWrapper> siadap2AndWorkingRelationWithoutQuotaUniverse =
				getSiadap2AndWorkingRelationWithoutQuotaUniverse();
		Set<PersonSiadapWrapper> siadap3AndWorkingRelationWithoutQuotaUniverse =
				getSiadap3AndWorkingRelationWithoutQuotaUniverse();
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
		if (!siadapYearConfiguration.getSiadapStructureTopUnit().equals(getUnit())) {
			excludeResponsibles = true;
		} else {
			excludeResponsibles = false;
		}

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
		return withQuota ? getTotalPeopleWorkingInUnit(getUnit(), continueToSubUnits, getConfiguration().getWorkingRelation()) : getTotalPeopleWorkingInUnit(
				getUnit(), continueToSubUnits, getConfiguration().getWorkingRelationWithNoQuota());
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
			if (predicate.evaluate(siadapWrapper)) {
				counter++;
			}
		}
		return counter;
	}

	/**
	 * 
	 * @param descendOnStructure
	 * @return a {@link PersonSiadapWrapper#PERSON_COMPARATOR_BY_NAME_FALLBACK_YEAR_THEN_PERSON_OID} sorted tree set with the
	 *         people that are harmonized in this unit,
	 *         or in this unit and below, if descendOnStructure is true
	 */
	public Set<PersonSiadapWrapper> getPeopleHarmonizedInThisUnit(boolean descendOnStructure) {
		TreeSet<PersonSiadapWrapper> personsToReturn =
				new TreeSet<PersonSiadapWrapper>(PersonSiadapWrapper.PERSON_COMPARATOR_BY_NAME_FALLBACK_YEAR_THEN_PERSON_OID);
		SiadapYearConfiguration configuration = getConfiguration();
		List<Person> childPersons =
				getChildPersons(configuration.getSiadap3HarmonizationRelation(), configuration.getSiadap2HarmonizationRelation());
		if (descendOnStructure) {
			for (Unit childUnit : getChildUnits(configuration.getHarmonizationUnitRelations())) {
				personsToReturn.addAll(new UnitSiadapWrapper(childUnit, configuration.getYear())
						.getPeopleHarmonizedInThisUnit(true));
			}
		}

		personsToReturn.addAll(Collections2.transform(childPersons, new Function<Person, PersonSiadapWrapper>() {

			@Override
			@Nullable
			public PersonSiadapWrapper apply(@Nullable Person input) {
				if (input == null) {
					return null;
				}
				return new PersonSiadapWrapper(input, getYear());
			}
		}));
		return personsToReturn;

	}

	@Service
	public ArrayList<SiadapException> executeValidation(Collection<SiadapUniverseWrapper> siadapUniverseWrappers,

	ValidationSubActivity validationSubActivity) throws DomainException, ActivityException {

		ArrayList<SiadapException> warningsToReturn = new ArrayList<SiadapException>();
		HashSet<Person> evaluatorsToNotify = new HashSet<Person>();
		// if we are closing down the validation, we should make sure that this
		// is the top unit
		if (validationSubActivity.equals(ValidationSubActivity.TERMINATE_VALIDATION)) {
			if (!getUnit().equals(getConfiguration().getSiadapStructureTopUnit())) {
				throw new ValidationTerminationException("error.validation.must.be.closed.on.top.unit.only");
			}
			// let's check if it is already closed or not
			if (getConfiguration().getClosedValidation()) {
				throw new SiadapException("error.validation.already.closed");
			}
			// let's check the quotas
			for (SiadapUniverseWrapper siadapUniverseWrapper : siadapUniverseWrappers) {
				if (siadapUniverseWrapper.isAboveQuotasValidation()) {
					String isQuotaAware =
							siadapUniverseWrapper.getSiadapUniverse().iterator().next().isQuotaAware() ? "Sim" : "Não";
					throw new ValidationTerminationException("error.validation.above.quotas", siadapUniverseWrapper
							.getSiadapUniverseEnum().getLocalizedName(), isQuotaAware);
				}

			}
		}

		WorkflowActivity activity = SiadapProcess.getActivityStaticly(Validation.class.getSimpleName());

		for (SiadapUniverseWrapper universeWrapper : siadapUniverseWrappers) {
			for (PersonSiadapWrapper personSiadapWrapper : universeWrapper.getSiadapUniverse()) {
				ActivityInformation<?> validationActivityInformation =
						new ValidationActivityInformation(personSiadapWrapper, activity, validationSubActivity,
								universeWrapper.getSiadapUniverseEnum());
				if (validationActivityInformation.hasAllneededInfo()) {
					activity.execute(validationActivityInformation);
					if (validationSubActivity.equals(ValidationSubActivity.TERMINATE_VALIDATION)) {
						// let's add the evaluator to the list of evaluators to
						// notify
						if (personSiadapWrapper.getEvaluator() != null) {
							evaluatorsToNotify.add(personSiadapWrapper.getEvaluator().getPerson());
						}
					}
				}
			}
		}

		// mark as validated
		getConfiguration().setClosedValidation(Boolean.TRUE);

		// notify whoever
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

	public BigDecimal getExcellencyEvaluationPercentage() {
		int totalPeopleWorkingForUnit = getUnitEmployees(true).size();
		Collection<PersonSiadapWrapper> excellentEvaluationPersons = getUnitEmployees(true, new Predicate() {

			@Override
			public boolean evaluate(Object personObject) {
				PersonSiadapWrapper personWrapper = (PersonSiadapWrapper) personObject;
				if (personWrapper.getSiadap() == null || personWrapper.getSiadap().getDefaultSiadapEvaluationUniverse() == null) {
					return false;
				}
				return personWrapper.getSiadap().getDefaultSiadapEvaluationUniverse().hasExcellencyAwarded();
			}
		});
		int excellentCount = excellentEvaluationPersons.size();
		if ((excellentCount == 0) || (totalPeopleWorkingForUnit == 0)) {
			return BigDecimal.ZERO;
		}

		return new BigDecimal(excellentCount).divide(new BigDecimal(totalPeopleWorkingForUnit), UnitSiadapWrapper.SCALE,
				RoundingMode.HALF_EVEN).multiply(new BigDecimal(100), new MathContext(UnitSiadapWrapper.SCALE));
	}

	public BigDecimal getRelevantEvaluationPercentage() {
		int totalPeopleWorkingForUnit = getUnitEmployees(true).size();
		Collection<PersonSiadapWrapper> relevantEvaluationPersons = getUnitEmployees(true, new Predicate() {

			@Override
			public boolean evaluate(Object personObject) {
				PersonSiadapWrapper personWrapper = (PersonSiadapWrapper) personObject;
				if (personWrapper.getSiadap() == null || personWrapper.getSiadap().getDefaultSiadapEvaluationUniverse() == null) {
					return false;
				}
				return personWrapper.getSiadap().getDefaultSiadapEvaluationUniverse().hasRelevantEvaluation();
			}

		});
		int relevantCount = relevantEvaluationPersons.size();

		if ((relevantCount == 0) || (totalPeopleWorkingForUnit == 0)) {
			return BigDecimal.ZERO;
		}

		return new BigDecimal(relevantCount).divide(new BigDecimal(totalPeopleWorkingForUnit), UnitSiadapWrapper.SCALE,
				RoundingMode.HALF_EVEN).multiply(new BigDecimal(100), new MathContext(UnitSiadapWrapper.SCALE));
	}

	public Person getEvaluationResponsible() {
		List<Person> childPersons = getChildPersons(getConfiguration().getEvaluationRelation());
		if (childPersons.size() > 1) {
			throw new SiadapException("inconsistency.unit.with.more.than.one.evaluation.responsible");
		}
		return childPersons.isEmpty() ? null : childPersons.iterator().next();
	}

	public Collection<Person> getHarmonizationResponsibles() {
		return getChildPersons(getConfiguration().getHarmonizationResponsibleRelation());
	}

	// Quotas SIADAP 2 with quota
	public Integer getExcellencySiadap2WithQuotaQuota() {
		Integer quotaExcellencySiadap2WithQuota =
				SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getQuotaExcellencySiadap2WithQuota();
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
		Integer quotaRelevantSiadap2WithQuota =
				SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getQuotaRelevantSiadap2WithQuota();
		int totalPeople = getPeopleHarmonizedInUnitSiadap2WithQuotas();
		return calculateQuota(totalPeople, quotaRelevantSiadap2WithQuota);
	}

	public Integer getNumberCurrentRelevantsSiadap2WithQuota() {
		return getNrEvaluationsBasedOnPredicate(getSiadap2AndWorkingRelationWithQuotaUniverse(), new Predicate() {

			@Override
			public boolean evaluate(Object arg0) {
				PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
				Siadap siadap = personSiadapWrapper.getSiadap();
				if (siadap != null && siadap.hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation.HIGH, SiadapUniverse.SIADAP2)) {
					return true;
				}
				return false;
			}
		});
	}

	// Quotas SIADAP 2 WITHOUT quota
	public Integer getExcellencySiadap2WithoutQuotaQuota() {
		Integer quotaExcellencySiadap2WithoutQuota =
				SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getQuotaExcellencySiadap2WithoutQuota();
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
						&& siadap.hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation.EXCELLENCY, SiadapUniverse.SIADAP2)) {
					return true;
				}
				return false;
			}
		});
	}

	public Integer getRelevantSiadap2WithoutQuotaQuota() {
		Integer quotaRelevantSiadap2WithoutQuota =
				SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getQuotaRelevantSiadap2WithoutQuota();
		int totalPeople = getPeopleHarmonizedInUnitSiadap2WithQuotas();
		return calculateQuota(totalPeople, quotaRelevantSiadap2WithoutQuota);
	}

	public Integer getNumberCurrentRelevantsSiadap2WithoutQuota() {
		return getNrEvaluationsBasedOnPredicate(getSiadap2AndWorkingRelationWithoutQuotaUniverse(), new Predicate() {

			@Override
			public boolean evaluate(Object arg0) {
				PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
				Siadap siadap = personSiadapWrapper.getSiadap();
				if (siadap != null && siadap.hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation.HIGH, SiadapUniverse.SIADAP2)) {
					return true;
				}
				return false;
			}
		});
	}

	// Quotas SIADAP 3 WITH quota
	public Integer getExcellencySiadap3WithQuotaQuota() {
		Integer quotaExcellencySiadap3WithQuota =
				SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getQuotaExcellencySiadap3WithQuota();
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
						&& siadap.hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation.EXCELLENCY, SiadapUniverse.SIADAP3)) {
					return true;
				}
				return false;
			}
		});
	}

	public Integer getRelevantSiadap3WithQuotaQuota() {
		Integer quotaRelevantSiadap3WithQuota =
				SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getQuotaRelevantSiadap3WithQuota();
		int totalPeople = getPeopleHarmonizedInUnitSiadap2WithQuotas();
		return calculateQuota(totalPeople, quotaRelevantSiadap3WithQuota);
	}

	// TODO: joantune: these getNumberCurrent... could be done in a different
	// way with a more generic predicate with a constructor,
	// buttttt it is done this way because there was a refactor on this and no
	// time to change this
	public Integer getNumberCurrentRelevantsSiadap3WithQuota() {
		return getNrEvaluationsBasedOnPredicate(getSiadap3AndWorkingRelationWithQuotaUniverse(), new Predicate() {

			@Override
			public boolean evaluate(Object arg0) {
				PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
				Siadap siadap = personSiadapWrapper.getSiadap();
				if (siadap != null && siadap.hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation.HIGH, SiadapUniverse.SIADAP3)) {
					return true;
				}
				return false;
			}
		});
	}

	// Quotas SIADAP 3 WITHOUT quota
	public Integer getExcellencySiadap3WithoutQuotaQuota() {
		Integer quotaExcellencySiadap3WithoutQuota =
				SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getQuotaExcellencySiadap3WithoutQuota();
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
						&& siadap.hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation.EXCELLENCY, SiadapUniverse.SIADAP3)) {
					return true;
				}
				return false;
			}
		});
	}

	public Integer getRelevantSiadap3WithoutQuotaQuota() {
		Integer quotaRelevantSiadap3WithoutQuota =
				SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getQuotaRelevantSiadap3WithoutQuota();
		int totalPeople = getPeopleHarmonizedInUnitSiadap2WithQuotas();
		return calculateQuota(totalPeople, quotaRelevantSiadap3WithoutQuota);
	}

	public Integer getNumberCurrentRelevantsSiadap3WithoutQuota() {
		return getNrEvaluationsBasedOnPredicate(getSiadap3AndWorkingRelationWithoutQuotaUniverse(), new Predicate() {

			@Override
			public boolean evaluate(Object arg0) {
				PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
				Siadap siadap = personSiadapWrapper.getSiadap();
				if (siadap != null && siadap.hasGivenSiadapGlobalEvaluation(SiadapGlobalEvaluation.HIGH, SiadapUniverse.SIADAP3)) {
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * Creates an o
	 * 
	 * @param year
	 * @param name
	 * @param number
	 * @return
	 */
	public static Unit createSiadapHarmonizationUnit(int year, MultiLanguageString name, int number) {
		// let's get the data we need
		SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
		Unit siadapStructureTopUnit = siadapYearConfiguration.getSiadapStructureTopUnit();

		AccountabilityType harmonizationUnitRelation = siadapYearConfiguration.getHarmonizationUnitRelations();

		LocalDate beginDate = siadapYearConfiguration.getFirstDay();

		PartyType harmonizationPartyType = PartyType.readBy(SIADAP_HARMONIZATION_UNIT_TYPE);

		return siadapStructureTopUnit.create(siadapStructureTopUnit, name,
				HARMONIZATION_UNIT_NAME_PREFIX + String.valueOf(number), harmonizationPartyType, harmonizationUnitRelation,
				beginDate, null);
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
		return value > 0 ? value : 1; // if the quota is 0 the the quota shifts
		// to 1

	}

	public final static String HARMONIZATION_UNIT_NAME_PREFIX = "SIADAP - U.H. ";

	public static Unit getHarmonizationUnit(int number) {
		PartyType harmonizationPartyType = PartyType.readBy(SIADAP_HARMONIZATION_UNIT_TYPE);
		for (Party party : harmonizationPartyType.getParties()) {
			if (!(party instanceof Unit)) {
				throw new SiadapException("Error, the harmonization party type should only have units associated with it");
			}
			Unit unit = (Unit) party;
			if (unit.getAcronym().equals(HARMONIZATION_UNIT_NAME_PREFIX + String.valueOf(number))) {
				return unit;
			}
		}
		return null;
	}

	public Unit getHarmonizationUnit() {
		return getHarmonizationUnit(getUnit());
	}

	private Unit getHarmonizationUnit(Unit unit) {
		UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, getYear());
		if (isHarmonizationUnit(unit)) {
			return unit;
		}
		Collection<Unit> units = wrapper.getParentUnits(getConfiguration().getHarmonizationUnitRelations());
		return units.isEmpty() ? null : getHarmonizationUnit(units.iterator().next());
	}

	public Unit getValidHarmonizationUnit() {
		return getHarmonizationUnit(getUnit());
	}

	private Unit getValidHarmonizationUnit(Unit unit) {
		UnitSiadapWrapper wrapper = new UnitSiadapWrapper(unit, getYear());
		if (isValidHarmonizationUnit(unit)) {
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

	public void addAndNotifyResonsibleForHarmonization(Person person) {
		addResponsibleForHarmonization(person);
	}

	public void addResponsibleForHarmonization(Person person) {
		AccountabilityType harmonizationResponsibleRelation = getConfiguration().getHarmonizationResponsibleRelation();
		Collection<Accountability> childrenAccountabilities =
				getUnit().getChildrenAccountabilities(Collections.singleton(harmonizationResponsibleRelation));

		if (!childrenAccountabilities.isEmpty()) {
			for (Accountability accountability : childrenAccountabilities) {
				if (accountability.isActive(getConfiguration().getLastDayForAccountabilities())) {
					// if we already have that person there, let's just return
					if (accountability.getChild().equals(person)) {
						return;
					}
				} else if (!accountability.getChild().equals(person)) {
					accountability.editDates(accountability.getBeginDate(), getConfiguration().getFirstDay());
				}
			}
		}
		getUnit().addChild(person, harmonizationResponsibleRelation, getConfiguration().getFirstDay(), null);
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

	public List<PersonSiadapWrapper> getUnitEmployeesWithProcessesInReviewCommission() {
		List<PersonSiadapWrapper> unitEmployees = getUnitEmployees(true, new Predicate() {
			@Override
			public boolean evaluate(Object personObject) {
				PersonSiadapWrapper personWrapper = (PersonSiadapWrapper) personObject;
				if (personWrapper.getSiadap().isWaitingForReviewCommission()) {
					return true;
				}
				return false;
			}
		});
		Collections.sort(unitEmployees, PersonSiadapWrapper.PERSON_COMPARATOR_BY_NAME_FALLBACK_YEAR_THEN_PERSON_OID);
		return unitEmployees;
	}

	public List<PersonSiadapWrapper> getUnitEmployeesWithOngoingProcesses() {
		List<PersonSiadapWrapper> unitEmployees = getUnitEmployees(true, new Predicate() {
			@Override
			public boolean evaluate(Object personObject) {
				PersonSiadapWrapper personWrapper = (PersonSiadapWrapper) personObject;
				return personWrapper.getSiadap().isOngoing();
			}
		});
		Collections.sort(unitEmployees, PersonSiadapWrapper.PERSON_COMPARATOR_BY_NAME_FALLBACK_YEAR_THEN_PERSON_OID);
		return unitEmployees;
	}

	public List<PersonSiadapWrapper> getUnitEmployeesWithProcessesHomologated() {
		List<PersonSiadapWrapper> unitEmployees = getUnitEmployees(true, new Predicate() {
			@Override
			public boolean evaluate(Object personObject) {
				PersonSiadapWrapper personWrapper = (PersonSiadapWrapper) personObject;
				if (personWrapper.getSiadap().isHomologated()) {
					return true;
				}
				return false;
			}
		});
		Collections.sort(unitEmployees, PersonSiadapWrapper.PERSON_COMPARATOR_BY_NAME_FALLBACK_YEAR_THEN_PERSON_OID);
		return unitEmployees;
	}

	public List<PersonSiadapWrapper> getUnitEmployeesWithProcessesPendingHomologation() {
		List<PersonSiadapWrapper> unitEmployees = getUnitEmployees(true, new Predicate() {
			@Override
			public boolean evaluate(Object personObject) {
				PersonSiadapWrapper personWrapper = (PersonSiadapWrapper) personObject;
				if ((personWrapper.getSiadap() != null) && (personWrapper.getSiadap().isWaitingHomologation())) {
					return true;
				}
				return false;
			}
		});
		Collections.sort(unitEmployees, PersonSiadapWrapper.PERSON_COMPARATOR_BY_NAME_FALLBACK_YEAR_THEN_PERSON_OID);
		return unitEmployees;
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

		List<AccountabilityType> unitAccTypesToUse = new ArrayList<AccountabilityType>();
		if (isHarmonizationUnit()) {
			// let's include the harm. relation as well
			unitAccTypesToUse.add(getConfiguration().getHarmonizationUnitRelations());
		} else {
			// let's iterate useing the unit relations at the very least
			unitAccTypesToUse.add(getConfiguration().getUnitRelations());
		}
		getUnitAttachedPersons(getUnit(), employees, continueToSubUnits, predicate, unitAccTypesToUse, getConfiguration()
				.getWorkingRelation(), getConfiguration().getWorkingRelationWithNoQuota());
		return employees;
	}

	public List<PersonSiadapWrapper> getUnitEmployeesWithQuotas(boolean continueToSubUnits) {
		return getUnitEmployeesWithQuotas(continueToSubUnits, null);
	}

	public List<PersonSiadapWrapper> getUnitEmployeesWithQuotas(Predicate predicate) {
		return getUnitEmployeesWithQuotas(true, predicate);
	}

	public boolean isValidSIADAPUnit() {
		return isValidSIADAPUnit(getUnit(), getYear());
	}

	public List<PersonSiadapWrapper> getUnitEmployeesWithQuotas(boolean continueToSubUnits, Predicate predicate) {
		List<PersonSiadapWrapper> employees = new ArrayList<PersonSiadapWrapper>();

		List<AccountabilityType> unitAccTypesToUse = new ArrayList<AccountabilityType>();
		if (isHarmonizationUnit()) {
			// let's include the harm. relation as well
			unitAccTypesToUse.add(getConfiguration().getHarmonizationUnitRelations());
		}
		// let's iterate useing the unit relations at the very least
		unitAccTypesToUse.add(getConfiguration().getUnitRelations());

		getUnitAttachedPersons(getUnit(), employees, continueToSubUnits, predicate, unitAccTypesToUse, getConfiguration()
				.getWorkingRelation());
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

		List<AccountabilityType> unitAccTypesToUse = new ArrayList<AccountabilityType>();
		if (isHarmonizationUnit()) {
			// let's include the harm. relation as well
			unitAccTypesToUse.add(getConfiguration().getHarmonizationUnitRelations());
		}
		// let's iterate useing the unit relations at the very least
		unitAccTypesToUse.add(getConfiguration().getUnitRelations());

		getUnitAttachedPersons(getUnit(), employees, continueToSubUnits, predicate, unitAccTypesToUse, getConfiguration()
				.getWorkingRelationWithNoQuota());
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
					// seen that we are in the top harmonization unit, we will
					// show info about all of the persons on the sub units
					return true;
				}
			}
		}
		return false;

	}

	public boolean isValidHarmonizationUnit() {
		return isValidHarmonizationUnit(getUnit());
	}

	public boolean isValidHarmonizationUnit(Unit unit) {
		if (unit != null) {
			for (PartyType partyType : unit.getPartyTypes()) {
				if (partyType.getType().equalsIgnoreCase(SIADAP_HARMONIZATION_UNIT_TYPE)) {
					// seen that we are in the top harmonization unit, we will
					// show info about all of the persons on the sub units
					return isConnectedToTopUnit(getConfiguration().getHarmonizationUnitRelations());
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
				// WTF?!?! the greatest boolean riddle... probably should be
				// broken down... but where's the fun in that :P
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
			} else {
				return true;
			}
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
			if (checkIfExistsOnly) {
				return personWrapper.getYear() == year && personWrapper.getSiadap() != null
						&& (!excludeResponsibles || !harmonizationResponsibles.contains(personWrapper.getPerson()));
			}
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
		if (!SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getSiadapStructureTopUnit().equals(getUnit())) {
			excludeResponsibles = true;
		} else {
			excludeResponsibles = false;
		}

		SiadapUniverseFilter siadapUniverseFilter =
				new SiadapUniverseFilter(excludeResponsibles, getYear(), this, true, includePositivelyHarmonizedOnly,
						SiadapUniverse.SIADAP2);
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
		if (!SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getSiadapStructureTopUnit().equals(getUnit())) {
			excludeResponsibles = true;
		} else {
			excludeResponsibles = false;
		}

		SiadapUniverseFilter siadapUniverseFilter =
				new SiadapUniverseFilter(excludeResponsibles, getYear(), this, true, includePositivelyHarmonizedOnly,
						SiadapUniverse.SIADAP3);

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
		if (!SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getSiadapStructureTopUnit().equals(getUnit())) {
			excludeResponsibles = true;
		} else {
			excludeResponsibles = false;
		}

		SiadapUniverseFilter siadapUniverseFilter =
				new SiadapUniverseFilter(excludeResponsibles, getYear(), this, false, includePositivelyHarmonizedOnly,
						SiadapUniverse.SIADAP2);

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
		if (!SiadapYearConfiguration.getSiadapYearConfiguration(getYear()).getSiadapStructureTopUnit().equals(getUnit())) {
			excludeResponsibles = true;
		} else {
			excludeResponsibles = false;
		}

		SiadapUniverseFilter siadapUniverseFilter =
				new SiadapUniverseFilter(excludeResponsibles, getYear(), this, false, includePositivelyHarmonizedOnly,
						SiadapUniverse.SIADAP3);

		List<PersonSiadapWrapper> universePersons = new ArrayList<PersonSiadapWrapper>();
		getUnitAttachedPersons(unit, universePersons, isHarmonizationUnit() || isSiadapStructureTopUnit(), siadapUniverseFilter,
				Collections.singleton(getConfiguration().getHarmonizationUnitRelations()), siadap3HarmonizationRelation);
		return new HashSet<PersonSiadapWrapper>(universePersons);

	}

	public Collection<SiadapProcess> getAllSiadapProcesses() {
		Set<SiadapProcess> allProcesses = new HashSet<SiadapProcess>();
		for (PersonSiadapWrapper person : getSiadap3AndWorkingRelationWithoutQuotaUniverse()) {
			allProcesses.add(person.getSiadap().getProcess());
		}
		for (PersonSiadapWrapper person : getSiadap3AndWorkingRelationWithQuotaUniverse()) {
			allProcesses.add(person.getSiadap().getProcess());
		}
		for (PersonSiadapWrapper person : getSiadap2AndWorkingRelationWithoutQuotaUniverse()) {
			allProcesses.add(person.getSiadap().getProcess());
		}
		for (PersonSiadapWrapper person : getSiadap2AndWorkingRelationWithQuotaUniverse()) {
			allProcesses.add(person.getSiadap().getProcess());
		}
		return allProcesses;
	}

	public Collection<SiadapProcess> getSiadapProcessesOngoing() {
		Set<SiadapProcess> processes = new HashSet<SiadapProcess>();
		for (SiadapProcess process : getAllSiadapProcesses()) {
			if (process.getSiadap().isOngoing()) {
				processes.add(process);
			}
		}
		return processes;
	}

	public Collection<SiadapProcess> getSiadapProcessesInReviewCommission() {
		Set<SiadapProcess> processes = new HashSet<SiadapProcess>();
		for (SiadapProcess process : getAllSiadapProcesses()) {
			if (process.getSiadap().isWaitingForReviewCommission()) {
				processes.add(process);
			}
		}
		return processes;
	}

	public Collection<SiadapProcess> getSiadapProcessesPendingHomologation() {
		Set<SiadapProcess> processes = new HashSet<SiadapProcess>();
		for (SiadapProcess process : getAllSiadapProcesses()) {
			if (process.getSiadap().isWaitingHomologation()) {
				processes.add(process);
			}
		}
		return processes;
	}

	public Collection<SiadapProcess> getSiadapProcessesHomologated() {
		Set<SiadapProcess> processes = new HashSet<SiadapProcess>();
		for (SiadapProcess process : getAllSiadapProcesses()) {
			if (process.getSiadap().isHomologated()) {
				processes.add(process);
			}
		}
		return processes;
	}

	/**
	 * 
	 * @return the next 'tier' of units that are connected tho this one by the
	 *         HarmonizationUnitRelation
	 */
	public List<UnitSiadapWrapper> getSubHarmonizationUnits() {
		List<UnitSiadapWrapper> unitWrappers = new ArrayList<UnitSiadapWrapper>();
		// fillSubHarmonizationUnits(this, getConfiguration(), unitWrappers);
		for (Unit unit : this.getChildUnits(getConfiguration().getHarmonizationUnitRelations())) {
			unitWrappers.add(new UnitSiadapWrapper(unit, getYear()));
		}
		return unitWrappers;
	}

	// private void fillSubHarmonizationUnits(UnitSiadapWrapper wrapper,
	// SiadapYearConfiguration configuration,
	// List<UnitSiadapWrapper> wrappers) {
	// AccountabilityType unitHarmonizationRelation =
	// configuration.getHarmonizationUnitRelations();
	// AccountabilityType harmonizationResponsibleRelation =
	// configuration.getHarmonizationResponsibleRelation();
	// int year = configuration.getYear();
	//
	// for (Unit unit : wrapper.getChildUnits(unitHarmonizationRelation)) {
	// UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(unit, year);
	// if
	// (!unitSiadapWrapper.getChildPersons(harmonizationResponsibleRelation).isEmpty())
	// {
	// wrappers.add(unitSiadapWrapper);
	// }
	// fillSubHarmonizationUnits(unitSiadapWrapper, configuration, wrappers);
	// }
	//
	// }

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

	/**
	 * 
	 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 28 de Jan de 2013
	 * 
	 *         Convenience method to transverse on both SIADAP's structures
	 * 
	 *         (Harmonization and work structure). This class was created a
	 *         little bit apart from the rest of the API by Luis. Some of its
	 *         methods may be redundant versus some of the methods already
	 *         available @ {@link UnitSiadapWrapper}. Refactoring will
	 *         eventually be a good thing to cut on the lines of the code :)
	 * 
	 * 
	 */
	public static class UnitTransverseUtil {

		/**
		 * 
		 * @param unit
		 * @param configuration
		 * @return the active children units, that have active workers, using {@link SiadapYearConfiguration#getUnitRelations()}
		 *         for the
		 *         units and {@link SiadapYearConfiguration#getWorkingRelation()},
		 *         {@link SiadapYearConfiguration#getWorkingRelationWithNoQuota()} for the workers
		 */
		public static Collection<Party> getActiveChildren(final Unit unit, final SiadapYearConfiguration configuration) {
			return getActiveChildren(unit, configuration, configuration.getUnitRelations(), configuration.getWorkingRelation(),
					configuration.getWorkingRelationWithNoQuota());
		}

		/**
		 * 
		 * @param unit
		 * @param configuration
		 *            the year configuration
		 * @param unitAcc
		 *            the accountability to use to transverse between units
		 * @param employeeAccs
		 *            the accountability types {@link AccountabilityType} to use
		 *            for the last arg of {@link #hasSomeWorker(Unit, SiadapYearConfiguration, LocalDate, AccountabilityType[])} .
		 *            Basicly the unit will only be considered active if it
		 *            has at least one party with that kind of accountability
		 *            active, or its children parties do
		 * @return
		 */
		public static Collection<Party> getActiveChildren(final Unit unit, final SiadapYearConfiguration configuration,
				AccountabilityType unitAcc, AccountabilityType... employeeAccs) {
			final LocalDate dayToUse = SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(configuration.getYear());
			final SortedSet<Party> result = new TreeSet<Party>(Party.COMPARATOR_BY_NAME);
			for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
				if (isActiveUnit(accountability, configuration, unitAcc)
						&& hasSomeWorker((Unit) accountability.getChild(), configuration, unitAcc, employeeAccs)) {
					result.add(accountability.getChild());
				}
			}
			return result;
		}

		public static Collection<Party> getActiveParents(final Unit unit, final SiadapYearConfiguration configuration) {
			return getActiveParents(unit, configuration, configuration.getUnitRelations());
		}

		public static Collection<Party> getActiveParents(final Unit unit, final SiadapYearConfiguration configuration,
				AccountabilityType unitAcc) {
			final LocalDate dayToUse = SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(configuration.getYear());
			final SortedSet<Party> result = new TreeSet<Party>(Party.COMPARATOR_BY_NAME);
			for (final Accountability accountability : unit.getParentAccountabilitiesSet()) {
				if (isActiveUnit(accountability, configuration, unitAcc)) {
					result.add(accountability.getParent());
				}
			}
			return result;
		}

		private static Unit findUnitParent(final Unit unit, final LocalDate dayToUse,

		final AccountabilityType accountabilityType) {
			for (final Accountability accountability : unit.getParentAccountabilitiesSet()) {
				if (isActive(accountability, dayToUse, accountabilityType)) {
					return (Unit) accountability.getParent();
				}
			}
			return null;
		}

		private static boolean isActiveUnit(Accountability accountability, SiadapYearConfiguration configuration) {

			final LocalDate dayToUse = SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(configuration.getYear());
			return isActive(accountability, dayToUse, configuration.getUnitRelations());
		}

		private static boolean isActiveUnit(Accountability accountability, SiadapYearConfiguration configuration,
				AccountabilityType unitAcc) {
			final LocalDate dayToUse = SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(configuration.getYear());
			return isActive(accountability, dayToUse, unitAcc);
		}

		/**
		 * 
		 * @param accountability
		 * @param configuration
		 * @param dayToUse
		 * @return true if the given accountability is active on the last day of
		 *         the configuration year, and of the types {@link SiadapYearConfiguration#getWorkingRelation()} and
		 *         {@link SiadapYearConfiguration#getWorkingRelationWithNoQuota()}
		 */
		private static boolean isActiveWorker(Accountability accountability, SiadapYearConfiguration configuration) {
			LocalDate dayToUse = configuration.getLastDayForAccountabilities();
			return isActive(accountability, dayToUse, configuration.getWorkingRelation(),
					configuration.getWorkingRelationWithNoQuota());
		}

		private static boolean isActiveWorker(Accountability accountability, SiadapYearConfiguration configuration,
				AccountabilityType... accountabiltyTypes) {
			LocalDate dayToUse = configuration.getLastDayForAccountabilities();
			return isActive(accountability, dayToUse, accountabiltyTypes);
		}

		/**
		 * 
		 * @param unit
		 * @param configuration
		 * @param dayToUse
		 * @return true if the given unit or any of its children have some
		 *         active worker on the given day
		 */
		private static boolean hasSomeWorker(final Unit unit, final SiadapYearConfiguration configuration) {
			for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
				if (isActiveWorker(accountability, configuration)
						|| (isActiveUnit(accountability, configuration) && hasSomeWorker((Unit) accountability.getChild(),
								configuration))) {
					return true;
				}
			}
			return false;
		}

		private static boolean hasSomeWorker(final Unit unit, final SiadapYearConfiguration configuration,
				final AccountabilityType unitAcc, final AccountabilityType... employeeAccsType) {
			for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
				if (isActiveWorker(accountability, configuration, employeeAccsType)
						|| (isActiveUnit(accountability, configuration, unitAcc) && hasSomeWorker(
								(Unit) accountability.getChild(), configuration, unitAcc, employeeAccsType))) {
					return true;
				}
			}
			return false;
		}

		public static Collection<Accountability> getActiveChildrenWorkers(final Unit unit,
				final SiadapYearConfiguration configuration) {
			final LocalDate dayToUse = SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(configuration.getYear());
			final SortedSet<Accountability> result = new TreeSet<Accountability>(Accountability.COMPARATOR_BY_CHILD_PARTY_NAMES);
			for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
				if (isActiveWorker(accountability, configuration)) {
					result.add(accountability);
				}
			}
			return result;
		}

		public static Collection<Accountability> getActiveChildrenWorkers(final Unit unit,
				final SiadapYearConfiguration configuration, AccountabilityType... workerAccTypes) {
			final LocalDate dayToUse = SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(configuration.getYear());
			final SortedSet<Accountability> result = new TreeSet<Accountability>(Accountability.COMPARATOR_BY_CHILD_PARTY_NAMES);
			for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
				if (isActiveWorker(accountability, configuration, workerAccTypes)) {
					result.add(accountability);
				}
			}
			return result;
		}

		private static boolean isActive(final Accountability accountability, final LocalDate dayToUse,
				final AccountabilityType... accountabilityTypes) {
			final AccountabilityType accountabilityType = accountability.getAccountabilityType();
			if (accountability.isActive(dayToUse)) {
				for (final AccountabilityType type : accountabilityTypes) {
					if (type == accountabilityType) {
						return true;
					}
				}
			}
			return false;
		}

	}

	/**
	 * 
	 * @return the Organization model of name #SIADAP_ORGANIZATION_MODEL_NAME
	 */
	public static OrganizationalModel findRegularOrgModel() {
		final MyOrg instance = MyOrg.getInstance();
		for (final OrganizationalModel organizationalModel : instance.getOrganizationalModelsSet()) {
			if (organizationalModel.getName().getContent().equals(SIADAP_ORGANIZATION_MODEL_NAME)) {
				return organizationalModel;
			}
		}
		return null;
	}

	public static Unit getUnit(final OrganizationalModel organizationalModel, String unitExternalId) {
		final Unit unit = AbstractDomainObject.fromExternalId(unitExternalId);
		return unit == null ? (organizationalModel.hasAnyParties() ? (Unit) organizationalModel.getPartiesIterator().next() : null) : unit;
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

	public String getPresentationName() {
		return getUnit().getPresentationName();
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

	/**
	 * 
	 * @param harmonizationUnit
	 *            the unit which the subUnit is going to connect to. It must be
	 *            an HarmonizationUnit
	 * @param subUnit
	 *            the sub unit to connect
	 * @param year
	 *            the year, which is used to get the accountability type and the
	 *            dates of the accountability
	 * @param justification
	 *            String representation of the reason of this accountability, or
	 *            null
	 */
	@SuppressWarnings("boxing")
	public static void addHarmonizationUnitRelation(Unit harmonizationUnit, Unit subUnit, int year, String justification)
			throws SiadapException {
		if (!harmonizationUnit.getPartyTypes().contains(PartyType.readBy(SIADAP_HARMONIZATION_UNIT_TYPE))) {
			throw new SiadapException("given harmonizationUnit: " + harmonizationUnit.getPresentationName()
					+ " must have a PartyType of: " + SIADAP_HARMONIZATION_UNIT_TYPE);
		}

		SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);

		if (siadapYearConfiguration.isHarmonizationPeriodOpenNow()) {
			throw new SiadapException("error.cannot.change.harmonization.structure.with.open.harmonization.period");
		}

		// let's see if we already have the connection
		UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(subUnit, year);

		Unit currentHU = unitSiadapWrapper.getHarmonizationUnit();
		if (currentHU != null) {
			if (currentHU.equals(harmonizationUnit)) {
				return; // we already have that relation
			}
			unitSiadapWrapper.removeHarmonizationUnitRelation(currentHU, subUnit, year, justification);
		}

		// ok, so now let's add the relation
		subUnit.addParent(harmonizationUnit, siadapYearConfiguration.getHarmonizationUnitRelations(),
				siadapYearConfiguration.getFirstDay(), null, justification);

	}

	/**
	 * 
	 * @param harmonizationUnit
	 *            the unit which the subUnit is going to connect to. It must be
	 *            an HarmonizationUnit
	 * @param subUnit
	 *            the sub unit to connect
	 * @param year
	 *            the year, which is used to get the accountability type and the
	 *            dates of the accountability
	 * @param justification
	 *            String representation of the reason of this accountability, or
	 *            null
	 */
	@SuppressWarnings("boxing")
	public static void removeHarmonizationUnitRelation(Unit harmonizationUnit, Unit subUnit, int year, String justification)
			throws SiadapException {
		if (!harmonizationUnit.getPartyTypes().contains(PartyType.readBy(SIADAP_HARMONIZATION_UNIT_TYPE))) {
			throw new SiadapException("given harmonizationUnit: " + harmonizationUnit.getPresentationName()
					+ " must have a PartyType of: " + SIADAP_HARMONIZATION_UNIT_TYPE);
		}

		SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);

		if (siadapYearConfiguration.isHarmonizationPeriodOpenNow()) {
			throw new SiadapException("error.cannot.change.harmonization.structure.with.open.harmonization.period");
		}

		// let's see if we have the connection
		UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(subUnit, year);

		Unit currentHU = unitSiadapWrapper.getHarmonizationUnit();
		if (currentHU == null || !currentHU.equals(harmonizationUnit)) {
			return; // nothing to do here, the H.U. is already not this one
		}
		if (currentHU != null) {
			// we need to remove the old relation
			Collection<Accountability> parentAccountabilities =
					subUnit.getParentAccountabilities(siadapYearConfiguration.getLastDayForAccountabilities(),
							siadapYearConfiguration.getLastDayForAccountabilities(),
							siadapYearConfiguration.getHarmonizationUnitRelations());
			if (parentAccountabilities.size() > 1) {
				throw new SiadapException(
						"Too many accoutabilities for a given day. Data inconsistency on an Harm. Unit relation between unit "
								+ currentHU.getPresentationName() + " and " + subUnit.getPresentationName());
			}
			if (parentAccountabilities.size() != 1) {
				throw new SiadapException("Error, we should have one accountability between unit "
						+ currentHU.getPresentationName() + " and " + subUnit.getPresentationName());
			}
			Accountability accToEnd = parentAccountabilities.iterator().next();
			accToEnd.setEndDate(accToEnd.getBeginDate(), justification);
		}

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
		if (configuration == null || configuration.getFirstLevelHarmonizationBegin() == null) {
			return false;
		}

		Interval interval =
				new Interval(SiadapMiscUtilClass.convertDateToBeginOfDay(configuration.getFirstLevelHarmonizationBegin()),
						SiadapMiscUtilClass.convertDateToEndOfDay(configuration.getFirstLevelHarmonizationEnd()));
		return interval.containsNow();
	}

	public boolean isHarmonizationActive() {
		// SiadapYearConfiguration configuration = getConfiguration();
		// LocalDate harmonizationBegin =
		// configuration.getFirstLevelHarmonizationBegin();
		// LocalDate harmonizationEnd =
		// configuration.getFirstLevelHarmonizationEnd();
		// if (!isHarmonizationUnit())
		// return false;

		if (!isSpecialHarmonizationUnit()) {
			// the special harmonization unit has no date constraints,
			// the others have!
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
		if (!isHarmonizationActive()) {
			throw new SiadapException("error.harmonization.period.is.closed");
		}
		if (configuration.getLockHarmonizationOnQuota() && (isSiadap2WithQuotasAboveQuota() || isSiadap3WithQuotasAboveQuota())) {
			throw new SiadapException("error.harmonization.unit.is.not.harmonized.for.quota");
		}
		if (configuration.getLockHarmonizationOnQuotaOutsideOfQuotaUniverses()
				&& (isSiadap2WithoutQuotasAboveQuota() || isSiadap3WithoutQuotasAboveQuota())) {
			throw new SiadapException("error.harmonization.unit.is.not.harmonized.outside.quotas.universes");
		}

		// let's make sure we harmonize everybody now

		finishHarmonizationFor(SiadapUniverse.SIADAP2, currentDate);
		finishHarmonizationFor(SiadapUniverse.SIADAP3, currentDate);
		getConfiguration().addHarmonizationClosedUnits(getUnit());

		// if (configuration.getLockHarmonizationOnQuota() && isAboveQuotas()) {
		// throw new
		// DomainException("error.canOnlyCloseHarmonizationWhenQuotasDoNotExceedValues",
		// DomainException
		// .getResourceFor("resources/SiadapResources"));
		// }

		// for (UnitSiadapWrapper wrapper : getSubHarmonizationUnits()) {
		// if (!wrapper.isHarmonizationFinished()) {
		// throw new
		// DomainException("error.tryingToFinishHarmonizationWithSubHarmonizationOpen",
		// DomainException
		// .getResourceFor("resources/SiadapResources"), getName().getContent(),
		// wrapper.getName().getContent());
		// }
		// }
		// for (UnitSiadapWrapper wrapper : getAllChildUnits()) {
		// if (!wrapper.isWithAllSiadapsFilled(false)) {
		// throw new
		// DomainException("error.tryingToFinishHarmonizationWithSiadapProcessYetToBeEvaluated",
		// DomainException
		// .getResourceFor("resources/SiadapResources"), getName().getContent(),
		// wrapper.getName().getContent());
		// }
		// }
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
		if (!isHarmonizationActive()) {
			throw new SiadapException("error.harmonization.period.is.closed");
		}

		if (!isHarmonizationFinished()) {
			throw new SiadapException("error.harmonization.can.not.reopen.unclosed.harmonization");
		}

		if (!isHarmonizationUnit()) {
			throw new SiadapException("error.cannot.close.harmonization.in.a.non-harmonization.unit");
		}
		LocalDate currentDate = new LocalDate();
		reOpenHarmonizationFor(SiadapUniverse.SIADAP2);
		reOpenHarmonizationFor(SiadapUniverse.SIADAP3);

		// UnitSiadapWrapper topHarmonization = getTopHarmonizationUnit();
		// if (topHarmonization != null &&
		// topHarmonization.isHarmonizationFinished()) {
		// throw new
		// DomainException("error.unableToReopenTopUnitHasAlreadyFinishedHarmonization",
		// DomainException.getResourceFor("resources/SiadapResources"),
		// getName().getContent(), topHarmonization
		// .getName().getContent());
		// }
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
									&& evalUniverse.getHarmonizationAssessment() == false) {
								return true;
							}
							if (evalUniverse.getHarmonizationAssessmentForExcellencyAward() != null
									&& evalUniverse.getHarmonizationAssessmentForExcellencyAward() == false) {
								return true;
							}
						}
						return false;
					}
				}, Collections.singleton(siadapYearConf.getHarmonizationUnitRelations()),
				siadapYearConf.getSiadap2HarmonizationRelation(), siadapYearConf.getSiadap3HarmonizationRelation());
		return listPeopleToReturn;
	}

	public boolean isSiadapStructureTopUnit() {
		Unit siadapStructureTopUnit = getConfiguration().getSiadapStructureTopUnit();
		if (siadapStructureTopUnit == null) {
			return false;
		}
		return siadapStructureTopUnit.equals(getUnit());
	}

	/**
	 * Connects this harmonization unit with the top harmonization unit for the
	 * year which is implicit
	 * 
	 * @param justification
	 *            the justification/reason for the accountability that is going
	 *            to be created
	 * 
	 * @throws IllegalArgumentException
	 *             in case this is not an HarmonizationUnit
	 * @throws SiadapException
	 *             in case we cannot change because the harmonization period is
	 *             already open, or we don't have all the data we need
	 */
	public void connectToTopHarmonizationUnit(String justification) throws IllegalArgumentException, SiadapException {
		Preconditions.checkNotNull(getUnit());
		if (!isHarmonizationUnit()) {
			throw new IllegalArgumentException("this method should only be called in a HarmonizationUnit. Called in unit: "
					+ getUnit().getPresentationName());
		}
		SiadapYearConfiguration configuration = getConfiguration();
		if (configuration.isHarmonizationPeriodOpenNow()) {
			throw new SiadapException("error.shant.make.changes.to.harm.structure.when.harm.is.occurring");
		}

		if (isConnectedToTopUnit(configuration.getHarmonizationUnitRelations())) {
			return;
		} else {
			configuration.getSiadapStructureTopUnit().addChild(getUnit(), configuration.getHarmonizationUnitRelations(),
					configuration.getFirstDay(), configuration.getLastDay(), justification);
		}

	}

	/**
	 * 
	 * @param justification
	 *            the justification/reason for the accountability that is going
	 *            to be created
	 * @throws IllegalArgumentException
	 *             in case this is not an HarmonizationUnit
	 * @throws SiadapException
	 *             in case we cannot change because the harmonization period is
	 *             already open, or we don't have all the data we need
	 * @return true if it leaves 'behind' orphaned units. i.e. units that are
	 *         connected to this H.U.
	 */
	public boolean deactivateHarmonizationUnit(String justification) throws IllegalArgumentException, SiadapException {
		if (!isHarmonizationUnit()) {
			throw new IllegalArgumentException("this method should only be called in a HarmonizationUnit");
		}
		SiadapYearConfiguration configuration = getConfiguration();
		if (configuration.isHarmonizationPeriodOpenNow()) {
			throw new SiadapException("error.shant.make.changes.to.harm.structure.when.harm.is.occurring");
		}
		boolean hasOrphanedUnits = !getSubHarmonizationUnits().isEmpty();

		AccountabilityType harmonizationUnitRelation = configuration.getHarmonizationUnitRelations();

		if (!isConnectedToTopUnit(harmonizationUnitRelation)) {
			return hasOrphanedUnits;
		}

		Unit siadapStructureTopUnit = configuration.getSiadapStructureTopUnit();

		// let's disconnect it
		Collection<Accountability> parentAccountabilities =
				getUnit().getParentAccountabilities(configuration.getLastDayForAccountabilities(),
						configuration.getLastDayForAccountabilities(), harmonizationUnitRelation);
		if (parentAccountabilities.size() > 1) {
			throw new SiadapException(
					"Too many accoutabilities for a given day. Data inconsistency on an Harm. Unit relation between unit "
							+ siadapStructureTopUnit.getPresentationName() + " and " + getUnit().getPresentationName());
		}
		if (parentAccountabilities.size() != 1) {
			throw new SiadapException("Error, we should have one accountability between unit "
					+ siadapStructureTopUnit.getPresentationName() + " and " + getUnit().getPresentationName());
		}
		Accountability accToEnd = parentAccountabilities.iterator().next();

		accToEnd.setEndDate(configuration.getFirstDay().minusDays(1), justification);

		return hasOrphanedUnits;

	}

	// public List<ExcedingQuotaProposal> getExcedingQuotaProposalSuggestions()
	// {
	// return getExcedingQuotaProposalSuggestions(null);
	// }
	//
	// public List<ExcedingQuotaProposal>
	// getExcedingQuotaProposalSuggestions(ExceddingQuotaSuggestionType type) {
	// int year = getYear();
	// List<ExcedingQuotaProposal> list = new
	// ArrayList<ExcedingQuotaProposal>();
	// for (ExcedingQuotaProposal suggestion :
	// getUnit().getExcedingQuotasProposals()) {
	// if (suggestion.getYear() == year && (type == null ||
	// suggestion.getSuggestionType() == type)) {
	// list.add(suggestion);
	// }
	// }
	// return list;
	// }

	// public void addExcedingQuotaProposalSuggestion(Person person,
	// ExceddingQuotaSuggestionType type) {
	// new ExcedingQuotaProposal(getConfiguration(), person, getUnit(), type);
	// }
	//
	// public List<ExcedingQuotaProposal>
	// getOrderedExcedingQuotaProposalSuggestionsForHighEvaluation() {
	// return
	// getOrderedExcedingQuotaProposalSuggestions(ExceddingQuotaSuggestionType.HIGH_SUGGESTION);
	// }
	//
	// public List<ExcedingQuotaProposal>
	// getOrderedExcedingQuotaProposalSuggestionsForExcellencyAward() {
	// return
	// getOrderedExcedingQuotaProposalSuggestions(ExceddingQuotaSuggestionType.EXCELLENCY_SUGGESTION);
	// }
	//
	// public List<ExcedingQuotaProposal>
	// getOrderedExcedingQuotaProposalSuggestions(ExceddingQuotaSuggestionType
	// type) {
	//
	// List<ExcedingQuotaProposal> list =
	// getExcedingQuotaProposalSuggestions(type);
	// Collections.sort(list, new Comparator<ExcedingQuotaProposal>() {
	//
	// @Override
	// public int compare(ExcedingQuotaProposal o1, ExcedingQuotaProposal o2) {
	// return o1.getProposalOrder().compareTo(o2.getProposalOrder());
	// }
	//
	// });
	// return list;
	// }

}
