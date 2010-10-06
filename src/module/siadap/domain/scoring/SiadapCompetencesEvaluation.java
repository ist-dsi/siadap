package module.siadap.domain.scoring;

import java.math.BigDecimal;

import myorg.util.BundleUtil;
import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

public enum SiadapCompetencesEvaluation implements IPresentableEnum, IScoring {

    HIGH(5), MEDIUM(3), LOW(1);

    private BigDecimal points;

    SiadapCompetencesEvaluation(int points) {
	this.points = new BigDecimal(points);
    }

    public BigDecimal getPoints() {
	return points;
    }

    @Override
    public String getLocalizedName() {
	return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getClass().getName() + "." + name());
    }
}
