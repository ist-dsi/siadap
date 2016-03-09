/*
 * @(#)SiadapProcessCounter.java
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import javax.naming.OperationNotSupportedException;

import org.joda.time.LocalDate;

import com.google.common.base.Predicate;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.presentationTier.actions.UnitManagementInterfaceAction.Mode;

/**
 * 
 * @author João Antunes
 * @author Luis Cruz
 * 
 */
public class SiadapProcessCounter implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int counts[] = new int[SiadapProcessStateEnum.values().length];

    private final HashMap<Boolean, HashMap<String, NumberAndGradeCounter>> countsByQuotaAndCategories =
            new HashMap<Boolean, HashMap<String, NumberAndGradeCounter>>();

    private final LocalDate dayToUse;
    private final transient SiadapYearConfiguration configuration;
    private final transient AccountabilityType unitRelations;
//	private final transient AccountabilityType evaluationRelation;
    private final transient AccountabilityType firstKindOfWorkerRelation;
    private final transient AccountabilityType secondKindOfWorkerRelation;

    private final Multiset<Person> duplicatedPersons = ConcurrentHashMultiset.create();
    private final Multiset<Unit> duplicatedUnit = ConcurrentHashMultiset.create();
    private final boolean filteredDuplicatedPersonsSet = false;
    private final boolean filteredDuplicatedUnitSet = false;
    private final Unit topUnit;

    private boolean gatherSiadaps = true;

    private final Set<Siadap> siadaps = new HashSet<Siadap>();

    private final Set<Siadap> curricularPonderationSiadaps = new HashSet<Siadap>();

    private final Map<Integer, Set<Siadap>> notListedSiadaps = new HashMap<Integer, Set<Siadap>>();

    public SiadapProcessCounter(final Unit unit, boolean distinguishBetweenUniverses, SiadapYearConfiguration configuration,
            boolean gatherSiadaps) {
        this(unit, distinguishBetweenUniverses, configuration);
        this.gatherSiadaps = gatherSiadaps;

    }

    private SiadapProcessCounter(final Unit unit, boolean distinguishBetweenUniverses, SiadapYearConfiguration configuration,
            AccountabilityType unitAccType, AccountabilityType firstKindOfAccType, AccountabilityType secondKindOfAccType) {
        topUnit = unit;
        this.configuration = configuration;
        this.dayToUse = SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(configuration.getYear());
        unitRelations = unitAccType;
//      evaluationRelation = configuration.getEvaluationRelation();
        firstKindOfWorkerRelation = firstKindOfAccType;
        secondKindOfWorkerRelation = secondKindOfAccType;
        init(distinguishBetweenUniverses);
    }

    public SiadapProcessCounter(final Unit unit, boolean distinguishBetweenUniverses, SiadapYearConfiguration configuration,
            Mode mode) throws OperationNotSupportedException {
        this(unit, distinguishBetweenUniverses, configuration, mode.getUnitAccType(configuration),
                mode.getEmployeeAccTypes(configuration)[0], mode.getEmployeeAccTypes(configuration)[1]);
        AccountabilityType[] employeeAccTypes = mode.getEmployeeAccTypes(configuration);
        if (employeeAccTypes.length != 2) {
            throw new OperationNotSupportedException("do not support Mode: " + mode.name());
        }

    }

    public SiadapProcessCounter(final Unit unit, boolean distinguishBetweenUniverses, SiadapYearConfiguration configuration) {
        this(unit, distinguishBetweenUniverses, configuration, configuration.getUnitRelations(),
                configuration.getWorkingRelation(), configuration.getWorkingRelationWithNoQuota());

    }

    void init(boolean distinguishBetweenUniverses) {
        if (distinguishBetweenUniverses) {
            count(topUnit, distinguishBetweenUniverses);
            // if we are distinguishing between universes, let's also include
            // the not lister persons
            for (Siadap siadap : getOrCreateSiadapsNotListed()) {
                PersonSiadapWrapper siadapWrapper = new PersonSiadapWrapper(siadap);
                count(siadapWrapper.getPerson(), siadapWrapper.isQuotaAware());
            }
        } else {
            count(topUnit);
        }
    }

    private void count(Unit unit, boolean distinguishBetweenUniverses) {
        unit.getChildAccountabilityStream().filter(a -> a.isActive(dayToUse)).forEach(new Consumer<Accountability>() {
            @Override
            public void accept(Accountability accountability) {
                final AccountabilityType accountabilityType = accountability.getAccountabilityType();
                if (accountabilityType == unitRelations) {
                    final Unit child = (Unit) accountability.getChild();
                    duplicatedUnit.add(child);
                    count(child, distinguishBetweenUniverses);
                } else if (accountabilityType == firstKindOfWorkerRelation) {
                    final Person person = (Person) accountability.getChild();
                    count(person, true);

                } else if (accountabilityType == secondKindOfWorkerRelation) {
                    final Person person = (Person) accountability.getChild();
                    count(person, false);

                }

            }
        });
    }

    private void count(final Unit unit) {
        unit.getChildAccountabilityStream().filter(a -> a.isActive(dayToUse)).forEach(new Consumer<Accountability>() {
            @Override
            public void accept(Accountability accountability) {
                final AccountabilityType accountabilityType = accountability.getAccountabilityType();
                if (accountabilityType == unitRelations) {
                    final Unit child = (Unit) accountability.getChild();
                    duplicatedUnit.add(child);
                    count(child);
                } else if (accountabilityType == firstKindOfWorkerRelation || accountabilityType == secondKindOfWorkerRelation) {
                    final Person person = (Person) accountability.getChild();
                    count(person);
                }
            }
        });
    }

    private void count(final Person person) {
        final Siadap siadap = configuration.getSiadapFor(person);
        final SiadapProcessStateEnum state =
                siadap == null ? SiadapProcessStateEnum.NOT_CREATED : SiadapProcessStateEnum.getState(siadap);
        duplicatedPersons.add(person);
        counts[state.ordinal()]++;
        if (gatherSiadaps && siadap != null) {
            getSiadaps().add(siadap);
        }
    }

    public Set<Siadap> getOrCreateSiadapsNotListed() {
        final int year = configuration.getYear();
        Set<Siadap> notListedSiadapsForGivenYear = notListedSiadaps.get(year);
        if (notListedSiadapsForGivenYear == null) {
            notListedSiadapsForGivenYear =
                    new TreeSet<Siadap>(Siadap.COMPARATOR_BY_EVALUATED_PRESENTATION_NAME_FALLBACK_YEAR_THEN_OID);

            notListedSiadapsForGivenYear
                    .addAll(Sets.filter(SiadapRootModule.getInstance().getSiadapsSet(), new Predicate<Siadap>() {

                        @Override
                        public boolean apply(Siadap input) {
                            if (input == null) {
                                return false;
                            }
                            return input.getYear() == year;
                        }
                    }));

            notListedSiadapsForGivenYear.removeAll(getSiadaps());
            notListedSiadaps.put(year, notListedSiadapsForGivenYear);
            return notListedSiadapsForGivenYear;

        } else {
            return notListedSiadapsForGivenYear;
        }
    }

    public int getOrCreateSiadapsNotListedSize() {
        return getOrCreateSiadapsNotListed().size();
    }

    public static class NumberAndGradeCounter {

        public NumberAndGradeCounter(String categoryName, SiadapStatisticsSummaryBoardUniversesEnum universesEnum) {
            this.categoryName = categoryName;
            this.subCategoryTypeEnum = universesEnum;
            this.subCategoryCounter = new int[universesEnum.getNrOfSubCategories()];
            this.totalPeopleEvaluatedByCompetencesOnlyCounter = 0;
        }

        String categoryName;

        private final SiadapStatisticsSummaryBoardUniversesEnum subCategoryTypeEnum;

        int totalNumberOfCategoryPeople = 0;

        int[] subCategoryCounter;

        int totalPeopleEvaluatedByCompetencesOnlyCounter;

        final Set<Siadap> siadapsForThisCategory = new HashSet<Siadap>();

        Multiset<SiadapGlobalEvaluation> gradeCounter = HashMultiset.create();

        void addPerson(int subCategoryIndex) {
            this.subCategoryCounter[subCategoryIndex]++;
            totalNumberOfCategoryPeople++;
        }

        void addPersonEvaluatedOnlyByCompetences() {
            this.totalPeopleEvaluatedByCompetencesOnlyCounter++;
        }

        void addGrade(SiadapGlobalEvaluation evaluation) {
            gradeCounter.add(evaluation);
        }

        public boolean hasSubCategories() {
            return getNumberSubCategories() > 1;
        }

        public int getNumberSubCategories() {
            return this.subCategoryCounter.length;
        }

        public int[] getSubCategoryCounter() {
            return this.subCategoryCounter;
        }

        public void addSiadapForThisCategory(Siadap siadap) {
            this.siadapsForThisCategory.add(siadap);
        }

        public Set<Siadap> getSiadapsForThisCategory() {
            return siadapsForThisCategory;
        }

        public int getNumberOfPeopleForSubcategory(int subcategoryIndex) {
            return this.subCategoryCounter[subcategoryIndex];
        }

        public int getNumberOfPeopleEvaluatedOnlyByCompetences() {
            return this.totalPeopleEvaluatedByCompetencesOnlyCounter;
        }

        public int getTotalNumberOfCategoryPeople() {
            return totalNumberOfCategoryPeople;
        }

        public Multiset<SiadapGlobalEvaluation> getGradeCounter() {
            return gradeCounter;
        }

        public SiadapStatisticsSummaryBoardUniversesEnum getSubCategoryTypeEnum() {
            return subCategoryTypeEnum;
        }

    }

    private void count(final Person person, boolean withQuota) {
        final Siadap siadap = configuration.getSiadapFor(person);
        final SiadapProcessStateEnum state =
                siadap == null ? SiadapProcessStateEnum.NOT_CREATED : SiadapProcessStateEnum.getState(siadap);

        duplicatedPersons.add(person);

        // let's fill the complicated hashmap
        HashMap<String, NumberAndGradeCounter> categoryHashMap = getCountsByQuotaAndCategories().get(Boolean.valueOf(withQuota));
        if (categoryHashMap == null)
        // if it doesn't exist for this quotaaware/noquotaaware universe, let's
        // create it
        {
            categoryHashMap = new HashMap<String, NumberAndGradeCounter>();
            getCountsByQuotaAndCategories().put(Boolean.valueOf(withQuota), categoryHashMap);
        }

        SiadapStatisticsSummaryBoardUniversesEnum universesEnum =
                SiadapStatisticsSummaryBoardUniversesEnum.getStatisticsUniverse(state);
        String categoryName = universesEnum.getCategoryString(siadap);
        NumberAndGradeCounter numberAndGradeCounter = categoryHashMap.get(categoryName);
        if (numberAndGradeCounter == null) {
            // do we already have a counter for this category?!, if not, we
            // create it
            numberAndGradeCounter = new NumberAndGradeCounter(categoryName, universesEnum);
            categoryHashMap.put(universesEnum.getCategoryString(siadap), numberAndGradeCounter);
        }

        // Registering the SIADAPS in curricular ponderation
        if (siadap != null && siadap.hasAnAssociatedCurricularPonderationEval()) {
            getCurricularPonderationSiadaps().add(siadap);
        }

        // let's register the person
        numberAndGradeCounter.addPerson(universesEnum.getSubCategoryIndex(state));

        if (siadap != null) {
            Boolean evaluatedOnlyByCompetences = siadap.getEvaluatedOnlyByCompetences();
            if (evaluatedOnlyByCompetences != null && evaluatedOnlyByCompetences) {
                numberAndGradeCounter.addPersonEvaluatedOnlyByCompetences();
            }

        }

        // let's register the person's grade
        if (siadap != null) {

            SiadapEvaluationUniverse defaultSiadapEvaluationUniverse = siadap.getDefaultSiadapEvaluationUniverse();
            if (defaultSiadapEvaluationUniverse != null
                    && defaultSiadapEvaluationUniverse.getLatestSiadapGlobalEvaluationEnum() != null) {
                numberAndGradeCounter.addGrade(defaultSiadapEvaluationUniverse.getLatestSiadapGlobalEvaluationEnum());
            }
        }

        if (gatherSiadaps && siadap != null) {
            getSiadaps().add(siadap);
            numberAndGradeCounter.addSiadapForThisCategory(siadap);

        }
    }

    public Set<Person> getDuplicatePersons() {
        if (!filteredDuplicatedPersonsSet) {
            filterDuplicatedPersonsSet();
        }
        return duplicatedPersons.elementSet();

    }

    public Set<Unit> getDuplicateUnits() {
        if (!filteredDuplicatedUnitSet) {
            filterDuplicatedUnitSet();
        }
        return duplicatedUnit.elementSet();

    }

    public Multiset<Unit> getOriginalDuplicateUnits() {
        if (!filteredDuplicatedUnitSet) {
            filterDuplicatedUnitSet();
        }
        return duplicatedUnit;

    }

    private void filterDuplicatedUnitSet() {
        for (Unit unit : duplicatedUnit) {
            if (duplicatedUnit.count(unit) == 1) {
                duplicatedUnit.remove(unit);
            }
        }

    }

    private void filterDuplicatedPersonsSet() {
        for (Person person : duplicatedPersons) {
            if (duplicatedPersons.count(person) == 1) {
                duplicatedPersons.remove(person);
            }
        }
    }

    public int[] getCounts() {
        return counts;
    }

    public boolean hasAnyPendingProcesses() {
        for (int i = 0; i < counts.length; i++) {
            if (i != 6 && counts[i] > 0) {
                return true;
            }
        }
        return false;
    }

    public HashMap<Boolean, HashMap<String, NumberAndGradeCounter>> getCountsByQuotaAndCategories() {
        return countsByQuotaAndCategories;
    }

    public Set<Siadap> getSiadaps() {
        return siadaps;
    }

    public Set<Siadap> getCurricularPonderationSiadaps() {
        return curricularPonderationSiadaps;
    }

    public static Set<Siadap> getSiadapsInState(final int year, final SiadapProcessStateEnum... states) {

        HashSet<Siadap> allSiadapsInGivenStates = new HashSet<Siadap>();

        allSiadapsInGivenStates.addAll(Sets.filter(SiadapRootModule.getInstance().getSiadapsSet(), new Predicate<Siadap>() {

            @Override
            public boolean apply(Siadap siadapInstance) {
                if (siadapInstance == null) {
                    return false;
                }
                if (siadapInstance.getYear() != year) {
                    return false;
                }
                for (SiadapProcessStateEnum state : states) {
                    if (siadapInstance.getState().equals(state)) {
                        return true;
                    }
                }
                return false;
            }
        }));

        return allSiadapsInGivenStates;

    }

}
