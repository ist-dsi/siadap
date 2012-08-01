/*
 * @(#)SiadapYearsFromExistingSiadapConfigurations.java
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

import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
import pt.ist.fenixWebFramework.renderers.DataProvider;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;

import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;

/**
 * 
 * @author João Antunes
 * 
 */
public class SiadapYearsFromExistingSiadapConfigurations implements DataProvider {

    /*
     * (non-Javadoc)
     * 
     * @see
     * pt.ist.fenixWebFramework.renderers.DataProvider#provide(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public Object provide(Object source, Object currentValue) {
	return getYearsWithExistingConfigs();
    }

    /**
     * Method wrapper which is used by other interface packages other than the
     * renders
     * 
     * @return an {@link ArrayList} of Integers with the years that have a
     *         configuration associated
     */
    static public ArrayList<Integer> getYearsWithExistingConfigs() {
	ArrayList<Integer> years = new ArrayList<Integer>();
	for (SiadapYearConfiguration siadapYearConfiguration : SiadapRootModule.getInstance().getYearConfigurations()) {
	    //let's make the 2010 year disappear for all of the users which aren't on the test group
	    if (siadapYearConfiguration.getYear() == 2010
		    && !SiadapRootModule.getInstance().getSiadapTestUserGroup().hasUsers(UserView.getCurrentUser()))
		continue;
	    else {
		years.add(new Integer(siadapYearConfiguration.getYear()));
	    }
	}
	Collections.sort(years);
	return years;
    }

    /*
     * (non-Javadoc)
     * 
     * @see pt.ist.fenixWebFramework.renderers.DataProvider#getConverter()
     */
    @Override
    public Converter getConverter() {
	return null;
	// return new Converter() {
	//
	// @Override
	// public Object convert(Class type, Object value) {
	// if (value != null)
	// return ((SiadapYearConfiguration) value).getYear();
	// return null;
	// }
	// };
    }

}
