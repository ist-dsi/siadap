package module.siadap.domain;

import myorg.domain.exceptions.DomainException;

import org.apache.commons.lang.StringUtils;

public class SiadapEvaluation extends SiadapEvaluation_Base {

    public SiadapEvaluation(Siadap siadap, String noEvaluationJustification) {
	setSiadapRootModule(SiadapRootModule.getInstance());
	setSiadap(siadap);
	setNoEvaluationJustification(noEvaluationJustification);
    }

    public SiadapEvaluation(Siadap siadap, String evaluationJustification, String personalDevelopment, String trainningNeeds,
	    Boolean excellencyAward) {
	setSiadapRootModule(SiadapRootModule.getInstance());
	setSiadap(siadap);
	edit(evaluationJustification, personalDevelopment, trainningNeeds, excellencyAward);
    }

    public void edit(String evaluationJustification, String personalDevelopment, String trainningNeeds, Boolean excellencyAward) {
	Siadap siadap = getSiadap();
	if (siadap.isInadequate() && (StringUtils.isEmpty(personalDevelopment) || StringUtils.isEmpty(trainningNeeds))) {

	    throw new DomainException("error.siadapEvaluation.mustFillDataForBadEvaluation", DomainException
		    .getResourceFor("resources/SiadapResources"));
	}
	if ((siadap.isInadequate() || siadap.hasRelevantEvaluation()) && StringUtils.isEmpty(evaluationJustification)) {
	    throw new DomainException("error.siadapEvaluation.mustFillEvaluationJustification", DomainException
		    .getResourceFor("resources/SiadapResources"));

	}
	setEvaluationJustification(evaluationJustification);
	setPersonalDevelopment(personalDevelopment);
	setTrainningNeeds(trainningNeeds);
	setExcellencyAward(excellencyAward);
    }

}
