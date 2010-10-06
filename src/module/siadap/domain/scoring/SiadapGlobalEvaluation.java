package module.siadap.domain.scoring;

import java.math.BigDecimal;

import myorg.util.BundleUtil;
import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

public enum SiadapGlobalEvaluation implements IPresentableEnum, IScoring {

    HIGH(5, new BigDecimal(4), new BigDecimal(5)), MEDIUM(3, new BigDecimal(2), new BigDecimal(3.999)), LOW(1, new BigDecimal(1),
	    new BigDecimal(1.999));

    private BigDecimal points;
    private BigDecimal lowerBound;
    private BigDecimal upperBound;

    SiadapGlobalEvaluation(int points, BigDecimal lowerBound, BigDecimal upperBound) {
	this.points = new BigDecimal(points);
	this.lowerBound = lowerBound;
	this.upperBound = upperBound;
    }

    public BigDecimal getPoints() {
	return points;
    }

    @Override
    public String getLocalizedName() {
	return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getClass().getName() + "." + name());
    }

    public boolean accepts(BigDecimal totalEvaluationScoring) {
	return lowerBound.compareTo(totalEvaluationScoring) <= 0 && upperBound.compareTo(totalEvaluationScoring) >= 0;
    }
}
