/**
 * 
 */
package module.siadap.domain.util.scripts;

import java.util.ArrayList;
import java.util.List;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.PartyType;
import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.util.SiadapMiscUtilClass;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import myorg.domain.scheduler.WriteCustomTask;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 3 de Fev de 2012
 *         Simple script done to correct the relation between the TopUnit and
 *         the Harmonization Units (which should be an Harmonization Unit
 *         relation, but isn't ATM)
 * 
 */
public class CorrectAccTypeBetweenTopAndHarmUnits extends WriteCustomTask {

    public final static int YEAR_TO_USE = 2011;
    private SiadapYearConfiguration siadapYearConfiguration;
    private final List<Accountability> accToReplace = new ArrayList<Accountability>();
    /* (non-Javadoc)
     * @see myorg.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
	//let's get all the data that we need
	siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(YEAR_TO_USE);
	AccountabilityType harmonizationUnitRelations = siadapYearConfiguration.getHarmonizationUnitRelations();
	AccountabilityType unitRelations = siadapYearConfiguration.getUnitRelations();
	PartyType harmonizationType = PartyType.readBy(UnitSiadapWrapper.SIADAP_HARMONIZATION_UNIT_TYPE);

	//let's get all the children Accs which are of the U.H. type and that are active for the given year
	for (Accountability acc : siadapYearConfiguration.getSiadapStructureTopUnit().getChildrenAccountabilities(
		harmonizationUnitRelations, unitRelations)) {
	    if (acc.isActive(SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(YEAR_TO_USE))) {
		if (acc.getChild() instanceof Unit) {
		    //let's check to see if it is of the correct type
		    Unit childUnit = (Unit) acc.getChild();
		    if (childUnit.getPartyTypes().contains(harmonizationType)
			    && acc.getAccountabilityType().equals(unitRelations)) {
			//let's change this one
			acc.setAccountabilityType(harmonizationUnitRelations);
			out.println("Changed acc of '"
				+ siadapYearConfiguration.getSiadapStructureTopUnit().getPartyName().getContent() + "' to '"
				+ childUnit.getPresentationName() + "' from acc type Unit Relations to HarmonizationUnit");
		    }
		}

	    }
	}

    }

}
