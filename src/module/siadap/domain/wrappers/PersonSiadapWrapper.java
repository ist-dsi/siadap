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
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.exceptions.DomainException;

import org.apache.commons.collections.Predicate;
import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.services.Service;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

public class PersonSiadapWrapper extends PartyWrapper implements Serializable {

    private Person person;

    public PersonSiadapWrapper(Person person, int year) {
	super(year);
	this.person = person;
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
	return getConfiguration().getSiadapFor(getPerson(), getYear());
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

    public boolean hasPendingActions()
    {
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
	ArrayList<PersonSiadapWrapper> personSiadapWrappers = SiadapRootModule.getInstance().getAssociatedSiadaps(getPerson(), getYear(), false);
	for (PersonSiadapWrapper personSiadapWrapper : personSiadapWrappers) {
	    if (personSiadapWrapper.hasPendingActions())
		counterPendingActions++;
	}
	return counterPendingActions;
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

    public BigDecimal getTotalEvaluationScoring() {
	Siadap siadap = getSiadap();
	return siadap != null ? (siadap.isEvaluationDone() ? siadap.getTotalEvaluationScoring() : null) : null;
    }

    public UnitSiadapWrapper getWorkingUnit() {
	Collection<Unit> parentUnits = getParentUnits(getConfiguration().getWorkingRelation(), getConfiguration()
		.getWorkingRelationWithNoQuota());
	return parentUnits.isEmpty() ? null : new UnitSiadapWrapper(parentUnits
		.iterator().next(), getConfiguration().getYear());
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

    public MultiLanguageString getName() {
	return getPerson().getPartyName();
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
    private void verifyDate(LocalDate dateToVerify)
 {
	if (dateToVerify.getYear() != getYear())
	{
	    throw new DomainException("manage.workingUnitOrEvaluator.invalid.date",
		    DomainException.getResourceFor("resources/SiadapResources"), String.valueOf(getYear()));
	}
    }

    @Service
    public void changeWorkingUnitTo(Unit unit, Boolean withQuotas, LocalDate dateOfChange) {
	verifyDate(dateOfChange);
	LocalDate now = new LocalDate();
	LocalDate startOfYear = new LocalDate(now.getYear(), 1, 1);
	LocalDate endOfYear = new LocalDate(now.getYear(), 12, 31);
	SiadapYearConfiguration configuration = getConfiguration();
	for (Accountability accountability : getParentAccountabilityTypes(configuration.getWorkingRelation(), configuration
		.getWorkingRelationWithNoQuota())) {
	    if (accountability.isActiveNow()) {
		accountability.setEndDate(now.minusDays(1));
	    }
	}
	unit.addChild(getPerson(), withQuotas ? configuration.getWorkingRelation() : configuration
.getWorkingRelationWithNoQuota(), startOfYear,
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

    public void changeEvaluatorTo(Person newEvaluator, LocalDate dateOfChange) {
	verifyDate(dateOfChange);
	LocalDate now = new LocalDate();
	LocalDate startOfYear = new LocalDate(now.getYear(), 1, 1);
	LocalDate endOfYear = new LocalDate(now.getYear(), 12, 31);
	SiadapYearConfiguration configuration = getConfiguration();
	AccountabilityType evaluationRelation = configuration.getEvaluationRelation();
	for (Accountability accountability : getParentAccountabilityTypes(evaluationRelation)) {
	    if (accountability.isActiveNow() && accountability.getParent() instanceof Person
		    && accountability.getChild() instanceof Person) {
		accountability.editDates(accountability.getBeginDate(), now.minusDays(1));
	    }
	}
	//let's
	newEvaluator.addChild(getPerson(), evaluationRelation, startOfYear, endOfYear);

    }

    public boolean isCustomEvaluatorDefined() {
	return !getParentPersons(getConfiguration().getEvaluationRelation()).isEmpty();
    }

    public void removeCustomEvaluator() {
	LocalDate now = new LocalDate();
	AccountabilityType evaluationRelation = getConfiguration().getEvaluationRelation();
	for (Accountability accountability : getParentAccountabilityTypes(evaluationRelation)) {
	    if (accountability.isActiveNow() && accountability.getChild() instanceof Person
		    && accountability.getParent() instanceof Person) {
		accountability.setEndDate(now.minusDays(1));
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
	    history.addAll(person.getParentAccountabilities(configuration.getWorkingRelation(), configuration
		    .getWorkingRelationWithNoQuota()));
	}

	return history;
    }
}
