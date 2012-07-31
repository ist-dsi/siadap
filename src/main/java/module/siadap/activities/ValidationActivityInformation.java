/*
 * @(#)ValidationActivityInformation.java
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import pt.ist.bennu.core.domain.VirtualHost;
import pt.ist.bennu.core.util.BundleUtil;
import pt.ist.emailNotifier.domain.Email;

import module.organization.domain.Person;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;

/**
 * 
 * ActivityInformation for all validation related activities.
 * 
 * @author João Antunes
 * 
 */
public class ValidationActivityInformation extends ActivityInformation<SiadapProcess> {

    public static enum ValidationSubActivity {
	SET_VALIDATION_DATA {
	    @Override
	    public void process(PersonSiadapWrapper personWrapper, SiadapUniverse siadapUniverse) {
		// let's apply everything
		SiadapEvaluationUniverse siadapEvaluationUniverseForSiadapUniverse = personWrapper.getSiadap()
			.getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse);
		Boolean validationAssessment = null;
		Boolean validationExcellencyAssessment = null;
		BigDecimal validationGrade = null;
		BigDecimal harmonizationClassification = siadapEvaluationUniverseForSiadapUniverse
			.getHarmonizationClassification();
		switch (siadapUniverse) {
		case SIADAP2:
		    validationAssessment = personWrapper.getValidationCurrentAssessmentForSIADAP2();
		    validationExcellencyAssessment = personWrapper.getValidationCurrentAssessmentForExcellencyAwardForSIADAP2();
		    validationGrade = personWrapper.getValidationClassificationForSIADAP2();
		    break;
		case SIADAP3:
		    validationAssessment = personWrapper.getValidationCurrentAssessmentForSIADAP3();
		    validationExcellencyAssessment = personWrapper.getValidationCurrentAssessmentForExcellencyAwardForSIADAP3();
		    validationGrade = personWrapper.getValidationClassificationForSIADAP3();
		    break;
		}

		// now let's check the values:
		validationGrade = checkValidationDataAndAssignGrade(validationAssessment, validationExcellencyAssessment,
			validationGrade, harmonizationClassification);

		// let's apply them
		siadapEvaluationUniverseForSiadapUniverse.setCcaAssessment(validationAssessment);
		siadapEvaluationUniverseForSiadapUniverse.setCcaClassificationExcellencyAward(validationExcellencyAssessment);
		siadapEvaluationUniverseForSiadapUniverse.setCcaClassification(validationGrade);

	    }

	    /**
	     * Validates the given data according to the validation rules, and
	     * sets the grade accordingly:
	     * <ul>
	     * <li>We can only have an excellent with a grade bigger than 3.999;
	     * </li>
	     * <li>If we have an Assessment of true, then the grade must be null
	     * and set to the value of harmonizationClassification;</li>
	     * <li>harmonizationClassification must not be null!!</li>
	     * 
	     * 
	     * @param validationAssessment
	     * @param validationExcellencyAssessment
	     * @param validationGrade
	     * @param harmonizationClassification
	     * @return the validation grade to use
	     * @throws SiadapException
	     *             if the data is inconsistent
	     * 
	     */
	    private BigDecimal checkValidationDataAndAssignGrade(Boolean validationAssessment,
		    Boolean validationExcellencyAssessment, BigDecimal validationGrade, BigDecimal harmonizationClassification)
		    throws SiadapException {
		if (harmonizationClassification == null)
		    throw new SiadapException("error.validation.must.have.harmonization.grade");
		BigDecimal gradeInEffect = null;
		if (validationAssessment != null && validationAssessment) {
		    // if (validationGrade != null)
		    // something went wrong in the interface, or someone fiddled
		    // with this!!
		    // TODO as soon as JS is implemented to always give null
		    // here, remove this comment
		    // throw new
		    // SiadapException("error.validation.grade.inconsistent");
		    gradeInEffect = harmonizationClassification;
		    validationGrade = gradeInEffect;
		} else {
		    gradeInEffect = validationGrade;
		}

		if (validationExcellencyAssessment != null && validationExcellencyAssessment) {
		    if (!SiadapGlobalEvaluation.EXCELLENCY.accepts(gradeInEffect, validationExcellencyAssessment))
			throw new SiadapException("error.validation.excellent.not.accepted.for.grade");
		}

		return gradeInEffect;

	    }

