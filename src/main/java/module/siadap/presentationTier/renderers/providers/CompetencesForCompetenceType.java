/*
 * @(#)CompetencesForCompetenceType.java
 *
 * Copyright 2010 Instituto Superior Tecnico
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

import java.util.Collections;

import module.siadap.domain.CompetenceType;
import pt.ist.fenixWebFramework.renderers.DataProvider;
import pt.ist.fenixWebFramework.renderers.components.converters.Converter;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public class CompetencesForCompetenceType implements DataProvider {

    public static interface ContainsCompetenceType {
        public CompetenceType getCompetenceType();

        public Boolean getEvaluatedOnlyByCompetences();
    }

    @Override
    public Converter getConverter() {
        return null;
    }

    @Override
    public Object provide(Object arg0, Object arg1) {

        ContainsCompetenceType someObject = (ContainsCompetenceType) arg0;
        CompetenceType competenceType = someObject.getCompetenceType();
        Boolean evaluatedOnlyByCompetences = someObject.getEvaluatedOnlyByCompetences();
        return (competenceType != null && evaluatedOnlyByCompetences != null) ? competenceType.getCompetences() : Collections
                .emptyList();
    }

}
