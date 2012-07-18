/*
 * @(#)CurricularPonderationActivityInformation.java
 *
 * Copyright 2011 Instituto Superior Tecnico
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

import java.math.BigDecimal;
import java.util.ArrayList;

import module.organization.domain.Person;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

/**
 * 
 * @author João Antunes
 * 
 */
public class CurricularPonderationActivityInformation extends ActivityInformation<SiadapProcess> {

    private final SiadapUniverse siadapUniverseToApply;

    private String curricularPonderationRemarks;

    private Boolean assignExcellentGrade;

    private String excellentGradeJustification;

    private BigDecimal assignedGrade;

    private Person evaluator;

    private final Siadap siadap;

    public CurricularPonderationActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
	this.siadap = process.getSiadap();
	ArrayList<SiadapUniverse> freeUniversesToUse = new ArrayList<SiadapUniverse>();
	freeUniversesToUse.add(SiadapUniverse.SIADAP2);
	freeUniversesToUse.add(SiadapUniverse.SIADAP3);
	for (SiadapEvaluationUniverse evaluationUniverse : process.getSiadap().getSiadapEvaluationUniverses()) {
	    freeUniversesToUse.remove(evaluationUniverse.getSiadapUniverse());
	}
	if (freeUniversesToUse.size() != 1) {
	    throw new SiadapException("error.inconsistancy.user.has.more.than.1.evaluation.for.a.siadap.universe");
	}
	this.siadapUniverseToApply = freeUniversesToUse.get(0);
    }

    @Override
    public boolean hasAllneededInfo() {
	if (evaluator != null && siadapUniverseToApply != null && curricularPonderationRemarks != null
		&& assignExcellentGrade != null
		&& assignedGrade != null
		&& SiadapGlobalEvaluation.isValidGrade(assignedGrade, assignExcellentGrade.booleanValue()))
	    return true;
	return false;
    }

    public SiadapUniverse getSiadapUniverseToApply() {
	return siadapUniverseToApply;
    }

    public String getCurricularPonderationRemarks() {
	return curricularPonderationRemarks;
    }

    public void setCurricularPonderationRemarks(String curricularPonderationRemarks) {
	this.curricularPonderationRemarks = curricularPonderationRemarks;
    }

    public Boolean getAssignExcellentGrade() {
	return assignExcellentGrade;
    }

    public void setAssignExcellentGrade(Boolean assignExcellentGrade) {
	this.assignExcellentGrade = assignExcellentGrade;
    }

    public BigDecimal getAssignedGrade() {
	return assignedGrade;
    }

    public void setAssignedGrade(BigDecimal assignedGrade) {
	this.assignedGrade = assignedGrade;
    }

    public Siadap getSiadap() {
	return siadap;
    }

    public String getExcellentGradeJustification() {
	return excellentGradeJustification;
    }

    public void setExcellentGradeJustification(String excellentGradeJustification) {
	this.excellentGradeJustification = excellentGradeJustification;
    }

    public Person getEvaluator() {
	return evaluator;
    }

    public void setEvaluator(Person evaluator) {
	this.evaluator = evaluator;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

}
