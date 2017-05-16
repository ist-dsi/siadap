/*
 * @(#)CurricularPonderationEvaluationItem.java
 *
 * Copyright 2011 Instituto Superior Tecnico
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

import java.math.BigDecimal;

import module.organization.domain.Person;
import module.siadap.domain.scoring.IScoring;

/**
 * 
 * @author João Antunes
 * 
 */
public class CurricularPonderationEvaluationItem extends CurricularPonderationEvaluationItem_Base {

    protected CurricularPonderationEvaluationItem(BigDecimal assignedGrade, Boolean assignedExcellency,
            String excellencyAwardJustification, String curricularPonderationJustification,
            SiadapEvaluationUniverse siadapEvaluationUniverse, Person evaluator) {
        super();
        setSiadapEvaluationUniverse(siadapEvaluationUniverse);
        setEvaluation(assignedGrade);
        setExcellencyAward(assignedExcellency);
        setExcellencyAwardJustification(excellencyAwardJustification);
        setCurricularPonderationJustification(curricularPonderationJustification);
        setAssignedCPEvaluator(evaluator);
    }

    //TODO do the edit (similar to the constructor) with the difference of being associated with an activity (yet to be required)

    static public BigDecimal getCurricularPonderationValue(SiadapEvaluationUniverse siadapEvaluationUniverse) {
        for (SiadapEvaluationItem siadapEvaluationItem : siadapEvaluationUniverse.getSiadapEvaluationItems()) {
            if (siadapEvaluationItem instanceof CurricularPonderationEvaluationItem) {
                return ((CurricularPonderationEvaluationItem) siadapEvaluationItem).getItemEvaluation().getPoints();
            }
        }
        return null;
    }

    @Override
    public boolean isValid() {
        if (getEvaluation() != null) {
            return true;
        }
        return false;
    }

    @Override
    public IScoring getItemEvaluation() {
        return new IScoring() {

            @Override
            public BigDecimal getPoints() {
                return getEvaluation();
            }
        };
    }

    @Override
    public IScoring getItemAutoEvaluation() {
        return null;
    }

}
