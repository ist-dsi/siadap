package module.siadap.domain;

import myorg.util.BundleUtil;
import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

public enum SiadapEvaluation implements IPresentableEnum {

    HIGH(5), MEDIUM(3), LOW(1);

    private int points;

    SiadapEvaluation(int points) {
	this.points = points;
    }

    public int getPoints() {
	return points;
    }

    @Override
    public String getLocalizedName() {
	return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getClass().getName() + "." + name());
    }
}
