package module.siadap.domain;

import myorg.util.BundleUtil;
import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

public enum SiadapEvaluationObjectivesType implements IPresentableEnum {

    EFICIENCY, PERFORMANCE, QUALITY;

    @Override
    public String getLocalizedName() {
	return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getClass().getName() + "." + name());
    }

}
