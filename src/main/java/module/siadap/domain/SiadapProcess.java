/*
 * @(#)SiadapProcess.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.MissingResourceException;

import module.organization.domain.Person;
import module.siadap.activities.AcknowledgeEvaluationObjectives;
import module.siadap.activities.AcknowledgeEvaluationValidation;
import module.siadap.activities.AcknowledgeHomologation;
import module.siadap.activities.AutoEvaluation;
import module.siadap.activities.ChangeCustomSchedule;
import module.siadap.activities.ChangeGradeAnytimeAfterValidationByCCA;
import module.siadap.activities.ChangePersonnelSituation;
import module.siadap.activities.CreateCompetenceEvaluation;
import module.siadap.activities.CreateObjectiveEvaluation;
import module.siadap.activities.CurricularPonderationAttribution;
import module.siadap.activities.EditCompetenceEvaluation;
import module.siadap.activities.EditObjectiveEvaluation;
import module.siadap.activities.Evaluation;
import module.siadap.activities.ForceEditCompetenceSlashCareerEvaluationByCCA;
import module.siadap.activities.ForceReadinessToHomologate;
import module.siadap.activities.Homologate;
import module.siadap.activities.NoEvaluation;
import module.siadap.activities.NullifyProcess;
import module.siadap.activities.RectifyNullifiedProcess;
import module.siadap.activities.RemoveCustomSchedule;
import module.siadap.activities.RemoveObjectiveEvaluation;
import module.siadap.activities.RevertNoEvaluation;
import module.siadap.activities.RevertState;
import module.siadap.activities.RevertStateActivityInformation;
import module.siadap.activities.SealObjectivesAndCompetences;
import module.siadap.activities.SendToReviewCommission;
import module.siadap.activities.SubmitAutoEvaluation;
import module.siadap.activities.SubmitEvaluation;
import module.siadap.activities.SubmitForObjectivesAcknowledge;
import module.siadap.activities.SubmitValidatedEvaluation;
import module.siadap.activities.ValidateEvaluation;
import module.siadap.activities.Validation;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.LabelLog;
import module.workflow.domain.ProcessFile;
import module.workflow.domain.WorkflowLog;
import module.workflow.domain.WorkflowProcess;
import module.workflow.domain.WorkflowSystem;
import module.workflow.util.ClassNameBundle;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.DynamicGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.groups.UserGroup;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.messaging.domain.MessagingSystem;
import org.fenixedu.messaging.domain.Sender;
import org.fenixedu.messaging.domain.Message.MessageBuilder;
import org.joda.time.LocalDate;

import pt.ist.fenixframework.Atomic;

/**
 * 
 * @author João Antunes
 * @author Luis Cruz
 * @author Paulo Abrantes
 * 
 */
@ClassNameBundle(bundle = "SiadapResources")
public class SiadapProcess extends SiadapProcess_Base {

    private static List<WorkflowActivity<SiadapProcess, ? extends ActivityInformation<SiadapProcess>>> activities =
            new ArrayList<WorkflowActivity<SiadapProcess, ? extends ActivityInformation<SiadapProcess>>>();

