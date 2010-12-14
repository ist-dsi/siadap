package module.siadap.domain;

import pt.ist.fenixWebFramework.services.Service;
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

    public void delete()
    {
	if (getAutoEvaluation() != null || getEvaluation() != null) {
	    // TODO ist154457: improve the error, assert what kind of exception
	    // should be thrown here
	    throw new Error("Error while trying to delete a competence that has evaluation data assigned");
	}
	removeSiadap();
	removeSiadapRootModule();
	removeCompetence();
	deleteDomainObject();
    }

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

}
