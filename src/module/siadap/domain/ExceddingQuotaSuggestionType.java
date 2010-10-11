package module.siadap.domain;

import myorg.util.BundleUtil;
import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

public enum ExceddingQuotaSuggestionType implements IPresentableEnum {

    HIGH_SUGGESTION, EXCELLENCY_SUGGESTION;

    @Override
    public String getLocalizedName() {
	return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getClass().getName() + "." + name());
    }

}
