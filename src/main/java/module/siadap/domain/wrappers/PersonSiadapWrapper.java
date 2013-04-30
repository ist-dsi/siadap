/*
 * @(#)PersonSiadapWrapper.java
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.activities.NullifyProcess;
import module.siadap.domain.CompetenceType;
import module.siadap.domain.CurricularPonderationEvaluationItem;
import module.siadap.domain.ExceedingQuotaProposal;
import module.siadap.domain.ObjectiveEvaluation;
import module.siadap.domain.ObjectiveEvaluationIndicator;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapActionChangeValidatorEnum;
import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.siadap.domain.util.SiadapMiscUtilClass;
import module.siadap.domain.util.actions.SiadapUtilActions;
import module.siadap.presentationTier.actions.SiadapManagement;

import org.apache.commons.collections.Predicate;
import org.joda.time.LocalDate;

import pt.ist.bennu.core.applicationTier.Authenticate.UserView;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.exceptions.DomainException;
import pt.ist.bennu.core.util.BundleUtil;
import pt.ist.fenixframework.Atomic;

/**
 * 
 * @author João Antunes
 * @author Luis Cruz
 * @author Paulo Abrantes
 * 
 */
public class PersonSiadapWrapper extends PartyWrapper implements Serializable {

    public static final Comparator<PersonSiadapWrapper> PERSON_COMPARATOR_BY_NAME_FALLBACK_YEAR_THEN_PERSON_OID =
            new Comparator<PersonSiadapWrapper>() {

                @Override
                public int compare(PersonSiadapWrapper o1, PersonSiadapWrapper o2) {
                    int nameComparison = o1.getName().compareTo(o2.getName());
                    if (nameComparison == 0) {
                        return (o1.getYear() - o2.getYear()) == 0 ? o1.getPerson().getExternalId()
                                .compareTo(o2.getPerson().getExternalId()) : o1.getYear() - o2.getYear();
                    }

                    return nameComparison;
                }
            };

    private Person person;

    /*
     * We need these two Booleans because of the render problems while writing
     * to a Bean
     */
    private Boolean harmonizationCurrentAssessmentForSIADAP3;
    private Boolean harmonizationCurrentAssessmentForSIADAP2;

    private Boolean harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2;
    private Boolean harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3;

    private Boolean validationCurrentAssessmentForSIADAP3;
    private Boolean validationCurrentAssessmentForSIADAP2;

    private Boolean validationCurrentAssessmentForExcellencyAwardForSIADAP2;
    private Boolean validationCurrentAssessmentForExcellencyAwardForSIADAP3;

    private Boolean selectedForHomologation;

    private BigDecimal validationClassificationForSIADAP3;
    private BigDecimal validationClassificationForSIADAP2;

    private BigDecimal evaluatorClassificationForSIADAP3;
    private BigDecimal evaluatorClassificationForSIADAP2;

    public PersonSiadapWrapper(Person person, int year) {
        super(year);
        this.person = person;
        // initing the harmonization booleans
        if (person != null) {
            initIntermediateValues();
        }

    }

    public PersonSiadapWrapper(Siadap siadap) {
        super(siadap.getYear());
        this.person = siadap.getEvaluated();

        if (person != null) {
            initIntermediateValues();
        }

    }

    /**
     * Inits the intermediate values used for display purposes. More
     * specifically: {@link #harmonizationCurrentAssessmentForSIADAP2}, {@link #harmonizationCurrentAssessmentForSIADAP3}, and
     * {@link #exceedingQuotaPriorityNumber}
     */
    private void initIntermediateValues() {
        if (getSiadap() == null) {
            this.harmonizationCurrentAssessmentForSIADAP2 = null;
            this.harmonizationCurrentAssessmentForSIADAP3 = null;
        } else {
            SiadapEvaluationUniverse siadapEvaluationUniverseForSIADAP3 =
                    getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(SiadapUniverse.SIADAP3);
            SiadapEvaluationUniverse siadapEvaluationUniverseForSIADAP2 =
                    getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(SiadapUniverse.SIADAP2);
            if (siadapEvaluationUniverseForSIADAP2 == null) {
                this.harmonizationCurrentAssessmentForSIADAP2 = null;
                setValidationCurrentAssessmentForSIADAP2(null);
                setValidationCurrentAssessmentForExcellencyAwardForSIADAP2(null);
                setValidationClassificationForSIADAP2(null);
                setEvaluatorClassificationForSIADAP2(null);
            } else {
                setValidationCurrentAssessmentForSIADAP2(siadapEvaluationUniverseForSIADAP2.getCcaAssessment());
                setValidationCurrentAssessmentForExcellencyAwardForSIADAP2(siadapEvaluationUniverseForSIADAP2
                        .getCcaClassificationExcellencyAward());
                setValidationClassificationForSIADAP2(siadapEvaluationUniverseForSIADAP2.getCcaClassification());
                setEvaluatorClassificationForSIADAP2(siadapEvaluationUniverseForSIADAP2.getEvaluatorClassification());
                this.harmonizationCurrentAssessmentForSIADAP2 = siadapEvaluationUniverseForSIADAP2.getHarmonizationAssessment();
                this.harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2 =
                        siadapEvaluationUniverseForSIADAP2.getHarmonizationAssessmentForExcellencyAward();
            }

            setSelectedForHomologation(false);
            if (siadapEvaluationUniverseForSIADAP3 == null) {
                this.harmonizationCurrentAssessmentForSIADAP3 = null;
                setValidationCurrentAssessmentForSIADAP3(null);
                setValidationCurrentAssessmentForExcellencyAwardForSIADAP3(null);
                setValidationClassificationForSIADAP3(null);
                setEvaluatorClassificationForSIADAP3(null);
            } else {
                setValidationCurrentAssessmentForSIADAP3(siadapEvaluationUniverseForSIADAP3.getCcaAssessment());
                setValidationCurrentAssessmentForExcellencyAwardForSIADAP3(siadapEvaluationUniverseForSIADAP3
                        .getCcaClassificationExcellencyAward());
                setValidationClassificationForSIADAP3(siadapEvaluationUniverseForSIADAP3.getCcaClassification());
                setEvaluatorClassificationForSIADAP3(siadapEvaluationUniverseForSIADAP3.getEvaluatorClassification());
                this.harmonizationCurrentAssessmentForSIADAP3 = siadapEvaluationUniverseForSIADAP3.getHarmonizationAssessment();
                this.harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3 =
                        siadapEvaluationUniverseForSIADAP3.getHarmonizationAssessmentForExcellencyAward();
            }
        }
    }

