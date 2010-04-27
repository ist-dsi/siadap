package module.siadap.activities;

import org.apache.commons.lang.StringUtils;

import module.siadap.domain.ObjectiveEvaluation;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

public class EditObjectiveEvaluationActivityInformation extends CreateObjectiveEvaluationActivityInformation {

    private ObjectiveEvaluation evaluation;
    private String justification;

    public EditObjectiveEvaluationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
    }

    public void setEvaluation(ObjectiveEvaluation evaluation) {
	this.evaluation = evaluation;
	setObjective(evaluation.getObjective());
	setMeasurementIndicator(evaluation.getMeasurementIndicator());
	setSuperationCriteria(evaluation.getSuperationCriteria());
	setType(evaluation.getType());
    }

    public ObjectiveEvaluation getEvaluation() {
	return evaluation;
    }

    public void setJustification(String justification) {
	this.justification = justification;
    }

    public String getJustification() {
	return justification;
    }

    @Override
    public boolean hasAllneededInfo() {
	return evaluation != null && !StringUtils.isEmpty(justification) && isForwardedFromInput();
    }
}
