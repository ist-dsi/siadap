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
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.messaging.domain.Message;
import org.fenixedu.messaging.template.DeclareMessageTemplate;
import org.fenixedu.messaging.template.TemplateParameter;
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
@DeclareMessageTemplate(id = "siadap.evaluation", bundle = Siadap.SIADAP_BUNDLE_STRING,
        description = "template.siadap.evaluation", subject = "template.siadap.evaluation.subject",
        text = "template.siadap.evaluation.text", parameters = {
                @TemplateParameter(id = "applicationUrl", description = "template.parameter.application.url"),
                @TemplateParameter(id = "year", description = "template.parameter.year") })
public class SubmitValidatedEvaluation extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
        Siadap siadap = process.getSiadap();
        if (siadap.getEvaluator() == null) {
            return false;
        }
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
            Message.fromSystem().to(Group.users(evaluatedPerson.getUser())).template("siadap.evaluation")
                    .parameter("applicationUrl", CoreConfiguration.getConfiguration().applicationUrl())
                    .parameter("year", process.getSiadap().getYear()).and().send();
        } catch (Throwable ex) {
            System.out.println("Unable to send email to: " + evaluatedPerson.getUser().getUsername() + " Error: "
                    + ex.getMessage());
            process.addWarningMessage("warning.message.could.not.send.email.now");
        }
    }
}
