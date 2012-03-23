/*
 * @(#)SiadapGlobalEvaluation.java
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
package module.siadap.domain.scoring;

import java.math.BigDecimal;

import myorg.util.BundleUtil;
import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public enum SiadapGlobalEvaluation implements IPresentableEnum, IScoring {

    NONEXISTING(0, null, null), WITHSKIPPEDEVAL(0, null, null), EXCELLENCY(5, new BigDecimal(4), new BigDecimal(5)), HIGH(
	    5,
	    new BigDecimal(4), new BigDecimal(5)), MEDIUM(
	    3, new BigDecimal(2), new BigDecimal(3.999)), LOW(1, new BigDecimal(1),
	    new BigDecimal(1.999)), ZERO(0, new BigDecimal(0), new BigDecimal(0.999));

    private BigDecimal points;
    private BigDecimal lowerBound;
    private BigDecimal upperBound;

    SiadapGlobalEvaluation(int points, BigDecimal lowerBound, BigDecimal upperBound) {
	this.points = new BigDecimal(points);
	this.lowerBound = lowerBound;
	this.upperBound = upperBound;
    }

    @Override
    public BigDecimal getPoints() {
	return points;
    }

    public static SiadapGlobalEvaluation getGlobalEvaluation(BigDecimal totalEvaluationScoring, boolean excellencyRequested)
    {
	for (SiadapGlobalEvaluation siadapGlobalEval : SiadapGlobalEvaluation.values()) {
	    if (siadapGlobalEval.accepts(totalEvaluationScoring, excellencyRequested)) {
		return siadapGlobalEval;
	    }
	}
	return NONEXISTING;
    }

    public static boolean isValidGrade(BigDecimal grade, boolean excellentAssigned) {
	if (getGlobalEvaluation(grade, excellentAssigned).equals(NONEXISTING))
		return false;
	    return true;
    }

    @Override
    public String getLocalizedName() {
	return BundleUtil.getStringFromResourceBundle("resources/SiadapResources", getClass().getName() + "." + name());
    }

    public boolean accepts(BigDecimal totalEvaluationScoring, boolean excellencyAwarded) {
	if (this.equals(NONEXISTING) || this.equals(WITHSKIPPEDEVAL)) {
	    if (totalEvaluationScoring == null) {
		return true;
	    } else
		return false;
	}
	if (totalEvaluationScoring == null) {
	    return false;
	}
	return lowerBound.compareTo(totalEvaluationScoring) <= 0 && upperBound.compareTo(totalEvaluationScoring) >= 0
		&& ((excellencyAwarded && this.equals(EXCELLENCY)) || (!excellencyAwarded && !this.equals(EXCELLENCY)));
    }

}
