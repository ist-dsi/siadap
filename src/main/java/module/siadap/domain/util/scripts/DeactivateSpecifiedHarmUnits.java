/**
 * 
 */
package module.siadap.domain.util.scripts;

import java.util.ArrayList;
import java.util.List;

import module.organization.domain.AccountabilityVersion;
import module.organization.domain.Unit;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 11 de Fev de 2013
 * 
 *         There was a problem with {@link ImportHarmztnStructureForYear}, that was corrected meanwhile, but that made it so that
 *         the units that were suppposed to be deactivated, weren't. This script is here to ammend this, without rerunning
 *         {@link ImportHarmztnStructureForYear} again, which would solve the problem, but potentially create others, or simply
 *         new {@link AccountabilityVersion} for the unit accs.
 * 
 * 
 */
public class DeactivateSpecifiedHarmUnits extends WriteCustomTask {

    private static final List<Integer> nrHarmUnitsToDeactivate = new ArrayList<Integer>();

    private static final int year = 2012;

    private static final String JUSTIFICATION = "Importação dos dados de harmonização facultados pela DRH, "
            + "com o nome Harmonizacao SIADAP 17-01-2013 e alterações as bibliotecas, e RT#364277";

    static {
        nrHarmUnitsToDeactivate.add(11);
        nrHarmUnitsToDeactivate.add(3);
    }

    /* (non-Javadoc)
     * @see pt.ist.bennu.core.domain.scheduler.WriteCustomTask#doService()
     */
    @SuppressWarnings("boxing")
    @Override
    protected void doService() {
        for (Integer nrHarmUnit : nrHarmUnitsToDeactivate) {
            Unit harmonizationUnit = UnitSiadapWrapper.getHarmonizationUnit(nrHarmUnit);
            if (harmonizationUnit == null)
                throw new Error("Can't find Harm. Unit nr: " + nrHarmUnit);
            UnitSiadapWrapper harmUnitWrapper = new UnitSiadapWrapper(harmonizationUnit, year);
            harmUnitWrapper.deactivateHarmonizationUnit(JUSTIFICATION);

        }

    }

}
