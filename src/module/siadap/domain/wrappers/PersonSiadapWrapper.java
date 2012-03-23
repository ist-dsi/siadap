/*
 * @(#)PersonSiadapWrapper.java
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.CompetenceType;
import module.siadap.domain.ExceedingQuotaProposal;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.siadap.domain.util.SiadapMiscUtilClass;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;

import org.apache.commons.collections.Predicate;
import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.plugins.remote.domain.exception.RemoteException;

/**
 * 
 * @author João Antunes
 * @author Luis Cruz
 * @author Paulo Abrantes
 * 
 */
public class PersonSiadapWrapper extends PartyWrapper implements Serializable {

    public static final Comparator<PersonSiadapWrapper> PERSON_COMPARATOR_BY_NAME_FALLBACK_YEAR_THEN_PERSON_OID = new Comparator<PersonSiadapWrapper>() {

	@Override
	public int compare(PersonSiadapWrapper o1, PersonSiadapWrapper o2) {
	    int nameComparison = o1.getName().compareTo(o2.getName());
	    if (nameComparison == 0)
		return (o1.getYear() - o2.getYear()) == 0 ? o1.getPerson().getExternalId()
			.compareTo(o2.getPerson().getExternalId()) : o1.getYear() - o2.getYear();

	    return nameComparison;
	}
    };

    private Person person;

    /*
     * We need these two Booleans because of the render problems while writing
     * to a Bean
     */
    private Boolean harmonizationCurrentAssessmentForSIADAP3;
    private Boolean harmonizationCurrentAssessmentForSIADAP2;

    private Boolean harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2;
    private Boolean harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3;

    private Boolean validationCurrentAssessmentForSIADAP3;
    private Boolean validationCurrentAssessmentForSIADAP2;

    private Boolean validationCurrentAssessmentForExcellencyAwardForSIADAP2;
    private Boolean validationCurrentAssessmentForExcellencyAwardForSIADAP3;

    private BigDecimal validationClassificationForSIADAP3;
    private BigDecimal validationClassificationForSIADAP2;

    private BigDecimal evaluatorClassificationForSIADAP3;
    private BigDecimal evaluatorClassificationForSIADAP2;

    public PersonSiadapWrapper(Person person, int year) {
	super(year);
	this.person = person;
	//initing the harmonization booleans
	if (person != null)
	    initIntermediateValues();

    }

    /**
     * Inits the intermediate values used for display purposes. More
     * specifically: {@link #harmonizationCurrentAssessmentForSIADAP2},
     * {@link #harmonizationCurrentAssessmentForSIADAP3}, and
     * {@link #exceedingQuotaPriorityNumber}
     */
    private void initIntermediateValues() {
	if (getSiadap() == null) {
	    this.harmonizationCurrentAssessmentForSIADAP2 = null;
	    this.harmonizationCurrentAssessmentForSIADAP3 = null;
	} else {
	    SiadapEvaluationUniverse siadapEvaluationUniverseForSIADAP3 = getSiadap()
		    .getSiadapEvaluationUniverseForSiadapUniverse(SiadapUniverse.SIADAP3);
	    SiadapEvaluationUniverse siadapEvaluationUniverseForSIADAP2 = getSiadap()
		    .getSiadapEvaluationUniverseForSiadapUniverse(SiadapUniverse.SIADAP2);
	    if (siadapEvaluationUniverseForSIADAP2 == null) {
		this.harmonizationCurrentAssessmentForSIADAP2 = null;
		setValidationCurrentAssessmentForSIADAP2(null);
		setValidationCurrentAssessmentForExcellencyAwardForSIADAP2(null);
		setValidationClassificationForSIADAP2(null);
		setEvaluatorClassificationForSIADAP2(null);
	    } else {
		setValidationCurrentAssessmentForSIADAP2(siadapEvaluationUniverseForSIADAP2.getCcaAssessment());
		setValidationCurrentAssessmentForExcellencyAwardForSIADAP2(siadapEvaluationUniverseForSIADAP2
			.getCcaClassificationExcellencyAward());
		setValidationClassificationForSIADAP2(siadapEvaluationUniverseForSIADAP2.getCcaClassification());
		setEvaluatorClassificationForSIADAP2(siadapEvaluationUniverseForSIADAP2.getEvaluatorClassification());
		this.harmonizationCurrentAssessmentForSIADAP2 = siadapEvaluationUniverseForSIADAP2.getHarmonizationAssessment();
		this.harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2 = siadapEvaluationUniverseForSIADAP2
			.getHarmonizationAssessmentForExcellencyAward();
	    }

	    if (siadapEvaluationUniverseForSIADAP3 == null) {
		this.harmonizationCurrentAssessmentForSIADAP3 = null;
		setValidationCurrentAssessmentForSIADAP3(null);
		setValidationCurrentAssessmentForExcellencyAwardForSIADAP3(null);
		setValidationClassificationForSIADAP3(null);
		setEvaluatorClassificationForSIADAP3(null);
	    } else {
		setValidationCurrentAssessmentForSIADAP3(siadapEvaluationUniverseForSIADAP3.getCcaAssessment());
		setValidationCurrentAssessmentForExcellencyAwardForSIADAP3(siadapEvaluationUniverseForSIADAP3
			.getCcaClassificationExcellencyAward());
		setValidationClassificationForSIADAP3(siadapEvaluationUniverseForSIADAP3.getCcaClassification());
		setEvaluatorClassificationForSIADAP3(siadapEvaluationUniverseForSIADAP3.getEvaluatorClassification());
		this.harmonizationCurrentAssessmentForSIADAP3 = siadapEvaluationUniverseForSIADAP3.getHarmonizationAssessment();
		this.harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3 = siadapEvaluationUniverseForSIADAP3
			.getHarmonizationAssessmentForExcellencyAward();
	    }
	}
    }

