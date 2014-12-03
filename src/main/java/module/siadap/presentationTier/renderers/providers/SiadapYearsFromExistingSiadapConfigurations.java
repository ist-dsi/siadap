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
import java.util.Set;
import java.util.TreeSet;

import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;

import org.fenixedu.bennu.core.security.Authenticate;

import pt.ist.fenixWebFramework.renderers.DataProvider;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;

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
        return getLabelsWithExistingConfigs();
    }

    /**
     * Method wrapper which is used by other interface packages other than the
     * renders
     * 
     * @return an {@link ArrayList} of Strings with the {@link SiadapYearConfiguration#getLabel()} that have a
     *         configuration associated
     */
    static public Set<String> getLabelsWithExistingConfigs() {
        Set<String> years = new TreeSet<String>();
        for (SiadapYearConfiguration siadapYearConfiguration : SiadapRootModule.getInstance().getYearConfigurations()) {
            years.add(siadapYearConfiguration.getLabel());
        }
        return years;
    }

    /**
     * Method wrapper which is used by other interface packages other than the
     * renders
     * 
     * @return an {@link ArrayList} of Strings with the {@link SiadapYearConfiguration#getLabel()} that have a
     *         configuration associated
     */
    static public ArrayList<Integer> getYearsWithExistingConfigs() {
        ArrayList<Integer> years = new ArrayList<Integer>();
        for (SiadapYearConfiguration siadapYearConfiguration : SiadapRootModule.getInstance().getYearConfigurations()) {
            years.add(siadapYearConfiguration.getYear());
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
