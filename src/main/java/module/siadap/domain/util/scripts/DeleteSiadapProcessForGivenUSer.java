/**
 * 
 */
package module.siadap.domain.util.scripts;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.presentationTier.actions.SiadapProcessCountAction;
import module.workflow.domain.LabelLog;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.core.WriteOnReadError;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 5 de Fev de 2013
 * 
 * 
 */
public class DeleteSiadapProcessForGivenUSer extends WriteCustomTask {

    private final String WF_PROCESS_OID = "6214818075818";

    private final String LABEL_LOG_OID = "1073743523396";

    /* (non-Javadoc)
     * @see pt.ist.bennu.core.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
        SiadapProcess process = FenixFramework.getDomainObject(this.WF_PROCESS_OID);

        LabelLog labelLog = FenixFramework.getDomainObject(LABEL_LOG_OID);

        labelLog.delete();

        Siadap siadap = process.getSiadap();

        try {

            Method deleteMethod = SiadapProcessCountAction.class.getDeclaredMethod("deleteSiadapEvenWithFiles", Siadap.class);
            deleteMethod.setAccessible(true);

            SiadapProcessCountAction siadapProcessCountAction = new SiadapProcessCountAction();
            deleteMethod.invoke(siadapProcessCountAction, siadap);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            if (ex.getCause() instanceof WriteOnReadError) {
                throw (WriteOnReadError) ex.getCause();
            } else {
                throw new Error(ex);
            }
        }

    }

}
