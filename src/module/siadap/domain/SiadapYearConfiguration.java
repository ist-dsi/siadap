package module.siadap.domain;

import java.util.ArrayList;
import java.util.List;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.MyOrg;
import myorg.domain.User;
import myorg.domain.groups.NamedGroup;
import myorg.domain.groups.PersistentGroup;
import pt.ist.fenixWebFramework.services.Service;

public class SiadapYearConfiguration extends SiadapYearConfiguration_Base {

    public static final Double DEFAULT_OBJECTIVES_PONDERATION = 75.0;
    public static final Double DEFAULT_COMPETENCES_PONDERATION = 25.0;
    public static final Double MAXIMUM_HIGH_GRADE_QUOTA = 25.0;
    public static final Double MAXIMUM_EXCELLENCY_GRADE_QUOTA = 5.0; // 1.25; //
    
    private static final String CCA_MEMBERS_GROUPNAME = "CCA Members";
    private static final String HOMOLOGATION_MEMBERS_GROUPNAME = "Homologation Members";
    
    private static NamedGroup ccaMembersGroup;
    
    public static NamedGroup getCcaMembersGroup() {
    	initGroups();
		return ccaMembersGroup;
	}

	public static NamedGroup getHomologationMembersGroup() {
		initGroups();
		return homologationMembersGroup;
	}

	private static NamedGroup homologationMembersGroup;
    private static boolean groupsInitialized = false;
    
    private static void initGroups() {
    	if (groupsInitialized)
    		return;
    	//get the ccaMembersGroup
    	for (PersistentGroup group : MyOrg.getInstance().getPersistentGroups()) {
    		if (group instanceof NamedGroup)
    		{
    			if (((NamedGroup) group).getName().equals(CCA_MEMBERS_GROUPNAME))
    			{
    				ccaMembersGroup = (NamedGroup) group;
    			}
    		}
		}
    	//let us create the group if we haven't found it
    	if (ccaMembersGroup == null)
    		createCCAMembersGroup();
    	
    	//get the homologationMembersGroup
    	for (PersistentGroup group : MyOrg.getInstance().getPersistentGroups()) {
    		if (group instanceof NamedGroup)
    		{
    			if (((NamedGroup) group).getName().equals(CCA_MEMBERS_GROUPNAME))
    			{
    				homologationMembersGroup = (NamedGroup) group;
    			}
    		}
		}
    	//let us create the group if we haven't found it
    	if (homologationMembersGroup == null)
    		createHomologationMembersGroup();
    }
    
    @Service
    private static void createHomologationMembersGroup() {
    	homologationMembersGroup = new NamedGroup(HOMOLOGATION_MEMBERS_GROUPNAME);
	}

	@Service
    private static void createCCAMembersGroup() {
    		ccaMembersGroup = new NamedGroup(CCA_MEMBERS_GROUPNAME);
    }
	
	@Service
	public static void addCCAMember(User user)
	{
		getCcaMembersGroup().addUsers(user);
	}
	@Service
	public static void addHomologationMember(User user)
	{
		getHomologationMembersGroup().addUsers(user);
	}
	
	@Service
	public static void removeHomologationMember(User user)
	{
		getHomologationMembersGroup().removeUsers(user);
	}

	@Service
	public static void removeCCAMember(User user)
	{
		getCcaMembersGroup().removeUsers(user);
		
	}
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
