/*
 * @(#)SiadapProcessStateEnumWrapper.java
 *
 * Copyright 2012 Instituto Superior Tecnico
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
package module.siadap.domain.wrappers;

import java.io.Serializable;

import module.siadap.domain.SiadapProcessStateEnum;

/**
 * The purpose of this class is to provide to the interface a bean with the
 * SiadapProcessStateEnum that is selected so that the SIADAP processes can be
 * filtered by state (initially useful for the statistics
 * [SiadapProcessCountAction] interface)
 * 
 * @author João Antunes
 * 
 */
public class SiadapProcessStateEnumWrapper implements Serializable {
	
	
    /**
     * Default serial version id
     */
    private static final long serialVersionUID = 1L;
    private SiadapProcessStateEnum processStateEnum;
	
	
    public SiadapProcessStateEnumWrapper(SiadapProcessStateEnum stateEnum)
	{
	this.setProcessStateEnum(stateEnum);

	}


    public SiadapProcessStateEnum getProcessStateEnum() {
	return processStateEnum;
	}

    public void setProcessStateEnum(SiadapProcessStateEnum processStateEnum) {
	this.processStateEnum = processStateEnum;
	}


}
