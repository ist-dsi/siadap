/**
 * 
 */
package module.siadap.activities;

import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

import org.apache.commons.lang.StringUtils;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 4 de Jul de 2012
 *
 * 
 */
public class NullifyRatifyActivityInformation extends ActivityInformation<SiadapProcess> {

    private static final long serialVersionUID = 1L;

    private String justification;

    public NullifyRatifyActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
    }

    @Override
    public boolean hasAllneededInfo() {
	return !StringUtils.isEmpty(getJustification());
    }

    public String getJustification() {
	return justification;
    }

    public void setJustification(String justification) {
	this.justification = justification;
    }


}
