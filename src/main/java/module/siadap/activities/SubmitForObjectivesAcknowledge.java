/*
 * @(#)SubmitForObjectivesAcknowledge.java
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
package module.siadap.activities;

import module.organization.domain.Person;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.exceptions.SiadapException;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.UserGroup;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.messaging.domain.Message;
import org.fenixedu.messaging.template.DeclareMessageTemplate;
import org.fenixedu.messaging.template.TemplateParameter;
import org.joda.time.LocalDate;

/**
 *
 * @author João Antunes
 * @author Luis Cruz
 * @author Paulo Abrantes
 *
 */
@DeclareMessageTemplate(id = "siadap.submit", bundle = Siadap.SIADAP_BUNDLE_STRING, description = "template.siadap.submit",
        subject = "template.siadap.submit.subject", text = "template.siadap.submit.text", parameters = {
                @TemplateParameter(id = "applicationUrl", description = "template.parameter.application.url"),
                @TemplateParameter(id = "year", description = "template.parameter.year") })
@DeclareMessageTemplate(id = "siadap.submit.revert", bundle = Siadap.SIADAP_BUNDLE_STRING,
        description = "template.siadap.submit.revert", subject = "template.siadap.submit.revert.subject",
        text = "template.siadap.submit.revert.text", parameters = {
                @TemplateParameter(id = "year", description = "template.parameter.year"),
                @TemplateParameter(id = "reason", description = "template.parameter.revert.reason"),
                @TemplateParameter(id = "applicationUrl", description = "template.parameter.application.url") })
public class SubmitForObjectivesAcknowledge extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
        Siadap siadap = process.getSiadap();
        if (siadap.getEvaluator() == null) {
            return false;
        }
        return user == siadap.getEvaluator().getPerson().getUser() && siadap.isWithObjectivesFilled()
                && siadap.isCoherentOnTypeOfEvaluation() && siadap.hasAllEvaluationItemsValid()
                && siadap.getRequestedAcknowledgeDate() == null && siadap.getObjectivesAndCompetencesSealedDate() != null;
    }

    static protected void revertProcess(RevertStateActivityInformation activityInformation, boolean notifyIntervenients) {
        activityInformation.getProcess().getSiadap().setRequestedAcknowledgeDate(null);
        activityInformation.getProcess().getSiadap().setAutoEvaluationSealedDate(null);
        if (notifyIntervenients) {

            SiadapProcess siadapProcess = activityInformation.getProcess();
            Siadap siadap = siadapProcess.getSiadap();
            Person evaluatorPerson = null;
            try {
                evaluatorPerson = activityInformation.getProcess().getSiadap().getEvaluator().getPerson();
                siadapProcess.checkEmailExistenceImportAndWarnOnError(evaluatorPerson);
            } catch (Throwable ex) {
                if (evaluatorPerson != null) {
                    System.out.println("Could not get e-mail for evaluator " + evaluatorPerson.getName());
                } else {
                    System.out.println("Could not get e-mail for evaluator which has no Person object associated!");
                }
            }

            try {
                final Person evaluatedPerson = activityInformation.getProcess().getSiadap().getEvaluated();
                siadapProcess.checkEmailExistenceImportAndWarnOnError(evaluatedPerson);
                Message.fromSystem().to(UserGroup.of(evaluatedPerson.getUser())).cc(UserGroup.of(evaluatorPerson.getUser()))
                        .template("siadap.submit.revert")
                        .parameter("applicationUrl", CoreConfiguration.getConfiguration().applicationUrl())
                        .parameter("year", siadap.getYear()).parameter("reason", activityInformation.getJustification()).and()
                        .send();
            } catch (Throwable ex) {
                System.out.println("Unable to send email to: "
                        + activityInformation.getProcess().getSiadap().getEvaluated().getUser().getUsername() + " Error: "
                        + ex.getMessage());
                siadapProcess.addWarningMessage("warning.message.could.not.send.email.now");
            }
        }

    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
        if (!activityInformation.getProcess().getSiadap().hasAllEvaluationItemsValid()) {
            throw new SiadapException("activity.SealObjectivesAndCompetences.invalid.objectives");
        }
        activityInformation.getProcess().getSiadap().setRequestedAcknowledgeDate(new LocalDate());
        SiadapProcess siadapProcess = activityInformation.getProcess();
        Siadap siadap = siadapProcess.getSiadap();
        Person evaluatorPerson = null;
        try {
            evaluatorPerson = activityInformation.getProcess().getSiadap().getEvaluator().getPerson();
            siadapProcess.checkEmailExistenceImportAndWarnOnError(evaluatorPerson);

        } catch (Throwable ex) {
            if (evaluatorPerson != null) {
                System.out.println("Could not get e-mail for evaluator " + evaluatorPerson.getName());
            } else {
                System.out.println("Could not get e-mail for evaluator which has no Person object associated!");
            }
        }

        try {
            final Person evaluatedPerson = activityInformation.getProcess().getSiadap().getEvaluated();
            siadapProcess.checkEmailExistenceImportAndWarnOnError(evaluatedPerson);
            Message.fromSystem().to(UserGroup.of(evaluatedPerson.getUser())).cc(UserGroup.of(evaluatorPerson.getUser()))
                    .template("siadap.submit").parameter("applicationUrl", CoreConfiguration.getConfiguration().applicationUrl())
                    .parameter("year", siadap.getYear()).and().send();
        } catch (Throwable ex) {
            System.out.println("Unable to lookup email address for: "
                    + activityInformation.getProcess().getSiadap().getEvaluated().getUser().getUsername() + " Error: "
                    + ex.getMessage());
            siadapProcess.addWarningMessage("warning.message.could.not.send.email.now");
        }

    }

    @Override
    public String getUsedBundle() {
        return "resources/SiadapResources";
    }
}
