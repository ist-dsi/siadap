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

public class CreateOrEditCompetenceEvaluationActivityInformation extends
		ActivityInformation<SiadapProcess> implements ContainsCompetenceType {

	private Siadap siadap;
	private CompetenceType competenceType;
	private List<Competence> competences;
	// Variable used to make sure that the JSP is displayed to the user upon
	// creation/edition of the competences
	private boolean inputDisplayed = false;

	private Boolean evaluatedOnlyByCompetences;

	public CreateOrEditCompetenceEvaluationActivityInformation(
			SiadapProcess process,
			WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
		super(process, activity);
		setCompetences(new ArrayList<Competence>(getSiadap().getCompetences()));
//		setCompetences(process.getSiadap().getCompetences());
		setCompetenceType(process.getSiadap().getCompetenceType());
		setEvaluatedOnlyByCompetences(process.getSiadap()
				.getEvaluatedOnlyByCompetences());
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
	

	public void setCompetenceType(CompetenceType competenceType) {
		//clean the other fields if this is changed
		if (this.competenceType != null && this.competenceType != competenceType)
		{
//			setCompetences(new ArrayList<Competence>());			
			getCompetences().clear();
			setInputDisplayed(false);
		}
		this.competenceType = competenceType;
	}

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
		return isForwardedFromInput()
				&& getInputDisplayed()
				&& getSiadap() != null
				&& getCompetences() != null
				&& getCompetenceType() != null
				&& (evaluatedOnlyByCompetences != null);
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
