/*
 * @(#)SiadapAutoEvaluation.java
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

import pt.ist.bennu.core.util.BundleUtil;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public class SiadapAutoEvaluation extends SiadapAutoEvaluation_Base {

    public SiadapAutoEvaluation(Siadap siadap, SiadapEvaluationUniverse siadapEvaluationUniverse, String objectivesJustification,
            String competencesJustification, String otherFactorsJustification, String extremesJustification,
            String commentsAndProposals, Integer factorOneClassification, Integer factorTwoClassification,
            Integer factorThreeClassification, Integer factorFourClassification, Integer factorFiveClassification,
            Integer factorSixClassification) {

        setSiadapRootModule(SiadapRootModule.getInstance());
        setSiadapEvaluationUniverse(siadapEvaluationUniverse);

        setObjectivesJustification(objectivesJustification);
        setCompetencesJustification(competencesJustification);
        setOtherFactorsJustification(otherFactorsJustification);
        setExtremesJustification(extremesJustification);
        setCommentsAndProposals(commentsAndProposals);

        setFactorOneClassification(factorOneClassification);
        setFactorTwoClassification(factorTwoClassification);
        setFactorThreeClassification(factorThreeClassification);
        setFactorFourClassification(factorFourClassification);
        setFactorFiveClassification(factorFiveClassification);
        setFactorSixClassification(factorSixClassification);

    }

    public void validateData() {
        Siadap siadap = getSiadapEvaluationUniverse().getSiadap();

        if (StringUtils.isBlank(getObjectivesJustification())) {
            throw new SiadapException("error.siadapAutoEvaluation.mustFillX", BundleUtil.getStringFromResourceBundle(
                    SiadapRootModule.SIADAP_RESOURCES, "label.autoEvaluation.objectivesJustification"));
        }
        if (StringUtils.isBlank(getCompetencesJustification())) {
            throw new SiadapException("error.siadapAutoEvaluation.mustFillX", BundleUtil.getStringFromResourceBundle(
                    SiadapRootModule.SIADAP_RESOURCES, "label.autoEvaluation.competencesJustification"));
        }

        if (getFactorSixClassification() != null && StringUtils.isBlank(getOtherFactorsJustification())) {
            throw new SiadapException("error.siadapAutoEvaluation.mustFillOtherFactorsJustification");
        }
        if (StringUtils.isBlank(getExtremesJustification())
                && (isExtremeClassification(getFactorOneClassification())
                        || isExtremeClassification(getFactorTwoClassification())
                        || isExtremeClassification(getFactorThreeClassification())
                        || isExtremeClassification(getFactorFourClassification())
                        || isExtremeClassification(getFactorFiveClassification()) || isExtremeClassification(getFactorSixClassification()))) {
            throw new SiadapException("error.siadapAutoEvaluation.mustFillExtremesJustification");
        }

        //   	String personalDevelopment = getPersonalDevelopment();
        //   	String evaluationJustification = getEvaluationJustification();
        //   	String trainningNeeds = getTrainningNeeds();
        //   	if (siadap.isInadequate() && (StringUtils.isEmpty(personalDevelopment) || StringUtils.isEmpty(trainningNeeds))) {
        //
        //   	    throw new DomainException("error.siadapEvaluation.mustFillDataForBadEvaluation",
        //   		    DomainException.getResourceFor("resources/SiadapResources"));
        //   	}
        //   	if ((siadap.isInadequate() || siadap.hasRelevantEvaluation()) && StringUtils.isEmpty(evaluationJustification)) {
        //   	    throw new DomainException("error.siadapEvaluation.mustFillEvaluationJustification",
        //   		    DomainException.getResourceFor("resources/SiadapResources"));
        //
        //   	}
    }

    /**
     * 
     * @param factorClassification
     *            the classification of the factor to assert if it is extreme or
     *            not
     * @return true if it is classificated as an extreme, false otherwise
     */
    private boolean isExtremeClassification(Integer factorClassification) {
        if (factorClassification == null) {
            return false;
        }
        if (factorClassification.intValue() <= 2 || factorClassification.intValue() >= 5) {
            return true;
        }
        return false;

    }

}
