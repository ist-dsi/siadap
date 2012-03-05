/*
 * @(#)EvaluationActivityInformation.java
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

import module.siadap.domain.SiadapEvaluation;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public class EvaluationActivityInformation extends ActivityInformation<SiadapProcess> {

    private String noEvaluationJustification;
    private String personalDevelopment;
    private String trainningNeeds;
    private String evaluationJustification;
    private Boolean excellencyAward;
    private String excellencyAwardJustification;

    public EvaluationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
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
	    setExcellencyAwardJustification(evaluationData.getExcellencyAwardJustification());
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

    public String getExcellencyAwardJustification() {
	return excellencyAwardJustification;
    }

    public void setExcellencyAwardJustification(String excellencyAwardJustification) {
	this.excellencyAwardJustification = excellencyAwardJustification;
    }
}
