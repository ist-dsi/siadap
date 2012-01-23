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
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;

import org.apache.commons.collections.Predicate;
import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.plugins.remote.domain.exception.RemoteException;

public class PersonSiadapWrapper extends PartyWrapper implements Serializable {

    private Person person;

    /*
     * We need these two Booleans because of the render problems while writing
     * to a Bean
     */
    private Boolean harmonizationCurrentAssessmentForSIADAP3;
    private Boolean harmonizationCurrentAssessmentForSIADAP2;


    public PersonSiadapWrapper(Person person, int year) {
	super(year);
	this.person = person;
	//initing the harmonization booleans
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
	    } else {
		this.harmonizationCurrentAssessmentForSIADAP2 = siadapEvaluationUniverseForSIADAP2.getHarmonizationAssessment();
	    }

	    if (siadapEvaluationUniverseForSIADAP3 == null) {
		this.harmonizationCurrentAssessmentForSIADAP3 = null;
	    } else {
		this.harmonizationCurrentAssessmentForSIADAP3 = siadapEvaluationUniverseForSIADAP3.getHarmonizationAssessment();
	    }
	}
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
	boolean excellencyGiven = false;
	Siadap siadap = getSiadap();
	if (siadap == null)
	    return SiadapGlobalEvaluation.NONEXISTING.getLocalizedName();
	SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse = siadap.getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
	if (siadapEvaluationUniverseForSiadapUniverse == null )
	    return SiadapGlobalEvaluation.NONEXISTING.getLocalizedName();
	else
	    excellencyGiven = siadapEvaluationUniverseForSiadapUniverse.hasExcellencyAwarded();
	if (!siadap.isEvaluationDone(siadapUniverse) && !siadapEvaluationUniverseForSiadapUniverse.isWithSkippedEvaluation()) {
	    return SiadapGlobalEvaluation.NONEXISTING.getLocalizedName();
	}
	if (siadapEvaluationUniverseForSiadapUniverse.isWithSkippedEvaluation())
	    return SiadapGlobalEvaluation.WITHSKIPPEDEVAL.getLocalizedName();

	return SiadapGlobalEvaluation.getGlobalEvaluation(siadapEvaluationUniverseForSiadapUniverse.getTotalEvaluationScoring(),
		excellencyGiven).getLocalizedName();
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
	if (getSiadap().isDefaultEvaluationDone() && getSiadap().getSubmittedForValidationEvaluationAcknowledgementDate() != null)
	    return true;
	return false;
    }

    //    public BigDecimal getTotalEvaluationScoring() {
    //	Siadap siadap = getSiadap();
    //	return siadap != null ? (siadap.isEvaluationDone() ? siadap.getTotalEvaluationScoring() : null) : null;
    //    }
    
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
	LocalDate now = new LocalDate();
	LocalDate startOfYear = new LocalDate(dateOfChange.getYear(), 1, 1);
	LocalDate endOfYear = new LocalDate(dateOfChange.getYear(), 12, 31);
	SiadapYearConfiguration configuration = getConfiguration();
	for (Accountability accountability : getParentAccountabilityTypes(configuration.getWorkingRelation(),
		configuration.getWorkingRelationWithNoQuota())) {
	    if (accountability.isActiveNow()) {
		accountability.editDates(accountability.getBeginDate(), dateOfChange);
	    }
	}
	unit.addChild(getPerson(),
		withQuotas ? configuration.getWorkingRelation() : configuration.getWorkingRelationWithNoQuota(), startOfYear,
		endOfYear);
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
	//	LocalDate now = new LocalDate();
	LocalDate startOfYear = new LocalDate(dateOfChange.getYear(), 1, 1);
	LocalDate endOfYear = new LocalDate(dateOfChange.getYear(), 12, 31);
	SiadapYearConfiguration configuration = getConfiguration();
	AccountabilityType evaluationRelation = configuration.getEvaluationRelation();
	for (Accountability accountability : getParentAccountabilityTypes(evaluationRelation)) {
	    if (accountability.isActiveNow() && accountability.getParent() instanceof Person
		    && accountability.getChild() instanceof Person) {
		accountability.editDates(accountability.getBeginDate(), dateOfChange);
	    }
	}
	//let's
	newEvaluator.addChild(getPerson(), evaluationRelation, startOfYear, endOfYear);

    }


    public boolean isCustomEvaluatorDefined() {
	return !getParentPersons(getConfiguration().getEvaluationRelation()).isEmpty();
    }

    @Service
    public void removeCustomEvaluator() {
	LocalDate now = new LocalDate();
	//make sure we are making changes in the current year TODO not sure if this is what we want for all of the use cases, but for now let's keep it this way
	verifyDate(now);
	AccountabilityType evaluationRelation = getConfiguration().getEvaluationRelation();
	for (Accountability accountability : getParentAccountabilityTypes(evaluationRelation)) {
	    if (accountability.isActiveNow() && accountability.getChild() instanceof Person
		    && accountability.getParent() instanceof Person) {
		accountability.editDates(accountability.getBeginDate(), now.minusDays(1));
	    }
	}
    }

    public Boolean getProcessValidation() {
	Siadap siadap = getSiadap();
	return siadap != null ? siadap.getValidated() : null;
    }

    public Boolean getHomologationDone() {
	Siadap siadap = getSiadap();
	return siadap != null ? siadap.isHomologated() : null;
    }

    public boolean isCCAMember() {
	return getConfiguration().getCcaMembers().contains(getPerson());
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

    @Service
    public void changeUniverseTo(SiadapUniverse siadapUniverseToChangeTo) {
	getSiadap().setDefaultSiadapUniverse(siadapUniverseToChangeTo);

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

    private Boolean getHarmonizationCurrentAssessmentFor(SiadapEvaluationUniverse siadapEvaluationUniverse) {
	return siadapEvaluationUniverse.getHarmonizationAssessment();
    }

    public Boolean getHarmonizationCurrentAssessmentForSIADAP2() {
	return this.harmonizationCurrentAssessmentForSIADAP2;
    }

    public Boolean getHarmonizationCurrentAssessmentForSIADAP3() {
	return this.harmonizationCurrentAssessmentForSIADAP3;
    }

    private boolean isAbleToRemoveAssessmentFor(SiadapUniverse siadapUniverse) {
	SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse = getSiadap()
		.getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);

	if (siadapEvaluationUniverseForSiadapUniverse == null)
	    return false;
	return isHarmonizationPeriodOpen() && siadapEvaluationUniverseForSiadapUniverse.getHarmonizationDate() == null
		&& siadapEvaluationUniverseForSiadapUniverse.getHarmonizationAssessment() != null;
    }

    public boolean getAbleToRemoveAssessmentForSIADAP3() {
	return isAbleToRemoveAssessmentFor(SiadapUniverse.SIADAP3);
    }

    public boolean getAbleToRemoveAssessmentForSIADAP2() {
	return isAbleToRemoveAssessmentFor(SiadapUniverse.SIADAP2);
    }

    @Service
    public void setHarmonizationCurrentAssessment(SiadapUniverse siadapUniverse) {
	SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse = getSiadap()
		.getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
	Boolean harmonizationCurrentAssessment = null;
	if (siadapUniverse.equals(SiadapUniverse.SIADAP2)) {
	    harmonizationCurrentAssessment = getHarmonizationCurrentAssessmentForSIADAP2();
	} else if (siadapUniverse.equals(SiadapUniverse.SIADAP3)) {
	    harmonizationCurrentAssessment = getHarmonizationCurrentAssessmentForSIADAP3();
	}
	siadapEvaluationUniverseForSiadapUniverse.setHarmonizationAssessment(harmonizationCurrentAssessment);
    }

    public void setHarmonizationCurrentAssessmentForSIADAP3(Boolean assessment) {
	this.harmonizationCurrentAssessmentForSIADAP3 = assessment;
    }

    public void setHarmonizationCurrentAssessmentForSIADAP2(Boolean assessment) {
	this.harmonizationCurrentAssessmentForSIADAP2 = assessment;
    }
}
