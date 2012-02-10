/**
 * 
 */
package module.siadap.domain.util;

import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 26 de Dez de 2011
 * 
 *         Class with Misc. utility methods
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
		if (year > yearToUse && yearConfiguration.getStructureManagementGroupMembers() != null)
		    yearToUse = year;
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

    /**
     * 
     * @param object1
     * @param object2
     * @return true if object1 and object2 are the same. If both are null this
     *         returns true
     */
    public static boolean isObjectEqual(Object object1, Object object2) {
	if (object1 == null && object2 == null)
	    return true;
	if (object1 != null)
	    return object1.equals(object2);
	if (object2 != null)
	    return object2.equals(object1);
	return false;
    }

}
