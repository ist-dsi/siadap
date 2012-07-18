/*
 * @(#)RevertStateActivityInformation.java
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

import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author João Antunes
 * 
 */
public class RevertStateActivityInformation extends ActivityInformation<SiadapProcess> {

    /**
     * Version of the class - currently 1st version
     */
    private static final long serialVersionUID = 1L;

    private String justification;

    private SiadapProcessStateEnum siadapProcessStateEnum;

    public RevertStateActivityInformation(SiadapProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
    }

    public void setSiadapProcessStateEnum(SiadapProcessStateEnum siadapProcessStateEnum) {
	this.siadapProcessStateEnum = siadapProcessStateEnum;
    }

    public SiadapProcessStateEnum getSiadapProcessStateEnum() {
	return siadapProcessStateEnum;
    }

    public void setJustification(String justification) {
	this.justification = justification;
    }

    public String getJustification() {
	return justification;
    }

    @Override
    public boolean hasAllneededInfo() {
	if (!StringUtils.isBlank(getJustification()) && getSiadapProcessStateEnum() != null)
	    return true;
	else
	    return false;
    }


}