    public Boolean getValidationCurrentAssessmentForSIADAP3() {
	return validationCurrentAssessmentForSIADAP3;
    }

    public void setValidationCurrentAssessmentForSIADAP3(Boolean validationCurrentAssessmentForSIADAP3) {
	this.validationCurrentAssessmentForSIADAP3 = validationCurrentAssessmentForSIADAP3;
    }

    public Boolean getValidationCurrentAssessmentForSIADAP2() {
	return validationCurrentAssessmentForSIADAP2;
    }

    public void setValidationCurrentAssessmentForSIADAP2(Boolean validationCurrentAssessmentForSIADAP2) {
	this.validationCurrentAssessmentForSIADAP2 = validationCurrentAssessmentForSIADAP2;
    }

    public Boolean getValidationCurrentAssessmentForExcellencyAwardForSIADAP2() {
	return validationCurrentAssessmentForExcellencyAwardForSIADAP2;
    }

    public void setValidationCurrentAssessmentForExcellencyAwardForSIADAP2(
	    Boolean validationCurrentAssessmentForExcellencyAwardForSIADAP2) {
	this.validationCurrentAssessmentForExcellencyAwardForSIADAP2 = validationCurrentAssessmentForExcellencyAwardForSIADAP2;
    }

    public Boolean getValidationCurrentAssessmentForExcellencyAwardForSIADAP3() {
	return validationCurrentAssessmentForExcellencyAwardForSIADAP3;
    }

    public void setValidationCurrentAssessmentForExcellencyAwardForSIADAP3(
	    Boolean validationCurrentAssessmentForExcellencyAwardForSIADAP3) {
	this.validationCurrentAssessmentForExcellencyAwardForSIADAP3 = validationCurrentAssessmentForExcellencyAwardForSIADAP3;
    }

    public BigDecimal getValidationClassificationForSIADAP3() {
	return validationClassificationForSIADAP3;
    }

    public void setValidationClassificationForSIADAP3(BigDecimal validationClassificationForSIADAP3) {
	this.validationClassificationForSIADAP3 = validationClassificationForSIADAP3;
    }

    public BigDecimal getValidationClassificationForSIADAP2() {
	return validationClassificationForSIADAP2;
    }

    public void setValidationClassificationForSIADAP2(BigDecimal validationClassificationForSIADAP2) {
	this.validationClassificationForSIADAP2 = validationClassificationForSIADAP2;
    }

    public BigDecimal getFinalClassificationForSIADAP2() {
	if (getValidationClassificationForSIADAP2() != null) {
	    return getValidationClassificationForSIADAP2();
	}
	return getEvaluatorClassificationForSIADAP2();
    }

    public BigDecimal getFinalClassificationForSIADAP3() {
	if (getValidationClassificationForSIADAP3() != null) {
	    return getValidationClassificationForSIADAP3();
	}
	return getEvaluatorClassificationForSIADAP3();
    }

    public Person getPerson() {
	return this.person;
    }

    public void setPerson(Person person) {
	this.person = person;
    }

    @Override
    protected Party getParty() {
	return this.person;
    }

    public boolean isEvaluationStarted() {
	return getSiadap() != null;
    }

    /**
     * 
     * @param universe
     *            the universe to consider the validation for
     * @return true if it hasn't been validated and it has been harmonized,
     *         false otherwise
     */
    private boolean isAbleToBeValidated(SiadapUniverse universe) {
	SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse = getSiadap()
		.getSiadapEvaluationUniverseForSiadapUniverse(universe);
	if (siadapEvaluationUniverseForSiadapUniverse == null)
	    return false;
	return siadapEvaluationUniverseForSiadapUniverse.getValidationDate() == null
		&& siadapEvaluationUniverseForSiadapUniverse.getHarmonizationDate() != null;

    }

    public boolean isSiadap2AbleToBeValidated() {
	return isAbleToBeValidated(SiadapUniverse.SIADAP2);
    }

    public boolean isSiadap3AbleToBeValidated() {
	return isAbleToBeValidated(SiadapUniverse.SIADAP3);
    }

    public Siadap getSiadap() {
	return getConfiguration() == null ? null : getConfiguration().getSiadapFor(getPerson(), getYear());
    }

    /**
     * @return a {@link SiadapProcessStateEnum} which represents the current
     *         state of the SIADAP process
     */
    public SiadapProcessStateEnum getCurrentProcessState() {
	return SiadapProcessStateEnum.getState(getSiadap());
    }

    /**
     * @return a string with the explanation of what should be done next, based
     *         on the user, if he is an evaluator or an evaluated
     */
    public String getNextStep() {
	return SiadapProcessStateEnum.getNextStep(getSiadap(), UserView.getCurrentUser());

    }

