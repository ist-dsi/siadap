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

import java.util.ArrayList;
import java.util.Collections;

import module.organization.domain.Person;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

import org.joda.time.LocalDate;

import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.VirtualHost;
import pt.ist.emailNotifier.domain.Email;

/**
 * 
 * @author João Antunes
 * 
 */
public class SubmitValidatedEvaluation extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

	@Override
	public boolean isActive(SiadapProcess process, User user) {
		Siadap siadap = process.getSiadap();
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

		ArrayList<String> toAddress = new ArrayList<String>();
		ArrayList<String> ccAddress = new ArrayList<String>();
		String emailEvaluated = null;
		Person evaluatedPerson = process.getSiadap().getEvaluated();

		try {
			SiadapProcess.checkEmailExistenceImportAndWarnOnError(evaluatedPerson);
			emailEvaluated = Siadap.getRemoteEmail(evaluatedPerson);

			if (emailEvaluated != null) {
				toAddress.add(emailEvaluated);
				Integer year = process.getSiadap().getYear();

				StringBuilder body =
						new StringBuilder("A nota final (pós-validação) do seu processo SIADAP de " + year
								+ " encontra-se disponível na plataforma. Necessita de tomar conhecimento da validação\n");
				body.append("\nPara mais informações consulte https://dot.ist.utl.pt\n");
				body.append("\n\n---\n");
				body.append("Esta mensagem foi enviada por meio das Aplicações Centrais do IST.\n");

				final VirtualHost virtualHost = VirtualHost.getVirtualHostForThread();
				new Email(virtualHost.getApplicationSubTitle().getContent(), virtualHost.getSystemEmailAddress(),
						new String[] {}, toAddress, ccAddress, Collections.EMPTY_LIST, "SIADAP - " + year
								+ " Nota final disponível", body.toString());
			}
		} catch (Throwable ex) {
			System.out.println("Unable to lookup email address for: " + evaluatedPerson.getUser().getUsername() + " Error: "
					+ ex.getMessage());
			process.addWarningMessage("warning.message.could.not.send.email.now");
		}
	}

}
