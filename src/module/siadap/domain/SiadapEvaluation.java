package module.siadap.domain;

import myorg.domain.exceptions.DomainException;

import org.apache.commons.lang.StringUtils;

public class SiadapEvaluation extends SiadapEvaluation_Base {

    public SiadapEvaluation(Siadap siadap, String evaluationJustification, String personalDevelopment, String trainningNeeds,
	    Boolean excellencyAward) {
	setSiadapRootModule(SiadapRootModule.getInstance());
	setSiadap(siadap);
	edit(evaluationJustification, personalDevelopment, trainningNeeds, excellencyAward);
    }

    public void edit(String evaluationJustification, String personalDevelopment, String trainningNeeds, Boolean excellencyAward) {
	if (getSiadap().isInadequate() && (StringUtils.isEmpty(personalDevelopment) || StringUtils.isEmpty(trainningNeeds))) {

	    throw new DomainException("error.siadapEvaluation.mustFillDataForBadEvaluation", DomainException
		    .getResourceFor("resources/SiadapResources"));
	}
	setEvaluationJustification(evaluationJustification);
	setPersonalDevelopment(personalDevelopment);
	setTrainningNeeds(trainningNeeds);
	setExcellencyAward(excellencyAward);
    }

}
