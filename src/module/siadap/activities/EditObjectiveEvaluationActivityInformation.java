package module.siadap.activities;

import module.siadap.domain.ObjectiveEvaluation;
import module.siadap.domain.ObjectiveEvaluationIndicator;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

import org.apache.commons.lang.StringUtils;

public class EditObjectiveEvaluationActivityInformation extends
		CreateObjectiveEvaluationActivityInformation {

	private ObjectiveEvaluation evaluation;
	private String justification;
	private boolean employJustification;

	public EditObjectiveEvaluationActivityInformation(
			SiadapProcess process,
			WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
		super(process, activity, false);
		if (process.getSiadap().getObjectivesAndCompetencesSealedDate() != null) {
			setEmployJustification(true);
		} else {
			setEmployJustification(false);

		}
	}

	public void setEvaluation(ObjectiveEvaluation evaluation) {
		this.evaluation = evaluation;
		setObjective(evaluation.getObjective());
		setType(evaluation.getType());
		for (ObjectiveEvaluationIndicator indicator : evaluation
				.getIndicators()) {
			addNewIndicator(indicator.getMeasurementIndicator(),
					indicator.getSuperationCriteria(),
					indicator.getPonderationFactor());
		}
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
	return evaluation != null
		&& (!isEmployJustification() || (isEmployJustification() && !StringUtils.isEmpty(justification)))
		&& isForwardedFromInput() && !StringUtils.isEmpty(getObjective()) && indicatorsFilled();
	}

	public void setEmployJustification(boolean employJustification) {
		this.employJustification = employJustification;
	}

	public boolean isEmployJustification() {
		return employJustification;
	}
}
