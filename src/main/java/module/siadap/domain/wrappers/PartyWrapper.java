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
import java.util.ArrayList;
import java.util.List;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;

import org.apache.commons.collections.Predicate;
import org.jfree.data.time.Month;
import org.joda.time.LocalDate;

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
        List<Unit> units = new ArrayList<Unit>();
        if (partyToConsider != null) {
            for (Accountability accountability : partyToConsider.getParentAccountabilities(types)) {
                if (predicate == null || predicate.evaluate(accountability)) {
                    Party parent = accountability.getParent();
                    if (parent.isUnit()) {
                        units.add(((Unit) parent));
                    }
                }
            }
        }
        return units;
    }

    protected List<Person> getParentPersons(AccountabilityType... types) {
        return getParentPersons(new FilterAccountabilities(getYear(), true), types);
    }

    private List<Person> getParentPersons(Predicate predicate, AccountabilityType... types) {
        List<Person> person = new ArrayList<Person>();
        for (Accountability accountability : getParty().getParentAccountabilities(types)) {
            if (predicate == null || predicate.evaluate(accountability)) {
                Party parent = accountability.getParent();
                if (parent.isPerson()) {
                    person.add(((Person) parent));
                }
            }
        }
        return person;
    }

    protected List<Person> getChildPersons(AccountabilityType... types) {
        return getChildPersons(getParty(), new FilterAccountabilities(getYear(), true), types);
    }

    protected List<Person> getChildPersons(Party partyToConsider, AccountabilityType... types) {
        return getChildPersons(partyToConsider, new FilterAccountabilities(getYear(), true), types);
    }

    protected List<Person> getChildPersons(Party partyToConsider, Predicate predicate, AccountabilityType... types) {
        List<Person> people = new ArrayList<Person>();
        if (partyToConsider != null) {
            for (Accountability accountability : partyToConsider.getChildrenAccountabilities(types)) {
                if (predicate == null || predicate.evaluate(accountability)) {
                    Party parent = accountability.getChild();
                    if (parent.isPerson()) {
                        people.add(((Person) parent));
                    }
                }
            }
        }
        return people;
    }

    protected List<Unit> getChildUnits(AccountabilityType... types) {
        return getChildUnits(new FilterAccountabilities(getYear(), true), types);
    }

    protected List<Unit> getChildUnits(Predicate predicate, AccountabilityType... types) {
        List<Unit> units = new ArrayList<Unit>();
        for (Accountability accountability : getParty().getChildrenAccountabilities(types)) {
            if (predicate == null || predicate.evaluate(accountability)) {
                Party parent = accountability.getChild();
                if (parent.isUnit()) {
                    units.add(((Unit) parent));
                }
            }
        }
        return units;
    }

    protected List<Accountability> getParentAccountabilityTypes(AccountabilityType... types) {
        return getParentAccountabilityTypes(new FilterAccountabilities(getYear(), true), types);
    }

    protected List<Accountability> getParentAccountabilityTypes(Predicate predicate, AccountabilityType... types) {
        List<Accountability> accountabilityTypes = new ArrayList<Accountability>();
        for (Accountability accountability : getParty().getParentAccountabilities(types)) {
            if (predicate == null || predicate.evaluate(accountability)) {
                accountabilityTypes.add(accountability);
            }
        }
        return accountabilityTypes;
    }

    protected List<Accountability> getChildAccountabilityTypes(AccountabilityType... types) {
        return getChildAccountabilityTypes(new FilterAccountabilities(getYear(), true), types);
    }

    protected List<Accountability> getChildAccountabilityTypes(Predicate predicate, AccountabilityType... types) {
        List<Accountability> accountabilityTypes = new ArrayList<Accountability>();
        for (Accountability accountability : getParty().getChildrenAccountabilities(types)) {
            if (predicate == null || predicate.evaluate(accountability)) {
                accountabilityTypes.add(accountability);
            }
        }
        return accountabilityTypes;
    }

    public static class FilterAccountabilities implements Predicate {
        private final LocalDate begin;
        private final LocalDate end;
        private final boolean skipClosedAccountabilities;

        public FilterAccountabilities(int year, boolean skipClosedAccountabilities) {
            this.begin = new LocalDate(year, Month.JANUARY, 1);
            this.end = new LocalDate(year, Month.DECEMBER, 31);
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
