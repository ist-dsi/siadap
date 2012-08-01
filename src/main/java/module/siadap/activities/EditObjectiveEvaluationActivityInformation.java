/*
 * @(#)EditObjectiveEvaluationActivityInformation.java
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

import org.apache.commons.lang.StringUtils;

import module.siadap.domain.ObjectiveEvaluation;
import module.siadap.domain.ObjectiveEvaluationIndicator;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

/**
 * 
 * @author João Antunes
 * @author Luis Cruz
 * @author Paulo Abrantes
 * 
 */
public class EditObjectiveEvaluationActivityInformation extends
		CreateObjectiveEvaluationActivityInformation {

	private ObjectiveEvaluation evaluation;
	private String justification;
	private boolean employJustification;

	public EditObjectiveEvaluationActivityInformation(
			SiadapProcess process,
			WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
		super(process, activity, false);
	if (process.getSiadap().getRequestedAcknowledgeDate() != null) {
			setEmployJustification(true);
		} else {
			setEmployJustification(false);

		}
	}

	public void setEvaluation(ObjectiveEvaluation evaluation) {
		this.evaluation = evaluation;
		setObjective(evaluation.getObjective());
		setType(evaluation.getType());
		for (ObjectiveEvaluationIndicator indicator : evaluation
				.getIndicators()) {
			addNewIndicator(indicator.getMeasurementIndicator(),
					indicator.getSuperationCriteria(),
					indicator.getPonderationFactor());
		}
	}

	public ObjectiveEvaluation getEvaluation() {
		return evaluation;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

	public String getJustification() {
		return justification;
	}

	@Override
	public boolean hasAllneededInfo() {
	return evaluation != null
		&& (!isEmployJustification() || (isEmployJustification() && !StringUtils.isEmpty(justification)))
		&& isForwardedFromInput() && !StringUtils.isEmpty(getObjective()) && indicatorsFilled();
	}

	public void setEmployJustification(boolean employJustification) {
		this.employJustification = employJustification;
	}

	public boolean isEmployJustification() {
		return employJustification;
	}

}
