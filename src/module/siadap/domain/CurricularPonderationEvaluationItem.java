package module.siadap.domain;

import java.math.BigDecimal;

import module.siadap.domain.scoring.IScoring;

public class CurricularPonderationEvaluationItem extends CurricularPonderationEvaluationItem_Base {
    
    public  CurricularPonderationEvaluationItem() {
        super();
    }
    
    static public BigDecimal getCurricularPonderationValue(SiadapEvaluationUniverse siadapEvaluationUniverse)
    {
	for (SiadapEvaluationItem siadapEvaluationItem : siadapEvaluationUniverse.getSiadapEvaluationItems())
	{
	    return ((CurricularPonderationEvaluationItem) siadapEvaluationItem).getItemEvaluation().getPoints();
	}
	return null;
    }

    @Override
    public boolean isValid() {
	if (getEvaluation() != null)
	    return true;
	return false;
    }

    @Override
    public IScoring getItemEvaluation() {
	return new IScoring() {

	    @Override
	    public BigDecimal getPoints() {
		return getEvaluation();
	    }
	};
    }

    @Override
    public IScoring getItemAutoEvaluation() {
	return null;
    }

}
