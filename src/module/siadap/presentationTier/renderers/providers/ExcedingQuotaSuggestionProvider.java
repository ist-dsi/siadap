package module.siadap.presentationTier.renderers.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import myorg.presentationTier.renderers.autoCompleteProvider.AutoCompleteProvider;
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.utl.ist.fenix.tools.util.StringNormalizer;

public class ExcedingQuotaSuggestionProvider implements AutoCompleteProvider {

    @Override
    public Collection getSearchResults(Map<String, String> argsMap, String value, int maxCount) {
	Unit unit = AbstractDomainObject.fromExternalId(argsMap.get("unitId"));
	int year = new LocalDate().getYear();

	List<PersonSiadapWrapper> unitEmployeesWithQuotas = new UnitSiadapWrapper(unit, year).getUnitEmployeesWithQuotas(false);
	List<Person> person = new ArrayList<Person>();

	String[] values = StringNormalizer.normalize(value).toLowerCase().split(" ");

	for (PersonSiadapWrapper wrapper : unitEmployeesWithQuotas) {
	    final String normalizedName = StringNormalizer.normalize(wrapper.getPerson().getName()).toLowerCase();
	    if (hasMatch(values, normalizedName)) {
		person.add(wrapper.getPerson());
	    }
	}

	return person;
    }

    private boolean hasMatch(String[] input, String personNameParts) {
	for (final String namePart : input) {
	    if (personNameParts.indexOf(namePart) == -1) {
		return false;
	    }
	}
	return true;
    }

}
