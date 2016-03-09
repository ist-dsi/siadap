/*
 * @(#)SiadapYearConfiguration.java
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
package module.siadap.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.impl.xb.xsdschema.NamedGroup;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.jfree.data.time.Month;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import jvstm.cps.ConsistencyPredicate;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import pt.ist.fenixframework.Atomic;

/**
 * 
 * @author João Antunes
 * @author Luis Cruz
 * @author Paulo Abrantes
 * 
 */
public class SiadapYearConfiguration extends SiadapYearConfiguration_Base {

    public static final Integer DEFAULT_SIADAP2_OBJECTIVES_PONDERATION = 75;
    public static final Integer DEFAULT_SIADAP2_COMPETENCES_PONDERATION = 25;
    public static final Integer DEFAULT_SIADAP3_OBJECTIVES_PONDERATION = 60;
    public static final Integer DEFAULT_SIADAP3_COMPETENCES_PONDERATION = 40;
    public static final Integer DEFAULT_REVIEW_COMMISSION_WAITING_PERIOD = 21;
    //    public static final Double MAXIMUM_HIGH_GRADE_QUOTA = 25.0;
    //    public static final Double MAXIMUM_EXCELLENCY_GRADE_QUOTA = 5.0; // 1.25; //

    private static final Logger LOGGER = LoggerFactory.getLogger(SiadapYearConfiguration.class);

    public static int getNextYear() {
        int nextYear = -1;
        for (SiadapYearConfiguration configuration : Bennu.getInstance().getSiadapRootModule().getYearConfigurations()) {
            int endYear = configuration.getBiannual() ? configuration.getYear() + 1 : configuration.getYear();
            endYear += 1;
            if (endYear > nextYear) {
                nextYear = endYear;
            }
        }
        return nextYear;
    }

    /**
     * 
     * @return {@link #getLastDay()} minus 1 day = 30th December
     */
    public LocalDate getLastDayForAccountabilities() {
        return getLastDay().minusDays(1);
    }

    public LocalDate getLastDay() {
        return getBiannual() ? new LocalDate(getYear() + 1, Month.DECEMBER, 31) : new LocalDate(getYear(), Month.DECEMBER, 31);
    }

    public LocalDate getFirstDay() {
        return new LocalDate(getYear(), Month.JANUARY, 1);

    }

    public boolean isOnlyAllowedToCreateSIADAP3() {
        return getBiannual();
    }

    public SiadapYearConfiguration getPreviousSiadapYearConfiguration() {
        int currentYear = getYear();
        SiadapYearConfiguration configurationToReturn = null;
        for (SiadapYearConfiguration configuration : SiadapRootModule.getInstance().getYearConfigurations()) {
            if ((configurationToReturn == null && configuration != this)
                    || (configuration.getYear() < currentYear && configurationToReturn.getYear() < configuration.getYear())) {
                configurationToReturn = configuration;
            }
        }
        return configurationToReturn;
    }

    public boolean isHarmonizationPeriodOpenNow() {
        return getFirstLevelHarmonizationBegin() != null && getFirstLevelHarmonizationBegin().isBefore(new LocalDate());

    }

    private static NamedGroup homologationMembersGroup;
    private static boolean groupsInitialized = false;

    // 5% of

    // the 25%

    public SiadapYearConfiguration(Integer year, int siadap2ObjectivesPonderation, int siadap2CompetencesPonderation,
            int siadap3CompetencesPonderdation, int siadap3ObjectivesPonderation, int reviewCommissionWaitingPeriod) {
        super();
        setYear(year);
        setSiadap2CompetencesPonderation(siadap2CompetencesPonderation);
        setSiadap2ObjectivesPonderation(siadap2ObjectivesPonderation);

        setSiadap3CompetencesPonderation(siadap3CompetencesPonderdation);
        setSiadap3ObjectivesPonderation(siadap3ObjectivesPonderation);

        setReviewCommissionWaitingPeriod(reviewCommissionWaitingPeriod);

        setSiadapRootModule(SiadapRootModule.getInstance());
        setLockHarmonizationOnQuota(Boolean.TRUE);
        setLockHarmonizationOnQuotaOutsideOfQuotaUniverses(Boolean.TRUE);
        setClosedValidation(Boolean.FALSE);
        setBiannual(Boolean.TRUE);

        //let us prefill the AccountabilityType slots, Top unit, Special harmonization unit, and
        //members of groups, with the previous configuration, if possible
        prefillWithPreviousConf();
    }

