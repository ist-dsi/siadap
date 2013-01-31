/*
 * @(#)RemoveCustomScheduleActivityInformation.java
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

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessSchedulesEnum;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

/**
 * 
 * @author João Antunes
 * 
 */
public class RemoveCustomScheduleActivityInformation extends ActivityInformation<SiadapProcess> {

	private String siadapProcessSchedulesEnumToRemove;
	private Siadap siadap;

	public RemoveCustomScheduleActivityInformation(SiadapProcess process,
			WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
		super(process, activity);
	}

	public void setSiadapProcessSchedulesEnumToRemove(String siadapProcessSchedulesEnumToRemove) {
		this.siadapProcessSchedulesEnumToRemove = siadapProcessSchedulesEnumToRemove;
	}

	public String getSiadapProcessSchedulesEnumToRemove() {
		return siadapProcessSchedulesEnumToRemove;
	}

	@Override
	public boolean hasAllneededInfo() {
		if (getSiadapProcessSchedulesEnumToRemove() != null
				&& SiadapProcessSchedulesEnum.valueOf(getSiadapProcessSchedulesEnumToRemove()) instanceof SiadapProcessSchedulesEnum) {
			return true;
		}
		return false;
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

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

}
