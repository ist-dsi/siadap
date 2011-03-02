package module.siadap.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessSchedulesEnum;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;
import myorg.domain.exceptions.DomainException;

import org.joda.time.LocalDate;

public class ChangeCustomScheduleActivityInformation extends ActivityInformation<SiadapProcess> {

    private Siadap siadap;
    private List<CustomScheduleRepresentation> customScheduleRepresentations;

    public static class CustomScheduleRepresentation implements Serializable {
	private final SiadapProcessSchedulesEnum typeOfSchedule;
	private String justification;
	private LocalDate newDeadlineDate;

	public CustomScheduleRepresentation(SiadapProcessSchedulesEnum typeOfSchedule, LocalDate newDeadlineDate,
		String justification) {
	    super();
	    this.typeOfSchedule = typeOfSchedule;
	}

	public String getJustification() {
	    return justification;
	}

	public void setJustification(String justification) {
	    this.justification = justification;
	}

	public LocalDate getNewDeadlineDate() {
	    return newDeadlineDate;
	}

	public void setNewDeadlineDate(LocalDate newDeadlineDate) {
	    this.newDeadlineDate = newDeadlineDate;
	}

	public SiadapProcessSchedulesEnum getTypeOfSchedule() {
	    return typeOfSchedule;
	}

	public boolean isComplete() {
	    return (getTypeOfSchedule() != null && getJustification() != null && getNewDeadlineDate() != null);
	}

    }

    public ChangeCustomScheduleActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	this(process, activity, true);
    }

    protected ChangeCustomScheduleActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity,
	    boolean addCustomScheduleRepresentation) {
	super(process, activity);
	setCustomScheduleRepresentation(new ArrayList<CustomScheduleRepresentation>());
	if (addCustomScheduleRepresentation) {
	    addNewCustomScheduleRepresentation();
	}
    }

    //check if each existing CustomScheduleRepresentation is complete and only one of each kind exists
    private void checkCustomScheduleRepresentations() {
	boolean foundObjectiveSpecificationBeginPeriod = false;
	boolean foundObjectiveSpecificationEndPeriod = false;

	boolean foundValidAutoEvaluationBeginPeriod = false;
	boolean foundValidAutoEvaluationEndPeriod = false;

	boolean foundEvaluationBeginPeriod = false;
	boolean foundEvaluationEndPeriod = false;

	for (CustomScheduleRepresentation customScheduleRepresentation : getCustomScheduleRepresentations()) {
	    ResourceBundle resourceBundle = DomainException.getResourceFor("resources/SiadapResources");

	    if (!customScheduleRepresentation.isComplete())
		throw new DomainException("error.incomplete.CustomScheduleRepresentation",resourceBundle);

	    switch (customScheduleRepresentation.getTypeOfSchedule()) {
	    case OBJECTIVES_SPECIFICATION_BEGIN_DATE:
		if (!foundObjectiveSpecificationBeginPeriod)
		    foundEvaluationBeginPeriod = true;
		else throw new DomainException("error.duplicated.CustomScheduleRepresentation.obj.specification.begin", resourceBundle);
		break;
	    case OBJECTIVES_SPECIFICATION_END_DATE:
		if (!foundObjectiveSpecificationEndPeriod)
		    foundObjectiveSpecificationEndPeriod = true;
		else throw new DomainException("error.duplicated.CustomScheduleRepresentation.obj.specification.end", resourceBundle);
		break;
	    case AUTOEVALUATION_BEGIN_DATE:
		if (!foundValidAutoEvaluationBeginPeriod)
		    foundValidAutoEvaluationBeginPeriod = true;
		else
		    throw new DomainException("error.duplicated.CustomScheduleRepresentation.autoevaluation.begin",
			    resourceBundle);
		break;
	    case AUTOEVALUATION_END_DATE:
		if (!foundValidAutoEvaluationEndPeriod)
		    foundValidAutoEvaluationEndPeriod = true;
		else
		    throw new DomainException("error.duplicated.CustomScheduleRepresentation.autoevaluation.end", resourceBundle);
		break;
	    case EVALUATION_BEGIN_DATE:
		if (!foundEvaluationBeginPeriod)
		    foundEvaluationBeginPeriod = true;
		else
		    throw new DomainException("error.duplicated.CustomScheduleRepresentation.evaluation.begin", resourceBundle);
		break;
	    case EVALUATION_END_DATE:
		if (!foundEvaluationEndPeriod)
		    foundEvaluationEndPeriod = true;
		else
		    throw new DomainException("error.duplicated.CustomScheduleRepresentation.evaluation.end", resourceBundle);
		break;
	    }

	}

    }

    private void addNewCustomScheduleRepresentation() {
	getCustomScheduleRepresentations().add(new CustomScheduleRepresentation(null, null, null));

    }

    public void removeCustomScheduleRepresentation(int i) {
	getCustomScheduleRepresentations().remove(i);
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

    private boolean customScheduleRepresentationsFilled() {
	for (CustomScheduleRepresentation customScheduleRepresentation : getCustomScheduleRepresentations()) {
	    if (!customScheduleRepresentation.isComplete()) {
		return false;
	    }
	}
	return true;
    }

    @Override
    public boolean hasAllneededInfo() {
	checkCustomScheduleRepresentations();
	return getSiadap() != null;
    }

    public List<CustomScheduleRepresentation> getCustomScheduleRepresentations() {
	return customScheduleRepresentations;
    }

    public void setCustomScheduleRepresentation(List<CustomScheduleRepresentation> customScheduleRepresentations) {
	this.customScheduleRepresentations = customScheduleRepresentations;
    }
}
