/**
 * 
 */
package module.siadap.presentationTier.renderers.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import pt.ist.bennu.core.presentationTier.renderers.autoCompleteProvider.AutoCompleteProvider;
import pt.utl.ist.fenix.tools.util.StringNormalizer;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 24 de Jan de 2013
 * 
 * 
 */
public class HarmonizationUnitAutoCompleteProvider implements AutoCompleteProvider {

    @Override
    public Collection getSearchResults(Map<String, String> argsMap, String value, int maxCount) {
        final List<Unit> units = new ArrayList<Unit>();

        final String trimmedValue = value.trim();
        final String[] input = trimmedValue.split(" ");
        StringNormalizer.normalize(input);

        for (final Unit unit : getParties(argsMap, value)) {
            final String unitName = StringNormalizer.normalize(unit.getPartyName().getContent());
            if (hasMatch(input, unitName)) {
                units.add(unit);
            } else {
                final String unitAcronym = StringNormalizer.normalize(unit.getAcronym());
                if (hasMatch(input, unitAcronym)) {
                    units.add(unit);
                }
            }
        }

        Collections.sort(units, Unit.COMPARATOR_BY_PRESENTATION_NAME);

        return units;
    }

    private boolean hasMatch(final String[] input, final String unitNameParts) {
        for (final String namePart : input) {
            if (unitNameParts.indexOf(namePart) == -1) {
                return false;
            }
        }
        return true;
    }

    protected Set<Unit> getParties(Map<String, String> argsMap, String value) {
        Integer year = Integer.valueOf(argsMap.get("year"));
        Collection<Unit> harmonizationUnits =
                Collections2.transform(SiadapYearConfiguration.getAllHarmonizationUnitsFor(year),
                        new Function<UnitSiadapWrapper, Unit>() {

                            @Override
                            @Nullable
                            public Unit apply(@Nullable UnitSiadapWrapper input) {
                                if (input == null) {
                                    return null;
                                }
                                return input.getUnit();
                            }
                        });
        return new HashSet<Unit>(harmonizationUnits);
    }

}
