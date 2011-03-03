package module.siadap.domain;

import java.util.MissingResourceException;

import myorg.util.BundleUtil;
import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

public enum SiadapProcessSchedulesEnum implements IPresentableEnum {
    OBJECTIVES_SPECIFICATION_BEGIN_DATE, OBJECTIVES_SPECIFICATION_END_DATE,

    AUTOEVALUATION_BEGIN_DATE, AUTOEVALUATION_END_DATE,

    EVALUATION_BEGIN_DATE, EVALUATION_END_DATE;


    private SiadapProcessSchedulesEnum() {
    }


    @Override
    public String getLocalizedName() {
	try {
	return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", SiadapProcessSchedulesEnum.class.getSimpleName() + "."
		+ name());
	} catch (MissingResourceException ex) {
	    return name();
	}

    }

}
