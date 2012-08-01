/*
 * @(#)CurricularPonderationAttribution.java
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

import org.apache.commons.lang.StringUtils;

import pt.ist.bennu.core.domain.User;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

/**
 * 
 * @author João Antunes
 * 
 */
public class CurricularPonderationAttribution extends WorkflowActivity<SiadapProcess, CurricularPonderationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	if (!process.isActive())
	    return false;
	Siadap siadap = process.getSiadap();
	SiadapYearConfiguration siadapYearConfiguration = siadap.getSiadapYearConfiguration();
	if (!siadapYearConfiguration.getClosedValidation() && !siadap.hasAnAssociatedCurricularPonderationEval()
		&& siadapYearConfiguration.isCurrentUserMemberOfCCA())
	    return true;
	return false;
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
	return false;
    }

    @Override
    protected void process(CurricularPonderationActivityInformation activityInformation) {
	Siadap siadap = activityInformation.getSiadap();
	siadap.createCurricularPonderation(activityInformation.getSiadapUniverseToApply(),
		activityInformation.getAssignedGrade(), activityInformation.getAssignExcellentGrade(),
		activityInformation.getExcellentGradeJustification(), activityInformation.getCurricularPonderationRemarks(),
		activityInformation.getEvaluator());
    }

    //Ponderação curricular atríbuida no universo {0} com a nota {1} ({2}) - Observações "{3}"
    @Override
    protected String[] getArgumentsDescription(CurricularPonderationActivityInformation activityInformation) {
	String observations = (StringUtils.isEmpty(activityInformation.getExcellentGradeJustification())) ? activityInformation
		.getCurricularPonderationRemarks() : activityInformation.getCurricularPonderationRemarks() + " - "
		+ activityInformation.getExcellentGradeJustification();
	return new String[] {
		activityInformation.getSiadapUniverseToApply().getLocalizedName(),
		activityInformation.getAssignedGrade().toString(),
		SiadapGlobalEvaluation.getGlobalEvaluation(activityInformation.getAssignedGrade(),
			activityInformation.getAssignExcellentGrade().booleanValue()).getLocalizedName(), observations,
		activityInformation.getEvaluator().getPresentationName() };
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
	return new CurricularPonderationActivityInformation(process, this);
    }

    @Override
    public String getUsedBundle() {
	return Siadap.SIADAP_BUNDLE_STRING;
    }
}
