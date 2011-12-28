package module.siadap.domain.scoring;

import java.math.BigDecimal;

import myorg.util.BundleUtil;
import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

public enum SiadapGlobalEvaluation implements IPresentableEnum, IScoring {

    NONEXISTING(0, null, null), EXCELLENCY(5, new BigDecimal(4), new BigDecimal(4)), HIGH(5, new BigDecimal(4), new BigDecimal(5)), MEDIUM(
	    3, new BigDecimal(2), new BigDecimal(3.999)), LOW(1, new BigDecimal(1),
	    new BigDecimal(1.999)), ZERO(0, new BigDecimal(0), new BigDecimal(0.999));

    private BigDecimal points;
    private BigDecimal lowerBound;
    private BigDecimal upperBound;

    SiadapGlobalEvaluation(int points, BigDecimal lowerBound, BigDecimal upperBound) {
	this.points = new BigDecimal(points);
	this.lowerBound = lowerBound;
	this.upperBound = upperBound;
    }

    @Override
    public BigDecimal getPoints() {
	return points;
    }

    public static SiadapGlobalEvaluation getGlobalEvaluation(BigDecimal totalEvaluationScoring, boolean excellencyRequested)
    {
	if (totalEvaluationScoring == null)
	    return NONEXISTING;
	if (excellencyRequested && HIGH.accepts(totalEvaluationScoring))
	    return EXCELLENCY;
	else if (HIGH.accepts(totalEvaluationScoring))
	    return HIGH;
	else if (MEDIUM.accepts(totalEvaluationScoring))
	    return MEDIUM;
	else if (LOW.accepts(totalEvaluationScoring))
	    return LOW;
	else if (ZERO.accepts(totalEvaluationScoring))
	    return ZERO;
	else
	    return NONEXISTING;
	    
    }

    @Override
    public String getLocalizedName() {
	return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getClass().getName() + "." + name());
    }

    public boolean accepts(BigDecimal totalEvaluationScoring) {
	return lowerBound.compareTo(totalEvaluationScoring) <= 0 && upperBound.compareTo(totalEvaluationScoring) >= 0;
    }
}
