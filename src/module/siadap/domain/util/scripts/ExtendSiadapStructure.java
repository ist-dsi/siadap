/**
 * 
 */
package module.siadap.domain.util.scripts;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.CompetenceType;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.util.SiadapMiscUtilClass;
import myorg.applicationTier.Authenticate;
import myorg.domain.User;
import myorg.domain.scheduler.WriteCustomTask;
import pt.ist.fenixWebFramework.security.UserView;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 3 de Fev de 2012
 * 
 *         Script used to extend the SIADAP accountabilities from a given year
 *         {@link #YEAR_TO_EXTEND} to another {@link #YEAR_TO_EXTEND_TO} which
 *         must have a SiadapYearConfiguration already configured with the
 *         accountability types configured
 */
public class ExtendSiadapStructure extends WriteCustomTask {

    class SiadapBean {

	private final CompetenceType competenceType;

	public CompetenceType getCompetenceType() {
	    return competenceType;
	}

	public SiadapUniverse getDefaultSiadapUniverse() {
	    return defaultSiadapUniverse;
	}

	private final SiadapUniverse defaultSiadapUniverse;

	private final Siadap siadap;

	SiadapBean(Siadap siadap) {
	    competenceType = siadap.getCompetenceType();
	    defaultSiadapUniverse = siadap.getDefaultSiadapUniverse();
	    this.siadap = siadap;

	    if (defaultSiadapUniverse == null || competenceType == null) {
		throw new SiadapException("competence.type.or.siadap.universe.were.null");
	    }
	}

	public Siadap getSiadap() {
	    return siadap;
	}
    }

    private final static int YEAR_TO_EXTEND = 2011;
    private final static int YEAR_TO_EXTEND_TO = YEAR_TO_EXTEND + 1;
    //let's get the configuration and all of the accountabilities that we should 'clone'
    SiadapYearConfiguration yearConfigurationToExtend;
    SiadapYearConfiguration yearConfigurationToExtendTo;
    AccountabilityType unitRelations;
    AccountabilityType harmonizationUnitRelations;
    AccountabilityType harmonizationResponsibleRelation;
    AccountabilityType siadap2HarmonizationRelation;
    AccountabilityType siadap3HarmonizationRelation;
    AccountabilityType workingRelation;
    AccountabilityType workingRelationWithNoQuota;
    private AccountabilityType evaluationRelation;

    AccountabilityType newUnitRelations;
    AccountabilityType newHarmonizationUnitRelations;
    AccountabilityType newHarmonizationResponsibleRelation;
    AccountabilityType newSiadap2HarmonizationRelation;
    AccountabilityType newSiadap3HarmonizationRelation;
    AccountabilityType newWorkingRelation;
    AccountabilityType newWorkingRelationWithNoQuota;
    private AccountabilityType newEvaluationRelation;

    private Set<Accountability> accsToClone;
    private Set<SiadapBean> siadapsToClone;

    /*
     * (non-Javadoc)
     * 
     * @see myorg.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
	accsToClone = new HashSet<Accountability>();
	siadapsToClone = new HashSet<SiadapBean>();

	yearConfigurationToExtend = SiadapYearConfiguration.getSiadapYearConfiguration(YEAR_TO_EXTEND);
	yearConfigurationToExtendTo = SiadapYearConfiguration.getSiadapYearConfiguration(YEAR_TO_EXTEND_TO);

	unitRelations = yearConfigurationToExtend.getUnitRelations();
	harmonizationUnitRelations = yearConfigurationToExtend.getHarmonizationUnitRelations();
	harmonizationResponsibleRelation = yearConfigurationToExtend.getHarmonizationResponsibleRelation();
	siadap2HarmonizationRelation = yearConfigurationToExtend.getSiadap2HarmonizationRelation();
	siadap3HarmonizationRelation = yearConfigurationToExtend.getSiadap3HarmonizationRelation();
	workingRelation = yearConfigurationToExtend.getWorkingRelation();
	workingRelationWithNoQuota = yearConfigurationToExtend.getWorkingRelationWithNoQuota();
	evaluationRelation = yearConfigurationToExtend.getEvaluationRelation();
	//now let's get them all (through the top unit)
	Unit topUnit = yearConfigurationToExtend.getSiadapStructureTopUnit();
	descendOnUnitAndRegisterAccs(topUnit);

	//now let's scour all of the existing SIADAPs for the given year, and register the data to extend to 2012 such as Universe (SIADAP2 or SIADAP3) and Career (Competence Type) 
	//as well as the accountabilities that are set directly between two persons

	int nrDirectEvaluatorsFound = 0;
	List<Siadap> siadaps = SiadapRootModule.getInstance().getSiadaps();
	for (Siadap siadap : siadaps) {
	    if (siadap.getYear().intValue() == YEAR_TO_EXTEND) {
		//check for direct accountabilities to extend
		for (Accountability acc : siadap.getEvaluated().getParentAccountabilities(evaluationRelation)) {
		    if (acc.isActive(SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(YEAR_TO_EXTEND))
			    && acc.getParent() instanceof Person) {
			accsToClone.add(acc);
			nrDirectEvaluatorsFound++;
		    }
		}

		//let's get the career and the SIADAP universe
		try {
		    siadapsToClone.add(new SiadapBean(siadap));
		} catch (SiadapException ex) {
		    out.println("SIADAP CLONING: Could not clone (due to null competence type/Universe) "
			    + siadap.getProcess().getProcessNumber());
		}

	    }
	}
	out.println("Caught " + nrDirectEvaluatorsFound + " direct evaluator relations");

	newUnitRelations = yearConfigurationToExtendTo.getUnitRelations();
	newHarmonizationUnitRelations = yearConfigurationToExtendTo.getHarmonizationUnitRelations();
	newHarmonizationResponsibleRelation = yearConfigurationToExtendTo.getHarmonizationResponsibleRelation();
	newSiadap2HarmonizationRelation = yearConfigurationToExtendTo.getSiadap2HarmonizationRelation();
	newSiadap3HarmonizationRelation = yearConfigurationToExtendTo.getSiadap3HarmonizationRelation();
	newWorkingRelation = yearConfigurationToExtendTo.getWorkingRelation();
	newWorkingRelationWithNoQuota = yearConfigurationToExtendTo.getWorkingRelationWithNoQuota();
	newEvaluationRelation = yearConfigurationToExtendTo.getEvaluationRelation();

	int extendedAccs = 0;
	//ok, so now let's take care of extending/creating the accs, based on the ones we got
	for (Accountability acc : accsToClone) {
	    AccountabilityType accTypeToUse = null;
	    AccountabilityType accountabilityType = acc.getAccountabilityType();
	    if (accountabilityType.equals(unitRelations)) {
		accTypeToUse = newUnitRelations;

	    } else if (accountabilityType.equals(harmonizationUnitRelations)) {
		accTypeToUse = newHarmonizationUnitRelations;

	    } else if (accountabilityType.equals(harmonizationResponsibleRelation)) {
		accTypeToUse = newHarmonizationResponsibleRelation;

	    } else if (accountabilityType.equals(siadap2HarmonizationRelation)) {
		accTypeToUse = newSiadap2HarmonizationRelation;

	    } else if (accountabilityType.equals(siadap3HarmonizationRelation)) {
		accTypeToUse = newSiadap3HarmonizationRelation;

	    } else if (accountabilityType.equals(workingRelation)) {
		accTypeToUse = newWorkingRelation;

	    } else if (accountabilityType.equals(workingRelationWithNoQuota)) {
		accTypeToUse = newWorkingRelationWithNoQuota;

	    } else if (accountabilityType.equals(evaluationRelation)) {
		accTypeToUse = newEvaluationRelation;

	    }

	    if (accountabilityType.equals(accTypeToUse)) {
		//it's the same kind of acc, let's see what we shall do with it
		if (YEAR_TO_EXTEND_TO - YEAR_TO_EXTEND == 1) {
		    //if we are talking about subsequent years, let's just check if we need to extend it or not
		    if (!acc.isActive(SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(YEAR_TO_EXTEND_TO))) {
			//if we have to extend it, let's extend it untill infinity
			acc.editDates(acc.getBeginDate(), null);
			extendedAccs++;

		    }
		}
	    }
	}

	//done, everything is extended
	out.println("*FINISHED*\nSummary: ");
	out.println("Total Accs that were marked to be extended " + accsToClone.size() + " extended: " + extendedAccs);

	int clonedSiadaps = 0;
	//let's now 'clone' the SIADAPs
	try {
	    UserView.setUser(Authenticate.authenticate(User.findByUsername("ist23470")));
	    for (SiadapBean siadapBean : siadapsToClone) {
		Siadap siadap = siadapBean.getSiadap();
		boolean siadapAlreadyExists = false;
		for (Siadap currentSiadap : siadap.getEvaluated().getSiadapsAsEvaluated()) {
		    if (currentSiadap.getYear().intValue() == YEAR_TO_EXTEND_TO) {
			siadapAlreadyExists = true;
			break;
		    }

		}
		if (!siadapAlreadyExists) {
		SiadapProcess.createNewProcess(siadap.getEvaluated(), YEAR_TO_EXTEND_TO, siadapBean.getDefaultSiadapUniverse(),
			siadapBean.getCompetenceType());
		clonedSiadaps++;
		}
	    }
	} finally {
	    UserView.setUser(null);
	    out.println("There were cloned " + clonedSiadaps + " cloned SIADAPs");
	}
    }

    private void descendOnUnitAndRegisterAccs(Unit unit) {
	for (Accountability acc : unit.getChildrenAccountabilities(harmonizationResponsibleRelation, unitRelations,
		harmonizationUnitRelations, siadap2HarmonizationRelation, siadap3HarmonizationRelation, workingRelation,
		workingRelationWithNoQuota, evaluationRelation)) {
	    if (acc.isActive(SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(YEAR_TO_EXTEND))) {
		//it was active, so let's add it to the list of accs to clone/extend
		accsToClone.add(acc);

		//if we have one of these accs with an end date beyond 31/12/YEAR_TO_EXTEND, let's print its details
		if (acc.getEndDate() == null || !acc.getEndDate().equals(SiadapMiscUtilClass.lastDayOfYear(YEAR_TO_EXTEND))) {
		    String parentString = acc.getParent() == null || acc.getParent().getPartyName() == null ? "null" : acc
			    .getParent().getPartyName().getContent();
		    String childString = acc.getChild() == null || acc.getChild().getPartyName() == null ? "null" : acc
			    .getChild().getPartyName().getContent();
		    out.println("Acc.Type: " + acc.getAccountabilityType().getName().getContent() + " Acc parent: "
			    + parentString + " child: " + childString + " acc start: " + String.valueOf(acc.getBeginDate())
			    + " acc end: " + String.valueOf(acc.getEndDate()));
		}
		//if we are dealing with a unit in the other end, let's descend
		if (acc.getChild() instanceof Unit) {
		    descendOnUnitAndRegisterAccs((Unit) acc.getChild());
		}
	    }
	}

    }

}
