/**
 * 
 */
package module.siadap.domain.util.scripts;

import module.organization.domain.Accountability;
import module.workflow.domain.WorkflowLog;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;
import pt.ist.fenixframework.FenixFramework;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 7 de Fev de 2013
 * 
 *         Temporary SCRIPT - to remove a screw up..
 * 
 */
public class CorrectSiadapAccontabilityMistake extends WriteCustomTask {

    public final String ACC_OID = "352188192914";

    public final String WORKFLOW_LOG_OID = "317829299549";

    /* (non-Javadoc)
     * @see pt.ist.bennu.core.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
        User prevericator = User.findByUsername("ist23470");
        Accountability accToChange = FenixFramework.getDomainObject(ACC_OID);
        if (accToChange.getCreatorUser().equals(prevericator))
            out.println("Found the accountability");
        else
            return;

        WorkflowLog log = FenixFramework.getDomainObject(WORKFLOW_LOG_OID);
        if (log.getActivityExecutor().equals(prevericator))
        {
            out.println("Got log: " + log.getDescription());
        }
        else return;

        accToChange.setEndDate(null, "Correcção de dados Script: " + getClass().getSimpleName());
        log.delete();


    }

}
