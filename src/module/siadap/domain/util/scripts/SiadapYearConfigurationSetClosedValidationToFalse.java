/**
 * 
 */
package module.siadap.domain.util.scripts;

import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;
import myorg.domain.scheduler.WriteCustomTask;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 17 de Fev de 2012
 * 
 *         This is a simple script that will make the new slot on the
 *         {@link SiadapYearConfiguration}
 *         SiadapYearConfiguration#getClosedValidation to false
 * 
 */
public class SiadapYearConfigurationSetClosedValidationToFalse extends WriteCustomTask {

    /* (non-Javadoc)
     * @see myorg.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
	int nrOfBoolsSet = 0;
	for (SiadapYearConfiguration siadapYearConfiguration : SiadapRootModule.getInstance().getYearConfigurations()) {
	    if (siadapYearConfiguration.getClosedValidation() == null) {
		siadapYearConfiguration.setClosedValidation(Boolean.FALSE);
		nrOfBoolsSet++;
	    }
	}

	out.println("Number of bools that were set: " + nrOfBoolsSet);

    }

}
