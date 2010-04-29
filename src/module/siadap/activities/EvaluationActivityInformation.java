package module.siadap.activities;

import org.apache.commons.lang.StringUtils;

import module.siadap.domain.SiadapEvaluation;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

public class EvaluationActivityInformation extends ActivityInformation<SiadapProcess> {

    private String noEvaluationJustification;
    private String personalDevelopment;
    private String trainningNeeds;
    private String evaluationJustification;

    public EvaluationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
    }

    @Override
    public void setProcess(SiadapProcess process) {
	super.setProcess(process);
	SiadapEvaluation evaluationData = process.getSiadap().getEvaliationData();
	if (evaluationData != null) {
	    setNoEvaluationJustification(evaluationData.getNoEvaluationJustification());
	    setPersonalDevelopment(evaluationData.getPersonalDevelopment());
	    setTrainningNeeds(evaluationData.getTrainningNeeds());
	    setEvaluationJustification(evaluationData.getEvaluationJustification());
	}

    }

    public String getNoEvaluationJustification() {
	return noEvaluationJustification;
    }

    public void setNoEvaluationJustification(String noEvaluationJustification) {
	this.noEvaluationJustification = noEvaluationJustification;
    }

    public String getPersonalDevelopment() {
	return personalDevelopment;
    }

    public void setPersonalDevelopment(String personalDevelopment) {
	this.personalDevelopment = personalDevelopment;
    }

    public String getTrainningNeeds() {
	return trainningNeeds;
    }

    public void setTrainningNeeds(String trainningNeeds) {
	this.trainningNeeds = trainningNeeds;
    }

    public String getEvaluationJustification() {
	return evaluationJustification;
    }

    public void setEvaluationJustification(String evaluationJustification) {
	this.evaluationJustification = evaluationJustification;
    }

    @Override
    public boolean hasAllneededInfo() {
	return isForwardedFromInput()
		&& (getProcess().getSiadap().getQualitativeEvaluation() != SiadapGlobalEvaluation.LOW || (!StringUtils
			.isEmpty(getPersonalDevelopment())
			&& !StringUtils.isEmpty(getTrainningNeeds()) && !StringUtils.isEmpty(getEvaluationJustification())))
		&& (getProcess().getSiadap().getQualitativeEvaluation() != SiadapGlobalEvaluation.HIGH || !StringUtils
			.isEmpty(getEvaluationJustification()));
    }
}