    static {
        activities.add(new NullifyProcess());
        activities.add(new RectifyNullifiedProcess());

        activities.add(new ChangeCustomSchedule());
        activities.add(new RevertState());
        activities.add(new RemoveCustomSchedule());
        activities.add(new CreateObjectiveEvaluation());
        activities.add(new RemoveObjectiveEvaluation());
        activities.add(new CreateCompetenceEvaluation());
        activities.add(new EditCompetenceEvaluation());
        activities.add(new SealObjectivesAndCompetences());
        activities.add(new AcknowledgeEvaluationObjectives());
        activities.add(new AutoEvaluation());
        activities.add(new SubmitAutoEvaluation());
        activities.add(new Evaluation());
        activities.add(new SubmitEvaluation());
        activities.add(new CurricularPonderationAttribution());
        activities.add(new ValidateEvaluation());
        activities.add(new ForceReadinessToHomologate());
        activities.add(new SubmitValidatedEvaluation());
        activities.add(new AcknowledgeEvaluationValidation());
        activities.add(new Homologate());
        activities.add(new AcknowledgeHomologation());
        activities.add(new EditObjectiveEvaluation());
        activities.add(new SubmitForObjectivesAcknowledge());
        // activities.add(new NotValidateEvaluation());
        activities.add(new NoEvaluation());
        activities.add(new RevertNoEvaluation());
        // activities.add(new GrantExcellencyAward());
        // activities.add(new RevokeExcellencyAward());

        activities.add(new ForceEditCompetenceSlashCareerEvaluationByCCA());

        activities.add(new Validation());

        // SiadapPersonnelManagement activities:
        activities.add(new ChangePersonnelSituation());

        activities.add(new SendToReviewCommission());

        activities.add(new ChangeGradeAnytimeAfterValidationByCCA());

    }

    private HashMap<User, ArrayList<String>> userWarningsKey = new HashMap<User, ArrayList<String>>();

    public SiadapProcess(Integer year, Person evaluated, SiadapUniverse siadapUniverse, CompetenceType competenceType,
            boolean skipUniverseCheck) {
        super();

        if (competenceType == null || siadapUniverse == null) {
            throw new SiadapException("error.create.siadap.must.fill.competenceType.and.SiadapUniverse");
        }

        User currentUser = Authenticate.getUser();
        Person possibleEvaluator = currentUser.getPerson();
        PersonSiadapWrapper evaluator = new PersonSiadapWrapper(evaluated, year).getEvaluator();
        SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
        if (skipUniverseCheck == false) {
            if (configuration.isOnlyAllowedToCreateSIADAP3() && siadapUniverse.equals(SiadapUniverse.SIADAP2)) {
                throw new SiadapException("Unable to create a SIADAP2 proccess due to the configuration");
            }
        }

        boolean belongsToASuperGroup = false;
        if ((configuration.getCcaMembers() != null && configuration.getCcaMembers().contains(currentUser.getPerson()))
                || (configuration.getScheduleEditors() != null && configuration.getScheduleEditors().contains(
                        currentUser.getPerson())) || DynamicGroup.get("managers").isMember(currentUser)
                || configuration.getStructureManagementGroupMembers().contains(currentUser.getPerson())) {
            belongsToASuperGroup = true;
        }
        if (!belongsToASuperGroup) {
            if (evaluator == null || evaluator.getPerson() != possibleEvaluator) {
                throw new SiadapException("error.onlyEvaluatorCanCreateSiadap");
            }
        }

        setWorkflowSystem(WorkflowSystem.getInstance());
        setSiadap(new Siadap(year, evaluated, siadapUniverse, competenceType));
        setProcessNumber("S" + configuration.getLabel() + "/" + evaluated.getUser().getUsername());

        //let us put in order the harmonization relations
        PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(getSiadap());
        personSiadapWrapper.correctHarmonizationRelationsForRecentlyCreatedProcess();

        new LabelLog(this, currentUser, this.getClass().getName() + ".creation", "resources/SiadapResources",
                evaluated.getName(), year.toString(), siadapUniverse.getLocalizedName(), competenceType.getName());
    }

    public List<String> getAndClearWarningMessages() {
        List<String> warningMessages = getWarningMessages();
        clearWarningMessagesForCurrentUser();
        return warningMessages;
    }

    @Override
    public boolean isCreatedByAvailable() {
        return false;
    }

    public List<String> getWarningMessages() {
        User currentUser = Authenticate.getUser();
        ArrayList<String> warningMessagesToReturn = new ArrayList<String>();
        // for each let's try to translate it using the resources, case it can't
        // be found we print it as it is
        ArrayList<String> warningMessages = getUserWarningsKey().get(currentUser);
        if (warningMessages == null) {
            warningMessages = new ArrayList<String>();
            getUserWarningsKey().put(currentUser, warningMessages);
        }
        for (String string : warningMessages) {
            try {
                warningMessagesToReturn.add(BundleUtil.getString("resources/SiadapResources", string));
            } catch (MissingResourceException e) {
                warningMessagesToReturn.add(string);
            }
        }

        return warningMessagesToReturn;
    }

