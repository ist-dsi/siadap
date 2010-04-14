package module.siadap.presentationTier.renderers.providers;

import java.util.Collections;

import module.siadap.domain.CompetenceType;
import pt.ist.fenixWebFramework.renderers.DataProvider;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;

public class CompetencesForCompetenceType implements DataProvider {

    public static interface ContainsCompetenceType {
	public CompetenceType getCompetenceType();
    }

    @Override
    public Converter getConverter() {
	return null;
    }

    @Override
    public Object provide(Object arg0, Object arg1) {
	ContainsCompetenceType someObject = (ContainsCompetenceType) arg0;
	CompetenceType competenceType = someObject.getCompetenceType();
	return competenceType != null ? competenceType.getCompetences() : Collections.emptyList();
    }

}
