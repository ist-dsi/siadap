package module.siadap.activities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationObjectivesType;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

import org.apache.commons.lang.StringUtils;

public class CreateObjectiveEvaluationActivityInformation extends ActivityInformation<SiadapProcess> {

    private Siadap siadap;
    private String objective;
    private SiadapEvaluationObjectivesType type;
    private final List<ObjectiveIndicator> indicators;
    
    private static Integer HUNDRED_PERCENT = new Integer(100);

    public static class ObjectiveIndicator implements Serializable {
	String measurementIndicator;
	String superationCriteria;
	Integer ponderationFactor;

	public ObjectiveIndicator(String measurementIndicator, String superationCriteria, Integer ponderationFactor) {
	    super();
	    setMeasurementIndicator(measurementIndicator);
	    setSuperationCriteria(superationCriteria);
	    setPonderationFactor(ponderationFactor);
	}

	public Integer getPonderationFactor() {
	    return ponderationFactor;
	}
	
    public BigDecimal getBigDecimalPonderationFactor() {
    	return new BigDecimal(getPonderationFactor()).divide(new BigDecimal(100));
    }

	public void setPonderationFactor(Integer ponderationFactor) {
	    this.ponderationFactor = ponderationFactor;
	}

	public String getMeasurementIndicator() {
	    return measurementIndicator;
	}

	public void setMeasurementIndicator(String measurementIndicator) {
	    this.measurementIndicator = measurementIndicator;
	}

	public String getSuperationCriteria() {
	    return superationCriteria;
	}

	public void setSuperationCriteria(String superationCriteria) {
	    this.superationCriteria = superationCriteria;
	}

	public boolean isFilled() {
	    return !StringUtils.isEmpty(measurementIndicator) && !StringUtils.isEmpty(superationCriteria);
	}

    }

    public CreateObjectiveEvaluationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	this(process, activity, true);
    }

    protected CreateObjectiveEvaluationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity, boolean addIndicator) {
	super(process, activity);
	indicators = new ArrayList<ObjectiveIndicator>();
	if (addIndicator) {
	    addNewIndicator();
	}
    }

    public void addNewIndicator() {
	indicators.add(new ObjectiveIndicator(null, null, indicators.size() == 0 ? HUNDRED_PERCENT : null));
    }

    protected void addNewIndicator(String measurementIndicator, String superationCriteria, BigDecimal ponderationFactor) {
	indicators.add(new ObjectiveIndicator(measurementIndicator, superationCriteria, new Integer(ponderationFactor.multiply(new BigDecimal(100)).intValue())));
    }

    public void removeIndicator(int i) {
	indicators.remove(i);
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

    public String getObjective() {
	return objective;
    }

    public void setObjective(String objective) {
	this.objective = objective;
    }

    public SiadapEvaluationObjectivesType getType() {
	return type;
    }

    public void setType(SiadapEvaluationObjectivesType type) {
	this.type = type;
    }

    public List<ObjectiveIndicator> getIndicators() {
	return this.indicators;
    }

    @Override
    public boolean hasAllneededInfo() {
	return getSiadap() != null && !StringUtils.isEmpty(getObjective()) && indicatorsFilled() && getType() != null;
    }

    protected boolean indicatorsFilled() {
	if (indicators.size() == 0) {
	    return false;
	} else {
	    for (ObjectiveIndicator indicator : indicators) {
		if (!indicator.isFilled()) {
		    return false;
		}
	    }
	}
	return true;
    }
}
