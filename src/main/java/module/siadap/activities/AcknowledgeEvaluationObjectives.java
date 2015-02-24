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

import java.util.ArrayList;

import module.organization.domain.Person;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationItem;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.UserGroup;
import org.fenixedu.messaging.domain.MessagingSystem;
import org.fenixedu.messaging.domain.Sender;
import org.fenixedu.messaging.domain.Message.MessageBuilder;
import org.joda.time.LocalDate;

/**
 * 
 * @author João Antunes
 * @author Luis Cruz
 * @author Paulo Abrantes
 * 
 */
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

            StringBuilder body =
                    new StringBuilder("O avaliado '" + activityInformation.getProcess().getSiadap().getEvaluated().getName()
                            + "' declarou que tomou conhecimento dos seus objectivos e competências.");
            body.append("\nPara mais informações consulte https://dot.ist.utl.pt\n");
            body.append("\n\n---\n");
            body.append("Esta mensagem foi enviada por meio das Aplicações Centrais do IST.\n");

            final Sender sender = MessagingSystem.getInstance().getSystemSender();
            final MessageBuilder message =
                    sender.message("SIADAP - Tomada de conhecimento de objectivos e competências", body.toString());
            message.to(UserGroup.of(evaluatorPerson.getUser()));
            message.cc(UserGroup.of(evaluatedPerson.getUser()));
            message.send();
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

                StringBuilder body =
                        new StringBuilder(
                                "O processo SIADAP do ano "
                                        + siadap.getYear()
                                        + " do avaliado '"
                                        + activityInformation.getProcess().getSiadap().getEvaluated().getName()
                                        + "' foi revertido para o estado em que necessita novamente de tomar conhecimento dos objectivos e competências");
                body.append("\nPara mais informações consulte https://dot.ist.utl.pt\n");
                body.append("\n\n---\n");
                body.append("Esta mensagem foi enviada por meio das Aplicações Centrais do IST.\n");

                final Sender sender = MessagingSystem.getInstance().getSystemSender();
                final MessageBuilder message =
                        sender.message("SIADAP - " + siadap.getYear()
                                + " - Processo revertido para o estado anterior ao de tomar conhecimento dos obj./competências",
                                body.toString());
                message.to(UserGroup.of(evaluatedPerson.getUser()));
                message.cc(UserGroup.of(evaluatorPerson.getUser()));
                message.send();
            } catch (final Throwable ex) {
                System.out.println("Unable to lookup email address for: "
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
