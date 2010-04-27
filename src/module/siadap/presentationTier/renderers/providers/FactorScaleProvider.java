package module.siadap.presentationTier.renderers.providers;

import java.util.ArrayList;
import java.util.List;

import pt.ist.fenixWebFramework.renderers.DataProvider;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;

public class FactorScaleProvider implements DataProvider {

    @Override
    public Converter getConverter() {
	return null;
    }

    @Override
    public Object provide(Object arg0, Object arg1) {
	List<Integer> scale = new ArrayList<Integer>();
	for (int i = 1; i <= 6; i++) {
	    scale.add(i);
	}
	return scale;
    }

}
