/*
 * @(#)SiadapYearsFromExistingSiadapCfgPlusOne.java
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
package module.siadap.presentationTier.renderers.providers;

import java.util.ArrayList;
import java.util.Collections;

import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;

import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.renderers.DataProvider;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;

/**
 * Gets the SiadapYearConfiguration years plus one and eventually the current we are at
 * 
 * @author joantune (joao.antunes@tagus.ist.utl.pt)
 * 
 * @author João Antunes
 * 
 */
public class SiadapYearsFromExistingSiadapCfgPlusOne implements DataProvider {

    /* (non-Javadoc)
     * @see pt.ist.fenixWebFramework.renderers.DataProvider#provide(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object provide(Object source, Object currentValue) {
        ArrayList<Integer> years = new ArrayList<Integer>();
        int maximumYear = 0;
        for (SiadapYearConfiguration siadapYearConfiguration : SiadapRootModule.getInstance().getYearConfigurations()) {
            years.add(new Integer(siadapYearConfiguration.getYear()));
            if (siadapYearConfiguration.getYear() > maximumYear) {
                maximumYear = siadapYearConfiguration.getYear();
            }
        }
        Integer currentYear = new Integer(new LocalDate().getYear());
        if (!years.contains(currentYear)) {
            years.add(currentYear);
        }
        Integer nextYear = new Integer(maximumYear + 1);
        if (!years.contains(nextYear)) {
            years.add(nextYear);
        }
        Collections.sort(years);
        return years;
    }

    /* (non-Javadoc)
     * @see pt.ist.fenixWebFramework.renderers.DataProvider#getConverter()
     */
    @Override
    public Converter getConverter() {
        return null;
//		return new Converter() {
//			
//			@Override
//			public Object convert(Class type, Object value) {
//				if (value != null)
//					return ((SiadapYearConfiguration) value).getYear();
//				return null;
//			}
//		};
    }

}
