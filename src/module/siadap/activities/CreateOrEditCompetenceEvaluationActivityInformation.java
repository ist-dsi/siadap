package module.siadap.activities;

import java.util.ArrayList;
import java.util.List;

import module.siadap.domain.Competence;
import module.siadap.domain.CompetenceType;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.presentationTier.renderers.providers.CompetencesForCompetenceType.ContainsCompetenceType;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

public class CreateOrEditCompetenceEvaluationActivityInformation extends ActivityInformation<SiadapProcess> implements
	ContainsCompetenceType {

    private Siadap siadap;
    private final CompetenceType competenceType;
    private List<Competence> competences;
    // Variable used to make sure that the JSP is displayed to the user upon
    // creation/edition of the competences
    private boolean inputDisplayed = false;

    private Boolean evaluatedOnlyByCompetences;

    public CreateOrEditCompetenceEvaluationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
	setCompetences(new ArrayList<Competence>(getSiadap().getCompetences()));
	// setCompetences(process.getSiadap().getCompetences());
	this.competenceType = process.getSiadap().getDefaultCompetenceType();
	if (process.getSiadap().getEvaluatedOnlyByCompetences() == null) {
	    setEvaluatedOnlyByCompetences(Boolean.FALSE);
	} else {
	    setEvaluatedOnlyByCompetences(process.getSiadap().getEvaluatedOnlyByCompetences());
	}
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

    @Override
    public CompetenceType getCompetenceType() {
	return competenceType;
    }

    public void setCompetences(List<Competence> competences) {
	this.competences = competences;
    }

    public List<Competence> getCompetences() {
	return competences;
    }

    @Override
    public boolean hasAllneededInfo() {
	return isForwardedFromInput() && getInputDisplayed() && getSiadap() != null && getCompetences() != null
		&& getCompetenceType() != null && (evaluatedOnlyByCompetences != null);
    }

    public void setInputDisplayed(boolean inputDisplayed) {
	this.inputDisplayed = inputDisplayed;
    }

    public boolean getInputDisplayed() {
	return inputDisplayed;
    }

    public void setEvaluatedOnlyByCompetences(Boolean evaluatedOnlyByCompetences) {
	this.evaluatedOnlyByCompetences = evaluatedOnlyByCompetences;
    }

    @Override
    public Boolean getEvaluatedOnlyByCompetences() {
	return evaluatedOnlyByCompetences;
    }

}
