/**
 * 
 */
package module.siadap.domain.util.scripts;

import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;
import myorg.domain.exceptions.DomainException;
import myorg.domain.scheduler.WriteCustomTask;

import org.joda.time.LocalDate;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 24 de Fev de 2012
 * 
 *         Task made to correct the lack of harmonization unit accountability
 *         between the special harmonization unit and the top unit
 * 
 */
public class SIADAPSpecialHarmonizationUnitAccCorrection extends WriteCustomTask {

    /* (non-Javadoc)
     * @see myorg.domain.scheduler.WriteCustomTask#doService()
     */
    private static final int YEAR_TO_START = 2011;
    /**
     * The year until which the accountability should be extended, or till
     * infinity if year =0;
     */
    //    private static final int YEAR_TO_END = 0;
    @Override
    protected void doService() {
	//let's get the year to begin data
	SiadapYearConfiguration siadapYearToStartConf = SiadapYearConfiguration.getSiadapYearConfiguration(YEAR_TO_START);
	Unit specialHarmUnit = siadapYearToStartConf.getSiadapSpecialHarmonizationUnit();
	//WARNING we are just blindly assigning an accountability without checking for an already existing one, so we should only run this script once
	if (true)
	    throw new DomainException("please.make.sure.you.want.to.run.this.again");

	Unit siadapTopUnit = siadapYearToStartConf.getSiadapStructureTopUnit();

	specialHarmUnit.addParent(siadapTopUnit, siadapYearToStartConf.getHarmonizationUnitRelations(), new LocalDate(
		YEAR_TO_START, 12, 20), null);

    }

}
