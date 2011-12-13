package module.siadap.domain;

import module.siadap.domain.exceptions.SiadapException;
import myorg.util.BundleUtil;

import org.apache.commons.lang.StringUtils;

public class SiadapAutoEvaluation extends SiadapAutoEvaluation_Base {

    public SiadapAutoEvaluation(Siadap siadap, SiadapEvaluationUniverse siadapEvaluationUniverse, String objectivesJustification,
	    String competencesJustification,
	    String otherFactorsJustification, String extremesJustification, String commentsAndProposals,
	    Integer factorOneClassification, Integer factorTwoClassification, Integer factorThreeClassification,
	    Integer factorFourClassification, Integer factorFiveClassification, Integer factorSixClassification) {

	setSiadapRootModule(SiadapRootModule.getInstance());
	setSiadapEvaluationUniverse(siadapEvaluationUniverse);

	setObjectivesJustification(objectivesJustification);
	setCompetencesJustification(competencesJustification);
	setOtherFactorsJustification(otherFactorsJustification);
	setExtremesJustification(extremesJustification);
	setCommentsAndProposals(commentsAndProposals);

	setFactorOneClassification(factorOneClassification);
	setFactorTwoClassification(factorTwoClassification);
	setFactorThreeClassification(factorThreeClassification);
	setFactorFourClassification(factorFourClassification);
	setFactorFiveClassification(factorFiveClassification);
	setFactorSixClassification(factorSixClassification);

    }

    public void validateData() {
	   	Siadap siadap = getSiadapEvaluationUniverse().getSiadap();
	   	
	if (StringUtils.isBlank(getObjectivesJustification())) {
	   	    throw new SiadapException("error.siadapAutoEvaluation.mustFillX", BundleUtil.getStringFromResourceBundle(SiadapRootModule.SIADAP_RESOURCES, "label.autoEvaluation.objectivesJustification"));
	   	}
	if (StringUtils.isBlank(getCompetencesJustification())) {
	    throw new SiadapException("error.siadapAutoEvaluation.mustFillX", BundleUtil.getStringFromResourceBundle(
		    SiadapRootModule.SIADAP_RESOURCES, "label.autoEvaluation.competencesJustification"));
	}
	
	if (getFactorSixClassification() != null && StringUtils.isBlank(getOtherFactorsJustification()))
	{
	    throw new SiadapException("error.siadapAutoEvaluation.mustFillOtherFactorsJustification");
	}
	if (StringUtils.isBlank(getExtremesJustification())
		&& (isExtremeClassification(getFactorOneClassification())
			|| isExtremeClassification(getFactorTwoClassification())
			|| isExtremeClassification(getFactorThreeClassification())
			|| isExtremeClassification(getFactorFourClassification())
			|| isExtremeClassification(getFactorFiveClassification()) || isExtremeClassification(getFactorSixClassification()))) {
	    throw new SiadapException("error.siadapAutoEvaluation.mustFillExtremesJustification");
	}
	   	
	//   	String personalDevelopment = getPersonalDevelopment();
	//   	String evaluationJustification = getEvaluationJustification();
	//   	String trainningNeeds = getTrainningNeeds();
	//   	if (siadap.isInadequate() && (StringUtils.isEmpty(personalDevelopment) || StringUtils.isEmpty(trainningNeeds))) {
	//
	//   	    throw new DomainException("error.siadapEvaluation.mustFillDataForBadEvaluation",
	//   		    DomainException.getResourceFor("resources/SiadapResources"));
	//   	}
	//   	if ((siadap.isInadequate() || siadap.hasRelevantEvaluation()) && StringUtils.isEmpty(evaluationJustification)) {
	//   	    throw new DomainException("error.siadapEvaluation.mustFillEvaluationJustification",
	//   		    DomainException.getResourceFor("resources/SiadapResources"));
	//
	//   	}
    }

    /**
     * 
     * @param factorClassification
     *            the classification of the factor to assert if it is extreme or
     *            not
     * @return true if it is classificated as an extreme, false otherwise
     */
    private boolean isExtremeClassification(Integer factorClassification) {
	if (factorClassification == null) {
	    return false;
	}
	if (factorClassification.intValue() <= 2 || factorClassification.intValue() >= 5) {
	    return true;
	}
	return false;

    }

}
