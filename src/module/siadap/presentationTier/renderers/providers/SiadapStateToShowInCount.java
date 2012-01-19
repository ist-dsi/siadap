/**
 * 
 */
package module.siadap.presentationTier.renderers.providers;

import java.util.ArrayList;

import module.siadap.domain.SiadapProcessStateEnum;
import pt.ist.fenixWebFramework.renderers.DataProvider;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;

/**
 * @author joantune (joao.antunes@tagus.ist.utl.pt)
 * 
 */
public class SiadapStateToShowInCount implements DataProvider {

    /*
     * (non-Javadoc)
     * 
     * @see
     * pt.ist.fenixWebFramework.renderers.DataProvider#provide(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public Object provide(Object source, Object currentValue) {
	ArrayList<SiadapProcessStateEnum> siadapProcessStateEnumToReturn = new ArrayList<SiadapProcessStateEnum>();
	for (SiadapProcessStateEnum stateEnum : SiadapProcessStateEnum.values()) {
	    if (stateEnum.ordinal() < getMaximumStateToShowInCount().ordinal())
		siadapProcessStateEnumToReturn.add(stateEnum);
	}
	return siadapProcessStateEnumToReturn;
    }

    public static SiadapProcessStateEnum getMaximumStateToShowInCount() {
	return SiadapProcessStateEnum.WAITING_SUBMITTAL_BY_EVALUATOR_AFTER_VALIDATION;
    }


    /*
     * (non-Javadoc)
     * 
     * @see pt.ist.fenixWebFramework.renderers.DataProvider#getConverter()
     */
    @Override
    public Converter getConverter() {
	return null;
	// return new Converter() {
	//
	// @Override
	// public Object convert(Class type, Object value) {
	// if (value != null)
	// return ((SiadapYearConfiguration) value).getYear();
	// return null;
	// }
	// };
    }

    public static SiadapProcessStateEnum getDefaultStateToFilter() {
	return SiadapProcessStateEnum.WAITING_SELF_EVALUATION;
    }

}
