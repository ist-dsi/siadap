/*
 * @(#)AcknowledgeEvaluationObjectives.java
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

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.template.DeclareMessageTemplate;
import org.fenixedu.messaging.core.template.TemplateParameter;
import org.joda.time.LocalDate;

import module.organization.domain.Person;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationItem;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

/**
 *
 * @author João Antunes
 * @author Luis Cruz
 * @author Paulo Abrantes
 *
 */
@DeclareMessageTemplate(id = "siadap.acknowledge", bundle = Siadap.SIADAP_BUNDLE_STRING,
        description = "template.siadap.acknowledge", subject = "template.siadap.acknowledge.subject",
        text = "template.siadap.acknowledge.text",
        parameters = { @TemplateParameter(id = "applicationUrl", description = "template.parameter.application.url"),
                @TemplateParameter(id = "evaluee", description = "template.parameter.evaluee") })
@DeclareMessageTemplate(id = "siadap.acknowledge.revert", bundle = Siadap.SIADAP_BUNDLE_STRING,
        description = "template.siadap.acknowledge.revert", subject = "template.siadap.acknowledge.revert.subject",
        text = "template.siadap.acknowledge.revert.text",
        parameters = { @TemplateParameter(id = "applicationUrl", description = "template.parameter.application.url"),
                @TemplateParameter(id = "evaluee", description = "template.parameter.evaluee"),
                @TemplateParameter(id = "year", description = "template.parameter.year") })
public class AcknowledgeEvaluationObjectives extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
        if (!process.isActive()) {
            return false;
        }
        Siadap siadap = process.getSiadap();
        return siadap.getEvaluated().getUser() == user && !siadap.isEvaluatedWithKnowledgeOfObjectives()
                && siadap.getRequestedAcknowledgeDate() != null;
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
        SiadapProcess siadapProcess = activityInformation.getProcess();
        Siadap siadap = siadapProcess.getSiadap();
        siadap.setAcknowledgeDate(new LocalDate());
        Person evaluatorPerson = null;
        Person evaluatedPerson = null;
        try {
            evaluatedPerson = activityInformation.getProcess().getSiadap().getEvaluated();
            siadapProcess.checkEmailExistenceImportAndWarnOnError(evaluatedPerson);
        } catch (Throwable ex) {
            if (evaluatorPerson != null) {
                System.out.println("Could not get e-mail for evaluator " + evaluatorPerson.getName());
            } else {
                System.out.println("Could not get e-mail for evaluator which has no Person object associated!");
            }
        }

        try {
            evaluatorPerson = activityInformation.getProcess().getSiadap().getEvaluator().getPerson();
            siadapProcess.checkEmailExistenceImportAndWarnOnError(evaluatorPerson);
            Message.fromSystem().to(Group.users(evaluatorPerson.getUser())).cc(Group.users(evaluatedPerson.getUser()))
                    .template("siadap.acknowledge")
                    .parameter("applicationUrl", CoreConfiguration.getConfiguration().applicationUrl())
                    .parameter("evaluee", evaluatedPerson.getName()).and().send();
        } catch (final Throwable ex) {
            System.out.println("Unable to lookup email address for: "
                    + activityInformation.getProcess().getSiadap().getEvaluated().getUser().getUsername() + " Error: "
                    + ex.getMessage());
            siadapProcess.addWarningMessage("warning.message.could.not.send.email.now");
        }
    }

    static protected void revertProcess(RevertStateActivityInformation activityInformation, boolean notifyIntervenients) {
        SiadapProcess siadapProcess = activityInformation.getProcess();
        Siadap siadap = siadapProcess.getSiadap();
        siadap.setAcknowledgeDate(null);
        siadap.setAutoEvaluationSealedDate(null);
        // also do revert the acknowledgeDate on the individual items
        if (siadap.getCurrentEvaluationItems() == null || siadap.getCurrentEvaluationItems().isEmpty()) {
            return;
        }
        for (SiadapEvaluationItem item : siadap.getCurrentEvaluationItems()) {
            item.setAcknowledgeDate(null);
        }

        if (notifyIntervenients) {

            Person evaluatorPerson = null;
            Person evaluatedPerson = null;
            try {

                evaluatedPerson = activityInformation.getProcess().getSiadap().getEvaluated();
                siadapProcess.checkEmailExistenceImportAndWarnOnError(evaluatedPerson);

            } catch (Throwable ex) {
                if (evaluatorPerson != null) {
                    System.out.println("Could not get e-mail for evaluator " + evaluatorPerson.getName());
                } else {
                    System.out.println("Could not get e-mail for evaluator which has no Person object associated!");
                }
            }

            try {
                evaluatorPerson = activityInformation.getProcess().getSiadap().getEvaluator().getPerson();
                siadapProcess.checkEmailExistenceImportAndWarnOnError(evaluatorPerson);
                Message.fromSystem().to(Group.users(evaluatedPerson.getUser())).cc(Group.users(evaluatorPerson.getUser()))
                        .template("siadap.acknowledge.revert")
                        .parameter("applicationUrl", CoreConfiguration.getConfiguration().applicationUrl())
                        .parameter("year", siadap.getYear()).parameter("evaluee", evaluatedPerson.getName()).and().send();
            } catch (final Throwable ex) {
                System.out.println("Unable to send email to: "
                        + activityInformation.getProcess().getSiadap().getEvaluated().getUser().getUsername() + " Error: "
                        + ex.getMessage());
                siadapProcess.addWarningMessage("warning.message.could.not.send.email.now");
            }
        }

    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
        return true;
    }

    @Override
    public String getUsedBundle() {
        return "resources/SiadapResources";
    }
}