	    @Override
	    public boolean hasAllneededInfo(PersonSiadapWrapper personWrapper, SiadapUniverse universe) {
		// if there is something different for the given universe, we
		// will set it and thus return true
		SiadapEvaluationUniverse siadapEvaluationUniverse = personWrapper.getSiadap()
			.getSiadapEvaluationUniverseForSiadapUniverse(universe);
		switch (universe) {
		case SIADAP2:
		    return ((siadapEvaluationUniverse.getCcaAssessment() != personWrapper
			    .getValidationCurrentAssessmentForSIADAP2()) || (siadapEvaluationUniverse
			    .getCcaClassificationExcellencyAward() != personWrapper
			    .getValidationCurrentAssessmentForExcellencyAwardForSIADAP2() || ((siadapEvaluationUniverse
			    .getCcaClassification() == null && personWrapper.getValidationClassificationForSIADAP2() != null) || (siadapEvaluationUniverse
			    .getCcaClassification() != null && !siadapEvaluationUniverse.getCcaClassification().equals(
			    personWrapper.getValidationClassificationForSIADAP2())))));
		case SIADAP3:
		    return ((siadapEvaluationUniverse.getCcaAssessment() != personWrapper
			    .getValidationCurrentAssessmentForSIADAP3()) || (siadapEvaluationUniverse
			    .getCcaClassificationExcellencyAward() != personWrapper
			    .getValidationCurrentAssessmentForExcellencyAwardForSIADAP3() || ((siadapEvaluationUniverse
			    .getCcaClassification() == null && personWrapper.getValidationClassificationForSIADAP3() != null) || (siadapEvaluationUniverse
			    .getCcaClassification() != null && !siadapEvaluationUniverse.getCcaClassification().equals(
			    personWrapper.getValidationClassificationForSIADAP3())))));
		default:
		    return false;
		}
	    }
	},
	TERMINATE_VALIDATION {
	    @Override
	    public void process(PersonSiadapWrapper personWrapper, SiadapUniverse siadapUniverse) {
		// Throw an exception if we are missing data

		if (!personWrapper.getSiadap().hasCompleteValidationAssessment(siadapUniverse))
		    throw new SiadapException("error.validation.can.not.close.without.validating.everybody");

		personWrapper.getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse)
			.setValidationDate(new LocalDate());
	    }

	    @Override
	    public boolean hasAllneededInfo(PersonSiadapWrapper personWrapper, SiadapUniverse universe) {
		// let's always return true, and later throw an exception if we
		// don't have all of the needed data
		return true;
	    }

	};

	public abstract void process(PersonSiadapWrapper personWrapper, SiadapUniverse siadapUniverse);

	public String[] getArgumentsDescription(ValidationActivityInformation activityInformation) {
	    return new String[] { BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING, name(),
		    activityInformation.getSiadapUniverse().name()) };
	}

	public abstract boolean hasAllneededInfo(PersonSiadapWrapper personWrapper, SiadapUniverse universe);

	public static void notifyEvaluatorOfFinishedValidation(Person evaluator, int year) throws SiadapException {
	    try {
		ArrayList<String> toAddress = new ArrayList<String>();
		SiadapProcess.checkEmailExistenceImportAndWarnOnError(evaluator);
		String emailEvaluator = Siadap.getRemoteEmail(evaluator);
		if (StringUtils.isBlank(emailEvaluator))
		    throw new SiadapException("error.could.not.notify.given.user", evaluator.getPresentationName());
		if (!StringUtils.isBlank(emailEvaluator)) {
		    toAddress.add(emailEvaluator);

		    StringBuilder body = new StringBuilder("Os processos SIADAP do ano " + year
			    + " estão validados, com a nota final atríbuida, e prontos a submeter para conhecimento do avaliado");
		    body.append("\nPara mais informações consulte https://dot.ist.utl.pt\n");
		    body.append("\n\n---\n");
		    body.append("Esta mensagem foi enviada por meio das Aplicações Centrais do IST.\n");

		    final VirtualHost virtualHost = VirtualHost.getVirtualHostForThread();
		    new Email(virtualHost.getApplicationSubTitle().getContent(), virtualHost.getSystemEmailAddress(),
			    new String[] {}, toAddress, Collections.EMPTY_LIST, Collections.EMPTY_LIST, "SIADAP - " + year
				    + " - Avaliações validadas", body.toString());
		}
	    } catch (final Throwable ex) {
		System.out.println("Unable to lookup email address for: " + evaluator.getPresentationName() + " Error: "
			+ ex.getMessage());
		throw new SiadapException("error.could.not.notify.given.user", evaluator.getPresentationName());
	    }

	}

    }

    private final ValidationSubActivity subActivity;

    private final PersonSiadapWrapper personSiadapWrapper;

    private final SiadapUniverse siadapUniverse;

    public ValidationActivityInformation(PersonSiadapWrapper personWrapper,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity,
	    ValidationSubActivity validationSubActivity, SiadapUniverse siadapUniverse) {
	super(personWrapper.getSiadap().getProcess(), activity);
	this.subActivity = validationSubActivity;
	this.personSiadapWrapper = personWrapper;
	this.siadapUniverse = siadapUniverse;
    }

    public ValidationSubActivity getSubActivity() {
	return subActivity;
    }

    @Override
    public boolean hasAllneededInfo() {
	return getSubActivity().hasAllneededInfo(getPersonSiadapWrapper(), getSiadapUniverse());
    }

    public PersonSiadapWrapper getPersonSiadapWrapper() {
	return personSiadapWrapper;
    }

    public SiadapUniverse getSiadapUniverse() {
	return siadapUniverse;
    }

    /**
     * Default version
     */
    private static final long serialVersionUID = 1L;

}
