/*
 * @(#)SiadapEvaluation.java
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

import module.siadap.domain.exceptions.SiadapException;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public class SiadapEvaluation extends SiadapEvaluation_Base {

    public SiadapEvaluation(Siadap siadap, String noEvaluationJustification, SiadapEvaluationUniverse siadapEvaluationUniverse) {
        setSiadapRootModule(SiadapRootModule.getInstance());
        setSiadapEvaluationUniverse(siadapEvaluationUniverse);
        siadapEvaluationUniverse.setSiadap(siadap);
        setNoEvaluationJustification(noEvaluationJustification);
    }

    public SiadapEvaluation(Siadap siadap, String evaluationJustification, String personalDevelopment, String trainningNeeds,
            Boolean excellencyAward, String excellencyAwardJustification, SiadapEvaluationUniverse siadapEvaluationUniverse) {
        setSiadapRootModule(SiadapRootModule.getInstance());
        setSiadapEvaluationUniverse(siadapEvaluationUniverse);
        siadapEvaluationUniverse.setSiadap(siadap);
        editWithoutValidation(evaluationJustification, personalDevelopment, trainningNeeds, excellencyAward,
                excellencyAwardJustification);
    }

    public void editWithoutValidation(String evaluationJustification, String personalDevelopment, String trainningNeeds,
            Boolean excellencyAward, String excellencyAwardJustification) {
        setExcellencyAwardJustification(excellencyAwardJustification);
        setEvaluationJustification(evaluationJustification);
        setPersonalDevelopment(personalDevelopment);
        setTrainningNeeds(trainningNeeds);
        setExcellencyAward(excellencyAward);
    }

    public void validateData() {
        Siadap siadap = getSiadapEvaluationUniverse().getSiadap();
        String personalDevelopment = getPersonalDevelopment();
        String evaluationJustification = getEvaluationJustification();
        String trainningNeeds = getTrainningNeeds();
        if (siadap.getDefaultSiadapEvaluationUniverse().isInadequate()
                && (StringUtils.isEmpty(personalDevelopment) || StringUtils.isEmpty(trainningNeeds))) {

            throw new SiadapException("error.siadapEvaluation.mustFillDataForBadEvaluation");
        }
        if ((siadap.getDefaultSiadapEvaluationUniverse().isInadequate() || siadap.getDefaultSiadapEvaluationUniverse()
                .hasRelevantEvaluation()) && StringUtils.isEmpty(evaluationJustification)) {
            throw new SiadapException("error.siadapEvaluation.mustFillEvaluationJustification");
        }
        if (getExcellencyAward().booleanValue() && StringUtils.isBlank(getExcellencyAwardJustification())) {
            throw new SiadapException("error.siadapEvaluation.mustFillExcellencyAwardJustification");
        }
    }

}
