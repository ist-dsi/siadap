/**
 * 
 */
package module.siadap.domain.util.scripts;

import module.siadap.domain.SiadapYearConfiguration;
import pt.ist.bennu.core.domain.MyOrg;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 8 de Abr de 2013
 * 
 *         Migrates the SiadapYearConfiguration to have the biannual field
 */
public class SiadapYearConfigurationMigrationToBiannual extends WriteCustomTask {

    @Override
    protected void doService() {
        for (SiadapYearConfiguration siadapYearConfiguration : MyOrg.getInstance().getSiadapRootModule().getYearConfigurations()) {
            if (siadapYearConfiguration.getBiannual() == null) {
                siadapYearConfiguration.setBiannual(Boolean.FALSE);
            }
        }

    }

}
