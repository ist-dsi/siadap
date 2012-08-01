/*
 * @(#)SiadapYearWrapper.java
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
package module.siadap.domain.wrappers;

import java.io.Serializable;

import module.siadap.domain.SiadapYearConfiguration;

/**
 * The purpose of this class is to provide to the interface a bean with the year
 * that is selected so that the SIADAP processes can be listed by year
 * 
 * @author João Antunes
 * 
 */
public class SiadapYearWrapper implements Serializable {
	
	
	private Integer chosenYear;
	
	
	public SiadapYearWrapper(int year)
	{
		chosenYear = new Integer(year);
//		for(SiadapYearConfiguration siadapYearConfiguration : SiadapRootModule.getInstance().getYearConfigurations())
//		{
//			if (siadapYearConfiguration.getYear() == year)
//				{this.siadapYearConfiguration = siadapYearConfiguration;}
//		}
	}


	public SiadapYearConfiguration getSiadapYearConfiguration() {
		return SiadapYearConfiguration.getSiadapYearConfiguration(chosenYear);
	}

	public void setChosenYear(Integer chosenYear) {
		this.chosenYear = chosenYear;
	}

	public Integer getChosenYear() {
		return chosenYear;
	}
}
