/*
 * @(#)SiadapMiscUtilClass.java
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
package module.siadap.domain.util;

import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;

/**
 * 
 * Class with Misc. utility methods
 * 
 * @author João Antunes
 * 
 */
public class SiadapMiscUtilClass {
    /**
     * 
     * @param date
     *            the {@link LocalDate} that will be converted to represent the
     *            date at the beginning of the day
     * @return an {@link ReadableInstant} with the same day/month/year but the
     *         last instant of it, that is the last hour, last minute, last
     *         second etc...
     */
    public static ReadableInstant convertDateToEndOfDay(LocalDate date) {
        ReadableInstant newLocalDate = null;
        if (date != null) {
            return new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 23, 59, 59, 59);

        }
        return newLocalDate;

    }

    /**
     * 
     * @return if there is no SiadapConfiguration for the current year, it
     *         returns the year for which there's a configuration
     */
    public static int returnLastUsableYear() {
        int yearToUse = new LocalDate().getYear();
        if (SiadapYearConfiguration.getSiadapYearConfiguration(yearToUse) == null) {
            yearToUse = 0;
            for (SiadapYearConfiguration yearConfiguration : SiadapRootModule.getInstance().getYearConfigurations()) {
                int year = yearConfiguration.getYear();
                if (year > yearToUse && yearConfiguration.getStructureManagementGroupMembers() != null) {
                    yearToUse = year;
                }
            }
        }
        return yearToUse;

    }

    /**
     * 
     * @param date
     *            the {@link LocalDate} that will be converted to represent the
     *            date at the beginning of the day
     * @return an {@link ReadableInstant} with the same day/month/year but the
     *         first instant of it, that is the first hour, first minute, first
     *         second etc...
     */
    public static ReadableInstant convertDateToBeginOfDay(LocalDate date) {
        ReadableInstant newLocalDate = null;
        if (date != null) {
            return date.toDateTimeAtStartOfDay();
        }
        return newLocalDate;

    }

    public static LocalDate lastDayOfYear(int year) {
        return new LocalDate(year, 12, 31);
    }

    /**
     * It is useful to get the previous to the last day due to the fact that an
     * accountability is considered not active in the last day
     * 
     * @param year
     * @return
     */
    public static LocalDate lastDayOfYearWhereAccsAreActive(int year) {
        return new LocalDate(year, 12, 30);
    }

    public static LocalDate firstDayOfYear(Integer year) {
        return new LocalDate(year, 1, 1);
    }

}