    public void addWarningMessage(String messageKeyOrContent) {
        User currentUser = Authenticate.getUser();
        addWarningMessage(currentUser, messageKeyOrContent);
    }

    protected void addWarningMessage(User user, String messageKeyOrContent) {
        ArrayList<String> warningMessages = getUserWarningsKey().get(user);
        if (warningMessages == null) {
            warningMessages = new ArrayList<String>();
        }
        warningMessages.add(messageKeyOrContent);
        getUserWarningsKey().put(user, warningMessages);
    }

    protected void clearWarningMessagesForCurrentUser() {
        User currentUser = Authenticate.getUser();
        clearWarningMessages(currentUser);
    }

    protected void clearWarningMessages(User user) {
        getUserWarningsKey().put(user, new ArrayList<String>());
    }

    @Override
    public <T extends WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>> List<T> getActivities() {
        return (List<T>) activities;
    }

    public static WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation<?>> getActivityStaticly(
            String activityName) {
        for (WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation<?>> activity : activities) {
            if (activity.getName().equals(activityName)) {
                return activity;
            }
        }
        return null;
    }

    @Override
    public User getProcessCreator() {
        return getSiadap().getEvaluator().getPerson().getUser();
    }

    @Override
    public boolean isActive() {
        if (getSiadap().getState().equals(SiadapProcessStateEnum.NULLED)) {
            return false;
        }
        return true;
    }

    @Override
    public void notifyUserDueToComment(User user, String comment) {
        final User loggedUser = Authenticate.getUser();
        final Sender sender = MessagingSystem.getInstance().getSystemSender();
        final Group ug = UserGroup.of(user);
        final MessageBuilder message = sender.message(BundleUtil.getString("resources/SiadapResources", "label.email.commentCreated.subject",
                getProcessNumber()), BundleUtil.getString("resources/SiadapResources",
                "label.email.commentCreated.body", loggedUser.getPerson().getName(), getProcessNumber(), comment,
                CoreConfiguration.getConfiguration().applicationUrl()));
        message.to(ug);
        message.send();
    }

    @Atomic
    public static SiadapProcess createNewProcess(Person evaluated, Integer year, SiadapUniverse siadapUniverse,
            CompetenceType competenceType, boolean skipUniverseCheck) throws SiadapException {
        return new SiadapProcess(year, evaluated, siadapUniverse, competenceType, skipUniverseCheck);
    }

    @Override
    public boolean isAccessible(User user) {
        Person person = user.getPerson();
        SiadapYearConfiguration configuration = getSiadap().getSiadapYearConfiguration();
        return person == getSiadap().getEvaluated() || person == getSiadap().getEvaluator().getPerson()
                || isResponsibleForHarmonization(person, getSiadap().getEvaluated()) || configuration.isPersonMemberOfCCA(person)
                || configuration.isPersonResponsibleForHomologation(person)
                || configuration.isPersonMemberOfScheduleExtenders(person);
    }

    private boolean isResponsibleForHarmonization(Person accessor, Person evaluated) {
        PersonSiadapWrapper wrapper = new PersonSiadapWrapper(evaluated, getSiadap().getYear());
        return wrapper.isResponsibleForHarmonization(accessor);
    }

    public void markAsHarmonized(SiadapEvaluationUniverse evaluationUniverse) {
        String siadapUniverseLocalizedName = evaluationUniverse.getSiadapUniverse().getLocalizedName();
        if (evaluationUniverse.isCurriculumPonderation()) {
            siadapUniverseLocalizedName +=
                    " (" + BundleUtil.getString(Siadap.SIADAP_BUNDLE_STRING, "label.curricularPonderation")
                            + " )";
        }
        new LabelLog(this, Authenticate.getUser(), "label.terminateHarmonization.for", "resources/SiadapResources",
                siadapUniverseLocalizedName);
    }

