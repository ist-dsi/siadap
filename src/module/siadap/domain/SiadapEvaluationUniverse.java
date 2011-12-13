package module.siadap.domain;


public class SiadapEvaluationUniverse extends SiadapEvaluationUniverse_Base {

    public SiadapEvaluationUniverse(Siadap siadap, SiadapUniverse siadapUniverse, boolean defaultUniverse) {
	super();
	setSiadap(siadap);
	setSiadapUniverse(siadapUniverse);
	setDefaultEvaluationUniverse(defaultUniverse);
    }
    
    // TODO joantune: assim que o Roxo integrar o trabalho da tese dele, isto já funciona :)
    //    @ConsistencyPredicate
    //    public boolean onlyOneDefaultInstancePerSiadap() {
    //	if (getDefaultEvaluationUniverse().booleanValue()) {
    //	    //let's make sure there's no other
    //	    for (SiadapEvaluationUniverse evaluationUniverse : getSiadap().getSiadapEvaluationUniverses()) {
    //		if (!evaluationUniverse.equals(this) && evaluationUniverse.getDefaultEvaluationUniverse())
    //		    return false;
    //	    }
    //	}
    //
    //	return true;
    //    }
}
