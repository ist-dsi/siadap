package module.siadap.domain;

import org.apache.commons.lang.StringUtils;

import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import myorg.domain.exceptions.DomainException;

public class SiadapEvaluation extends SiadapEvaluation_Base {

    public SiadapEvaluation(Siadap siadap, String personalDevelopment, String trainningNeeds) {
	setSiadapRootModule(SiadapRootModule.getInstance());
	setSiadap(siadap);
	if (siadap.getQualitativeEvaluation() == SiadapGlobalEvaluation.LOW
		&& (StringUtils.isEmpty(personalDevelopment) || StringUtils.isEmpty(trainningNeeds))) {

	    throw new DomainException("error.siadapEvaluation.mustFillDataForBadEvaluation", DomainException
		    .getResourceFor("resources/SiadapResources"));
	}
	setPersonalDevelopment(personalDevelopment);
	setTrainningNeeds(trainningNeeds);
    }
}
