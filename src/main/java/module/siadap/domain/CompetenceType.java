/*
 * @(#)CompetenceType.java
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
package module.siadap.domain;

import java.util.Collection;
import java.util.Collections;

import pt.ist.fenixframework.Atomic;

/**
 * 
 * @author Paulo Abrantes
 * 
 */
public class CompetenceType extends CompetenceType_Base {

    public CompetenceType(String name) {
        super();
        setName(name);
        setSiadapRootModule(SiadapRootModule.getInstance());
    }

    @Atomic
    public static CompetenceType createNewCompetenceType(String name) {
        return new CompetenceType(name);
    }

    public Integer getNextCompetenceNumber() {

        Collection<Competence> competences = getCompetences();
        if (competences.isEmpty()) {
            return 1;
        }
        Competence max = Collections.max(competences, Competence.COMPARATOR_BY_NUMBER);
        return max.getNumber() + 1;
    }
    @Deprecated
    public java.util.Set<module.siadap.domain.Competence> getCompetences() {
        return getCompetencesSet();
    }

    @Deprecated
    public java.util.Set<module.siadap.domain.SiadapEvaluationUniverse> getSiadapEvaluationUniverses() {
        return getSiadapEvaluationUniversesSet();
    }

}
