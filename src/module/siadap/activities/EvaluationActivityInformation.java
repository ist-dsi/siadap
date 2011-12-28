package module.siadap.activities;

import module.siadap.domain.SiadapEvaluation;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

public class EvaluationActivityInformation extends ActivityInformation<SiadapProcess> {

    private String noEvaluationJustification;
    private String personalDevelopment;
    private String trainningNeeds;
    private String evaluationJustification;
    private Boolean excellencyAward;

    public EvaluationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
	SiadapEvaluation evaluationData = process.getSiadap().getEvaluationData2();
    }

    @Override
    public void setProcess(SiadapProcess process) {
	super.setProcess(process);
	SiadapEvaluation evaluationData = process.getSiadap().getEvaluationData2();
	if (evaluationData != null) {
	    setNoEvaluationJustification(evaluationData.getNoEvaluationJustification());
	    setPersonalDevelopment(evaluationData.getPersonalDevelopment());
	    setTrainningNeeds(evaluationData.getTrainningNeeds());
	    setEvaluationJustification(evaluationData.getEvaluationJustification());
	    setExcellencyAward(evaluationData.getExcellencyAward());
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

    public Boolean getExcellencyAward() {
	return excellencyAward;
    }

    public void setExcellencyAward(Boolean excellencyAward) {
	this.excellencyAward = excellencyAward;
    }

    @Override
    public boolean hasAllneededInfo() {
	/*
	 * The verifications from the code are done in the
	 * module.siadap.domain.SiadapEvaluation.edit(...) method
	 */
	return isForwardedFromInput();
    }
}
