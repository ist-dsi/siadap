/**
 * 
 */
package module.siadap.activities;

import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

import org.apache.commons.lang.StringUtils;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt)
 * 
 */
public class RevertStateActivityInformation extends ActivityInformation<SiadapProcess> {

    /**
     * Version of the class - currently 1st version
     */
    private static final long serialVersionUID = 1L;

    private String justification;

    private SiadapProcessStateEnum siadapProcessStateEnum;

    public RevertStateActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
    }

    public void setSiadapProcessStateEnum(SiadapProcessStateEnum siadapProcessStateEnum) {
	this.siadapProcessStateEnum = siadapProcessStateEnum;
    }

    public SiadapProcessStateEnum getSiadapProcessStateEnum() {
	return siadapProcessStateEnum;
    }

    public void setJustification(String justification) {
	this.justification = justification;
    }

    public String getJustification() {
	return justification;
    }

    @Override
    public boolean hasAllneededInfo() {
	if (!StringUtils.isBlank(getJustification()) && getSiadapProcessStateEnum() != null)
	    return true;
	else
	    return false;
    }


}
