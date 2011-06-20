package module.siadap.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessSchedulesEnum;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

import org.joda.time.LocalDate;

public class ChangeCustomScheduleActivityInformation extends ActivityInformation<SiadapProcess> {

    private Siadap siadap;
    private List<CustomScheduleRepresentation> customScheduleRepresentations;

    public static class CustomScheduleRepresentation implements Serializable {
	private SiadapProcessSchedulesEnum typeOfSchedule;
	private String justification;
	private LocalDate newDeadlineDate;
	private final Siadap siadap;

	public CustomScheduleRepresentation(SiadapProcessSchedulesEnum typeOfSchedule, LocalDate newDeadlineDate,
		String justification, Siadap siadap) {
	    super();
	    this.typeOfSchedule = typeOfSchedule;
	    this.siadap = siadap;
	    this.newDeadlineDate = newDeadlineDate;
	    this.justification = justification;
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

	public void setTypeOfSchedule(SiadapProcessSchedulesEnum processSchedulesEnum) {
	    this.typeOfSchedule = processSchedulesEnum;
	}

	public boolean isComplete() {
	    if (getTypeOfSchedule() != null) {
		getTypeOfSchedule().validateDate(newDeadlineDate, siadap);
		return (getJustification() != null && getNewDeadlineDate() != null);
	    } else
		return false;
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
	setSiadap(process.getSiadap());
	setCustomScheduleRepresentation(new ArrayList<CustomScheduleRepresentation>());
	if (addCustomScheduleRepresentation) {
	    addCustomScheduleRepresentation();
	}
    }


    public void addCustomScheduleRepresentation() {
	getCustomScheduleRepresentations().add(new CustomScheduleRepresentation(null, null, null, siadap));

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
	//	checkCustomScheduleRepresentations();
	for (CustomScheduleRepresentation scheduleRepresentation : getCustomScheduleRepresentations()) {
	    if (!scheduleRepresentation.isComplete())
		return false;

	}
	return true;
    }

    public List<CustomScheduleRepresentation> getCustomScheduleRepresentations() {
	return customScheduleRepresentations;
    }

    public void setCustomScheduleRepresentation(List<CustomScheduleRepresentation> customScheduleRepresentations) {
	this.customScheduleRepresentations = customScheduleRepresentations;
    }


}
