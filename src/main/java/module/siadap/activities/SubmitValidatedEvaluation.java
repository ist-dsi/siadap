/*
 * @(#)SubmitValidatedEvaluation.java
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
package module.siadap.activities;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.messaging.domain.Message;
import org.fenixedu.messaging.domain.Message.MessageBuilder;
import org.fenixedu.messaging.domain.MessagingSystem;
import org.fenixedu.messaging.domain.Sender;
import org.joda.time.LocalDate;

import module.organization.domain.Person;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

/**
 * 
 * @author João Antunes
 * 
 */
public class SubmitValidatedEvaluation extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
        Siadap siadap = process.getSiadap();
        if (siadap.getEvaluator() == null)
            return false;
        return siadap.getEvaluator().getPerson().getUser() == user && siadap.getValidationDateOfDefaultEvaluation() != null
                && siadap.getRequestedAcknowledegeValidationDate() == null && siadap.getAcknowledgeValidationDate() == null
                && siadap.getState().equals(SiadapProcessStateEnum.WAITING_SUBMITTAL_BY_EVALUATOR_AFTER_VALIDATION);
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
        activityInformation.getProcess().getSiadap().setRequestedAcknowledegeValidationDate(new LocalDate());

    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
        return true;
    }

    @Override
    public String getUsedBundle() {
        return Siadap.SIADAP_BUNDLE_STRING;
    }

    @Override
    protected void notifyUsers(SiadapProcess process) {

        Person evaluatedPerson = process.getSiadap().getEvaluated();

        try {
            SiadapProcess.checkEmailExistenceImportAndWarnOnError(evaluatedPerson);

            Integer year = process.getSiadap().getYear();

            StringBuilder body =
                    new StringBuilder("A nota final (pós-validação) do seu processo SIADAP de " + year
                            + " encontra-se disponível na plataforma. Necessita de tomar conhecimento da validação\n");
            body.append("\nPara mais informações consulte https://dot.ist.utl.pt\n");
            body.append("\n\n---\n");
            body.append("Esta mensagem foi enviada por meio das Aplicações Centrais do IST.\n");

            final Sender sender = MessagingSystem.systemSender();
            final MessageBuilder message = Message.from(sender);
            message.subject("SIADAP - " + year + " Nota final disponível");
            message.textBody(body.toString());
            message.to(Group.users(evaluatedPerson.getUser()));
            message.send();
        } catch (Throwable ex) {
            System.out.println("Unable to lookup email address for: " + evaluatedPerson.getUser().getUsername() + " Error: "
                    + ex.getMessage());
            process.addWarningMessage("warning.message.could.not.send.email.now");
        }
    }

}
