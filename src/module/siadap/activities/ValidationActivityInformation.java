/**
 * 
 */
package module.siadap.activities;

import java.math.BigDecimal;

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
import myorg.util.BundleUtil;

import org.joda.time.LocalDate;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 17 de Fev de 2012
 * 
 *         ActivityInformation for all validation related activities.
 * 
 */
public class ValidationActivityInformation extends ActivityInformation<SiadapProcess> {

    public static enum ValidationSubActivity {
	SET_VALIDATION_DATA {
	    @Override
	    public void process(PersonSiadapWrapper personWrapper, SiadapUniverse siadapUniverse) {
		//let's apply everything
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

		//now let's check the values:
		validationGrade = checkValidationDataAndAssignGrade(validationAssessment, validationExcellencyAssessment,
			validationGrade, harmonizationClassification);

		//let's apply them
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
		    //		    if (validationGrade != null)
			//something went wrong in the interface, or someone fiddled with this!!
			//TODO as soon as JS is implemented to always give null here, remove this comment
			//			throw new SiadapException("error.validation.grade.inconsistent");
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
		//if there is something different for the given universe, we will set it and thus return true
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
		//Throw an exception if we are missing data

		if (!personWrapper.getSiadap().hasCompleteValidationAssessment(siadapUniverse))
		    throw new SiadapException("error.validation.can.not.close.without.validating.everybody");

		personWrapper.getSiadap().getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverse)
			.setValidationDate(new LocalDate());
	    }

	    @Override
	    public boolean hasAllneededInfo(PersonSiadapWrapper personWrapper, SiadapUniverse universe) {
		//let's always return true, and later throw an exception if we don't have all of the needed data
		return true;
	    }
	};

	public abstract void process(PersonSiadapWrapper personWrapper, SiadapUniverse siadapUniverse);

	public String[] getArgumentsDescription(ValidationActivityInformation activityInformation) {
	    return new String[] { BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING, name(),
		    activityInformation.getSiadapUniverse().name()) };
	}

	public abstract boolean hasAllneededInfo(PersonSiadapWrapper personWrapper, SiadapUniverse universe);
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
