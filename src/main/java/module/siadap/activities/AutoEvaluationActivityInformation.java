/*
 * @(#)AutoEvaluationActivityInformation.java
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

import module.siadap.domain.SiadapAutoEvaluation;
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
	SiadapAutoEvaluation autoEvaluationData = process.getSiadap().getAutoEvaluationData2();

	if (autoEvaluationData != null) {

	    setFactorOneClassification(autoEvaluationData.getFactorOneClassification());
	    setFactorTwoClassification(autoEvaluationData.getFactorTwoClassification());
	    setFactorThreeClassification(autoEvaluationData.getFactorThreeClassification());
	    setFactorFourClassification(autoEvaluationData.getFactorFourClassification());
	    setFactorFiveClassification(autoEvaluationData.getFactorFiveClassification());
	    setFactorSixClassification(autoEvaluationData.getFactorSixClassification());

	    setObjectivesJustification(autoEvaluationData.getObjectivesJustification());
	    setCompetencesJustification(autoEvaluationData.getCompetencesJustification());
	    setOtherFactorsJustification(autoEvaluationData.getOtherFactorsJustification());
	    setExtremesJustification(autoEvaluationData.getExtremesJustification());
	    setCommentsAndProposals(autoEvaluationData.getCommentsAndProposals());
	}
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