    private void prefillWithPreviousConf() {
        SiadapYearConfiguration previousSiadapYearConfiguration = getPreviousSiadapYearConfiguration();
        if (previousSiadapYearConfiguration != null) {
            LOGGER.info("Prefilling new SiadapYearConfiguration " + this.getExternalId() + " with data from previous, oid: "
                    + previousSiadapYearConfiguration.getExternalId() + " year: " + previousSiadapYearConfiguration.getYear());

            setUnitRelations(previousSiadapYearConfiguration.getUnitRelations());
            setHarmonizationResponsibleRelation(previousSiadapYearConfiguration.getHarmonizationResponsibleRelation());
            setWorkingRelation(previousSiadapYearConfiguration.getWorkingRelation());
            setWorkingRelationWithNoQuota(previousSiadapYearConfiguration.getWorkingRelationWithNoQuota());
            setEvaluationRelation(previousSiadapYearConfiguration.getEvaluationRelation());
            setSiadap2HarmonizationRelation(previousSiadapYearConfiguration.getSiadap2HarmonizationRelation());
            setSiadap3HarmonizationRelation(previousSiadapYearConfiguration.getSiadap3HarmonizationRelation());
            setHarmonizationUnitRelations(previousSiadapYearConfiguration.getHarmonizationUnitRelations());
            setSiadapStructureTopUnit(previousSiadapYearConfiguration.getSiadapStructureTopUnit());
            setSiadapSpecialHarmonizationUnit(previousSiadapYearConfiguration.getSiadapSpecialHarmonizationUnit());

            getCcaMembers().addAll(previousSiadapYearConfiguration.getCcaMembers());
            getScheduleEditors().addAll(previousSiadapYearConfiguration.getScheduleEditors());
            getRevertStateGroupMember().addAll(previousSiadapYearConfiguration.getRevertStateGroupMember());
            getHomologationMembers().addAll(previousSiadapYearConfiguration.getHomologationMembers());
            getStructureManagementGroupMembers().addAll(previousSiadapYearConfiguration.getStructureManagementGroupMembers());

        }

    }

    public static SiadapYearConfiguration getSiadapYearConfiguration(final String chosenYearConfigurationLabel) {
        if (StringUtils.isBlank(chosenYearConfigurationLabel)) {
            return null;
        }
        return Iterables.tryFind(SiadapRootModule.getInstance().getYearConfigurations(),
                new Predicate<SiadapYearConfiguration>() {
                    @Override
                    public boolean apply(SiadapYearConfiguration siadapYearConfiguration) {
                        if (siadapYearConfiguration == null) {
                            return false;
                        }
                        return siadapYearConfiguration.getLabel().equals(chosenYearConfigurationLabel);
                    }
                }).orNull();
    }

    public static SiadapYearConfiguration getSiadapYearConfiguration(Integer year) {
        if (year == null) {
            return null;
        }
        for (SiadapYearConfiguration configuration : SiadapRootModule.getInstance().getYearConfigurations()) {
            if (configuration.getYear() == year) {
                //TODO remove these lines after a while
                //R2
                configuration.initializePonderationsIfNeeded();
                return configuration;
            }
        }
        return null;
    }

    @ConsistencyPredicate
    public boolean ponderationCorrectness() {
        if ((getSiadap2CompetencesPonderation() + getSiadap2ObjectivesPonderation()) != 100) {
            return false;
        }
        if ((getSiadap3CompetencesPonderation() + getSiadap3ObjectivesPonderation()) != 100) {
            return false;
        }
        return true;
    }