    public boolean isWithSkippedEval(SiadapUniverse siadapUniverse) {
	if (getSiadap() == null || getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse) == null)
	    return false;
	return getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse).isWithSkippedEvaluation();
    }

    public boolean getWithoutExcellencyAwardForSiadap2() {
	return isWithoutExcellencyAwardFor(SiadapUniverse.SIADAP2);
    }

    public boolean getWithoutExcellencyAwardForSiadap3() {
	return isWithoutExcellencyAwardFor(SiadapUniverse.SIADAP3);
    }

    private boolean isWithoutExcellencyAwardFor(SiadapUniverse siadapUniverse) {
	SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse = getSiadap()
		.getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
	if (siadapEvaluationUniverseForSiadapUniverse == null)
	    return true;
	return siadapEvaluationUniverseForSiadapUniverse.getEvaluatorClassificationExcellencyAward() == null
		|| !siadapEvaluationUniverseForSiadapUniverse.getEvaluatorClassificationExcellencyAward();
    }

    public boolean getWithSkippedEvalForSiadap2() {
	return isWithSkippedEval(SiadapUniverse.SIADAP2);
    }

    public boolean getWithSkippedEvalForSiadap3() {
	return isWithSkippedEval(SiadapUniverse.SIADAP3);
    }

    /**
     * 
     * @return true if the current user is able to see the details of the
     *         process, false otherwise
     */
    public boolean isCurrentUserAbleToSeeDetails() {
	User currentUser = UserView.getCurrentUser();
	SiadapYearConfiguration configuration = getConfiguration();
	if (getSiadap().getProcess().isAccessibleToCurrentUser()) {
	    if (isResponsibleForHarmonization(currentUser.getPerson())) {
		return true;
	    }
	    if (configuration.getCcaMembers().contains(currentUser.getPerson()))
		return true;
	    if (configuration.getHomologationMembers().contains(currentUser.getPerson()))
		return true;
	    if (getEvaluator().getPerson().equals(currentUser.getPerson()) || getPerson().equals(currentUser.getPerson()))
		return true;
	}
	return false;
    }

    private String emailAddress;

    /**
     * 
     * @return true if the email is defined or if we just got a remote expcetion
     *         trying to get it from fenix. False if it is actually null or
     *         empty
     */
    public boolean isEmailDefined() {
	if (getEmailAddress() == null || getEmailAddress().equalsIgnoreCase("")) {
	    try {
		String fetchedEmail = getPerson().getRemotePerson().getEmailForSendingEmails();
		if (fetchedEmail == null || fetchedEmail.equalsIgnoreCase("")) {
		    return false;
		}
		setEmailAddress(fetchedEmail);
		return true;
	    } catch (RemoteException e) {
		return true;
	    }

	} else
	    return true;

    }

    public SiadapUniverse getDefaultSiadapUniverse() {
	if (getSiadap() == null)
	    return null;
	return getSiadap().getDefaultSiadapUniverse();
    }

    public PersonSiadapWrapper getEvaluator() {
	Person evaluator = null;

	Collection<Person> possibleCustomEvaluator = getParentPersons(getConfiguration().getEvaluationRelation());

	if (!possibleCustomEvaluator.isEmpty()) {
	    evaluator = possibleCustomEvaluator.iterator().next();
	} else {
	    if (getWorkingUnit() != null) {
		Collection<Unit> workingPlaces = getParentUnits(getConfiguration().getWorkingRelation(), getConfiguration()
			.getWorkingRelationWithNoQuota());
		Unit workingUnit = workingPlaces.iterator().next();
		Collection<Person> childPersons = workingUnit.getChildPersons(getConfiguration().getEvaluationRelation());
		if (!childPersons.isEmpty()) {
		    evaluator = childPersons.iterator().next();
		}
	    }
	}

	return evaluator != null ? new PersonSiadapWrapper(evaluator, getYear()) : null;
    }

    public Collection<UnitSiadapWrapper> getHarmozationUnits() {
	return getHarmozationUnits(true);
    }

    public boolean hasPendingActions() {
	Siadap siadap = getSiadap();
	if (siadap == null) {
	    if (isCurrentUserAbleToCreateProcess()) {
		return true;
	    }
	} else {
	    SiadapProcess process = siadap.getProcess();
	    if (process.isAccessibleToCurrentUser()) {
		return siadap.getProcess().hasAnyAvailableActivitity();
	    }
	}
	return false;
    }

    public int getNrPersonsWithUnreadComments() {
	int counter = 0;
	for (PersonSiadapWrapper personSiadapWrapper : getPeopleToEvaluate()) {
	    if (personSiadapWrapper.getHasUnreadComments())
		counter++;
	}
	return counter;

    }

    public int getNrPendingProcessActions() {
	int counterPendingActions = 0;
	ArrayList<PersonSiadapWrapper> personSiadapWrappers = SiadapRootModule.getInstance().getAssociatedSiadaps(getPerson(),
		getYear(), false);
	for (PersonSiadapWrapper personSiadapWrapper : personSiadapWrappers) {
	    if (personSiadapWrapper.hasPendingActions())
		counterPendingActions++;
	}
	return counterPendingActions;
    }

    public String getTotalQualitativeEvaluationScoring(SiadapUniverse siadapUniverse) {
	return getTotalQualitativeEvaluationScoringObject(siadapUniverse).getLocalizedName();
    }

    public SiadapGlobalEvaluation getTotalQualitativeEvaluationScoringObject(SiadapUniverse siadapUniverse) {
	boolean excellencyGiven = false;
	Siadap siadap = getSiadap();
	if (siadap == null)
	    return SiadapGlobalEvaluation.NONEXISTING;
	SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse = siadap
		.getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
	if (siadapEvaluationUniverseForSiadapUniverse == null)
	    return SiadapGlobalEvaluation.NONEXISTING;
	else
	    excellencyGiven = siadapEvaluationUniverseForSiadapUniverse.hasExcellencyAwarded();
	if (!siadap.isEvaluationDone(siadapUniverse) && !siadapEvaluationUniverseForSiadapUniverse.isWithSkippedEvaluation()) {
	    return SiadapGlobalEvaluation.NONEXISTING;
	}
	if (siadapEvaluationUniverseForSiadapUniverse.isWithSkippedEvaluation())
	    return SiadapGlobalEvaluation.WITHSKIPPEDEVAL;

	return SiadapGlobalEvaluation.getGlobalEvaluation(siadapEvaluationUniverseForSiadapUniverse.getTotalEvaluationScoring(),
		excellencyGiven);
    }

    public String getTotalQualitativeEvaluationScoringSiadap2() {
	return getTotalQualitativeEvaluationScoring(SiadapUniverse.SIADAP2);
    }

    public String getTotalQualitativeEvaluationScoringSiadap3() {
	return getTotalQualitativeEvaluationScoring(SiadapUniverse.SIADAP3);
    }

    public Collection<UnitSiadapWrapper> getHarmozationUnits(boolean skipClosedAccountabilities) {
	List<UnitSiadapWrapper> units = new ArrayList<UnitSiadapWrapper>();

	for (Unit unit : getParentUnits(getConfiguration().getHarmonizationResponsibleRelation())) {
	    units.add(new UnitSiadapWrapper(unit, getYear()));
	}
	return units;
    }

    public Unit getUnitWhereIsHarmonized(SiadapUniverse siadapUniverse) {
	if (siadapUniverse == null)
	    return null;
	List<Unit> parentUnits = getParentUnits(getParty(), siadapUniverse.getHarmonizationRelation(getConfiguration()));
	if (parentUnits.isEmpty())
	    return null;
	if (parentUnits.size() > 1) {
	    throw new SiadapException("inconsistent.harmonization.units");

	} else {
	    UnitSiadapWrapper unitWrapper = new UnitSiadapWrapper(parentUnits.get(0), getYear());
	    if (unitWrapper.isHarmonizationUnit())
		return unitWrapper.getUnit();
	    else {
		return unitWrapper.getHarmonizationUnit();
	    }
	}
    }

    public boolean isAccessibleToCurrentUser() {
	Siadap siadap = getSiadap();
	if (siadap == null) {
	    return false;
	}
	return siadap.getProcess().isAccessibleToCurrentUser();
    }

    public boolean isCurrentUserAbleToEvaluate() {
	return getEvaluator() != null && getEvaluator().getPerson() == UserView.getCurrentUser().getPerson();
    }

    public boolean isCurrentUserAbleToCreateProcess() {
	return getSiadap() == null && isCurrentUserAbleToEvaluate();
    }

    public boolean isCurrentUserAbleToSeeAutoEvaluationDetails() {
	User currentUser = UserView.getCurrentUser();
	if (!isCurrentUserAbleToSeeDetails())
	    return false;
	if (getSiadap().getEvaluated().equals(currentUser.getPerson()))
	    return true;
	if (getSiadap().isAutoEvaliationDone())
	    return true;
	return false;
    }

    public boolean isCurrentUserAbleToSeeEvaluationDetails() {
	User currentUser = UserView.getCurrentUser();
	if (!isCurrentUserAbleToSeeDetails())
	    return false;
	if (getSiadap().getEvaluator().getPerson().equals(currentUser.getPerson()))
	    return true;
	if (getSiadap().isDefaultEvaluationDone() && getSiadap().getEvaluated().equals(currentUser.getPerson())
		&& getSiadap().getRequestedAcknowledegeValidationDate() != null)
	    return true;
	if (getSiadap().isDefaultEvaluationDone() && isResponsibleForHarmonization(currentUser.getPerson()))
	    return true;
	if (getSiadap().isDefaultEvaluationDone() && getConfiguration().isCurrentUserMemberOfCCA())
	    return true;
	return false;
    }

    //TODO joantune: only here temporarily, probably should be removed
    public BigDecimal getTotalEvaluationScoring() {
	return getTotalEvaluationScoring(getSiadap().getDefaultSiadapUniverse());
    }

    public BigDecimal getTotalEvaluationScoring(SiadapUniverse siadapUniverse) {
	Siadap siadap = getSiadap();
	SiadapEvaluationUniverse siadapEvaluationUniverse = siadap.getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
	if (siadap == null || siadapEvaluationUniverse == null || !siadap.isEvaluationDone(siadapUniverse))
	    return null;

	return siadapEvaluationUniverse.getTotalEvaluationScoring();
    }

    public BigDecimal getTotalEvaluationScoringSiadap2() {
	return getTotalEvaluationScoring(SiadapUniverse.SIADAP2);
    }

    public BigDecimal getTotalEvaluationScoringSiadap3() {
	return getTotalEvaluationScoring(SiadapUniverse.SIADAP3);
    }

    public UnitSiadapWrapper getWorkingUnit() {
	Collection<Unit> parentUnits = getParentUnits(getConfiguration().getWorkingRelation(), getConfiguration()
		.getWorkingRelationWithNoQuota());
	return parentUnits.isEmpty() ? null : new UnitSiadapWrapper(parentUnits.iterator().next(), getConfiguration().getYear());
    }

    public boolean isQuotaAware() {
	return getParentUnits(getConfiguration().getWorkingRelationWithNoQuota()).isEmpty() ? true : false;
    }

    public boolean isResponsibleForHarmonization(Person accessor) {
	return getWorkingUnit().isPersonResponsibleForHarmonization(accessor);
    }

    public Set<PersonSiadapWrapper> getPeopleToEvaluate() {
	//if no configuration has been set for the current year, we retrieve null!
	if (SiadapYearConfiguration.getSiadapYearConfiguration(Integer.valueOf(getYear())) == null)
	    return null;
	Set<PersonSiadapWrapper> people = new HashSet<PersonSiadapWrapper>();
	final PersonSiadapWrapper evaluator = new PersonSiadapWrapper(getPerson(), getYear());
	final AccountabilityType evaluationRelation = getConfiguration().getEvaluationRelation();

	for (Person person : evaluator.getChildPersons(evaluationRelation)) {
	    people.add(new PersonSiadapWrapper(person, getYear()));
	}
	for (Unit unit : evaluator.getParentUnits(evaluationRelation)) {
	    people.addAll(new UnitSiadapWrapper(unit, getYear()).getUnitEmployees(new Predicate() {

		@Override
		public boolean evaluate(Object arg0) {
		    PersonSiadapWrapper wrapper = (PersonSiadapWrapper) arg0;
		    PersonSiadapWrapper evaluatorWrapper = wrapper.getEvaluator();
		    return evaluatorWrapper != null && evaluatorWrapper.equals(evaluator);
		}

	    }));
	}
	return people;

    }

    public boolean getHasUnreadComments() {
	if (getSiadap() == null || getSiadap().getProcess() == null)
	    return false;
	if (getSiadap().getProcess().getUnreadCommentsForCurrentUser().isEmpty())
	    return false;
	else
	    return true;
    }

    public Set<Siadap> getAllSiadaps() {
	Set<Siadap> processes = new TreeSet<Siadap>(new Comparator<Siadap>() {

	    @Override
	    public int compare(Siadap o1, Siadap o2) {
		return o1.getYear().compareTo(o2.getYear());
	    }

	});

	processes.addAll(getPerson().getSiadapsAsEvaluated());
	return processes;
    }

    public String getName() {
	if (getPerson() == null || getPerson().getName() == null) {
	    throw new DomainException("Person or person's name not defined");
	}
	return getPerson().getName();
    }

    @Override
    public int hashCode() {
	return getPerson().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof PersonSiadapWrapper) {
	    return ((PersonSiadapWrapper) obj).getPerson() == getPerson();
	}
	return false;
    }

    /**
     * Verifies that the given date is indeed within the {@link #getYear()}
     * year. If it isn't, it will throw a DomainException
     * 
     * @param dateToVerify
     *            the date to verify
     */
    private void verifyDate(LocalDate dateToVerify) {
	if (dateToVerify.getYear() != getYear()) {
	    throw new DomainException("manage.workingUnitOrEvaluator.invalid.date",
		    DomainException.getResourceFor("resources/SiadapResources"), String.valueOf(getYear()));
	}
    }

    @Service
    public void changeWorkingUnitTo(Unit unit, Boolean withQuotas, LocalDate dateOfChange) {
	verifyDate(dateOfChange);
	//if he has already submitted the eval objectives and competences for the evaluated person, we shouldn't do this
	if (getSiadap().getRequestedAcknowledgeDate() != null)
	    throw new SiadapException("error.changing.working.unit.already.submitted.for.acknowledgement");
	SiadapYearConfiguration configuration = getConfiguration();
	for (Accountability accountability : getParentAccountabilityTypes(configuration.getWorkingRelation(),
		configuration.getWorkingRelationWithNoQuota())) {
	    if (accountability.isActiveNow()) {
		accountability.editDates(accountability.getBeginDate(), dateOfChange);
	    }
	}
	unit.addChild(getPerson(),
		withQuotas ? configuration.getWorkingRelation() : configuration.getWorkingRelationWithNoQuota(), dateOfChange,
		null);
    }

    // use the version that allows a date instead (may not apply to all of the
    // cases. If so, please delete the Deprecated tag)
    @Deprecated
    public void changeWorkingUnitTo(Unit unit, Boolean withQuotas) {
	changeWorkingUnitTo(unit, withQuotas, new LocalDate());
    }

    // use the version that allows a date instead (may not apply to all of the
    // cases. If so, please delete the Deprecated tag)
    @Deprecated
    public void changeEvaluatorTo(Person newEvaluator) {
	changeEvaluatorTo(newEvaluator, new LocalDate());
    }

    @Service
    public void changeEvaluatorTo(Person newEvaluator, LocalDate dateOfChange) {
	verifyDate(dateOfChange);
	LocalDate startOfYear = new LocalDate(dateOfChange.getYear(), 1, 1);
	LocalDate endOfYear = new LocalDate(dateOfChange.getYear(), 12, 31);
	SiadapYearConfiguration configuration = getConfiguration();
	AccountabilityType evaluationRelation = configuration.getEvaluationRelation();
	boolean needToAddAcc = true;
	for (Accountability accountability : getParentAccountabilityTypes(evaluationRelation)) {
	    if (accountability.isActiveNow() && accountability.getParent() instanceof Person
		    && accountability.getChild() instanceof Person) {
		//let's close it if we have a different person here
		if (!accountability.getParent().equals(newEvaluator)) {
		    accountability.editDates(accountability.getBeginDate(), dateOfChange);
		} else {
		    needToAddAcc = false;
		}
	    }
	}
	if (needToAddAcc) {
	    //let's
	    newEvaluator.addChild(getPerson(), evaluationRelation, dateOfChange, null);

	}

    }

    /**
     * 
     * @param unit
     *            the unit to consider the harmonization for. It must be a
     *            harmonization unit
     * @return the {@link SiadapUniverse} for which he is being harmonized in
     *         the given unit
     */
    public SiadapUniverse getSiadapUniverseWhichIsBeingHarmonized(Unit unit) {
	//let's try to find this person ('ourselves') directly, if not let's descend if we are an harmonization unit 
	UnitSiadapWrapper unitWrapper = new UnitSiadapWrapper(unit, getYear());
	if (!unitWrapper.isHarmonizationUnit()) {
	    throw new IllegalArgumentException("you're doing it wrong :D harmonization units only");
	}
	return getSiadapUniverseInGivenUnit(unit);
    }

    private SiadapUniverse getSiadapUniverseInGivenUnit(Unit unit) {
	SiadapYearConfiguration siadapYearConfiguration = getConfiguration();
	AccountabilityType siadap2HarmonizationRelation = siadapYearConfiguration.getSiadap2HarmonizationRelation();
	AccountabilityType siadap3HarmonizationRelation = siadapYearConfiguration.getSiadap3HarmonizationRelation();
	UnitSiadapWrapper unitWrapper = new UnitSiadapWrapper(unit, getYear());
	List<Accountability> childAccountabilities = unitWrapper.getChildAccountabilityTypes(siadap2HarmonizationRelation,
		siadap3HarmonizationRelation);
	for (Accountability acc : childAccountabilities) {
	    if (acc.getChild().equals(getPerson())) {

		if (acc.hasAccountabilityType(siadap2HarmonizationRelation))
		    return SiadapUniverse.SIADAP2;
		if (acc.hasAccountabilityType(siadap3HarmonizationRelation))
		    return SiadapUniverse.SIADAP3;

	    }
	}
	//let's descend
	SiadapUniverse siadapUniverseToReturn = null;
	for (Unit childUnit : unitWrapper.getChildUnits(siadapYearConfiguration.getHarmonizationUnitRelations())) {
	    SiadapUniverse siadapUniverseInGivenUnit = getSiadapUniverseInGivenUnit(childUnit);
	    if (siadapUniverseInGivenUnit != null)
		siadapUniverseToReturn = siadapUniverseInGivenUnit;
	}
	return siadapUniverseToReturn;

    }

    public boolean isCustomEvaluatorDefined() {
	return !getParentPersons(getConfiguration().getEvaluationRelation()).isEmpty();
    }

    @Service
    public void removeCustomEvaluator() {
	AccountabilityType evaluationRelation = getConfiguration().getEvaluationRelation();
	for (Accountability accountability : getParentAccountabilityTypes(evaluationRelation)) {
	    if (accountability.isActiveNow() && accountability.getChild() instanceof Person
		    && accountability.getParent() instanceof Person) {
		//ok, so we have the acc.
		LocalDate dateToEndTheAcc = new LocalDate();
		if (accountability.getBeginDate().getYear() == getYear()) {
		    if (dateToEndTheAcc.isBefore(accountability.getBeginDate().plusDays(1))) {
			//then we actually have to 'delete' it
			accountability.delete();
		    }
		    dateToEndTheAcc = accountability.getBeginDate().plusDays(1);
		} else {
		    //let's close it on the last day of the previous year
		    dateToEndTheAcc = SiadapMiscUtilClass.lastDayOfYear(getYear() - 1);
		}
		if (!accountability.isErased()) {
		    accountability.setEndDate(dateToEndTheAcc);
		}
	    }
	}
    }

    @Service
    public void removeHarmonizationAssessments(SiadapUniverse siadapUniverse, Unit harmonizationUnit) {
	if (getSiadap() == null || harmonizationUnit == null)
	    throw new SiadapException("error.invalid.data");

	SiadapEvaluationUniverse evaluationUniverse = getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
	if ((evaluationUniverse.getHarmonizationAssessment() != null && !evaluationUniverse.getHarmonizationAssessment())
		|| (evaluationUniverse.getHarmonizationAssessmentForExcellencyAward() != null && !evaluationUniverse
			.getHarmonizationAssessmentForExcellencyAward())) {
	    //if we had a No on the harmonizationAssessment or in the regular one we might have an ExceedingQuotaProposal
	    //so let's check if it is so, and if it is, remove it and adjust the priority numbers of the rest of them
	    ExceedingQuotaProposal quotaProposalFor = ExceedingQuotaProposal.getQuotaProposalFor(harmonizationUnit, getYear(),
		    getPerson(), siadapUniverse, isQuotaAware());
	    if (quotaProposalFor != null)
		quotaProposalFor.remove();

	}
	evaluationUniverse.removeHarmonizationAssessments();
    }

    public String getCareerName() {
	if (getDefaultCompetenceTypeObject() == null)
	    return "";
	return getDefaultCompetenceTypeObject().getName();
    }

    public CompetenceType getDefaultCompetenceTypeObject() {
	if (getSiadap() == null || getSiadap().getDefaultSiadapEvaluationUniverse() == null
		|| getSiadap().getDefaultSiadapEvaluationUniverse().getCompetenceSlashCareerType() == null)
	    return null;
	return getSiadap().getDefaultSiadapEvaluationUniverse().getCompetenceSlashCareerType();

    }

    public Boolean getHomologationDone() {
	Siadap siadap = getSiadap();
	return siadap != null ? siadap.isHomologated() : null;
    }

    public boolean isCCAMember() {
	return SiadapRootModule.getInstance().getSiadapCCAGroup().isMember(getPerson().getUser());
    }

    public boolean isHomologationMember() {
	return getConfiguration().getHomologationMembers().contains(getPerson());
    }

    public boolean isHarmonizationPeriodOpen() {
	return UnitSiadapWrapper.isHarmonizationPeriodOpen(getConfiguration());
    }

    public Set<Accountability> getAccountabilitiesHistory() {
	Person person = getPerson();
	Set<Accountability> history = new TreeSet<Accountability>(new Comparator<Accountability>() {

	    @Override
	    public int compare(Accountability o1, Accountability o2) {
		int compareBegin = o1.getBeginDate().compareTo(o2.getBeginDate());
		LocalDate endDate = o1.getEndDate();
		LocalDate endDate2 = o2.getEndDate();
		return compareBegin != 0 ? compareBegin : endDate != null && endDate2 != null ? o1.getEndDate().compareTo(
			o2.getEndDate()) : endDate == null && endDate2 != null ? 1 : endDate2 == null && endDate != null ? -1
			: o1.getExternalId().compareTo(o2.getExternalId());
	    }

	});
	int year = getYear();
	for (SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year); configuration != null; configuration = SiadapYearConfiguration
		.getSiadapYearConfiguration(--year)) {
	    history.addAll(person.getParentAccountabilities(configuration.getWorkingRelation(),
		    configuration.getWorkingRelationWithNoQuota()));
	}

	return history;
    }

    public void setEmailAddress(String emailAddress) {
	this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
	return emailAddress;
    }

    /**
     * @see #changeDefaultUniverseTo(SiadapUniverse, LocalDate, boolean) - it's
     *      equal but with a force of false
     */
    public void changeDefaultUniverseTo(SiadapUniverse siadapUniverseToChangeTo, LocalDate dateOfChange) {
	changeDefaultUniverseTo(siadapUniverseToChangeTo, dateOfChange, false);
    }

    /**
     * 
     * @param siadapUniverseToChangeTo
     *            the default {@link SiadapUniverse} to change to
     * @param dateOfChange
     *            the date to change
     * @param forceChange
     *            if set to true, it will allow to make the change even if the
     *            evaluation has been harmonized, but NOT if it has been
     *            validated
     */
    @Service
    public void changeDefaultUniverseTo(SiadapUniverse siadapUniverseToChangeTo, LocalDate dateOfChange, boolean forceChange) {
	SiadapUniverse defaultSiadapUniverse = getSiadap().getDefaultSiadapUniverse();

	verifyDate(dateOfChange);
	if (!forceChange) {
	    //let's check if we have a closed harmonization, if so, we shouldn't allow the change
	    Unit unitWhereIsHarmonized = getUnitWhereIsHarmonized(defaultSiadapUniverse);
	    if (new UnitSiadapWrapper(unitWhereIsHarmonized, getYear()).isHarmonizationFinished())
		throw new SiadapException("error.cant.change.siadap.universe.because.it.has.closed.harmonization");
	} else {
	    //let's make sure it is not validated
	    if (getSiadap().getDefaultSiadapEvaluationUniverse() != null) {
		if (getSiadap().getDefaultSiadapEvaluationUniverse().getValidationDate() != null)
		    throw new SiadapException("error.cant.change.siadap.universe.because.it.has.closed.validation");
	    }
	}

	//let's also change the Harmonization relation
	Accountability retrieveDefaultHarmAccForGivenSiadapUniverse = retrieveDefaultHarmAccForGivenSiadapUniverse(getSiadap()
		.getDefaultSiadapUniverse());
	if (retrieveDefaultHarmAccForGivenSiadapUniverse != null) {
	    //if we had one, let's close it
	    retrieveDefaultHarmAccForGivenSiadapUniverse.setEndDate(dateOfChange);

	    //and now let's create a new one
	    retrieveDefaultHarmAccForGivenSiadapUniverse.getParent().addChild(getPerson(),
		    siadapUniverseToChangeTo.getHarmonizationRelation(getYear()), dateOfChange, null);
	}

	getSiadap().setDefaultSiadapUniverse(siadapUniverseToChangeTo);
    }

    /**
     * 
     * @param siadapUniverse
     *            the siadapUniverse to
     * @return the {@link Accountability} responsible for giving the hint on the
     *         HarmUnit
     */
    private Accountability retrieveDefaultHarmAccForGivenSiadapUniverse(SiadapUniverse siadapUniverse) {
	if (siadapUniverse == null) {
	    return null;
	}
	AccountabilityType harmonizationRelation = siadapUniverse.getHarmonizationRelation(getConfiguration());

	List<Accountability> parentAccountabilityTypes = getParentAccountabilityTypes(harmonizationRelation);
	//if it retrieved more than one, it actually shouldn't, as it shows that we are being evaluated with the same universe
	if (parentAccountabilityTypes.size() > 1)
	    throw new SiadapException("inconsistent.harmonization.units");
	if (parentAccountabilityTypes.size() == 1)
	    return parentAccountabilityTypes.get(0);

	return null;

    }

    //    private boolean hasHarmonizationAssessment(SiadapUniverse siadapUniverse) {
    //	return getHarmonizationCurrentAssessmentFor(getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse)) != null;
    //    }
    //
    //    public boolean getHarmonizationAssessmentForSIADAP2Defined() {
    //	return hasHarmonizationAssessment(SiadapUniverse.SIADAP2);
    //
    //    }
    //
    //    public boolean getHarmonizationAssessmentForSIADAP3Defined() {
    //	return hasHarmonizationAssessment(SiadapUniverse.SIADAP3);
    //
    //    }

    public Boolean getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP2() {
	return harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2;
    }

    public void setHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP2(
	    Boolean harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2) {
	this.harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2 = harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2;
    }

    public Boolean getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP3() {
	return harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3;
    }

    public void setHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP3(
	    Boolean harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3) {
	this.harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3 = harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3;
    }

    protected Boolean getHarmonizationCurrentAssessmentFor(SiadapUniverse siadapUniverse) {
	return getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse).getHarmonizationAssessment();
    }

    protected Boolean getHarmonizationCurrentExcellencyAssessmentFor(SiadapUniverse siadapUniverse) {
	return getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse)
		.getHarmonizationAssessmentForExcellencyAward();
    }

    public Boolean getHarmonizationCurrentAssessmentForSIADAP2() {
	return this.harmonizationCurrentAssessmentForSIADAP2;
    }

    public Boolean getHarmonizationCurrentAssessmentForSIADAP3() {
	return this.harmonizationCurrentAssessmentForSIADAP3;
    }

    private boolean isAbleToRemoveAssessmentsFor(SiadapUniverse siadapUniverse) {
	SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse = getSiadap()
		.getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);

	if (siadapEvaluationUniverseForSiadapUniverse == null)
	    return false;
	return isHarmonizationPeriodOpen()
		&& siadapEvaluationUniverseForSiadapUniverse.getHarmonizationDate() == null
		&& (siadapEvaluationUniverseForSiadapUniverse.getHarmonizationAssessment() != null || siadapEvaluationUniverseForSiadapUniverse
			.getHarmonizationAssessmentForExcellencyAward() != null);
    }

    public boolean getAbleToRemoveAssessmentsForSIADAP3() {
	return isAbleToRemoveAssessmentsFor(SiadapUniverse.SIADAP3);
    }

    public boolean getAbleToRemoveAssessmentsForSIADAP2() {
	return isAbleToRemoveAssessmentsFor(SiadapUniverse.SIADAP2);
    }

    @Service
    public void setHarmonizationCurrentAssessments(SiadapUniverse siadapUniverse) {
	SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse = getSiadap()
		.getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
	Boolean harmonizationCurrentAssessment = null;
	Boolean harmonizationCurrentAssessmentForExcellencyAward = null;
	if (siadapUniverse.equals(SiadapUniverse.SIADAP2)) {
	    harmonizationCurrentAssessment = getHarmonizationCurrentAssessmentForSIADAP2();
	    harmonizationCurrentAssessmentForExcellencyAward = getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP2();
	} else if (siadapUniverse.equals(SiadapUniverse.SIADAP3)) {
	    harmonizationCurrentAssessment = getHarmonizationCurrentAssessmentForSIADAP3();
	    harmonizationCurrentAssessmentForExcellencyAward = getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP3();
	}
	//if we have no 'No's in the new assessments, we should clean out any ExceedingQuotaProposals for that person
	if ((harmonizationCurrentAssessment == null || harmonizationCurrentAssessment)
		&& (harmonizationCurrentAssessmentForExcellencyAward == null || harmonizationCurrentAssessmentForExcellencyAward))
	//	if (siadapEvaluationUniverseForSiadapUniverse.getHarmonizationAssessment() != null
	//		&& !siadapEvaluationUniverseForSiadapUniverse.getHarmonizationAssessment()
	//		&& harmonizationCurrentAssessment != null
	//		&& harmonizationCurrentAssessment
	//		&& harmonizationCurrentAssessment.booleanValue() != siadapEvaluationUniverseForSiadapUniverse
	//			.getHarmonizationAssessment().booleanValue())
	{
	    ExceedingQuotaProposal quotaProposalFor = ExceedingQuotaProposal.getQuotaProposalFor(
		    getUnitWhereIsHarmonized(siadapUniverse), getYear(), person, siadapUniverse, isQuotaAware());
	    if (quotaProposalFor != null) {
		quotaProposalFor.remove();
	    }
	}

	//we must have an excellent of false if we have a regular of true
	if (siadapEvaluationUniverseForSiadapUniverse.hasExcellencyAwardedFromEvaluator()) {
	    if (harmonizationCurrentAssessmentForExcellencyAward != null && harmonizationCurrentAssessmentForExcellencyAward
		    && harmonizationCurrentAssessment != null && !harmonizationCurrentAssessment) {
		throw new SiadapException("error.harmonization.inconsistency.between.excellency.and.regular.assessment");
	    }
	}
	siadapEvaluationUniverseForSiadapUniverse.setHarmonizationAssessments(harmonizationCurrentAssessment,
		harmonizationCurrentAssessmentForExcellencyAward);
    }

    public void setHarmonizationCurrentAssessmentForSIADAP3(Boolean assessment) {
	this.harmonizationCurrentAssessmentForSIADAP3 = assessment;
    }

    public void setHarmonizationCurrentAssessmentForSIADAP2(Boolean assessment) {
	this.harmonizationCurrentAssessmentForSIADAP2 = assessment;
    }

    /**
     * 
     * @throws SiadapException
     *             if the SIADAP has already some info and thus cannot be
     *             deleted
     */
    @Service
    public void removeSiadap() throws SiadapException {

	if (getSiadap() != null) {
	    //check to see if we can remove the proccess
	    if (getSiadap().hasAnySiadapEvaluationItemsInAnyUniverse() || !getSiadap().getProcess().getComments().isEmpty())
		throw new SiadapException("error.has.items.in.it");
	    //ok, so now let's remove that and the relations
	    getSiadap().delete();
	}
	for (Accountability acc : getPerson().getParentAccountabilities(getConfiguration().getUnitRelations(),
		getConfiguration().getHarmonizationUnitRelations(), getConfiguration().getWorkingRelation(),
		getConfiguration().getWorkingRelationWithNoQuota(), getConfiguration().getEvaluationRelation(),
		getConfiguration().getSiadap2HarmonizationRelation(), getConfiguration().getSiadap3HarmonizationRelation())) {
	    if (acc.isActive(SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(getYear()))) {

		//let's close it on the last day of the previous year, or, in case it has a beginning year equal to this one, let's delete it because it was a mistake
		if (acc.getBeginDate().getYear() == getYear()) {
		    acc.delete();
		} else {
		    acc.setEndDate(SiadapMiscUtilClass.lastDayOfYear(getYear() - 1));
		}
	    }
	}
    }

    public BigDecimal getEvaluatorClassificationForSIADAP2() {
	return evaluatorClassificationForSIADAP2;
    }

    public void setEvaluatorClassificationForSIADAP2(BigDecimal evaluatorClassificationForSIADAP2) {
	this.evaluatorClassificationForSIADAP2 = evaluatorClassificationForSIADAP2;
    }

    public BigDecimal getEvaluatorClassificationForSIADAP3() {
	return evaluatorClassificationForSIADAP3;
    }

    public void setEvaluatorClassificationForSIADAP3(BigDecimal evaluatorClassificationForSIADAP3) {
	this.evaluatorClassificationForSIADAP3 = evaluatorClassificationForSIADAP3;
    }
}