    public void correctHarmonizationRelationsForRecentlyCreatedProcess() {
        //get all the relevant accountabilities
        List<Accountability> parentAccountabilityTypes =
                getParentAccountabilityTypes(getConfiguration().getSiadap2HarmonizationRelation(), getConfiguration()
                        .getSiadap3HarmonizationRelation());

        AccountabilityType accTypeToKeep = getDefaultSiadapUniverse().getHarmonizationRelation(getConfiguration());
        boolean hasNeededAccountability = false;
        for (Accountability acc : parentAccountabilityTypes) {
            if (acc.getAccountabilityType().equals(accTypeToKeep)) {
                hasNeededAccountability = true;
            } else {
                //let us close it
                acc.setEndDate(getConfiguration().getFirstDay(), BundleUtil.getStringFromResourceBundle(
                        Siadap.SIADAP_BUNDLE_STRING, "harmonization.unit.process.creation.justification"));
            }
        }
        if (hasNeededAccountability == false) {
            //let us create it
            if (getWorkingUnit() != null) {
                changeHarmonizationUnitTo(getWorkingUnit().getUnit(), getConfiguration().getFirstDay(),
                        BundleUtil.getStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                                "harmonization.unit.process.creation.justification"));
            }
        }
    }

    public Boolean getValidationCurrentAssessmentForSIADAP3() {
        return validationCurrentAssessmentForSIADAP3;
    }

    public void setValidationCurrentAssessmentForSIADAP3(Boolean validationCurrentAssessmentForSIADAP3) {
        this.validationCurrentAssessmentForSIADAP3 = validationCurrentAssessmentForSIADAP3;
    }

    public Boolean getValidationCurrentAssessmentForSIADAP2() {
        return validationCurrentAssessmentForSIADAP2;
    }

    public void setValidationCurrentAssessmentForSIADAP2(Boolean validationCurrentAssessmentForSIADAP2) {
        this.validationCurrentAssessmentForSIADAP2 = validationCurrentAssessmentForSIADAP2;
    }

    public Boolean getValidationCurrentAssessmentForExcellencyAwardForSIADAP2() {
        return validationCurrentAssessmentForExcellencyAwardForSIADAP2;
    }

    public void setValidationCurrentAssessmentForExcellencyAwardForSIADAP2(
            Boolean validationCurrentAssessmentForExcellencyAwardForSIADAP2) {
        this.validationCurrentAssessmentForExcellencyAwardForSIADAP2 = validationCurrentAssessmentForExcellencyAwardForSIADAP2;
    }

    public Boolean getValidationCurrentAssessmentForExcellencyAwardForSIADAP3() {
        return validationCurrentAssessmentForExcellencyAwardForSIADAP3;
    }

    public void setValidationCurrentAssessmentForExcellencyAwardForSIADAP3(
            Boolean validationCurrentAssessmentForExcellencyAwardForSIADAP3) {
        this.validationCurrentAssessmentForExcellencyAwardForSIADAP3 = validationCurrentAssessmentForExcellencyAwardForSIADAP3;
    }

    public BigDecimal getValidationClassificationForSIADAP3() {
        return validationClassificationForSIADAP3;
    }

    public void setValidationClassificationForSIADAP3(BigDecimal validationClassificationForSIADAP3) {
        this.validationClassificationForSIADAP3 = validationClassificationForSIADAP3;
    }

    public BigDecimal getValidationClassificationForSIADAP2() {
        return validationClassificationForSIADAP2;
    }

    public void setValidationClassificationForSIADAP2(BigDecimal validationClassificationForSIADAP2) {
        this.validationClassificationForSIADAP2 = validationClassificationForSIADAP2;
    }

    public BigDecimal getLatestClassificationForSIADAP2() {
        return getLatestClassification(SiadapUniverse.SIADAP2);
    }

    public BigDecimal getLatestClassificationForSIADAP3() {
        return getLatestClassification(SiadapUniverse.SIADAP3);
    }

    public BigDecimal getLatestClassification(SiadapUniverse siadapUniverse) {
        SiadapEvaluationUniverse siadapEvaluationUniverse =
                getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
        if (siadapEvaluationUniverse == null) {
            return null;
        }
        return siadapEvaluationUniverse.getCurrentGrade();

    }

    public SiadapGlobalEvaluation getLatestSiadapGlobalEvaluationForSIADAP2() {
        return getLatestSiadapGlobalEvaluation(SiadapUniverse.SIADAP2);
    }

    public SiadapGlobalEvaluation getLatestSiadapGlobalEvaluationForSIADAP3() {
        return getLatestSiadapGlobalEvaluation(SiadapUniverse.SIADAP3);
    }

    public SiadapGlobalEvaluation getLatestSiadapGlobalEvaluation(SiadapUniverse siadapUniverse) {
        SiadapEvaluationUniverse siadapEvaluationUniverse =
                getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
        if (siadapEvaluationUniverse == null) {
            return null;
        }
        return siadapEvaluationUniverse.getLatestSiadapGlobalEvaluationEnum();

    }

    public Person getPerson() {
        return this.person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Override
    protected Party getParty() {
        return this.person;
    }

    public boolean isEvaluationStarted() {
        return getSiadap() != null;
    }

    /**
     * 
     * @param universe
     *            the universe to consider the validation for
     * @return true if it hasn't been validated and it has been harmonized,
     *         false otherwise
     */
    private boolean isAbleToBeValidated(SiadapUniverse universe) {
        SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse =
                getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(universe);
        if (siadapEvaluationUniverseForSiadapUniverse == null) {
            return false;
        }
        return siadapEvaluationUniverseForSiadapUniverse.getValidationDate() == null
                && siadapEvaluationUniverseForSiadapUniverse.getHarmonizationDate() != null;

    }

    public boolean isSiadap2AbleToBeValidated() {
        return isAbleToBeValidated(SiadapUniverse.SIADAP2);
    }

    public boolean isSiadap3AbleToBeValidated() {
        return isAbleToBeValidated(SiadapUniverse.SIADAP3);
    }

    public Siadap getSiadap() {
        return getConfiguration() == null ? null : getConfiguration().getSiadapFor(getPerson(), getYear());
    }

    /**
     * @return a {@link SiadapProcessStateEnum} which represents the current
     *         state of the SIADAP process
     */
    public SiadapProcessStateEnum getCurrentProcessState() {
        return SiadapProcessStateEnum.getState(getSiadap());
    }

    /**
     * @return a string with the explanation of what should be done next, based
     *         on the user, if he is an evaluator or an evaluated
     */
    public String getNextStep() {
        return SiadapProcessStateEnum.getNextStep(getSiadap(), UserView.getCurrentUser());

    }

    public boolean isWithSkippedEval(SiadapUniverse siadapUniverse) {
        if (getSiadap() == null || getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse) == null) {
            return false;
        }
        return getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse).isWithSkippedEvaluation()
                || (getSiadap().getNulled() != null && getSiadap().getNulled());
    }

    public boolean getWithoutExcellencyAwardForSiadap2() {
        return isWithoutExcellencyAwardFor(SiadapUniverse.SIADAP2);
    }

    public boolean getWithoutExcellencyAwardForSiadap3() {
        return isWithoutExcellencyAwardFor(SiadapUniverse.SIADAP3);
    }

    private boolean isWithoutExcellencyAwardFor(SiadapUniverse siadapUniverse) {
        SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse =
                getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
        if (siadapEvaluationUniverseForSiadapUniverse == null) {
            return true;
        }
        if (siadapEvaluationUniverseForSiadapUniverse.isCurriculumPonderation()) {
            CurricularPonderationEvaluationItem curricularPonderationEvaluationItem =
                    (CurricularPonderationEvaluationItem) siadapEvaluationUniverseForSiadapUniverse.getSiadapEvaluationItems()
                            .iterator().next();
            return curricularPonderationEvaluationItem.getExcellencyAward() == null
                    || !curricularPonderationEvaluationItem.getExcellencyAward();
        }
        return siadapEvaluationUniverseForSiadapUniverse.getEvaluatorClassificationExcellencyAward() == null
                || !siadapEvaluationUniverseForSiadapUniverse.getEvaluatorClassificationExcellencyAward();
    }

    public boolean getWithSkippedEvalForSiadap2() {
        return isWithSkippedEval(SiadapUniverse.SIADAP2);
    }

    public boolean getWithSkippedEvalForSiadap3() {
        return isWithSkippedEval(SiadapUniverse.SIADAP3);
    }

    /**
     * 
     * @return true if the current user is able to see the details of the
     *         process, false otherwise
     */
    public boolean isCurrentUserAbleToSeeDetails() {
        User currentUser = UserView.getCurrentUser();
        SiadapYearConfiguration configuration = getConfiguration();
        if (getSiadap().getProcess().isAccessibleToCurrentUser()) {
            if (isResponsibleForHarmonization(currentUser.getPerson())) {
                return true;
            }
            if (configuration.getCcaMembers().contains(currentUser.getPerson())) {
                return true;
            }
            if (configuration.getHomologationMembers().contains(currentUser.getPerson())) {
                return true;
            }
            if (getEvaluator().getPerson().equals(currentUser.getPerson()) || getPerson().equals(currentUser.getPerson())) {
                return true;
            }
        }
        return false;
    }

    private String emailAddress;

    /**
     * 
     * @return true if the email is defined or if we just got a remote expcetion
     *         trying to get it from fenix. False if it is actually null or
     *         empty
     */
    public boolean isEmailDefined() {
        if (getEmailAddress() == null || getEmailAddress().equalsIgnoreCase("")) {
            try {
                String fetchedEmail = Siadap.getRemoteEmail(getPerson());
                if (fetchedEmail == null || fetchedEmail.equalsIgnoreCase("")) {
                    return false;
                }
                setEmailAddress(fetchedEmail);
                return true;
            } catch (Throwable e) {
                return true;
            }

        } else {
            return true;
        }

    }

    public SiadapUniverse getDefaultSiadapUniverse() {
        if (getSiadap() == null) {
            return null;
        }
        return getSiadap().getDefaultSiadapUniverse();
    }

    /**
     * 
     * @return false if the process is not sealed and the current user is not
     *         the evaluator, true otherwise
     */
    public boolean getShouldShowObjectivesAndCompetences() {
        return getSiadap().getObjectivesAndCompetencesSealedDate() != null
                || getSiadap().getEvaluator().getPerson().getUser().equals(UserView.getCurrentUser());
    }

    public PersonSiadapWrapper getEvaluator() {
        Person evaluator = null;

        Collection<Person> possibleCustomEvaluator = getParentPersons(getConfiguration().getEvaluationRelation());

        if (!possibleCustomEvaluator.isEmpty()) {
            evaluator = possibleCustomEvaluator.iterator().next();
        } else {
            if (getWorkingUnit() != null) {
                Collection<Unit> workingPlaces =
                        getParentUnits(getConfiguration().getWorkingRelation(), getConfiguration()
                                .getWorkingRelationWithNoQuota());
                Unit workingUnit = workingPlaces.iterator().next();
                Collection<Person> childPersons = workingUnit.getChildPersons(getConfiguration().getEvaluationRelation());
                if (!childPersons.isEmpty()) {
                    evaluator = childPersons.iterator().next();
                }
            }
        }

        return evaluator != null ? new PersonSiadapWrapper(evaluator, getYear()) : null;
    }

    public Collection<UnitSiadapWrapper> getHarmozationUnits() {
        return getHarmozationUnits(true);
    }

    public Collection<UnitSiadapWrapper> getActiveHarmonizationUnits() {
        return getHarmozationUnits(false);
    }

    public void removeAndNotifyHarmonizationResponsability(Unit unit, Person person, int year, HttpServletRequest request) {
        removeHarmonizationResponsability(unit);
        SiadapUtilActions.notifyRemovalOfHarmonizationResponsible(person, unit, year, request);
    }

    public void removeHarmonizationResponsability(Unit unit) {
        Set<AccountabilityType> accountabilityTypes =
                Collections.singleton(SiadapYearConfiguration.getSiadapYearConfiguration(getYear())
                        .getHarmonizationResponsibleRelation());
        Collection<Accountability> parentAccountabilities = getPerson().getParentAccountabilities(accountabilityTypes);

        for (Accountability accountability : parentAccountabilities) {
            if (accountability.getParent() == unit && accountability.isActive(getConfiguration().getLastDayForAccountabilities())) {
                accountability.editDates(accountability.getBeginDate(), getConfiguration().getFirstDay().plusDays(1));
            }
        }

    }

    public boolean hasPendingActions() {
        Siadap siadap = getSiadap();
        if (siadap == null) {
            if (isCurrentUserAbleToCreateProcess()) {
                return true;
            }
        } else {
            SiadapProcess process = siadap.getProcess();
            if (process.isAccessibleToCurrentUser()) {
                return siadap.getProcess().hasAnyAvailableActivitity();
            }
        }
        return false;
    }

    public int getNrPersonsWithUnreadComments() {
        int counter = 0;
        for (PersonSiadapWrapper personSiadapWrapper : getPeopleToEvaluate()) {
            if (personSiadapWrapper.getHasUnreadComments()) {
                counter++;
            }
        }
        return counter;

    }

    public int getNrPendingProcessActions() {
        int counterPendingActions = 0;
        ArrayList<PersonSiadapWrapper> personSiadapWrappers =
                SiadapRootModule.getInstance().getAssociatedSiadaps(getPerson(), getYear(), false);
        for (PersonSiadapWrapper personSiadapWrapper : personSiadapWrappers) {
            if (personSiadapWrapper.hasPendingActions()) {
                counterPendingActions++;
            }
        }
        return counterPendingActions;
    }

    public String getTotalQualitativeEvaluationScoring(SiadapUniverse siadapUniverse) {
        return getTotalQualitativeEvaluationScoringObject(siadapUniverse).getLocalizedName();
    }

    public SiadapGlobalEvaluation getTotalQualitativeEvaluationScoringObject(SiadapUniverse siadapUniverse) {
        boolean excellencyGiven = false;
        Siadap siadap = getSiadap();
        if (siadap == null) {
            return SiadapGlobalEvaluation.NONEXISTING;
        }
        SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse =
                siadap.getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
        if (siadapEvaluationUniverseForSiadapUniverse == null) {
            return SiadapGlobalEvaluation.NONEXISTING;
        } else {
            excellencyGiven = siadapEvaluationUniverseForSiadapUniverse.hasExcellencyAwarded();
        }
        if (!siadap.isEvaluationDone(siadapUniverse) && !siadapEvaluationUniverseForSiadapUniverse.isWithSkippedEvaluation()) {
            return SiadapGlobalEvaluation.NONEXISTING;
        }
        if (siadapEvaluationUniverseForSiadapUniverse.isWithSkippedEvaluation()) {
            return SiadapGlobalEvaluation.WITHSKIPPEDEVAL;
        }

        return SiadapGlobalEvaluation.getGlobalEvaluation(siadapEvaluationUniverseForSiadapUniverse.getTotalEvaluationScoring(),
                excellencyGiven);
    }

    public String getTotalQualitativeEvaluationScoringSiadap2() {
        return getTotalQualitativeEvaluationScoring(SiadapUniverse.SIADAP2);
    }

    public String getTotalQualitativeEvaluationScoringSiadap3() {
        return getTotalQualitativeEvaluationScoring(SiadapUniverse.SIADAP3);
    }

    public Collection<UnitSiadapWrapper> getHarmozationUnits(boolean considerInactiveHarmUnits) {
        List<UnitSiadapWrapper> units = new ArrayList<UnitSiadapWrapper>();

        for (Unit unit : getParentUnits(getConfiguration().getHarmonizationResponsibleRelation())) {
            UnitSiadapWrapper harmUnitWrapper = new UnitSiadapWrapper(unit, getYear());
            if (considerInactiveHarmUnits || harmUnitWrapper.isValidHarmonizationUnit()) {
                units.add(harmUnitWrapper);
            }
        }
        return units;
    }

    /**
     * 
     * @return the unit where is harmonized for the default harmonization universe
     */
    public Unit getUnitWhereIsHarmonized() {
        return getUnitWhereIsHarmonized(getDefaultSiadapUniverse());
    }

    public Unit getUnitWhereIsHarmonized(SiadapUniverse siadapUniverse) {
        if (siadapUniverse == null) {
            return null;
        }
        List<Unit> parentUnits = getParentUnits(getParty(), siadapUniverse.getHarmonizationRelation(getConfiguration()));
        if (parentUnits.isEmpty()) {
            return null;
        }
        if (parentUnits.size() > 1) {
            throw new SiadapException("inconsistent.harmonization.units");

        } else {
            UnitSiadapWrapper unitWrapper = new UnitSiadapWrapper(parentUnits.get(0), getYear());
            if (unitWrapper.isHarmonizationUnit()) {
                return unitWrapper.getUnit();
            } else {
                return unitWrapper.getHarmonizationUnit();
            }
        }
    }

    /**
     * 
     * @return the unit where is harmonized for the default harmonization universe. The harmoniztion unit must be a valid one,
     *         i.e. connected to the top structure unit for this year
     * @see UnitSiadapWrapper#isValidHarmonizationUnit();
     */
    public Unit getValidUnitWhereIsHarmonized() {
        return getValidUnitWhereIsHarmonized(getDefaultSiadapUniverse());
    }

    public Unit getValidUnitWhereIsHarmonized(SiadapUniverse siadapUniverse) {
        if (siadapUniverse == null) {
            return null;
        }
        List<Unit> parentUnits = getParentUnits(getParty(), siadapUniverse.getHarmonizationRelation(getConfiguration()));
        if (parentUnits.isEmpty()) {
            return null;
        }
        if (parentUnits.size() > 1) {
            throw new SiadapException("inconsistent.harmonization.units");

        } else {
            UnitSiadapWrapper unitWrapper = new UnitSiadapWrapper(parentUnits.get(0), getYear());
            if (unitWrapper.isValidHarmonizationUnit()) {
                return unitWrapper.getUnit();
            } else {
                return unitWrapper.getValidHarmonizationUnit();
            }
        }
    }

    public Unit getHarmonizationUnitForSIADAP2() {
        return getUnitWhereIsHarmonized(SiadapUniverse.SIADAP2);
    }

    public Unit getHarmonizationUnitForSIADAP3() {
        return getUnitWhereIsHarmonized(SiadapUniverse.SIADAP3);
    }

    public boolean isPersonWorkingInValidSIADAPUnit() {
        UnitSiadapWrapper workingUnit = getWorkingUnit();
        if (workingUnit == null) {
            return false;
        }

        return UnitSiadapWrapper.isValidSIADAPUnit(workingUnit.getUnit(), getYear());
    }

    public boolean isAccessibleToCurrentUser() {
        Siadap siadap = getSiadap();
        if (siadap == null) {
            return false;
        }
        return siadap.getProcess().isAccessibleToCurrentUser();
    }

    public boolean isCurrentUserAbleToEvaluate() {
        return getEvaluator() != null && getEvaluator().getPerson() == UserView.getCurrentUser().getPerson();
    }

    public boolean isCurrentUserAbleToCreateProcess() {
        return getSiadap() == null && SiadapYearConfiguration.getStructureManagementGroup().isMember(UserView.getCurrentUser());
    }

    public boolean isCurrentUserAbleToSeeAutoEvaluationDetails() {
        User currentUser = UserView.getCurrentUser();
        if (!isCurrentUserAbleToSeeDetails()) {
            return false;
        }
        if (getSiadap().getEvaluated().equals(currentUser.getPerson())) {
            return true;
        }
        if (getSiadap().isAutoEvaliationDone()) {
            return true;
        }
        return false;
    }

    public static class ObjectiveEvaluationWrapperBean implements Serializable {

        private final static int MEASUREMENT_INDICATOR_MAX_NR_CHARS = 10;
        private final static int SUPERATION_CRITERIA_MAX_NR_CHARS = 25;

        /**
         * 1st version
         */
        private static final long serialVersionUID = 1L;

        private String aggregatedMeasurementIndicator;

        private String aggregatedSuperationCriteria;

        private String aggregatedAutoEvaluation;

        private String aggregatedEvaluation;

        private String aggregatedPonderationFactor;

        private ObjectiveEvaluation objectiveEvaluation;

        public ObjectiveEvaluationWrapperBean(ObjectiveEvaluation objectiveEvaluation) {
            if (objectiveEvaluation == null) {
                // do nothing
            } else {

                this.objectiveEvaluation = objectiveEvaluation;

                Map<String, Integer> linesPerString;
                StringBuilder stringBuilderMeasurementIndicator = new StringBuilder();
                StringBuilder stringBuilderSuperationCriteria = new StringBuilder();
                StringBuilder stringBuilderAutoEvaluation = new StringBuilder();
                StringBuilder stringBuilderEvaluation = new StringBuilder();
                StringBuilder stringBuilderPonderationFactor = new StringBuilder();

                for (ObjectiveEvaluationIndicator objectiveEvaluationIndicator : objectiveEvaluation.getIndicators()) {
                    int maxNrLines = 0;
                    linesPerString = new HashMap<String, Integer>();
                    // asserting the string that has the max. number of lines
                    linesPerString.put(
                            objectiveEvaluationIndicator.getMeasurementIndicator(),
                            countNrLines(objectiveEvaluationIndicator.getMeasurementIndicator(),
                                    MEASUREMENT_INDICATOR_MAX_NR_CHARS));
                    linesPerString.put(objectiveEvaluationIndicator.getSuperationCriteria(),
                            countNrLines(objectiveEvaluationIndicator.getSuperationCriteria(), SUPERATION_CRITERIA_MAX_NR_CHARS));

                    for (Integer nrLines : linesPerString.values()) {
                        if (maxNrLines < nrLines.intValue()) {
                            maxNrLines = nrLines;
                        }
                    }
                    // StringUtils.countMatches(objectiveEvaluationIndicator.get

                    appendLineAndNrLines(stringBuilderSuperationCriteria,
                            linesPerString.get(objectiveEvaluationIndicator.getSuperationCriteria()),
                            objectiveEvaluationIndicator.getSuperationCriteria(), maxNrLines);
                    appendLineAndNrLines(stringBuilderMeasurementIndicator,
                            linesPerString.get(objectiveEvaluationIndicator.getMeasurementIndicator()),
                            objectiveEvaluationIndicator.getMeasurementIndicator(), maxNrLines);

                    if (objectiveEvaluationIndicator.getAutoEvaluation() != null) {
                        appendLineAndNrLines(stringBuilderAutoEvaluation, 1, objectiveEvaluationIndicator.getAutoEvaluation()
                                .getLocalizedName(), maxNrLines);
                    } else {
                        appendLineAndNrLines(stringBuilderAutoEvaluation, 1, "", maxNrLines);
                    }

                    if (objectiveEvaluationIndicator.getEvaluation() != null) {
                        appendLineAndNrLines(stringBuilderEvaluation, 1, objectiveEvaluationIndicator.getEvaluation()
                                .getLocalizedName(), maxNrLines);
                    } else {
                        appendLineAndNrLines(stringBuilderEvaluation, 1, "", maxNrLines);
                    }

                    appendLineAndNrLines(stringBuilderPonderationFactor, 1, objectiveEvaluationIndicator.getPonderationFactor()
                            .movePointRight(2).toString()
                            + "%", maxNrLines);

                }
                aggregatedMeasurementIndicator = stringBuilderMeasurementIndicator.toString().trim();
                aggregatedSuperationCriteria = stringBuilderSuperationCriteria.toString().trim();
                aggregatedAutoEvaluation = stringBuilderAutoEvaluation.toString().trim();
                aggregatedEvaluation = stringBuilderEvaluation.toString().trim();
                aggregatedPonderationFactor = stringBuilderPonderationFactor.toString().trim();
            }
        }

        private void appendLineAndNrLines(StringBuilder stringBuilderToAffect, int nrLinesOccupiedByString, String string,
                int maxNrLines) {
            // let's add the excedent nr of lines
            stringBuilderToAffect.append(string);
            for (int i = nrLinesOccupiedByString; i < maxNrLines; i++) {
                stringBuilderToAffect.append('\n');
            }
            stringBuilderToAffect.append('\n');
            stringBuilderToAffect.append('\n');
        }

        /**
         * 
         * @param string
         * @param maxCharsPerLine
         * @return the nr of lines that it occupies, with a trimmed string and
         *         counting the nr of \n as a line and also of max consecutive
         *         non \n chars
         */
        private int countNrLines(String string, int maxCharsPerLine) {
            String trimmedString = string.trim();
            int nrLines = 1;
            int aux = 1;
            for (int i = 0; i < trimmedString.length(); i++) {
                if (trimmedString.charAt(i) == '\n' || maxCharsPerLine == aux) {
                    aux = 1;
                    nrLines++;
                } else {
                    aux++;
                }
            }
            return nrLines;

        }

        public String getAggregatedMeasurementIndicator() {
            return aggregatedMeasurementIndicator;
        }

        public String getAggregatedSuperationCriteria() {
            return aggregatedSuperationCriteria;
        }

        public String getAggregatedAutoEvaluation() {
            return aggregatedAutoEvaluation;
        }

        public String getAggregatedEvaluation() {
            return aggregatedEvaluation;
        }

        public String getAggregatedPonderationFactor() {
            return aggregatedPonderationFactor;
        }

        public ObjectiveEvaluation getObjectiveEvaluation() {
            return objectiveEvaluation;
        }

        public void setAggregatedMeasurementIndicator(String aggregatedMeasurementIndicator) {
            this.aggregatedMeasurementIndicator = aggregatedMeasurementIndicator;
        }

        public void setAggregatedSuperationCriteria(String aggregatedSuperationCriteria) {
            this.aggregatedSuperationCriteria = aggregatedSuperationCriteria;
        }

        public void setAggregatedAutoEvaluation(String aggregatedAutoEvaluation) {
            this.aggregatedAutoEvaluation = aggregatedAutoEvaluation;
        }

        public void setAggregatedEvaluation(String aggregatedEvaluation) {
            this.aggregatedEvaluation = aggregatedEvaluation;
        }

        public void setAggregatedPonderationFactor(String aggregatedPonderationFactor) {
            this.aggregatedPonderationFactor = aggregatedPonderationFactor;
        }

        public void setObjectiveEvaluation(ObjectiveEvaluation objectiveEvaluation) {
            this.objectiveEvaluation = objectiveEvaluation;
        }

    }

    public List<ObjectiveEvaluationWrapperBean> getAllObjEvaluationWrapperBeansOfDefaultEval() {
        ArrayList<ObjectiveEvaluationWrapperBean> objBeans = new ArrayList<PersonSiadapWrapper.ObjectiveEvaluationWrapperBean>();

        for (ObjectiveEvaluation objectiveEvaluation : getSiadap().getDefaultSiadapEvaluationUniverse().getObjectiveEvaluations()) {
            objBeans.add(new ObjectiveEvaluationWrapperBean(objectiveEvaluation));
        }
        if (objBeans.size() == 0) {
            objBeans.add(new ObjectiveEvaluationWrapperBean(null));
        }

        return objBeans;
    }

    /**
     * 
     * @return A list with all {@link ObjectiveEvaluationIndicator} objects
     *         belonging to the default {@link SiadapEvaluationUniverse}. Method
     *         used by the Jasper Reports framework to generate the Siadap
     *         proccess document
     *         {@link SiadapManagement#downloadAndGenerateSiadapDocument(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
     */
    public List<ObjectiveEvaluationIndicator> getAllObjEvalIndicatorsOfDefaultEval() {
        ArrayList<ObjectiveEvaluationIndicator> listToReturn = new ArrayList<ObjectiveEvaluationIndicator>();
        if (getSiadap() == null || getSiadap().getDefaultSiadapEvaluationUniverse() == null
                || getSiadap().getDefaultSiadapEvaluationUniverse().getObjectiveEvaluations() == null) {
            return listToReturn;
        }
        for (ObjectiveEvaluation objectiveEvaluation : getSiadap().getDefaultSiadapEvaluationUniverse().getObjectiveEvaluations()) {
            listToReturn.addAll(objectiveEvaluation.getIndicators());
        }
        return listToReturn;

    }

    public boolean isCurrentUserAbleToSeeEvaluationDetails() {
        User currentUser = UserView.getCurrentUser();
        if (!isCurrentUserAbleToSeeDetails()) {
            return false;
        }
        if (getSiadap().getEvaluator().getPerson().equals(currentUser.getPerson())) {
            return true;
        }
        if (getSiadap().isDefaultEvaluationDone() && getSiadap().getEvaluated().equals(currentUser.getPerson())
                && getSiadap().getRequestedAcknowledegeValidationDate() != null) {
            return true;
        }
        if (getSiadap().isDefaultEvaluationDone() && isResponsibleForHarmonization(currentUser.getPerson())) {
            return true;
        }
        if (getSiadap().isDefaultEvaluationDone() && getConfiguration().isCurrentUserMemberOfCCA()) {
            return true;
        }
        return false;
    }

    // TODO joantune: only here temporarily, probably should be removed
    public BigDecimal getTotalEvaluationScoring() {
        return getTotalEvaluationScoring(getSiadap().getDefaultSiadapUniverse());
    }

    public BigDecimal getTotalEvaluationScoring(SiadapUniverse siadapUniverse) {
        Siadap siadap = getSiadap();
        SiadapEvaluationUniverse siadapEvaluationUniverse = siadap.getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
        if (siadap == null || siadapEvaluationUniverse == null || !siadap.isEvaluationDone(siadapUniverse)) {
            return null;
        }

        return siadapEvaluationUniverse.getTotalEvaluationScoring();
    }

    public BigDecimal getTotalEvaluationScoringSiadap2() {
        return getTotalEvaluationScoring(SiadapUniverse.SIADAP2);
    }

    public BigDecimal getTotalEvaluationScoringSiadap3() {
        return getTotalEvaluationScoring(SiadapUniverse.SIADAP3);
    }

    public UnitSiadapWrapper getWorkingUnit() {
        Collection<Unit> parentUnits =
                getParentUnits(getConfiguration().getWorkingRelation(), getConfiguration().getWorkingRelationWithNoQuota());
        return parentUnits.isEmpty() ? null : new UnitSiadapWrapper(parentUnits.iterator().next(), getConfiguration().getYear());
    }

    public boolean isQuotaAware() {
        return getParentUnits(getConfiguration().getWorkingRelationWithNoQuota()).isEmpty() ? true : false;
    }

    public boolean isResponsibleForHarmonization(Person accessor) {
        return getWorkingUnit().isPersonResponsibleForHarmonization(accessor);
    }

    public Set<PersonSiadapWrapper> getPeopleToEvaluate() {
        // if no configuration has been set for the current year, we retrieve
        // null!
        if (SiadapYearConfiguration.getSiadapYearConfiguration(Integer.valueOf(getYear())) == null) {
            return null;
        }
        Set<PersonSiadapWrapper> people = new HashSet<PersonSiadapWrapper>();
        final PersonSiadapWrapper evaluator = new PersonSiadapWrapper(getPerson(), getYear());
        final AccountabilityType evaluationRelation = getConfiguration().getEvaluationRelation();

        for (Person person : evaluator.getChildPersons(evaluationRelation)) {
            people.add(new PersonSiadapWrapper(person, getYear()));
        }
        for (Unit unit : evaluator.getParentUnits(evaluationRelation)) {
            people.addAll(new UnitSiadapWrapper(unit, getYear()).getUnitEmployees(new Predicate() {

                @Override
                public boolean evaluate(Object arg0) {
                    PersonSiadapWrapper wrapper = (PersonSiadapWrapper) arg0;
                    PersonSiadapWrapper evaluatorWrapper = wrapper.getEvaluator();
                    return evaluatorWrapper != null && evaluatorWrapper.equals(evaluator);
                }

            }));
        }
        return people;

    }

    public boolean getHasUnreadComments() {
        if (getSiadap() == null || getSiadap().getProcess() == null) {
            return false;
        }
        if (getSiadap().getProcess().getUnreadCommentsForCurrentUser().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public Set<Siadap> getAllSiadaps() {
        Set<Siadap> processes = new TreeSet<Siadap>(new Comparator<Siadap>() {

            @Override
            public int compare(Siadap o1, Siadap o2) {
                return o1.getYear().compareTo(o2.getYear());
            }

        });

        processes.addAll(getPerson().getSiadapsAsEvaluatedSet());
        return processes;
    }

    public String getName() {
        if (getPerson() == null || getPerson().getName() == null) {
            throw new DomainException("Person or person's name not defined");
        }
        return getPerson().getName();
    }

    @Override
    public int hashCode() {
        return getPerson().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PersonSiadapWrapper) {
            return ((PersonSiadapWrapper) obj).getPerson() == getPerson();
        }
        return false;
    }

    /**
     * Verifies that the given date is indeed within the {@link #getYear()} year. If it isn't, it will throw a DomainException
     * 
     * @param dateToVerify
     *            the date to verify
     */
    private void verifyDate(LocalDate dateToVerify) {
        if (dateToVerify.getYear() != getYear()) {
            throw new DomainException("manage.workingUnitOrEvaluator.invalid.date",
                    DomainException.getResourceFor("resources/SiadapResources"), String.valueOf(getYear()));
        }
    }

    public void changeHarmonizationUnitTo(Unit unit, LocalDate dateOfChange, String justification) throws SiadapException {
        SiadapActionChangeValidatorEnum.HARMONIZATION_UNIT_CHANGE.validate(this, unit);

        verifyDate(dateOfChange);

        SiadapYearConfiguration configuration = getConfiguration();

        if (!UnitSiadapWrapper.isValidSIADAPHarmonizationUnit(unit, getYear())) {
            throw new SiadapException("error.changing.harmonization.unit.to.an.invalid.one", unit.getPresentationName());
        }

        // find out what is the accountability we should be changing
        AccountabilityType defaultUniverseHarmonizationRelation =
                getDefaultSiadapUniverse().getHarmonizationRelation(configuration);

        // let's change the harmonization unit then
        for (Accountability accountability : getParentAccountabilityTypes(defaultUniverseHarmonizationRelation)) {
            if (accountability.isActive(getConfiguration().getLastDayForAccountabilities())) {
                accountability.editDates(accountability.getBeginDate(), dateOfChange, justification);
            }
        }
        unit.addChild(getPerson(), defaultUniverseHarmonizationRelation, dateOfChange, null, justification);

    }

    @Atomic
    public void changeWorkingUnitTo(Unit unit, Boolean withQuotas, LocalDate dateOfChange, String justification) {

        SiadapActionChangeValidatorEnum.WORKING_UNIT_CHANGE.validate(this);
        SiadapActionChangeValidatorEnum.EVALUATOR_CHANGE.validate(this);

        verifyDate(dateOfChange);
        // we must check if the quotas can be changed i.e. he is
        // waiting harmonization or not.

        SiadapYearConfiguration configuration = getConfiguration();

        if (getWorkingUnit() != null) {
            // let's check the state

            if (!UnitSiadapWrapper.isValidSIADAPUnit(unit, getYear())) {
                throw new SiadapException("error.changing.working.unit.to.an.unregistered.one");
            }

            for (Accountability accountability : getParentAccountabilityTypes(configuration.getWorkingRelation(),
                    configuration.getWorkingRelationWithNoQuota())) {
                if (accountability.isActive(configuration.getLastDayForAccountabilities())) {
                    accountability.editDates(accountability.getBeginDate(), dateOfChange);
                }
            }
        }
        unit.addChild(getPerson(),
                withQuotas ? configuration.getWorkingRelation() : configuration.getWorkingRelationWithNoQuota(), dateOfChange,
                null);

        // let's also change the harmonization unit
        changeHarmonizationUnitTo(unit, dateOfChange, justification);
    }

    // use the version that allows a date instead (may not apply to all of the
    // cases. If so, please delete the Deprecated tag)
    @Deprecated
    public void changeWorkingUnitTo(Unit unit, Boolean withQuotas) {
        changeWorkingUnitTo(unit, withQuotas, new LocalDate(), null);
    }

    // use the version that allows a date instead (may not apply to all of the
    // cases. If so, please delete the Deprecated tag)
    @Deprecated
    public void changeEvaluatorTo(Person newEvaluator) {
        changeEvaluatorTo(newEvaluator, new LocalDate());
    }

    @Atomic
    public void changeEvaluatorTo(Person newEvaluator, LocalDate dateOfChange) {

        SiadapActionChangeValidatorEnum.EVALUATOR_CHANGE.validate(this);

        verifyDate(dateOfChange);
        @SuppressWarnings("boxing")
        LocalDate startOfYear = SiadapMiscUtilClass.firstDayOfYear(dateOfChange.getYear());
        LocalDate endOfYear = SiadapMiscUtilClass.lastDayOfYear(dateOfChange.getYear());
        SiadapYearConfiguration configuration = getConfiguration();
        AccountabilityType evaluationRelation = configuration.getEvaluationRelation();
        boolean needToAddAcc = true;
        for (Accountability accountability : getParentAccountabilityTypes(evaluationRelation)) {
            if (accountability.isActive(getConfiguration().getLastDayForAccountabilities())
                    && accountability.getParent() instanceof Person && accountability.getChild() instanceof Person) {
                // let's close it if we have a different person here
                if (!accountability.getParent().equals(newEvaluator)) {
                    accountability.editDates(accountability.getBeginDate(), dateOfChange);
                } else {
                    needToAddAcc = false;
                }
            }
        }
        if (needToAddAcc) {
            // let's
            newEvaluator.addChild(getPerson(), evaluationRelation, dateOfChange, null);

        }

    }

    /**
     * 
     * @param unit
     *            the unit to consider the harmonization for. It must be a
     *            harmonization unit
     * @return the {@link SiadapUniverse} for which he is being harmonized in
     *         the given unit
     */
    public SiadapUniverse getSiadapUniverseWhichIsBeingHarmonized(Unit unit) {
        // let's try to find this person ('ourselves') directly, if not let's
        // descend if we are an harmonization unit
        UnitSiadapWrapper unitWrapper = new UnitSiadapWrapper(unit, getYear());
        if (!unitWrapper.isHarmonizationUnit()) {
            throw new IllegalArgumentException("you're doing it wrong :D harmonization units only");
        }
        return getSiadapUniverseInGivenUnit(unit);
    }

    private SiadapUniverse getSiadapUniverseInGivenUnit(Unit unit) {
        SiadapYearConfiguration siadapYearConfiguration = getConfiguration();
        AccountabilityType siadap2HarmonizationRelation = siadapYearConfiguration.getSiadap2HarmonizationRelation();
        AccountabilityType siadap3HarmonizationRelation = siadapYearConfiguration.getSiadap3HarmonizationRelation();
        UnitSiadapWrapper unitWrapper = new UnitSiadapWrapper(unit, getYear());
        List<Accountability> childAccountabilities =
                unitWrapper.getChildAccountabilityTypes(siadap2HarmonizationRelation, siadap3HarmonizationRelation);
        for (Accountability acc : childAccountabilities) {
            if (acc.getChild().equals(getPerson())) {

                if (acc.hasAccountabilityType(siadap2HarmonizationRelation)) {
                    return SiadapUniverse.SIADAP2;
                }
                if (acc.hasAccountabilityType(siadap3HarmonizationRelation)) {
                    return SiadapUniverse.SIADAP3;
                }

            }
        }
        // let's descend
        SiadapUniverse siadapUniverseToReturn = null;
        for (Unit childUnit : unitWrapper.getChildUnits(siadapYearConfiguration.getHarmonizationUnitRelations())) {
            SiadapUniverse siadapUniverseInGivenUnit = getSiadapUniverseInGivenUnit(childUnit);
            if (siadapUniverseInGivenUnit != null) {
                siadapUniverseToReturn = siadapUniverseInGivenUnit;
            }
        }
        return siadapUniverseToReturn;

    }

    public boolean isCustomEvaluatorDefined() {
        return !getParentPersons(getConfiguration().getEvaluationRelation()).isEmpty();
    }

    @Atomic
    public void removeCustomEvaluator() {
        SiadapActionChangeValidatorEnum.EVALUATOR_CHANGE.validate(this);

        AccountabilityType evaluationRelation = getConfiguration().getEvaluationRelation();
        for (Accountability accountability : getParentAccountabilityTypes(evaluationRelation)) {
            if (accountability.isActiveNow() && accountability.getChild() instanceof Person
                    && accountability.getParent() instanceof Person) {
                // ok, so we have the acc.
                LocalDate dateToEndTheAcc = new LocalDate();
                if (accountability.getBeginDate().getYear() == getYear()) {
                    if (dateToEndTheAcc.isBefore(accountability.getBeginDate().plusDays(1))) {
                        // then we actually have to 'delete' it
                        accountability.delete();
                    }
                    dateToEndTheAcc = accountability.getBeginDate().plusDays(1);
                } else {
                    // let's close it on the last day of the previous year
                    dateToEndTheAcc = getConfiguration().getPreviousSiadapYearConfiguration().getLastDay();
                }
                if (!accountability.isErased()) {
                    accountability.setEndDate(dateToEndTheAcc);
                }
            }
        }
    }

    @Atomic
    public void removeHarmonizationAssessments(SiadapUniverse siadapUniverse, Unit harmonizationUnit) {
        if (getSiadap() == null || harmonizationUnit == null) {
            throw new SiadapException("error.invalid.data");
        }

        SiadapEvaluationUniverse evaluationUniverse = getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
        if ((evaluationUniverse.getHarmonizationAssessment() != null && !evaluationUniverse.getHarmonizationAssessment())
                || (evaluationUniverse.getHarmonizationAssessmentForExcellencyAward() != null && !evaluationUniverse
                        .getHarmonizationAssessmentForExcellencyAward())) {
            // if we had a No on the harmonizationAssessment or in the regular
            // one we might have an ExceedingQuotaProposal
            // so let's check if it is so, and if it is, remove it and adjust
            // the priority numbers of the rest of them
            ExceedingQuotaProposal quotaProposalFor =
                    ExceedingQuotaProposal.getQuotaProposalFor(harmonizationUnit, getYear(), getPerson(), siadapUniverse,
                            isQuotaAware());
            if (quotaProposalFor != null) {
                quotaProposalFor.remove();
            }

        }
        evaluationUniverse.removeHarmonizationAssessments();
    }

    public String getCareerName() {
        if (getDefaultCompetenceTypeObject() == null) {
            return "";
        }
        return getDefaultCompetenceTypeObject().getName();
    }

    public CompetenceType getDefaultCompetenceTypeObject() {
        if (getSiadap() == null || getSiadap().getDefaultSiadapEvaluationUniverse() == null
                || getSiadap().getDefaultSiadapEvaluationUniverse().getCompetenceSlashCareerType() == null) {
            return null;
        }
        return getSiadap().getDefaultSiadapEvaluationUniverse().getCompetenceSlashCareerType();

    }

    public Boolean getHomologationDone() {
        Siadap siadap = getSiadap();
        return siadap != null ? siadap.isHomologated() : null;
    }

    public boolean isCCAMember() {
        return SiadapRootModule.getInstance().getSiadapCCAGroup().isMember(getPerson().getUser());
    }

    public boolean isHomologationMember() {
        return getConfiguration().getHomologationMembers().contains(getPerson());
    }

    public boolean isHarmonizationPeriodOpen() {
        return UnitSiadapWrapper.isHarmonizationPeriodOpen(getConfiguration());
    }

    public Set<Accountability> getAccountabilitiesHistory() {
        Person person = getPerson();
        Set<Accountability> history = new TreeSet<Accountability>(new Comparator<Accountability>() {

            @Override
            public int compare(Accountability o1, Accountability o2) {
                int compareBegin = o1.getBeginDate().compareTo(o2.getBeginDate());
                LocalDate endDate = o1.getEndDate();
                LocalDate endDate2 = o2.getEndDate();
                return compareBegin != 0 ? compareBegin : endDate != null && endDate2 != null ? o1.getEndDate().compareTo(
                        o2.getEndDate()) : endDate == null && endDate2 != null ? 1 : endDate2 == null && endDate != null ? -1 : o1
                        .getExternalId().compareTo(o2.getExternalId());
            }

        });
        int year = getYear();
        for (SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year); configuration != null; configuration =
                SiadapYearConfiguration.getSiadapYearConfiguration(--year)) {
            history.addAll(person.getParentAccountabilities(configuration.getWorkingRelation(),
                    configuration.getWorkingRelationWithNoQuota()));
        }

        return history;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * @see #changeDefaultUniverseTo(SiadapUniverse, LocalDate, boolean) - it's
     *      equal but with a force of false
     */
    public void changeDefaultUniverseTo(SiadapUniverse siadapUniverseToChangeTo, LocalDate dateOfChange) {
        changeDefaultUniverseTo(siadapUniverseToChangeTo, dateOfChange, false);
    }

    /**
     * 
     * @param siadapUniverseToChangeTo
     *            the default {@link SiadapUniverse} to change to
     * @param dateOfChange
     *            the date to change
     * @param forceChange
     *            if set to true, it will allow to make the change even if the
     *            evaluation has been harmonized, but NOT if it has been
     *            validated
     */
    @Atomic
    public void changeDefaultUniverseTo(SiadapUniverse siadapUniverseToChangeTo, LocalDate dateOfChange, boolean forceChange) {
        SiadapUniverse defaultSiadapUniverse = getSiadap().getDefaultSiadapUniverse();

        verifyDate(dateOfChange);
        if (!forceChange) {
            // let's check if we have a closed harmonization, if so, we
            // shouldn't allow the change
            Unit unitWhereIsHarmonized = getUnitWhereIsHarmonized(defaultSiadapUniverse);
            if (new UnitSiadapWrapper(unitWhereIsHarmonized, getYear()).isHarmonizationFinished()) {
                throw new SiadapException("error.cant.change.siadap.universe.because.it.has.closed.harmonization");
            }
        } else {
            // let's make sure it is not validated
            if (getSiadap().getDefaultSiadapEvaluationUniverse() != null) {
                if (getSiadap().getDefaultSiadapEvaluationUniverse().getValidationDate() != null) {
                    throw new SiadapException("error.cant.change.siadap.universe.because.it.has.closed.validation");
                }
            }
        }

        // let's also change the Harmonization relation
        Accountability retrieveDefaultHarmAccForGivenSiadapUniverse =
                retrieveDefaultHarmAccForGivenSiadapUniverse(getSiadap().getDefaultSiadapUniverse());
        if (retrieveDefaultHarmAccForGivenSiadapUniverse != null) {
            // if we had one, let's close it
            retrieveDefaultHarmAccForGivenSiadapUniverse.setEndDate(dateOfChange);

            // and now let's create a new one
            retrieveDefaultHarmAccForGivenSiadapUniverse.getParent().addChild(getPerson(),
                    siadapUniverseToChangeTo.getHarmonizationRelation(getYear()), dateOfChange, null);
        }

        getSiadap().setDefaultSiadapUniverse(siadapUniverseToChangeTo);
    }

    /**
     * 
     * @param siadapUniverse
     *            the siadapUniverse to
     * @return the {@link Accountability} responsible for giving the hint on the
     *         HarmUnit
     */
    private Accountability retrieveDefaultHarmAccForGivenSiadapUniverse(SiadapUniverse siadapUniverse) {
        if (siadapUniverse == null) {
            return null;
        }
        AccountabilityType harmonizationRelation = siadapUniverse.getHarmonizationRelation(getConfiguration());

        List<Accountability> parentAccountabilityTypes = getParentAccountabilityTypes(harmonizationRelation);
        // if it retrieved more than one, it actually shouldn't, as it shows
        // that we are being evaluated with the same universe
        if (parentAccountabilityTypes.size() > 1) {
            throw new SiadapException("inconsistent.harmonization.units");
        }
        if (parentAccountabilityTypes.size() == 1) {
            return parentAccountabilityTypes.get(0);
        }

        return null;

    }

    // private boolean hasHarmonizationAssessment(SiadapUniverse siadapUniverse)
    // {
    // return
    // getHarmonizationCurrentAssessmentFor(getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse))
    // != null;
    // }
    //
    // public boolean getHarmonizationAssessmentForSIADAP2Defined() {
    // return hasHarmonizationAssessment(SiadapUniverse.SIADAP2);
    //
    // }
    //
    // public boolean getHarmonizationAssessmentForSIADAP3Defined() {
    // return hasHarmonizationAssessment(SiadapUniverse.SIADAP3);
    //
    // }

    public Boolean getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP2() {
        return harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2;
    }

    public void setHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP2(
            Boolean harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2) {
        this.harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2 =
                harmonizationCurrentAssessmentForExcellencyAwardForSIADAP2;
    }

    public Boolean getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP3() {
        return harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3;
    }

    public void setHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP3(
            Boolean harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3) {
        this.harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3 =
                harmonizationCurrentAssessmentForExcellencyAwardForSIADAP3;
    }

    protected Boolean getHarmonizationCurrentAssessmentFor(SiadapUniverse siadapUniverse) {
        return getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse).getHarmonizationAssessment();
    }

    protected Boolean getHarmonizationCurrentExcellencyAssessmentFor(SiadapUniverse siadapUniverse) {
        return getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse)
                .getHarmonizationAssessmentForExcellencyAward();
    }

    public Boolean getHarmonizationCurrentAssessmentForSIADAP2() {
        return this.harmonizationCurrentAssessmentForSIADAP2;
    }

    public Boolean getHarmonizationCurrentAssessmentForSIADAP3() {
        return this.harmonizationCurrentAssessmentForSIADAP3;
    }

    private boolean isAbleToRemoveAssessmentsFor(SiadapUniverse siadapUniverse) {
        SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse =
                getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);

        if (siadapEvaluationUniverseForSiadapUniverse == null) {
            return false;
        }
        return isHarmonizationPeriodOpen()
                && siadapEvaluationUniverseForSiadapUniverse.getHarmonizationDate() == null
                && (siadapEvaluationUniverseForSiadapUniverse.getHarmonizationAssessment() != null || siadapEvaluationUniverseForSiadapUniverse
                        .getHarmonizationAssessmentForExcellencyAward() != null);
    }

    public boolean getAbleToRemoveAssessmentsForSIADAP3() {
        return isAbleToRemoveAssessmentsFor(SiadapUniverse.SIADAP3);
    }

    public boolean getAbleToRemoveAssessmentsForSIADAP2() {
        return isAbleToRemoveAssessmentsFor(SiadapUniverse.SIADAP2);
    }

    @Atomic
    public void setHarmonizationCurrentAssessments(SiadapUniverse siadapUniverse) {
        SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse =
                getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
        Boolean harmonizationCurrentAssessment = null;
        Boolean harmonizationCurrentAssessmentForExcellencyAward = null;
        if (siadapUniverse.equals(SiadapUniverse.SIADAP2)) {
            harmonizationCurrentAssessment = getHarmonizationCurrentAssessmentForSIADAP2();
            harmonizationCurrentAssessmentForExcellencyAward = getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP2();
        } else if (siadapUniverse.equals(SiadapUniverse.SIADAP3)) {
            harmonizationCurrentAssessment = getHarmonizationCurrentAssessmentForSIADAP3();
            harmonizationCurrentAssessmentForExcellencyAward = getHarmonizationCurrentAssessmentForExcellencyAwardForSIADAP3();
        }
        // if we have no 'No's in the new assessments, we should clean out any
        // ExceedingQuotaProposals for that person
        if ((harmonizationCurrentAssessment == null || harmonizationCurrentAssessment)
                && (harmonizationCurrentAssessmentForExcellencyAward == null || harmonizationCurrentAssessmentForExcellencyAward))
        // if
        // (siadapEvaluationUniverseForSiadapUniverse.getHarmonizationAssessment()
        // != null
        // &&
        // !siadapEvaluationUniverseForSiadapUniverse.getHarmonizationAssessment()
        // && harmonizationCurrentAssessment != null
        // && harmonizationCurrentAssessment
        // && harmonizationCurrentAssessment.booleanValue() !=
        // siadapEvaluationUniverseForSiadapUniverse
        // .getHarmonizationAssessment().booleanValue())
        {
            ExceedingQuotaProposal quotaProposalFor =
                    ExceedingQuotaProposal.getQuotaProposalFor(getUnitWhereIsHarmonized(siadapUniverse), getYear(), person,
                            siadapUniverse, isQuotaAware());
            if (quotaProposalFor != null) {
                quotaProposalFor.remove();
            }
        }

        // we must have an excellent of false if we have a regular of true
        if (siadapEvaluationUniverseForSiadapUniverse.hasExcellencyAwardedFromEvaluator()) {
            if (harmonizationCurrentAssessmentForExcellencyAward != null && harmonizationCurrentAssessmentForExcellencyAward
                    && harmonizationCurrentAssessment != null && !harmonizationCurrentAssessment) {
                throw new SiadapException("error.harmonization.inconsistency.between.excellency.and.regular.assessment");
            }
        }
        siadapEvaluationUniverseForSiadapUniverse.setHarmonizationAssessments(harmonizationCurrentAssessment,
                harmonizationCurrentAssessmentForExcellencyAward);
    }

    public void setHarmonizationCurrentAssessmentForSIADAP3(Boolean assessment) {
        this.harmonizationCurrentAssessmentForSIADAP3 = assessment;
    }

    public void setHarmonizationCurrentAssessmentForSIADAP2(Boolean assessment) {
        this.harmonizationCurrentAssessmentForSIADAP2 = assessment;
    }

    /**
     * @param perserveResponsabilityRelations preserve evaluation responsabilities, as well as harmonization ones. Only removes
     *            the working relations and the relations that tell you where you are harmonized i.e.
     *            {@link SiadapYearConfiguration#getSiadap2HarmonizationRelation()},
     *            {@link SiadapYearConfiguration#getSiadap3HarmonizationRelation()},
     *            {@link SiadapYearConfiguration#getWorkingRelation()},
     *            {@link SiadapYearConfiguration#getWorkingRelationWithNoQuota()}
     * 
     * @throws SiadapException
     *             if the SIADAP proccess exists. Then it should be nullified ( {@link NullifyProcess}) instead
     * 
     */
    @Atomic
    public void removeFromSiadapStructure(boolean preserveResponsabilityRelations) throws SiadapException {
        if (getSiadap() != null) {
            // shouldn't remove the structure, simply nullify it
            throw new SiadapException("error.should.nullify.not.remove",
                    getPerson() != null ? getPerson().getPresentationName() : "-");
        }

        Set<AccountabilityType> accTypesToConsider = new HashSet<AccountabilityType>();

        if (preserveResponsabilityRelations == false) {
            accTypesToConsider.add(getConfiguration().getUnitRelations());
            accTypesToConsider.add(getConfiguration().getHarmonizationUnitRelations());
            accTypesToConsider.add(getConfiguration().getHarmonizationResponsibleRelation());
        }

        accTypesToConsider.add(getConfiguration().getEvaluationRelation());
        accTypesToConsider.add(getConfiguration().getWorkingRelation());
        accTypesToConsider.add(getConfiguration().getWorkingRelationWithNoQuota());
        accTypesToConsider.add(getConfiguration().getSiadap2HarmonizationRelation());
        accTypesToConsider.add(getConfiguration().getSiadap3HarmonizationRelation());

        for (Accountability acc : getPerson().getParentAccountabilities(accTypesToConsider)) {
            if (acc.isActive(SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(getYear()))) {
                if (preserveResponsabilityRelations
                        && acc.getAccountabilityType().equals(getConfiguration().getEvaluationRelation())
                        && acc.getParent() instanceof Unit) {
                    //let us not remove this one, because if we did, we would have deleted a responsability towards
                    //evaluating a unit
                    continue;
                }

                // let's close it on the last day of the previous year, or, in
                // case it has a beginning year equal to this one, let's delete
                // it because it was a mistake
                if (acc.getBeginDate().getYear() == getYear()) {
                    acc.delete();
                } else {
                    acc.setEndDate(getConfiguration().getPreviousSiadapYearConfiguration().getLastDay());
                }
            }
        }
    }

    public BigDecimal getEvaluatorClassificationForSIADAP2() {
        return evaluatorClassificationForSIADAP2;
    }

    public void setEvaluatorClassificationForSIADAP2(BigDecimal evaluatorClassificationForSIADAP2) {
        this.evaluatorClassificationForSIADAP2 = evaluatorClassificationForSIADAP2;
    }

    public BigDecimal getEvaluatorClassificationForSIADAP3() {
        return evaluatorClassificationForSIADAP3;
    }

    public void setEvaluatorClassificationForSIADAP3(BigDecimal evaluatorClassificationForSIADAP3) {
        this.evaluatorClassificationForSIADAP3 = evaluatorClassificationForSIADAP3;
    }

    public Boolean getSelectedForHomologation() {
        return selectedForHomologation;
    }

    public Boolean isSelectedForHomologation() {
        return getSelectedForHomologation();
    }

    public void setSelectedForHomologation(Boolean selectedForHomologation) {
        this.selectedForHomologation = selectedForHomologation;
    }
}
