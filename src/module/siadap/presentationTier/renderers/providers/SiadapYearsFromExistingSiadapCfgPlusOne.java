/**
 * 
 */
package module.siadap.presentationTier.renderers.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;

import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.presentationTier.actions.SiadapManagement;
import pt.ist.fenixWebFramework.renderers.DataProvider;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;

/**
 * Gets the SiadapYearConfiguration years plus one and eventually the current we are at
 * @author joantune (joao.antunes@tagus.ist.utl.pt)
 *
 */
public class SiadapYearsFromExistingSiadapCfgPlusOne implements DataProvider {

	/* (non-Javadoc)
	 * @see pt.ist.fenixWebFramework.renderers.DataProvider#provide(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Object provide(Object source, Object currentValue) {
		ArrayList<Integer> years = new ArrayList<Integer>();
		int maximumYear = 0;
		for (SiadapYearConfiguration siadapYearConfiguration : SiadapRootModule.getInstance().getYearConfigurations()) {
			years.add(new Integer(siadapYearConfiguration.getYear()));
			if (siadapYearConfiguration.getYear() > maximumYear)
			{ maximumYear = siadapYearConfiguration.getYear();}
		}
		Integer currentYear = new Integer(new LocalDate().getYear());
		if (!years.contains(currentYear))
		{
			years.add(currentYear);
		}
		Integer nextYear = new Integer(maximumYear + 1);
		if (!years.contains(nextYear))
		{
			years.add(nextYear);
		}
		Collections.sort(years);
		return years;
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
