/**
 * 
 */
package module.siadap.presentationTier.renderers.providers;

import java.util.ArrayList;
import java.util.List;

import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;
import pt.ist.fenixWebFramework.renderers.DataProvider;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;

/**
 * @author joantune (joao.antunes@tagus.ist.utl.pt)
 *
 */
public class SiadapYearConfigurationsFromExisting implements DataProvider {

	/* (non-Javadoc)
	 * @see pt.ist.fenixWebFramework.renderers.DataProvider#provide(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Object provide(Object source, Object currentValue) {
		return new ArrayList<SiadapYearConfiguration>(SiadapRootModule.getInstance().getYearConfigurations());
	}

	/* (non-Javadoc)
	 * @see pt.ist.fenixWebFramework.renderers.DataProvider#getConverter()
	 */
	@Override
	public Converter getConverter() {
		return null;
//		return new Converter() {
//			
//			@Override
//			public Object convert(Class type, Object value) {
//				if (value != null)
//					return ((SiadapYearConfiguration) value).getYear();
//				return null;
//			}
//		};
	}

}
