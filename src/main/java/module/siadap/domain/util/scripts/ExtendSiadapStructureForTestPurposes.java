/*
 * @(#)ExtendSiadapStructure.java
 *
 * Copyright 2012 Instituto Superior Tecnico
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
package module.siadap.domain.util.scripts;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.CompetenceType;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.util.SiadapMiscUtilClass;
import module.siadap.domain.wrappers.PersonSiadapWrapper;

import org.joda.time.LocalDate;

import pt.ist.bennu.core.applicationTier.Authenticate;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;
import pt.ist.fenixWebFramework.security.UserView;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * 
 * Script used to extend the SIADAP accountabilities from a given year {@link #YEAR_TO_EXTEND} to another
 * {@link #YEAR_TO_EXTEND_TO} which must have
 * a SiadapYearConfiguration already configured with the accountability types
 * configured
 * 
 * @author João Antunes
 */
public class ExtendSiadapStructureForTestPurposes extends WriteCustomTask {

    class SiadapBean {

        private final CompetenceType competenceType;

        public CompetenceType getCompetenceType() {
            return competenceType;
        }

        public SiadapUniverse getDefaultSiadapUniverse() {
            return defaultSiadapUniverse;
        }

        private final SiadapUniverse defaultSiadapUniverse;

        private final Siadap siadap;

        SiadapBean(Siadap siadap) {
            competenceType = siadap.getDefaultCompetenceType();
            defaultSiadapUniverse = siadap.getDefaultSiadapUniverse();
            this.siadap = siadap;

            if (defaultSiadapUniverse == null || competenceType == null) {
                throw new SiadapException("competence.type.or.siadap.universe.were.null");
            }
        }

        public Siadap getSiadap() {
            return siadap;
        }
    }

    private final static boolean USE_PERSONNEL_ACC = true;
    private final static boolean BY_DEFAULT_DONT_CREATE_SIADAP2_PROCESS = true;

    private final static int YEAR_TO_EXTEND = 2012;
    private final static int YEAR_TO_EXTEND_TO = YEAR_TO_EXTEND + 1;
    protected static final LocalDate USE_PERSONNEL_ACC_DATE = new LocalDate(); //use the current day to filter the persons
    //with the PERSONNEL accountability
    //let's get the configuration and all of the accountabilities that we should 'clone'
    SiadapYearConfiguration yearConfigurationToExtend;
    SiadapYearConfiguration yearConfigurationToExtendTo;
    AccountabilityType unitRelations;
    AccountabilityType harmonizationUnitRelations;
    AccountabilityType harmonizationResponsibleRelation;
    AccountabilityType siadap2HarmonizationRelation;
    AccountabilityType siadap3HarmonizationRelation;
    AccountabilityType workingRelation;
    AccountabilityType workingRelationWithNoQuota;
    private AccountabilityType evaluationRelation;

    AccountabilityType newUnitRelations;
    AccountabilityType newHarmonizationUnitRelations;
    AccountabilityType newHarmonizationResponsibleRelation;
    AccountabilityType newSiadap2HarmonizationRelation;
    AccountabilityType newSiadap3HarmonizationRelation;
    AccountabilityType newWorkingRelation;
    AccountabilityType newWorkingRelationWithNoQuota;
    private AccountabilityType newEvaluationRelation;
    Set<Person> personsWithNulledOrNotCreatedProccesses;
    Set<Person> personsCurrentlyNotWorking;

    private Set<Accountability> accsToClone;
    private Set<SiadapBean> siadapsToClone;
    private Set<Person> siadap2Persons;
    private Set<Person> peopleToActuallyImport;

    @Override
    public String getServerName() {
        return "joantune-workstation";
    }

