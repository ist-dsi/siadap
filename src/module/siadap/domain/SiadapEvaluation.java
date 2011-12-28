package module.siadap.domain;

import myorg.domain.exceptions.DomainException;

import org.apache.commons.lang.StringUtils;

public class SiadapEvaluation extends SiadapEvaluation_Base {

    public SiadapEvaluation(Siadap siadap, String noEvaluationJustification, SiadapEvaluationUniverse siadapEvaluationUniverse) {
	setSiadapRootModule(SiadapRootModule.getInstance());
	setSiadapEvaluationUniverse(siadapEvaluationUniverse);
	siadapEvaluationUniverse.setSiadap(siadap);
	setNoEvaluationJustification(noEvaluationJustification);
    }

    public SiadapEvaluation(Siadap siadap, String evaluationJustification, String personalDevelopment, String trainningNeeds,
	    Boolean excellencyAward, String excellencyAwardJustification, SiadapEvaluationUniverse siadapEvaluationUniverse) {
	setSiadapRootModule(SiadapRootModule.getInstance());
	setSiadapEvaluationUniverse(siadapEvaluationUniverse);
	siadapEvaluationUniverse.setSiadap(siadap);
	editWithoutValidation(evaluationJustification, personalDevelopment, trainningNeeds, excellencyAward,
		excellencyAwardJustification);
    }

    public void editWithoutValidation(String evaluationJustification, String personalDevelopment, String trainningNeeds,
	    Boolean excellencyAward, String excellencyAwardJustification) {
	Siadap siadap = getSiadapEvaluationUniverse().getSiadap();
	setExcellencyAwardJustification(excellencyAwardJustification);
	setEvaluationJustification(evaluationJustification);
	setPersonalDevelopment(personalDevelopment);
	setTrainningNeeds(trainningNeeds);
	setExcellencyAward(excellencyAward);
    }


    public void validateData() {
	Siadap siadap = getSiadapEvaluationUniverse().getSiadap();
	String personalDevelopment = getPersonalDevelopment();
	String evaluationJustification = getEvaluationJustification();
	String trainningNeeds = getTrainningNeeds();
	if (siadap.getDefaultSiadapEvaluationUniverse().isInadequate()
		&& (StringUtils.isEmpty(personalDevelopment) || StringUtils.isEmpty(trainningNeeds))) {

	    throw new DomainException("error.siadapEvaluation.mustFillDataForBadEvaluation",
		    DomainException.getResourceFor("resources/SiadapResources"));
	}
	if ((siadap.getDefaultSiadapEvaluationUniverse().isInadequate() || siadap.getDefaultSiadapEvaluationUniverse()
		.hasRelevantEvaluation())
		&& StringUtils.isEmpty(evaluationJustification)) {
	    throw new DomainException("error.siadapEvaluation.mustFillEvaluationJustification",
		    DomainException.getResourceFor("resources/SiadapResources"));
	}
	if (getExcellencyAward().booleanValue() && StringUtils.isBlank(getExcellencyAwardJustification())) {
	    throw new DomainException("error.siadapEvaluation.mustFillExcellencyAwardJustification",
		    DomainException.getResourceFor("resources/SiadapResources"));
	}
    }

}
