/*
 * @(#)RevertStatesProvider.java
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
package module.siadap.presentationTier.renderers.providers;

import java.util.ArrayList;

import pt.ist.fenixWebFramework.renderers.DataProvider;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.workflow.activities.ActivityInformation;

/**
 * 
 * @author João Antunes
 * 
 */
public class RevertStatesProvider implements DataProvider {

    @Override
    public Converter getConverter() {
	return null;
    }

    @Override
    public Object provide(Object source, Object currentValue) {
	ActivityInformation<SiadapProcess> activityInformation = (ActivityInformation<SiadapProcess>) source;
	ArrayList<SiadapProcessStateEnum> enums = new ArrayList<SiadapProcessStateEnum>();
	SiadapProcess siadapProcess = activityInformation.getProcess();
	Siadap siadap = siadapProcess.getSiadap();
	//	SiadapProcessStateEnum state = SiadapProcessStateEnum.getState(siadapProcess.getSiadap());
	//	switch (state) {
	//	case value:
	//	    
	//	    break;
	//
	//	default:
	//	    break;
	//	}
	if (siadapProcess.isProcessSealed()) {
	    enums.add(SiadapProcessStateEnum.NOT_SEALED);
	}
	if (siadapProcess.isSubmittedForEvalObjsConfirmation()) {
	    enums.add(SiadapProcessStateEnum.NOT_YET_SUBMITTED_FOR_ACK);
	}
	if (siadapProcess.isEvalObjectivesAcknowledged()) {
	    enums.add(SiadapProcessStateEnum.WAITING_EVAL_OBJ_ACK);
	}
	if (siadap.isAutoEvaliationDone()) {
	    enums.add(SiadapProcessStateEnum.WAITING_SELF_EVALUATION);
	}
	if (siadap.isDefaultEvaluationDone()) {
	    enums.add(SiadapProcessStateEnum.NOT_YET_EVALUATED);
	}
	return enums;
    }

}