    @Atomic
    public Boolean initializePonderationsIfNeeded() {
        Boolean migratedAnything = Boolean.FALSE;
        if (getSiadap2CompetencesPonderation() == null) {
            setSiadap2CompetencesPonderation(Integer.valueOf(DEFAULT_SIADAP2_COMPETENCES_PONDERATION));
            migratedAnything = Boolean.TRUE;
        }
        if (getSiadap2ObjectivesPonderation() == null) {
            setSiadap2ObjectivesPonderation(Integer.valueOf(DEFAULT_SIADAP2_OBJECTIVES_PONDERATION));
            migratedAnything = Boolean.TRUE;
        }
        if (getSiadap3ObjectivesPonderation() == null) {
            setSiadap3ObjectivesPonderation(Integer.valueOf(DEFAULT_SIADAP3_OBJECTIVES_PONDERATION));
            migratedAnything = Boolean.TRUE;
        }
        if (getSiadap3CompetencesPonderation() == null) {
            setSiadap3CompetencesPonderation(Integer.valueOf(DEFAULT_SIADAP3_COMPETENCES_PONDERATION));
            migratedAnything = Boolean.TRUE;
        }
        return migratedAnything;
    }

    @Atomic
    public static SiadapYearConfiguration createNewSiadapYearConfiguration(String label) {
        SiadapYearConfiguration configuration = getSiadapYearConfiguration(label);
        if (configuration != null) {
            return configuration;
        }
        return new SiadapYearConfiguration(getNextYear(), DEFAULT_SIADAP2_OBJECTIVES_PONDERATION,
                DEFAULT_SIADAP2_COMPETENCES_PONDERATION, DEFAULT_SIADAP3_COMPETENCES_PONDERATION,
                DEFAULT_SIADAP3_OBJECTIVES_PONDERATION, DEFAULT_REVIEW_COMMISSION_WAITING_PERIOD);
    }

    public Siadap getSiadapFor(Person person, Integer year) {
        for (Siadap siadap : person.getSiadapsAsEvaluatedSet()) {
            if (siadap.getYear().equals(year)) {
                return siadap;
            }
        }
        return null;
    }

    @ConsistencyPredicate
    public boolean checkQuotasPredicate() {
        Integer quotaExcellencySiadap2WithoutQuota = getQuotaExcellencySiadap2WithoutQuota();
        Integer quotaRelevantSiadap2WithoutQuota = getQuotaRelevantSiadap2WithoutQuota();
        Integer quotaRegularSiadap2WithoutQuota = getQuotaRegularSiadap2WithoutQuota();

        Integer quotaExcellencySiadap2WithQuota = getQuotaExcellencySiadap2WithQuota();
        Integer quotaRelevantSiadap2WithQuota = getQuotaRelevantSiadap2WithQuota();
        Integer quotaRegularSiadap2WithQuota = getQuotaRegularSiadap2WithQuota();

        Integer quotaExcellencySiadap3WithoutQuota = getQuotaExcellencySiadap3WithoutQuota();
        Integer quotaRelevantSiadap3WithoutQuota = getQuotaRelevantSiadap3WithoutQuota();
        Integer quotaRegularSiadap3WithoutQuota = getQuotaRegularSiadap3WithoutQuota();

        Integer quotaExcellencySiadap3WithQuota = getQuotaExcellencySiadap3WithQuota();
        Integer quotaRelevantSiadap3WithQuota = getQuotaRelevantSiadap3WithQuota();
        Integer quotaRegularSiadap3WithQuota = getQuotaRegularSiadap3WithQuota();

        return checkQuotaInts(quotaExcellencySiadap2WithoutQuota, quotaRelevantSiadap2WithoutQuota,
                quotaRegularSiadap2WithoutQuota)
                && checkQuotaInts(quotaExcellencySiadap2WithQuota, quotaRelevantSiadap2WithQuota, quotaRegularSiadap2WithQuota)
                && checkQuotaInts(quotaExcellencySiadap3WithQuota, quotaRelevantSiadap3WithQuota, quotaRegularSiadap3WithQuota)
                && checkQuotaInts(quotaExcellencySiadap3WithoutQuota, quotaRelevantSiadap3WithoutQuota,
                        quotaRegularSiadap3WithoutQuota);

    }

    /**
     * 
     * @param quotaOne
     * @param quotaTwo
     * @param quotaThree
     * @return false if the three arguments are different from null and their
     *         sum is greater than 100. True otherwise
     */
    private boolean checkQuotaInts(Integer quotaOne, Integer quotaTwo, Integer quotaThree) {
        if (quotaOne != null && quotaTwo != null && quotaThree != null) {
            //all of them are set, so the sum must be less or equal than 100
            //as the quotas are expressed in percentual points
            if (quotaOne.intValue() + quotaTwo.intValue() + quotaThree.intValue() > 100) {
                return false;
            }
        }
        return true;
    }

