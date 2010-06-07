package module.siadap.activities;

import org.apache.commons.lang.StringUtils;

import module.siadap.domain.SiadapProcess;

import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

public class NoEvaluationActivityInformation extends ActivityInformation<SiadapProcess> {

    String noEvaluationJustification;

    public NoEvaluationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
    }

    public String getNoEvaluationJustification() {
	return noEvaluationJustification;
    }

    public void setNoEvaluationJustification(String noEvaluationJustification) {
	this.noEvaluationJustification = noEvaluationJustification;
    }

    @Override
    public boolean hasAllneededInfo() {
	return !StringUtils.isEmpty(getNoEvaluationJustification());
    }
}
