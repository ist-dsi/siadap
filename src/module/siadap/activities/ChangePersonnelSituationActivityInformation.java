/**
 * 
 */
package module.siadap.activities;

import module.siadap.domain.SiadapProcess;
import module.siadap.presentationTier.actions.SiadapPersonnelManagement.ActivityInformationBeanWrapper;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 14 de Fev de 2012
 *
 * 
 */
public class ChangePersonnelSituationActivityInformation extends ActivityInformation<SiadapProcess> {

    /**
     * Default serial version ID
     */
    private static final long serialVersionUID = 1L;

    private final ActivityInformationBeanWrapper beanWrapper;

    public ChangePersonnelSituationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity,
	    ActivityInformationBeanWrapper beanWrapper) {
	super(process, activity);
	this.beanWrapper = beanWrapper;
    }

    public ActivityInformationBeanWrapper getBeanWrapper() {
	return beanWrapper;
    }

    @Override
    public boolean hasAllneededInfo() {
	return getBeanWrapper().hasAllNeededInfo();
    }

}
