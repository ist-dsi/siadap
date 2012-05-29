/**
 * 
 */
package module.siadap.domain.util.scripts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.PartyType;
import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.util.SiadapMiscUtilClass;
import myorg.domain.scheduler.WriteCustomTask;

import org.joda.time.LocalDate;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 28 Maio de 2012 Remove
 *         the units that shouldn't be there for a given year and beyond
 * 
 */
public class CleanAccountabilitiesWithUselessPartyTypesOfGivenYear extends WriteCustomTask {

    /**
     * <b>WARNING</b> this is the year that will start to be cleaned, but
     * subsequent years will also if the accountability starts on this day
     */
    private static final int YEAR_TO_CLEAN = 2011;

    //if true, it will do nothing
    private static final Boolean DRY_RUN = Boolean.TRUE;
    private LocalDate dateToUse;
    private AccountabilityType siadapUnitRelationsAccType;

    private AccountabilityType workerRelWithQuota;
    private AccountabilityType workerRelWithoutQuota;

    private boolean okToRemove = true;

    private final Set<Unit> unitsToRemove = new HashSet<Unit>();

    private static final String[] PARTY_TYPES_TO_CLEAN = { "Project", "SubProject" };


    private boolean isOneOfPartyTypes(Party party) {
	for (String type : PARTY_TYPES_TO_CLEAN) {
	    PartyType pType = PartyType.readBy(type);
	    if (party.hasPartyTypes(pType)) {
		return true;
	    }
	}
	return false;
    }

    private void detectAndNavigate(Unit unit) {
	if (isOneOfPartyTypes(unit)) {
	    unitsToRemove.add(unit);
	}
	for (final Accountability accountability : unit.getChildAccountabilities())
	{
	    if (accountability.isActive(dateToUse)) {
		final AccountabilityType accountabilityType = accountability.getAccountabilityType();
		if (siadapUnitRelationsAccType.equals(accountabilityType))
		{
		    final Unit child = (Unit) accountability.getChild();
		    detectAndNavigate(child);
		}
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see myorg.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
	dateToUse = SiadapMiscUtilClass.lastDayOfYear(YEAR_TO_CLEAN);

	SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(YEAR_TO_CLEAN);

	Unit siadapStructureTopUnit = siadapYearConfiguration.getSiadapStructureTopUnit();

	//let's get the relevant acc
	siadapUnitRelationsAccType = siadapYearConfiguration.getUnitRelations();
	workerRelWithoutQuota = siadapYearConfiguration.getWorkingRelationWithNoQuota();
	workerRelWithQuota = siadapYearConfiguration.getWorkingRelation();

	//and navigate and detect all of the units to remove
	detectAndNavigate(siadapStructureTopUnit);

	//now make sure that from all of those listed, none below is of a valid type (so that we don't disconnect any that we shouldn't)

	List<AccountabilityType> listReleventAccTypes = new ArrayList<AccountabilityType>();
	listReleventAccTypes.add(siadapUnitRelationsAccType);
	listReleventAccTypes.add(workerRelWithQuota);
	listReleventAccTypes.add(workerRelWithoutQuota);

	out.println("Got " + unitsToRemove.size() + " units to remove\n");
	for (Unit unit : unitsToRemove) {
	    out.println(unit.getPresentationName() + " to be removed");
	    for (Accountability childAcc : unit.getChildrenAccountabilities(listReleventAccTypes)) {
		if (childAcc.isActive(dateToUse)) {
		    AccountabilityType childAccType = childAcc.getAccountabilityType();
		    if (siadapUnitRelationsAccType.equals(childAccType) && !isOneOfPartyTypes(childAcc.getChild())) {
			okToRemove = false;
			out.println("** Warning ** will not remove any units as unit "
				+ childAcc.getChild().getPresentationName() + " is below one to be removed");
		    } else if (workerRelWithoutQuota.equals(childAccType) || workerRelWithQuota.equals(childAccType)) {
			okToRemove = false;
			out.println("** Warning ** will not remove any units as unit " + unit.getPresentationName()
				+ " has persons under it");

		    }

		}
	    }

	}

	if (DRY_RUN) {
	    out.println("Dry run concluded");
	}

	if (okToRemove) {
	    for (Unit unit : unitsToRemove) {
		for (Accountability accountability : unit.getChildrenAccountabilities(siadapUnitRelationsAccType)) {
		    if (accountability.isActive(dateToUse)) {
			accountability.delete();
		    }
		}
		for (Accountability accountability : unit.getParentAccountabilities(siadapUnitRelationsAccType)) {
		    if (accountability.isActive(dateToUse)) {
			accountability.delete();
		    }
		}
	    }
	} else {
	    out.println("Found units that would be disconnected and shouldn't won't do anything");
	}

    }
}