    public Siadap getSiadapFor(final Person person) {
        final int year = getYear();
        for (final Siadap siadap : person.getSiadapsAsEvaluatedSet()) {
            if (siadap.getYear().intValue() == year) {
                return siadap;
            }
        }
        return null;
    }

    public static List<UnitSiadapWrapper> getAllHarmonizationUnitsFor(Integer year) {
        SiadapYearConfiguration configuration = getSiadapYearConfiguration(year);
        UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(configuration.getSiadapStructureTopUnit(), year);
        List<UnitSiadapWrapper> harmonizationUnits = unitSiadapWrapper.getSubHarmonizationUnits();
        return harmonizationUnits;
    }

    public static Set<UnitSiadapWrapper> getAllHarmonizationUnitsExceptSpecialUnit(Integer year) {
        Set<UnitSiadapWrapper> harmonizationUnits = new TreeSet<UnitSiadapWrapper>(UnitSiadapWrapper.COMPARATOR_BY_UNIT_NAME);
        for (UnitSiadapWrapper unitWrapper : getAllHarmonizationUnitsFor(year)) {
            if (!unitWrapper.getUnit().equals(getSiadapYearConfiguration(year).getSiadapSpecialHarmonizationUnit())) {
                harmonizationUnits.add(unitWrapper);
            }
        }
        return harmonizationUnits;
    }

    @Override
    @Atomic
    public void addStructureManagementGroupMembers(Person structureManagementGroupMembers) {
        super.addStructureManagementGroupMembers(structureManagementGroupMembers);
    }

    @Override
    @Atomic
    public void removeStructureManagementGroupMembers(Person structureManagementGroupMembers) {
        super.removeStructureManagementGroupMembers(structureManagementGroupMembers);
    }

    @Override
    @Atomic
    public void addCcaMembers(Person ccaMembers) {
        super.addCcaMembers(ccaMembers);
    }

    @Override
    @Atomic
    public void removeCcaMembers(Person ccaMembers) {
        super.removeCcaMembers(ccaMembers);
    }

    @Override
    @Atomic
    public void addHomologationMembers(Person homologationMembers) {
        super.addHomologationMembers(homologationMembers);
    }

    @Override
    @Atomic
    public void addScheduleEditors(Person scheduleEditors) {
        super.addScheduleEditors(scheduleEditors);
    };

    @Override
    @Atomic
    public void addRevertStateGroupMember(Person revertStateGroupMember) {
        super.addRevertStateGroupMember(revertStateGroupMember);
    }

    @Override
    @Atomic
    public void removeScheduleEditors(Person scheduleEditor) {
        super.removeScheduleEditors(scheduleEditor);
    };

    @Override
    @Atomic
    public void removeRevertStateGroupMember(Person revertStateGroupMember) {
        super.removeRevertStateGroupMember(revertStateGroupMember);
    }

    @Override
    @Atomic
    public void removeHomologationMembers(Person homologationMembers) {
        super.removeHomologationMembers(homologationMembers);
    }

    public boolean isCurrentUserMemberOfScheduleExtenders() {
        return isPersonMemberOfScheduleExtenders(Authenticate.getUser().getPerson());

    }

    public boolean isPersonMemberOfScheduleExtenders(Person person) {
        return getScheduleEditors().contains(person);
    }

    public boolean isPersonMemberOfCCA(Person person) {
        return getCcaMembers().contains(person);
    }

    public boolean isCurrentUserMemberOfCCA() {
        return isPersonMemberOfCCA(Authenticate.getUser().getPerson());
    }

    public boolean isCurrentUserMemberOfStructureManagementGroup() {
        return isPersonMemberOfStructureManagementGroup(Authenticate.getUser().getPerson());
    }

    public boolean isUserMemberOfStructureManagementGroup(User user) {
        return isPersonMemberOfStructureManagementGroup(user.getPerson());
    }

