/*
 * @(#)CreateObjectiveEvaluationActivityInformation.java
 *
 * Copyright 2010 Instituto Superior Tecnico
 * Founding Authors: Paulo Abrantes
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the SIADAP Module.
 *
 *   The SIADAP Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version
 *   3 of the License, or (at your option) any later version.
 *
 *   The SIADAP Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the SIADAP Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.siadap.activities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationObjectivesType;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.exceptions.SiadapException;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
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

    public void addNewIndicator() throws SiadapException {
        checkIndicatorsSize();
        indicators.add(new ObjectiveIndicator(null, null, indicators.size() == 0 ? HUNDRED_PERCENT : null));
    }

    private void checkIndicatorsSize() {
        Integer maxNrIndicators = getSiadap().getSiadapYearConfiguration().getMaximumNumberOfObjectiveIndicators();
        if (maxNrIndicators != null && indicators.size() >= maxNrIndicators) {
            throw new SiadapException("ObjectiveEvaluation.maximum.nr.of.indicators.reached", maxNrIndicators.toString());
        }
    }

    protected void addNewIndicator(String measurementIndicator, String superationCriteria, BigDecimal ponderationFactor)
            throws SiadapException {
        checkIndicatorsSize();
        indicators.add(new ObjectiveIndicator(measurementIndicator, superationCriteria, new Integer(ponderationFactor.multiply(
                new BigDecimal(100)).intValue())));
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
