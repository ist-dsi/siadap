/*
 * @(#)ExceedingQuotaSuggestionProvider.java
 *
 * Copyright 2012 Instituto Superior Tecnico
 * Founding Authors: Paulo Abrantes
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the SIADAP Module.
 *
 *   The SIADAP Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The SIADAP Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the SIADAP Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.siadap.presentationTier.renderers.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import pt.ist.bennu.core.presentationTier.renderers.autoCompleteProvider.AutoCompleteProvider;
import pt.ist.fenixframework.FenixFramework;
import pt.utl.ist.fenix.tools.util.StringNormalizer;

/**
 * 
 * @author João Antunes
 * 
 */
public class ExceedingQuotaSuggestionProvider implements AutoCompleteProvider {

    @Override
    public Collection getSearchResults(Map<String, String> argsMap, String value, int maxCount) {
        Unit unit = FenixFramework.getDomainObject(argsMap.get("unitId"));
        int year = Integer.parseInt(argsMap.get("year"));

        //we have to get the people with any kind of no given
        List<PersonSiadapWrapper> unitCandidatesForSuggestion =
                new UnitSiadapWrapper(unit, year).getPeopleHarmonizedWithAnyNoAssessment();
        List<Person> person = new ArrayList<Person>();

        String[] values = StringNormalizer.normalize(value).toLowerCase().split(" ");

        for (PersonSiadapWrapper wrapper : unitCandidatesForSuggestion) {
            final String normalizedName = StringNormalizer.normalize(wrapper.getPerson().getName()).toLowerCase();
            if (hasMatch(values, normalizedName)) {
                person.add(wrapper.getPerson());
            }
        }

        return person;
    }

    public static boolean willProviderReturnResults(Unit unit, int year) {
        return !new UnitSiadapWrapper(unit, year).getPeopleHarmonizedWithAnyNoAssessment().isEmpty();
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