    public boolean isPersonMemberOfStructureManagementGroup(Person person) {
        return getStructureManagementGroupMembers().contains(person);
    }

    public boolean isPersonResponsibleForHomologation(Person person) {
        return getHomologationMembers().contains(person);
    }

    public static final Predicate<Siadap> SIADAP_WITHOUT_VALID_HARM_UNIT = new Predicate<Siadap>() {

        @Override
        public boolean apply(Siadap input) {
            if (input == null) {
                return false;
            }
            PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(input);
            Unit unitWhereIsHarmonized = personSiadapWrapper.getValidUnitWhereIsHarmonized();
            if (unitWhereIsHarmonized == null) {
                return true;
            } else {
                return false;
            }
        }
    };

    public boolean hasSiadapsWithoutValidHarmonizationUnit() {
        return Iterators.any(getSiadaps().iterator(), SIADAP_WITHOUT_VALID_HARM_UNIT);
    }

    public Set<Siadap> getSiadapsWithoutValidHarmonizationUnit() {
        return new HashSet<Siadap>(Collections2.filter(getSiadaps(), SIADAP_WITHOUT_VALID_HARM_UNIT));
    }

    public boolean isCurrentUserResponsibleForHomologation() {
        return isPersonResponsibleForHomologation(Authenticate.getUser().getPerson());
    }

    //    public List<ExceedingQuotaProposal> getSuggestionsForUnit(Unit unit, ExceedingQuotaSuggestionType type) {
    //	return new UnitSiadapWrapper(unit, getYear()).getExcedingQuotaProposalSuggestions(type);
    //    }

    public boolean isPersonMemberOfRevertStateGroup(Person person) {
        return getRevertStateGroupMember().contains(person);
    }

    public String getLabel() {
        if (getBiannual() == null || getBiannual() == false) {
            return String.valueOf(getYear());
        } else {
            String shortVersionOfSecondYear = StringUtils.right(String.valueOf(getYear() + 1), 2);
            return String.valueOf(getYear()) + "-" + shortVersionOfSecondYear;
        }

    }

    private static final Integer MAXIMUM_NR_OBJ_INDICATORS_IN_BIANNUAL_PROCCESS = new Integer(3);
    private static final Integer MAXIMUM_NR_OF_OBJECTIVES_FOR_BIANNUAL_PROCCESS = new Integer(7);

    public Integer getMaximumNumberOfObjectives() {
        if (getBiannual() == true) {
            return MAXIMUM_NR_OF_OBJECTIVES_FOR_BIANNUAL_PROCCESS;
        } else {
            return null;
        }
    }

    public Integer getMaximumNumberOfObjectiveIndicators() {
        if (getBiannual() == true) {
            return MAXIMUM_NR_OBJ_INDICATORS_IN_BIANNUAL_PROCCESS;
        } else {
            return null;
        }
    }

    @Deprecated
    public java.util.Set<module.organization.domain.Person> getScheduleEditors() {
        return getScheduleEditorsSet();
    }

    @Deprecated
    public java.util.Set<module.organization.domain.Person> getCcaMembers() {
        return getCcaMembersSet();
    }

    @Deprecated
    public java.util.Set<module.organization.domain.Person> getRevertStateGroupMember() {
        return getRevertStateGroupMemberSet();
    }

    @Deprecated
    public java.util.Set<module.organization.domain.Person> getStructureManagementGroupMembers() {
        return getStructureManagementGroupMembersSet();
    }

    @Deprecated
    public java.util.Set<module.siadap.domain.Siadap> getSiadaps() {
        return getSiadapsSet();
    }

    @Deprecated
    public java.util.Set<module.organization.domain.Person> getHomologationMembers() {
        return getHomologationMembersSet();
    }

    @Deprecated
    public java.util.Set<module.siadap.domain.ExceedingQuotaProposal> getExceedingQuotasProposals() {
        return getExceedingQuotasProposalsSet();
    }

    @Deprecated
    public java.util.Set<module.organization.domain.Unit> getHarmonizationClosedUnits() {
        return getHarmonizationClosedUnitsSet();
    }

    public boolean containsYear(int year) {
	final int startYear = getYear();
	final int endYear = getBiannual() ? startYear + 1 : startYear;
	return year == startYear || year == endYear;
    }
}
