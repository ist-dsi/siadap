package module.siadap.domain;

import module.siadap.domain.scoring.IScoring;

public class CompetenceEvaluation extends CompetenceEvaluation_Base {

    public CompetenceEvaluation(Siadap siadap, Competence competence) {
	super();
	setSiadap(siadap);
	setCompetence(competence);
    }

    @Override
    public IScoring getItemAutoEvaluation() {
	return getAutoEvaluation();
    }

    @Override
    public IScoring getItemEvaluation() {
	return getEvaluation();
    }

}
