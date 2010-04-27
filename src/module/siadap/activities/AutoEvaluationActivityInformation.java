package module.siadap.activities;

import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

public class AutoEvaluationActivityInformation extends ActivityInformation<SiadapProcess> {

    private String objectivesJustification;
    private String competencesJustification;
    private String otherFactorsJustification;
    private String extremesJustification;
    private String commentsAndProposals;

    private Integer factorOneClassification;
    private Integer factorTwoClassification;
    private Integer factorThreeClassification;
    private Integer factorFourClassification;
    private Integer factorFiveClassification;
    private Integer factorSixClassification;

    public AutoEvaluationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
    }

    public String getObjectivesJustification() {
        return objectivesJustification;
    }

    public void setObjectivesJustification(String objectivesJustification) {
        this.objectivesJustification = objectivesJustification;
    }

    public String getCompetencesJustification() {
        return competencesJustification;
    }

    public void setCompetencesJustification(String competencesJustification) {
        this.competencesJustification = competencesJustification;
    }

    public String getOtherFactorsJustification() {
        return otherFactorsJustification;
    }

    public void setOtherFactorsJustification(String otherFactorsJustification) {
        this.otherFactorsJustification = otherFactorsJustification;
    }

    public String getExtremesJustification() {
        return extremesJustification;
    }

    public void setExtremesJustification(String extremesJustification) {
        this.extremesJustification = extremesJustification;
    }

    public String getCommentsAndProposals() {
        return commentsAndProposals;
    }

    public void setCommentsAndProposals(String commentsAndProposals) {
        this.commentsAndProposals = commentsAndProposals;
    }

    public Integer getFactorOneClassification() {
        return factorOneClassification;
    }

    public void setFactorOneClassification(Integer factorOneClassification) {
        this.factorOneClassification = factorOneClassification;
    }

    public Integer getFactorTwoClassification() {
        return factorTwoClassification;
    }

    public void setFactorTwoClassification(Integer factorTwoClassification) {
        this.factorTwoClassification = factorTwoClassification;
    }

    public Integer getFactorThreeClassification() {
        return factorThreeClassification;
    }

    public void setFactorThreeClassification(Integer factorThreeClassification) {
        this.factorThreeClassification = factorThreeClassification;
    }

    public Integer getFactorFourClassification() {
        return factorFourClassification;
    }

    public void setFactorFourClassification(Integer factorFourClassification) {
        this.factorFourClassification = factorFourClassification;
    }

    public Integer getFactorFiveClassification() {
        return factorFiveClassification;
    }

    public void setFactorFiveClassification(Integer factorFiveClassification) {
        this.factorFiveClassification = factorFiveClassification;
    }

    public Integer getFactorSixClassification() {
        return factorSixClassification;
    }

    public void setFactorSixClassification(Integer factorSixClassification) {
        this.factorSixClassification = factorSixClassification;
    }
    
    @Override
    public boolean hasAllneededInfo() {
	return isForwardedFromInput();
    }
    
}
