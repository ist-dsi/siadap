/**
 * 
 */
package module.siadap.domain.util.scripts;

import java.util.Set;
import java.util.TreeSet;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapRootModule;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 29 de Mai de 2012
 * 
 *         This script checks SIADAP processes with null Evaluated Person
 *         objects; Also checks on evaluateds with null names;
 */
public class CheckSIADAPsForStrangeInstances extends WriteCustomTask {

    /* (non-Javadoc)
     * @see pt.ist.bennu.core.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
        Set<Siadap> siadaps = new TreeSet<Siadap>(Siadap.COMPARATOR_BY_EVALUATED_PRESENTATION_NAME_FALLBACK_YEAR_THEN_OID);
        for (Siadap siadap : SiadapRootModule.getInstance().getSiadaps()) {
            if (siadap.getEvaluated() == null || siadap.getEvaluated().getPresentationName() == null) {
                siadaps.add(siadap);
            }
        }

        out.println("Got " + siadaps.size() + " processes");
        for (Siadap siadap : siadaps) {
            out.println("Siadap : " + siadap.getProcess().getProcessNumber() + " PR: "
                    + siadap.getEvaluated().getPresentationName() + " OID: " + siadap.getExternalId());
        }

    }

}
