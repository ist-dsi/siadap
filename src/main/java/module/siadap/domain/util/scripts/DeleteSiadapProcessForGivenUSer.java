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
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.ist.fenixframework.pstm.IllegalWriteException;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 5 de Fev de 2013
 * 
 * 
 */
public class DeleteSiadapProcessForGivenUSer extends WriteCustomTask {

    private final String WF_PROCESS_OID = "6214818079331";

    private final String LABEL_LOG_OID = "1073743539799";

    /* (non-Javadoc)
     * @see pt.ist.bennu.core.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
        SiadapProcess process = AbstractDomainObject.fromExternalId(this.WF_PROCESS_OID);

        LabelLog labelLog = AbstractDomainObject.fromExternalId(LABEL_LOG_OID);

        labelLog.delete();

        Siadap siadap = process.getSiadap();

        try {


            Method deleteMethod = SiadapProcessCountAction.class.getDeclaredMethod("deleteSiadapEvenWithFiles", Siadap.class);
            deleteMethod.setAccessible(true);

            SiadapProcessCountAction siadapProcessCountAction = new SiadapProcessCountAction();
            deleteMethod.invoke(siadapProcessCountAction, siadap);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            if (ex.getCause() instanceof IllegalWriteException)
                throw (IllegalWriteException) ex.getCause();
            else
                throw new Error(ex);
        }

    }

}
