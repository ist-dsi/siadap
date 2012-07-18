/**
 * 
 */
package module.siadap.activities;

import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 16 de Abr de 2012
 *
 * 
 */
public class HomologationActivityInformation extends ActivityInformation<SiadapProcess> {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;

    private boolean shouldShowChangeGradeInterface = true;

    private ChangeGradeAnytimeActivityInformation changeGradeAnytimeActivityInformation;

    public HomologationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
	setChangeGradeAnytimeActivityInformation(new ChangeGradeAnytimeActivityInformation(process, activity));
    }

    @Override
    public boolean hasAllneededInfo() {
	return isForwardedFromInput();
    }

    public boolean isShouldShowChangeGradeInterface() {
	return shouldShowChangeGradeInterface;
    }

    public void setShouldShowChangeGradeInterface(boolean shouldShowChangeGradeInterface) {
	this.shouldShowChangeGradeInterface = shouldShowChangeGradeInterface;
    }


    public ChangeGradeAnytimeActivityInformation getChangeGradeAnytimeActivityInformation() {
	return changeGradeAnytimeActivityInformation;
    }

    private void setChangeGradeAnytimeActivityInformation(
	    ChangeGradeAnytimeActivityInformation changeGradeAnytimeActivityInformation) {
	this.changeGradeAnytimeActivityInformation = changeGradeAnytimeActivityInformation;
    }

}
