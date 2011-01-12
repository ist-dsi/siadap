/**
 * 
 */
package module.siadap.activities;

import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

/**
 * @author joao.antunes@tagus.ist.utl.pt
 * 
 */
public class RemoveObjectiveEvaluationActivityInformation extends EditObjectiveEvaluationActivityInformation {

    public RemoveObjectiveEvaluationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
    }

    @Override
    public boolean hasAllneededInfo() {
	if (getEvaluation() != null)
	    return true;
	return false;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

}
