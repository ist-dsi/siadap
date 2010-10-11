package module.siadap.domain;

import java.util.ArrayList;
import java.util.List;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import myorg.applicationTier.Authenticate.UserView;
import pt.ist.fenixWebFramework.security.User;
import pt.ist.fenixWebFramework.services.Service;

public class SiadapYearConfiguration extends SiadapYearConfiguration_Base {

    public static final Double DEFAULT_OBJECTIVES_PONDERATION = 75.0;
    public static final Double DEFAULT_COMPETENCES_PONDERATION = 25.0;
    public static final Double MAXIMUM_HIGH_GRADE_QUOTA = 25.0;
    public static final Double MAXIMUM_EXCELLENCY_GRADE_QUOTA = 5.0; // 1.25; //

    // 5% of

    // the 25%

    public SiadapYearConfiguration(Integer year, Double objectivesPonderation, Double competencesPonderation) {
	super();
	setYear(year);
	setObjectivesPonderation(objectivesPonderation);
	setCompetencesPonderation(competencesPonderation);
	setSiadapRootModule(SiadapRootModule.getInstance());
	setLockHarmonizationOnQuota(Boolean.FALSE);
    }

    public static SiadapYearConfiguration getSiadapYearConfiguration(Integer year) {
	for (SiadapYearConfiguration configuration : SiadapRootModule.getInstance().getYearConfigurations()) {
	    if (configuration.getYear() == year) {
		return configuration;
	    }
	}
	return null;
    }

    @Service
    public static SiadapYearConfiguration createNewSiadapYearConfiguration(Integer year) {
	SiadapYearConfiguration configuration = getSiadapYearConfiguration(year);
	if (configuration != null) {
	    return configuration;
	}
	return new SiadapYearConfiguration(year, DEFAULT_OBJECTIVES_PONDERATION, DEFAULT_COMPETENCES_PONDERATION);
    }

    public Siadap getSiadapFor(Person person, Integer year) {
	for (Siadap siadap : person.getSiadapsAsEvaluated()) {
	    if (siadap.getYear().equals(year)) {
		return siadap;
	    }
	}
	return null;
    }

    public static List<UnitSiadapWrapper> getAllHarmonizationUnitsFor(Integer year) {
	SiadapYearConfiguration configuration = getSiadapYearConfiguration(year);
	UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(configuration.getSiadapStructureTopUnit(), year);
	List<UnitSiadapWrapper> harmonizationUnits = unitSiadapWrapper.getSubHarmonizationUnits();
	if (unitSiadapWrapper.isResponsibleForHarmonization()) {
	    harmonizationUnits.add(0, unitSiadapWrapper);
	}
	return harmonizationUnits;
    }

    @Override
    @Service
    public void addCcaMembers(Person ccaMembers) {
	super.addCcaMembers(ccaMembers);
    }

    @Override
    @Service
    public void removeCcaMembers(Person ccaMembers) {
	super.removeCcaMembers(ccaMembers);
    }

    @Override
    @Service
    public void addHomologationMembers(Person homologationMembers) {
	super.addHomologationMembers(homologationMembers);
    }

    @Override
    @Service
    public void removeHomologationMembers(Person homologationMembers) {
	super.removeHomologationMembers(homologationMembers);
    }

    public boolean isPersonMemberOfCCA(Person person) {
	return getCcaMembers().contains(person);
    }

    public boolean isCurrentUserMemberOfCCA() {
	return isPersonMemberOfCCA(UserView.getCurrentUser().getPerson());
    }

    public boolean isPersonResponsibleForHomologation(Person person) {
	return getHomologationMembers().contains(person);
    }

    public boolean isCurrentUserResponsibleForHomologation() {
	return isPersonResponsibleForHomologation(UserView.getCurrentUser().getPerson());
    }

    public List<ExcedingQuotaProposal> getSuggestionsForUnit(Unit unit, ExceddingQuotaSuggestionType type) {
	return new UnitSiadapWrapper(unit, getYear()).getExcedingQuotaProposalSuggestions(type);
    }
}
