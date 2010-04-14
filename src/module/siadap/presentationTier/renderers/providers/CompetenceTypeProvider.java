package module.siadap.presentationTier.renderers.providers;

import module.siadap.domain.SiadapRootModule;
import pt.ist.fenixWebFramework.renderers.DataProvider;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;

public class CompetenceTypeProvider implements DataProvider {

    @Override
    public Converter getConverter() {
	return null;
    }

    @Override
    public Object provide(Object arg0, Object arg1) {
	return SiadapRootModule.getInstance().getCompetenceTypes();
    }

}
