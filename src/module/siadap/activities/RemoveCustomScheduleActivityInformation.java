/**
 * 
 */
package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessSchedulesEnum;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt)
 * 
 */
public class RemoveCustomScheduleActivityInformation extends ActivityInformation<SiadapProcess> {
    
    private String siadapProcessSchedulesEnumToRemove;
    private Siadap siadap;

    public RemoveCustomScheduleActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
    }

    public void setSiadapProcessSchedulesEnumToRemove(String siadapProcessSchedulesEnumToRemove) {
	this.siadapProcessSchedulesEnumToRemove = siadapProcessSchedulesEnumToRemove;
    }

    public String getSiadapProcessSchedulesEnumToRemove() {
	return siadapProcessSchedulesEnumToRemove;
    }

    @Override
    public boolean hasAllneededInfo() {
	if (getSiadapProcessSchedulesEnumToRemove() != null
		&& SiadapProcessSchedulesEnum.valueOf(getSiadapProcessSchedulesEnumToRemove()) instanceof SiadapProcessSchedulesEnum)
	    return true;
	return false;
    }

    @Override
    public void setProcess(SiadapProcess process) {
	super.setProcess(process);
	setSiadap(process.getSiadap());
    }

    public Siadap getSiadap() {
	return siadap;
    }

    public void setSiadap(Siadap siadap) {
	this.siadap = siadap;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

}