    protected void markAsHarmonizationAssessmentGiven(SiadapEvaluationUniverse evaluationUniverse) {
        String siadapUniverseLocalizedName = evaluationUniverse.getSiadapUniverse().getLocalizedName();
        if (evaluationUniverse.isCurriculumPonderation()) {
            siadapUniverseLocalizedName +=
                    " (" + BundleUtil.getString(Siadap.SIADAP_BUNDLE_STRING, "label.curricularPonderation")
                            + " )";
        }
        new LabelLog(this, Authenticate.getUser(), "label.givenHarmonizationAssessment.for", "resources/SiadapResources",
                siadapUniverseLocalizedName);
    }

    public void removeHarmonizationMark(SiadapEvaluationUniverse evaluationUniverse) {
        String siadapUniverseLocalizedName = evaluationUniverse.getSiadapUniverse().getLocalizedName();
        if (evaluationUniverse.isCurriculumPonderation()) {
            siadapUniverseLocalizedName +=
                    " (" + BundleUtil.getString(Siadap.SIADAP_BUNDLE_STRING, "label.curricularPonderation")
                            + " )";
        }
        new LabelLog(this, Authenticate.getUser(), "label.reOpenHarmonization.for", "resources/SiadapResources",
                siadapUniverseLocalizedName);
    }

    public boolean isUserEvaluated(User user) {
        return getSiadap().getEvaluated().getUser() == user;
    }

    public boolean isProcessSealed() {
        return getSiadap().getObjectivesAndCompetencesSealedDate() != null;
    }

    public boolean isCurrentUserEvaluated() {
        return isUserEvaluated(Authenticate.getUser());
    }

    public static void checkEmailExistenceImportAndWarnOnError(Person person) {
        // if we have no info about the person, let's import it
        String emailToUse = person.getUser().getEmail();
        // if that didn't solved it, let's warn the admin by e-mail
        if (emailToUse == null) {
            StringBuilder message =
                    new StringBuilder("Error, could not import e-mail/info for person " + person.getName() + "\n");
            if (person.getUser() != null && person.getUser().getUsername() != null) {
                message.append("the username is: " + person.getUser().getUsername() + "\n");

            }
            message.append("Please take appropriate actions\n");
        }
    }

    @Override
    public boolean isTicketSupportAvailable() {
        return false;
    }

    public boolean isNotSubmittedForConfirmation() {
        return SiadapProcessStateEnum.isNotSubmittedForConfirmation(getSiadap());
    }

    public HashMap<User, ArrayList<String>> getUserWarningsKey() {
        if (userWarningsKey == null) {
            userWarningsKey = new HashMap<User, ArrayList<String>>();
        }
        return userWarningsKey;
    }

    @Override
    public List<Class<? extends ProcessFile>> getDisplayableFileTypes() {
        List<Class<? extends ProcessFile>> availableClasses = new ArrayList<Class<? extends ProcessFile>>();
        availableClasses.add(ProcessFile.class);
        availableClasses.add(HomologationDocumentFile.class);
        return availableClasses;
    }

