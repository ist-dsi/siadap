/*
 * @(#)PartyWrapper.java
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.Predicate;
import org.joda.time.LocalDate;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.util.SiadapMiscUtilClass;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public abstract class PartyWrapper implements Serializable {

    private int year;
    private final SiadapYearConfiguration configuration;

    public PartyWrapper(int year) {
        this.year = year;
        this.configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public SiadapYearConfiguration getConfiguration() {
        return configuration;
    }

    protected abstract Party getParty();

    protected List<Unit> getParentUnits(AccountabilityType... types) {
        return getParentUnits(getParty(), new FilterAccountabilities(getYear(), true), types);
    }

    protected List<Unit> getParentUnits(Party party, AccountabilityType... types) {
        return getParentUnits(party, new FilterAccountabilities(getYear(), true), types);
    }

    protected List<Unit> getParentUnits(Party partyToConsider, Predicate predicate, AccountabilityType... types) {
        return partyToConsider == null ? Collections.emptyList() : partyToConsider.getParentAccountabilityStream()
                .filter(a -> match(a, types) && predicate == null || predicate.evaluate(a)).map(a -> a.getParent())
                .filter(p -> p.isUnit()).map(p -> (Unit) p).collect(Collectors.toList());
    }

    static boolean match(final Accountability a, final AccountabilityType... types) {
        if (types == null || types.length == 0) {
            return true;
        }
        final AccountabilityType atype = a.getAccountabilityType();
        for (final AccountabilityType type : types) {
            if (atype == type) {
                return true;
            }
        }
        return false;
    }

    protected List<Person> getParentPersons(AccountabilityType... types) {
        return getParentPersons(new FilterAccountabilities(getYear(), true), types);
    }

    private List<Person> getParentPersons(Predicate predicate, AccountabilityType... types) {
        return getParty().getParentAccountabilityStream()
                .filter(a -> match(a, types) && predicate == null || predicate.evaluate(a)).map(a -> a.getParent())
                .filter(p -> p.isPerson()).map(p -> (Person) p).collect(Collectors.toList());
    }

    protected List<Person> getChildPersons(AccountabilityType... types) {
        return getChildPersons(getParty(), new FilterAccountabilities(getYear(), true), types);
    }

    protected List<Person> getChildPersons(Party partyToConsider, AccountabilityType... types) {
        return getChildPersons(partyToConsider, new FilterAccountabilities(getYear(), true), types);
    }

    protected List<Person> getChildPersons(Party partyToConsider, Predicate predicate, AccountabilityType... types) {
        return partyToConsider == null ? Collections.emptyList() : partyToConsider.getChildAccountabilityStream()
                .filter(a -> match(a, types) && predicate == null || predicate.evaluate(a)).map(a -> a.getChild())
                .filter(p -> p.isPerson()).map(p -> (Person) p).collect(Collectors.toList());
    }

    protected List<Unit> getChildUnits(AccountabilityType... types) {
        return getChildUnits(new FilterAccountabilities(getYear(), true), types);
    }

    protected List<Unit> getChildUnits(Predicate predicate, AccountabilityType... types) {
        return getParty().getChildAccountabilityStream()
                .filter(a -> match(a, types) && predicate == null || predicate.evaluate(a)).map(a -> a.getChild())
                .filter(p -> p.isUnit()).map(p -> (Unit) p).collect(Collectors.toList());
    }

    protected List<Accountability> getParentAccountabilityTypes(AccountabilityType... types) {
        return getParentAccountabilityTypes(new FilterAccountabilities(getYear(), true), types);
    }

    protected List<Accountability> getParentAccountabilityTypes(Predicate predicate, AccountabilityType... types) {
        return getParty().getParentAccountabilityStream()
                .filter(a -> match(a, types) && predicate == null || predicate.evaluate(a)).collect(Collectors.toList());
    }

    protected List<Accountability> getChildAccountabilityTypes(AccountabilityType... types) {
        return getChildAccountabilityTypes(new FilterAccountabilities(getYear(), true), types);
    }

    protected List<Accountability> getChildAccountabilityTypes(Predicate predicate, AccountabilityType... types) {
        return getParty().getChildAccountabilityStream()
                .filter(a -> match(a, types) && predicate == null || predicate.evaluate(a)).collect(Collectors.toList());
    }

    public static class FilterAccountabilities implements Predicate {
        private final LocalDate begin;
        private final LocalDate end;
        private final boolean skipClosedAccountabilities;

        public FilterAccountabilities(int year, boolean skipClosedAccountabilities) {
            this.begin = SiadapMiscUtilClass.firstDayOfYear(year);
            this.end = SiadapMiscUtilClass.lastDayOfYear(year);
            this.skipClosedAccountabilities = skipClosedAccountabilities;
        }

        @Override
        public boolean evaluate(Object arg0) {
            Accountability accountability = (Accountability) arg0;
            LocalDate accountabilityStart = accountability.getBeginDate();
            LocalDate accountabilityEnd = accountability.getEndDate();

            return accountability.isActive(end.minusDays(1));

            //	    return ((accountabilityEnd == null && accountabilityStart.isBefore(end)) || (!skipClosedAccountabilities && accountability
            //		    .intersects(begin, end)));
        }
    }

}
