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

import javax.annotation.Nullable;

import jvstm.cps.ConsistencyPredicate;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.groups.SiadapCCAGroup;
import module.siadap.domain.groups.SiadapStructureManagementGroup;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;

import org.jfree.data.time.Month;
import org.joda.time.LocalDate;

import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
import pt.ist.bennu.core.domain.MyOrg;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.groups.NamedGroup;
import pt.ist.bennu.core.domain.groups.PersistentGroup;
import pt.ist.fenixWebFramework.services.Service;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;

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

    private static final String CCA_MEMBERS_GROUPNAME = "CCA Members";
    private static final String HOMOLOGATION_MEMBERS_GROUPNAME = "Homologation Members";

    private static SiadapCCAGroup ccaMembersGroup;
    private static SiadapStructureManagementGroup siadapStructureManagementGroup;

    public static SiadapCCAGroup getCcaMembersGroup() {
        initGroups();
        return ccaMembersGroup;
    }

    public static PersistentGroup getStructureManagementGroup() {
        initGroups();
        return SiadapRootModule.getInstance().getSiadapStructureManagementGroup();
    }

    public static NamedGroup getHomologationMembersGroup() {
        initGroups();
        return homologationMembersGroup;
    }

    /**
     * 
     * @return {@link #getLastDay()} minus 1 day = 30th December
     */
    public LocalDate getLastDayForAccountabilities() {
        return getLastDay().minusDays(1);
    }

    public LocalDate getLastDay() {
        return new LocalDate(getYear(), Month.DECEMBER, 31);
    }

    public LocalDate getFirstDay() {
        return new LocalDate(getYear(), Month.JANUARY, 1);

    }

    public boolean isHarmonizationPeriodOpenNow() {
        return getFirstLevelHarmonizationBegin() != null && getFirstLevelHarmonizationBegin().isBefore(new LocalDate());

    }

    private static NamedGroup homologationMembersGroup;
    private static boolean groupsInitialized = false;

    private static void initGroups() {
        if (groupsInitialized) {
            return;
        }
        //get the ccaMembersGroup
        for (PersistentGroup group : MyOrg.getInstance().getPersistentGroups()) {
            if (group instanceof SiadapCCAGroup) {
                ccaMembersGroup = (SiadapCCAGroup) group;
            }
        }
        //let us create the group if we haven't found it
        if (ccaMembersGroup == null) {
            createCCAMembersGroup();
        }

        //get the homologationMembersGroup
        for (PersistentGroup group : MyOrg.getInstance().getPersistentGroups()) {
            if (group instanceof NamedGroup) {
                if (((NamedGroup) group).getName().equals(HOMOLOGATION_MEMBERS_GROUPNAME)) {
                    homologationMembersGroup = (NamedGroup) group;
                }
            }
        }
        //let us create the group if we haven't found it
        if (homologationMembersGroup == null) {
            createHomologationMembersGroup();
        }
        if (siadapStructureManagementGroup == null) {
            createStructureManagementGroup();
        }
        groupsInitialized = true;
    }

    @Service
    private static void createStructureManagementGroup() {
        siadapStructureManagementGroup = new SiadapStructureManagementGroup();
    }

    @Service
    private static void createHomologationMembersGroup() {
        homologationMembersGroup = new NamedGroup(HOMOLOGATION_MEMBERS_GROUPNAME);
    }

    @Service
    private static void createCCAMembersGroup() {
        ccaMembersGroup = new SiadapCCAGroup();
    }

    @Service
    public static void addHomologationMember(User user) {
        getHomologationMembersGroup().addUsers(user);
    }

    @Service
    public static void removeHomologationMember(User user) {
        getHomologationMembersGroup().removeUsers(user);
    }

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
        setLockHarmonizationOnQuota(Boolean.FALSE);
        setClosedValidation(Boolean.FALSE);
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

    @Service
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

    @Service
    public static SiadapYearConfiguration createNewSiadapYearConfiguration(Integer year) {
        SiadapYearConfiguration configuration = getSiadapYearConfiguration(year);
        if (configuration != null) {
            return configuration;
        }
        return new SiadapYearConfiguration(year, DEFAULT_SIADAP2_OBJECTIVES_PONDERATION, DEFAULT_SIADAP2_COMPETENCES_PONDERATION,
                DEFAULT_SIADAP3_COMPETENCES_PONDERATION, DEFAULT_SIADAP3_OBJECTIVES_PONDERATION,
                DEFAULT_REVIEW_COMMISSION_WAITING_PERIOD);
    }

    public Siadap getSiadapFor(Person person, Integer year) {
        for (Siadap siadap : person.getSiadapsAsEvaluated()) {
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
        for (final Siadap siadap : person.getSiadapsAsEvaluated()) {
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
    @Service
    public void addStructureManagementGroupMembers(Person structureManagementGroupMembers) {
        super.addStructureManagementGroupMembers(structureManagementGroupMembers);
    }

    @Override
    @Service
    public void removeStructureManagementGroupMembers(Person structureManagementGroupMembers) {
        super.removeStructureManagementGroupMembers(structureManagementGroupMembers);
    }

    @Override
    @Service
    public void addCcaMembers(Person ccaMembers) {
        super.addCcaMembers(ccaMembers);
    }

    @Override
    @Service
    public void removeCcaMembers(Person ccaMembers) {
        super.removeCcaMembers(ccaMembers);
    }

    @Override
    @Service
    public void addHomologationMembers(Person homologationMembers) {
        super.addHomologationMembers(homologationMembers);
    }

    @Override
    @Service
    public void addScheduleEditors(Person scheduleEditors) {
        super.addScheduleEditors(scheduleEditors);
    };

    @Override
    @Service
    public void addRevertStateGroupMember(Person revertStateGroupMember) {
        super.addRevertStateGroupMember(revertStateGroupMember);
    }

    @Override
    @Service
    public void removeScheduleEditors(Person scheduleEditor) {
        super.removeScheduleEditors(scheduleEditor);
    };

    @Override
    @Service
    public void removeRevertStateGroupMember(Person revertStateGroupMember) {
        super.removeRevertStateGroupMember(revertStateGroupMember);
    }

    @Override
    @Service
    public void removeHomologationMembers(Person homologationMembers) {
        super.removeHomologationMembers(homologationMembers);
    }

    public boolean isCurrentUserMemberOfScheduleExtenders() {
        return isPersonMemberOfScheduleExtenders(UserView.getCurrentUser().getPerson());

    }

    public boolean isPersonMemberOfScheduleExtenders(Person person) {
        return getScheduleEditors().contains(person);
    }

    public boolean isPersonMemberOfCCA(Person person) {
        return getCcaMembers().contains(person);
    }

    public boolean isCurrentUserMemberOfCCA() {
        return isPersonMemberOfCCA(UserView.getCurrentUser().getPerson());
    }

    public boolean isCurrentUserMemberOfStructureManagementGroup() {
        return isPersonMemberOfStructureManagementGroup(UserView.getCurrentUser().getPerson());
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
        public boolean apply(@Nullable Siadap input) {
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
        return isPersonResponsibleForHomologation(UserView.getCurrentUser().getPerson());
    }

    //    public List<ExceedingQuotaProposal> getSuggestionsForUnit(Unit unit, ExceedingQuotaSuggestionType type) {
    //	return new UnitSiadapWrapper(unit, getYear()).getExcedingQuotaProposalSuggestions(type);
    //    }

    public boolean isPersonMemberOfRevertStateGroup(Person person) {
        return getRevertStateGroupMember().contains(person);
    }
}