    /**
     * Changes the custom deadlines as specified by the parameter
     * processSchedulesEnum which must be one of {@link SiadapProcessSchedulesEnum}
     * 
     * @param processSchedulesEnum
     *            the representation of which schedule {@link SiadapProcessSchedulesEnum}
     * @param newDate
     *            the new date to use for this schedule
     */
    @Atomic
    public void changeCustomSiadapSchedule(SiadapProcessSchedulesEnum processSchedulesEnum, LocalDate newDate) {
        Siadap siadap = getSiadap();
        switch (processSchedulesEnum) {
        case OBJECTIVES_SPECIFICATION_BEGIN_DATE:
            siadap.setCustomObjectiveSpecificationBegin(newDate);
            break;
        case OBJECTIVES_SPECIFICATION_END_DATE:
            siadap.setCustomObjectiveSpecificationEnd(newDate);
            break;
        case AUTOEVALUATION_BEGIN_DATE:
            siadap.setCustomAutoEvaluationBegin(newDate);
            break;
        case AUTOEVALUATION_END_DATE:
            siadap.setCustomAutoEvaluationEnd(newDate);
            break;
        case EVALUATION_BEGIN_DATE:
            siadap.setCustomEvaluationBegin(newDate);
            break;
        case EVALUATION_END_DATE:
            siadap.setCustomEvaluationEnd(newDate);
            break;
        default:
            throw new SiadapException("error.unknown.type");
        }

    }

    /**
     * Method that is called whenever there are changes to the evaluation
     * objectives (either the objectives or the competences got deleted/edited)
     * in which case, this method triggers the side effects of doing so. At the
     * moment it will only revert the status from submitted to the evaluatee to
     * not submitted and possibly warn the user about it
     */
    public void signalChangesInEvaluationObjectives() {
        int stateOrdinal = SiadapProcessStateEnum.getState(getSiadap()).ordinal();
        if (stateOrdinal == SiadapProcessStateEnum.WAITING_EVAL_OBJ_ACK.ordinal()
                || stateOrdinal == SiadapProcessStateEnum.WAITING_SELF_EVALUATION.ordinal()) {
            RevertState revertState = null;
            for (WorkflowActivity activity : activities) {
                if (RevertState.class.isAssignableFrom(activity.getClass())) {
                    revertState = (RevertState) activity;
                    break;
                }
            }
            if (revertState == null) {
                revertState = new RevertState();
            }
            revertState.setSideEffect(true);
            RevertStateActivityInformation activityInformation =
                    (RevertStateActivityInformation) revertState.getActivityInformation(this);
            activityInformation.setSiadapProcessStateEnum(SiadapProcessStateEnum.NOT_YET_SUBMITTED_FOR_ACK);
            activityInformation.setJustification("Edição/remoção de objectivos/competências");
            revertState.execute(activityInformation);
            return;
        }
    }

    public boolean isSubmittedForEvalObjsConfirmation() {
        return getSiadap().getRequestedAcknowledgeDate() != null;
    }

    public boolean isEvalObjectivesAcknowledged() {
        return getSiadap().isEvaluatedWithKnowledgeOfObjectives();
    }

    public void removeHarmonizationAssessments(SiadapEvaluationUniverse siadapEvaluationUniverse) {
        String siadapUniverseLocalizedName = siadapEvaluationUniverse.getSiadapUniverse().getLocalizedName();
        if (siadapEvaluationUniverse.isCurriculumPonderation()) {
            siadapUniverseLocalizedName +=
                    " (" + BundleUtil.getString(Siadap.SIADAP_BUNDLE_STRING, "label.curricularPonderation")
                            + " )";
        }
        new LabelLog(this, Authenticate.getUser(), "label.removedHarmonizationAssessment.for", "resources/SiadapResources",
                siadapUniverseLocalizedName);

    }

    /*
     * Deletes the proccess if it has no relevant info on it (more than one
     * logged activity, any comment, etc)
     */
    protected void delete(boolean neglectLogSize) {
        releaseProcess();
        Collection<WorkflowLog> executionLogs = getExecutionLogs();
        if (!neglectLogSize && executionLogs.size() > 1) {
            throw new SiadapException("error.has.items.in.it");
        }
        for (WorkflowLog exLog : executionLogs) {
            removeExecutionLogs(exLog);
        }

        for (WorkflowLog workflowLog : getExecutionLogs()) {
            workflowLog.setProcess(null);
            workflowLog.delete();
        }
        setSiadap(null);
        setWorkflowSystem(null);
        deleteDomainObject();

    }

}