    /*
     * (non-Javadoc)
     * 
     * @see pt.ist.bennu.core.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
        accsToClone = new HashSet<Accountability>();
        siadapsToClone = new HashSet<SiadapBean>();

        yearConfigurationToExtend = SiadapYearConfiguration.getSiadapYearConfiguration(YEAR_TO_EXTEND);
        yearConfigurationToExtendTo = SiadapYearConfiguration.getSiadapYearConfiguration(YEAR_TO_EXTEND_TO);

        unitRelations = yearConfigurationToExtend.getUnitRelations();
        harmonizationUnitRelations = yearConfigurationToExtend.getHarmonizationUnitRelations();
        harmonizationResponsibleRelation = yearConfigurationToExtend.getHarmonizationResponsibleRelation();
        siadap2HarmonizationRelation = yearConfigurationToExtend.getSiadap2HarmonizationRelation();
        siadap3HarmonizationRelation = yearConfigurationToExtend.getSiadap3HarmonizationRelation();
        workingRelation = yearConfigurationToExtend.getWorkingRelation();
        workingRelationWithNoQuota = yearConfigurationToExtend.getWorkingRelationWithNoQuota();
        evaluationRelation = yearConfigurationToExtend.getEvaluationRelation();
        //now let's get them all (through the top unit)
        Unit topUnit = yearConfigurationToExtend.getSiadapStructureTopUnit();

        personsWithNulledOrNotCreatedProccesses = new HashSet<>();

        personsCurrentlyNotWorking = new HashSet<>();

        siadap2Persons = new HashSet();

        peopleToActuallyImport = new HashSet();
        peopleToActuallyImport.add(User.findByUsername("ist12048").getPerson());
        peopleToActuallyImport.add(User.findByUsername("ist138407").getPerson());
        peopleToActuallyImport.add(User.findByUsername("ist154457").getPerson());
        peopleToActuallyImport.add(User.findByUsername("ist24439").getPerson());
        peopleToActuallyImport.add(User.findByUsername("ist24616").getPerson());
        //now let's scour all of the existing SIADAPs for the given year, and register the data to extend to the next year such as Universe (SIADAP2 or SIADAP3) and Career (Competence Type)
        //as well as the accountabilities that are set directly between two persons

        int nrDirectEvaluatorsFound = 0;
        List<Siadap> siadaps = SiadapRootModule.getInstance().getSiadaps();
        for (Siadap siadap : siadaps) {
            if (siadap.getYear().intValue() == YEAR_TO_EXTEND) {
                if (siadap.getState().ordinal() > SiadapProcessStateEnum.NOT_CREATED.ordinal()) {
                    Person evaluated = siadap.getEvaluated();
                    if (peopleToActuallyImport.contains(evaluated)) {

                        if (USE_PERSONNEL_ACC
                                && Iterables.any(evaluated.getParentAccountabilities(AccountabilityType.readBy("Personnel")),
                                        new Predicate<Accountability>() {

                                    @Override
                                    public boolean apply(@Nullable Accountability input) {
                                        if (input == null)
                                            return false;
                                        return input.isActive(USE_PERSONNEL_ACC_DATE);
                                    }
                                })) {

                            //check for direct accountabilities to extend
                            for (Accountability acc : siadap.getEvaluated().getParentAccountabilities(evaluationRelation)) {
                                if (acc.isActive(SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(YEAR_TO_EXTEND))
                                        && acc.getParent() instanceof Person) {
                                    accsToClone.add(acc);
                                    nrDirectEvaluatorsFound++;
                                }
                            }

                            //let's get the career and the SIADAP universe
                            try {
                                siadapsToClone.add(new SiadapBean(siadap));
                            } catch (SiadapException ex) {
                                out.println("SIADAP CLONING: Could not clone (due to null competence type/Universe) "
                                        + siadap.getProcess().getProcessNumber());
                            }

                        } else {
                            //this is an innactive person, let's add it to the list
                            personsCurrentlyNotWorking.add(evaluated);
                        }
                    }
                } else {
                    personsWithNulledOrNotCreatedProccesses.add(siadap.getEvaluated());
                    out.println("Suppressing creation of SIADAP for: " + siadap.getEvaluated().getPresentationName());
                }
            }
        }
        out.println("Caught " + nrDirectEvaluatorsFound + " direct evaluator relations");


        descendOnUnitAndRegisterAccs(topUnit);

        newUnitRelations = yearConfigurationToExtendTo.getUnitRelations();
        newHarmonizationUnitRelations = yearConfigurationToExtendTo.getHarmonizationUnitRelations();
        newHarmonizationResponsibleRelation = yearConfigurationToExtendTo.getHarmonizationResponsibleRelation();
        newSiadap2HarmonizationRelation = yearConfigurationToExtendTo.getSiadap2HarmonizationRelation();
        newSiadap3HarmonizationRelation = yearConfigurationToExtendTo.getSiadap3HarmonizationRelation();
        newWorkingRelation = yearConfigurationToExtendTo.getWorkingRelation();
        newWorkingRelationWithNoQuota = yearConfigurationToExtendTo.getWorkingRelationWithNoQuota();
        newEvaluationRelation = yearConfigurationToExtendTo.getEvaluationRelation();

        int extendedAccs = 0;
        //ok, so now let's take care of extending/creating the accs, based on the ones we got
        for (Accountability acc : accsToClone) {
            AccountabilityType accTypeToUse = null;
            AccountabilityType accountabilityType = acc.getAccountabilityType();
            if (accountabilityType.equals(unitRelations)) {
                accTypeToUse = newUnitRelations;

            } else if (accountabilityType.equals(harmonizationUnitRelations)) {
                accTypeToUse = newHarmonizationUnitRelations;

            } else if (accountabilityType.equals(harmonizationResponsibleRelation)) {
                accTypeToUse = newHarmonizationResponsibleRelation;

            } else if (accountabilityType.equals(siadap2HarmonizationRelation)) {
                accTypeToUse = newSiadap2HarmonizationRelation;

            } else if (accountabilityType.equals(siadap3HarmonizationRelation)) {
                accTypeToUse = newSiadap3HarmonizationRelation;

            } else if (accountabilityType.equals(workingRelation)) {
                accTypeToUse = newWorkingRelation;

            } else if (accountabilityType.equals(workingRelationWithNoQuota)) {
                accTypeToUse = newWorkingRelationWithNoQuota;

            } else if (accountabilityType.equals(evaluationRelation)) {
                accTypeToUse = newEvaluationRelation;

            }

            if (accountabilityType.equals(accTypeToUse)) {
                //it's the same kind of acc, let's see what we shall do with it
                if (YEAR_TO_EXTEND_TO - YEAR_TO_EXTEND == 1) {
                    //if we are talking about subsequent years, let's just check if we need to extend it or not
                    if (!acc.isActive(SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(YEAR_TO_EXTEND_TO))) {
                        //if we have to extend it, let's extend it untill infinity
                        acc.editDates(acc.getBeginDate(), null);
                        extendedAccs++;

                    }
                }
            }
        }

        //done, everything is extended
        out.println("*FINISHED*\nSummary: ");
        out.println("Total Accs that were marked to be extended " + accsToClone.size() + " extended: " + extendedAccs);

        int clonedSiadaps = 0;
        //let's now 'clone' the SIADAPs
        try {
            UserView.setUser(Authenticate.authenticate(User.findByUsername("ist23470")));
            for (SiadapBean siadapBean : siadapsToClone) {
                Siadap siadap = siadapBean.getSiadap();
                boolean siadapAlreadyExists = false;
                for (Siadap currentSiadap : siadap.getEvaluated().getSiadapsAsEvaluated()) {
                    if (currentSiadap.getYear().intValue() == YEAR_TO_EXTEND_TO) {
                        siadapAlreadyExists = true;
                        break;
                    }

                }
                if (!siadapAlreadyExists) {
                    try {

                        SiadapProcess.createNewProcess(siadap.getEvaluated(), YEAR_TO_EXTEND_TO,
                                siadapBean.getDefaultSiadapUniverse(), siadapBean.getCompetenceType());
                        clonedSiadaps++;
                    } catch (SiadapException ex) {
                        if (siadapBean.getDefaultSiadapUniverse().equals(SiadapUniverse.SIADAP2))
                            siadap2Persons.add(siadapBean.getSiadap().getEvaluated());
                        else
                            throw ex;
                    }
                }
            }

            out.println("Removing from structure persons with nulled or not created processes. got "
                    + personsWithNulledOrNotCreatedProccesses.size() + " Persons. Listing them: ");
            for (Person person : personsWithNulledOrNotCreatedProccesses) {
                PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(person, YEAR_TO_EXTEND_TO);
                personSiadapWrapper.removeFromSiadapStructure(true);
                out.println(person.getPresentationName());
            }

            out.println("Removing from the structure persons without an active Personnel accountability. got "
                    + personsCurrentlyNotWorking.size());
            for (Person person : personsCurrentlyNotWorking) {
                PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(person, YEAR_TO_EXTEND_TO);
                personSiadapWrapper.removeFromSiadapStructure(true);
                out.println(person.getPresentationName());
            }

            out.println("Listing the " + siadap2Persons.size()
                    + " persons that had a SIADAP2 process and whose SIADAP process for this year wasn't created");
            for (Person person : siadap2Persons) {
                out.println(person.getPresentationName());
            }

        } finally {
            UserView.setUser(null);
            out.println("There were cloned " + clonedSiadaps + " cloned SIADAPs");
        }
    }

    private void descendOnUnitAndRegisterAccs(Unit unit) {
        for (Accountability acc : unit.getChildrenAccountabilities(harmonizationResponsibleRelation, unitRelations,
                harmonizationUnitRelations, siadap2HarmonizationRelation, siadap3HarmonizationRelation, workingRelation,
                workingRelationWithNoQuota, evaluationRelation)) {
            if (acc.getChild() instanceof Person && (acc.getAccountabilityType().equals(siadap2HarmonizationRelation)
                    || acc.getAccountabilityType().equals(siadap3HarmonizationRelation)
                    || acc.getAccountabilityType().equals(workingRelation) || acc.getAccountabilityType().equals(
                            workingRelationWithNoQuota))) {

                if ((personsWithNulledOrNotCreatedProccesses.contains(acc.getChild()))) {
                    out.println("Not registering acc " + acc.getDetailsString() + " because person: "
                            + acc.getChild().getPresentationName() + " has a nulled process");
                    continue;
                }
                if (personsCurrentlyNotWorking.contains(acc.getChild())) {
                    out.println("Not registering acc " + acc.getDetailsString() + " because person: "
                            + acc.getChild().getPresentationName() + " has no active personnel relation");

                    continue;
                }
                if (peopleToActuallyImport.contains(acc.getChild()) == false) {
                    personsWithNulledOrNotCreatedProccesses.add((Person) acc.getChild());
                    continue;

                }
            }
            if (acc.isActive(SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(YEAR_TO_EXTEND))) {
                //it was active, so let's add it to the list of accs to clone/extend
                accsToClone.add(acc);

                //if we have one of these accs with an end date beyond 31/12/YEAR_TO_EXTEND, let's print its details
                if (acc.getEndDate() == null || !acc.getEndDate().equals(SiadapMiscUtilClass.lastDayOfYear(YEAR_TO_EXTEND))) {
                    String parentString =
                            acc.getParent() == null || acc.getParent().getPartyName() == null ? "null" : acc.getParent()
                                    .getPartyName().getContent();
                    String childString =
                            acc.getChild() == null || acc.getChild().getPartyName() == null ? "null" : acc.getChild()
                                    .getPartyName().getContent();
                    out.println("Acc.Type: " + acc.getAccountabilityType().getName().getContent() + " Acc parent: "
                            + parentString + " child: " + childString + " acc start: " + String.valueOf(acc.getBeginDate())
                            + " acc end: " + String.valueOf(acc.getEndDate()));
                }
                //if we are dealing with a unit in the other end, let's descend
                if (acc.getChild() instanceof Unit) {
                    descendOnUnitAndRegisterAccs((Unit) acc.getChild());
                }
            }
        }

    }

}
