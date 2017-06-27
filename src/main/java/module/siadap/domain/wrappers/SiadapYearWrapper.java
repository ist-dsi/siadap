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
import java.util.ArrayList;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.presentationTier.renderers.providers.SiadapYearsFromExistingSiadapConfigurations;

import org.apache.commons.lang.ObjectUtils;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.joda.time.LocalDate;

/**
 * The purpose of this class is to provide to the interface a bean with the year
 * that is selected so that the SIADAP processes can be listed by year
 * 
 * @author João Antunes
 * 
 */
public class SiadapYearWrapper implements Serializable, Comparable<SiadapYearWrapper> {

    private String label;
    Integer year;
    private static final String NEW = "label.SiadapYearConfiguration.new";

    public static SiadapYearWrapper getCurrentYearOrLatestAvailableWrapper() {
        SiadapYearWrapper siadapYearWrapper = null;
        ArrayList<Integer> yearsWithConfigs = SiadapYearsFromExistingSiadapConfigurations.getYearsWithExistingConfigs();
        int year = new LocalDate().getYear();
        if (yearsWithConfigs.size() == 0) {
            SiadapYearConfiguration.createNewSiadapYearConfiguration(String.valueOf(year));
            yearsWithConfigs = SiadapYearsFromExistingSiadapConfigurations.getYearsWithExistingConfigs();
        }
        if (yearsWithConfigs.contains(new Integer(new LocalDate().getYear()))) {
            siadapYearWrapper = new SiadapYearWrapper(year);
        } else {
            siadapYearWrapper = new SiadapYearWrapper(yearsWithConfigs.get(yearsWithConfigs.size() - 1));
        }

        return siadapYearWrapper;

    }

    public static SiadapYearWrapper getPreviousYearOrLatestAvailableWrapper() {
        Integer currentYear = new LocalDate().getYear();
        SiadapYearWrapper siadapYearWrapper = null;
        ArrayList<Integer> yearsWithConfigs = SiadapYearsFromExistingSiadapConfigurations.getYearsWithExistingConfigs();
        if (SiadapYearsFromExistingSiadapConfigurations.getYearsWithExistingConfigs().size() < 1
                && SiadapYearConfiguration.getSiadapYearConfiguration(currentYear) != null) {
            siadapYearWrapper =
                    new SiadapYearWrapper(SiadapYearConfiguration.getSiadapYearConfiguration(currentYear)
                            .getPreviousSiadapYearConfiguration().getYear());
        } else if (yearsWithConfigs.contains(new Integer(currentYear - 1))) {
            int year = new LocalDate().getYear() - 1;
            siadapYearWrapper = new SiadapYearWrapper(year);
        } else {
            siadapYearWrapper = getCurrentYearOrLatestAvailableWrapper();
        }
        return siadapYearWrapper;

    }
    @SuppressWarnings("boxing")
    public SiadapYearWrapper(int year) {
        setChosenYear(year);
//		for(SiadapYearConfiguration siadapYearConfiguration : SiadapRootModule.getInstance().getYearConfigurations())
//		{
//			if (siadapYearConfiguration.getYear() == year)
//				{this.siadapYearConfiguration = siadapYearConfiguration;}
//		}
    }

    public SiadapYearConfiguration getSiadapYearConfiguration() {
        return SiadapYearConfiguration.getSiadapYearConfiguration(getChosenYear());
    }

    public void setChosenYear(Integer chosenYear) {
        this.year = chosenYear;
        SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(chosenYear);
        this.label = siadapYearConfiguration == null ? getNewYearLabel() : siadapYearConfiguration.getLabel();
    }

    public Integer getChosenYear() {
        return year;
    }

    public String getChosenYearLabel() {
        return label;
    }

    @Override
    public int compareTo(SiadapYearWrapper o) {
        if (o == null) {
            return 1;
        }
        return ObjectUtils.compare(year, o.getChosenYear());
    }

    public void setChosenYearLabel(String label) {
        SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(label);
        this.label = siadapYearConfiguration == null ? getNewYearLabel() : label;
        this.year = siadapYearConfiguration == null ? SiadapYearConfiguration.getNextYear() : siadapYearConfiguration.getYear();
    }

    public static String getNewYearLabel() {
        return BundleUtil.getString(Siadap.SIADAP_BUNDLE_STRING, NEW);
    }

}
